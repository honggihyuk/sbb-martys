package com.mysite.sbb.tripmap.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 날씨 : Open-Meteo (https://open-meteo.com, API 키 필요 없음)
//  - 지금 날씨 : forecast API 의 current
//  - 일정 날 날씨 : 16일 안이면 예보, 더 멀면 '작년 같은 날' 실제 기록(archive API)
//  같은 장소를 계속 부르지 않도록 30분 동안 메모리에 저장해 둔다.
@Slf4j
@Service
public class WeatherService {

    private static final Duration CACHE_TIME = Duration.ofMinutes(30);

    private final RestClient restClient = RestClient.create();
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    @Value("${app.weather.forecast-url:https://api.open-meteo.com/v1/forecast}")
    private String forecastUrl;

    @Value("${app.weather.archive-url:https://archive-api.open-meteo.com/v1/archive}")
    private String archiveUrl;

    private record Cached(WeatherInfo info, Instant at) {
    }

    // Open-Meteo 응답 중 필요한 부분만
    record Response(Current current, Daily daily) {
    }

    record Current(@JsonProperty("temperature_2m") Double temp,
                   @JsonProperty("weather_code") Integer code) {
    }

    record Daily(List<String> time,
                 @JsonProperty("weather_code") List<Integer> code,
                 @JsonProperty("temperature_2m_max") List<Double> max,
                 @JsonProperty("temperature_2m_min") List<Double> min,
                 @JsonProperty("precipitation_sum") List<Double> rain) {
    }

    public WeatherInfo getWeather(double lat, double lng, LocalDate date) {
        String key = String.format(Locale.ROOT, "%.2f,%.2f,%s", lat, lng, date);
        Cached cached = cache.get(key);
        if (cached != null && cached.at().plus(CACHE_TIME).isAfter(Instant.now())) {
            return cached.info();
        }
        try {
            WeatherInfo info = fetch(lat, lng, date);
            cache.put(key, new Cached(info, Instant.now()));
            return info;
        } catch (Exception e) {
            log.warn("날씨를 가져오지 못했어요 ({}) : {}", key, e.getMessage());
            return null;
        }
    }

    private WeatherInfo fetch(double lat, double lng, LocalDate date) {
        LocalDate today = LocalDate.now();
        long daysLeft = ChronoUnit.DAYS.between(today, date);

        // 1. 지금 날씨 (+ 16일 안이면 그날 예보도 같이)
        boolean forecastable = daysLeft >= 0 && daysLeft <= 15;
        String url = forecastUrl + "?latitude=" + lat + "&longitude=" + lng
                + "&current=temperature_2m,weather_code&timezone=auto"
                + (forecastable ? "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum"
                + "&start_date=" + date + "&end_date=" + date : "");
        Response now = restClient.get().uri(url).retrieve().body(Response.class);

        // 2. 일정 날 날씨
        Daily daily;
        String label;
        if (forecastable) {
            daily = now.daily();
            label = date.getMonthValue() + "." + date.getDayOfMonth() + " 예보";
        } else {
            // 기록은 며칠 늦게 올라오므로 일주일 전보다 이전 날짜만 쓴다
            LocalDate past = date.withYear(today.getYear());
            while (!past.isBefore(today.minusDays(7))) {
                past = past.minusYears(1);
            }
            String archive = archiveUrl + "?latitude=" + lat + "&longitude=" + lng
                    + "&start_date=" + past + "&end_date=" + past
                    + "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum&timezone=auto";
            Response r = restClient.get().uri(archive).retrieve().body(Response.class);
            daily = (r == null) ? null : r.daily();
            label = past.getYear() + "년 " + past.getMonthValue() + "." + past.getDayOfMonth() + " 기록";
        }

        Integer nowCode = now.current() == null ? null : now.current().code();
        Integer dayCode = first(daily == null ? null : daily.code());
        Double max = first(daily == null ? null : daily.max());
        Double min = first(daily == null ? null : daily.min());
        return new WeatherInfo(
                now.current() == null ? null : round(now.current().temp()), text(nowCode), icon(nowCode),
                label, round(max), round(min), text(dayCode), icon(dayCode),
                first(daily == null ? null : daily.rain()));
    }

    private static <T> T first(List<T> list) {
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    private static Integer round(Double d) {
        return d == null ? null : (int) Math.round(d);
    }

    // WMO 날씨 코드 → 한국어 (https://open-meteo.com/en/docs 의 WMO Weather interpretation codes)
    static String text(Integer code) {
        if (code == null) return null;
        if (code == 0) return "맑음";
        if (code <= 2) return "구름 조금";
        if (code == 3) return "흐림";
        if (code <= 48) return "안개";
        if (code <= 57) return "이슬비";
        if (code <= 67) return "비";
        if (code <= 77) return "눈";
        if (code <= 82) return "소나기";
        if (code <= 86) return "눈보라";
        return "뇌우";
    }

    static String icon(Integer code) {
        if (code == null) return null;
        if (code == 0) return "☀️";
        if (code <= 2) return "🌤️";
        if (code == 3) return "☁️";
        if (code <= 48) return "🌫️";
        if (code <= 67) return "🌧️";
        if (code <= 77) return "❄️";
        if (code <= 82) return "🌦️";
        if (code <= 86) return "🌨️";
        return "⛈️";
    }
}
