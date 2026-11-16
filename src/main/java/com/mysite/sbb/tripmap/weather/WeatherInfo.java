package com.mysite.sbb.tripmap.weather;

// 지도 메뉴 : 장소 하나의 날씨 (GET /map/weather 응답)
public record WeatherInfo(Integer nowTemp,      // 지금 기온 (°C)
                          String nowText,       // 지금 날씨 (예: 맑음)
                          String nowIcon,       // 이모지
                          String dayLabel,      // 일정 날 날씨 설명 (예: 2.1 예보 / 작년 2.1)
                          Integer dayMax,       // 최고 기온
                          Integer dayMin,       // 최저 기온
                          String dayText,
                          String dayIcon,
                          Double dayRain) {     // 강수량 (mm)
}
