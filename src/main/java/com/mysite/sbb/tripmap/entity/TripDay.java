package com.mysite.sbb.tripmap.entity;

import com.mysite.sbb.common.TripInfo;
import com.mysite.sbb.common.geo.GeoUtils;
import com.mysite.sbb.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

// 답사 일정 하루 (예: 3일차 · 에베소)
@Entity
@Getter
@Setter
@ToString(exclude = {"author", "stops"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer dayNo;                  // 며칠차인지 (1일차 = 출발일)

    @Column(length = 50, nullable = false)
    private String title;                   // 그날의 제목 (예: 에베소 · 라오디게아)

    @Column(columnDefinition = "TEXT")
    private String memo;                    // 그날의 메모 (숙소, 준비물 등)

    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;                  // 처음 만든 사람

    private LocalDateTime created;

    // 그날 들르는 곳 (순서대로)
    @Builder.Default
    @OrderBy("sortOrder asc, id asc")
    @OneToMany(mappedBy = "day", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripStop> stops = new ArrayList<>();

    // 날짜 : 출발일 + (dayNo - 1)
    public LocalDate getDate() {
        return TripInfo.DEPARTURE.plusDays(dayNo - 1L);
    }

    // 그날 경로를 Google 지도 길찾기로 열기 (API 키 없이 쓰는 주소)
    // https://developers.google.com/maps/documentation/urls/get-started
    public String getDirectionsUrl() {
        if (stops.size() < 2) {
            return null;
        }
        TripStop first = stops.get(0);
        TripStop last = stops.get(stops.size() - 1);
        StringBuilder url = new StringBuilder("https://www.google.com/maps/dir/?api=1")
                .append("&origin=").append(first.getLatLng())
                .append("&destination=").append(last.getLatLng());
        if (stops.size() > 2) {
            StringJoiner waypoints = new StringJoiner("%7C");   // '|' 로 구분
            for (TripStop s : stops.subList(1, stops.size() - 1)) {
                waypoints.add(s.getLatLng());
            }
            url.append("&waypoints=").append(waypoints);
        }
        return url.toString();
    }

    // i 번째 장소까지 앞 장소에서 얼마나 떨어져 있는지 (직선 거리, i >= 1)
    public double legKm(int i) {
        TripStop a = stops.get(i - 1), b = stops.get(i);
        return GeoUtils.distanceKm(a.getLat(), a.getLng(), b.getLat(), b.getLng());
    }

    public String legText(int i) {
        return GeoUtils.format(legKm(i));
    }

    // 앞 장소 → i 번째 장소 길찾기 (Google 지도, 자동차)
    public String legDirectionsUrl(int i) {
        return "https://www.google.com/maps/dir/?api=1&travelmode=driving"
                + "&origin=" + stops.get(i - 1).getLatLng()
                + "&destination=" + stops.get(i).getLatLng();
    }

    // 그날 전체 이동 거리 (직선 거리 합)
    public String getTotalDistance() {
        double sum = 0;
        for (int i = 1; i < stops.size(); i++) {
            sum += legKm(i);
        }
        return GeoUtils.format(sum);
    }

    public long getVisitedCount() {
        return stops.stream().filter(s -> Boolean.TRUE.equals(s.getVisited())).count();
    }
}
