package com.mysite.sbb.tripmap.entity;

import com.mysite.sbb.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

// 하루 일정 안에서 들르는 장소 하나 (예: 에베소 유적지 09:00)
@Entity
@Getter
@Setter
@ToString(exclude = {"day", "author"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private TripDay day;

    private Integer sortOrder;              // 그날 안에서의 순서 (0, 1, 2 …)

    @Column(length = 100, nullable = false)
    private String name;                    // 장소 이름

    private Double lat;                     // 위도
    private Double lng;                     // 경도

    @Column(length = 300)
    private String placeId;                 // Google 장소 ID (검색으로 고른 경우)

    @Column(length = 200)
    private String address;                 // 주소

    @Column(length = 10)
    private String time;                    // 시간 (예: 09:30)

    @Column(columnDefinition = "TEXT")
    private String memo;                    // 메모 / 다녀온 기록

    @Builder.Default
    private Boolean visited = false;        // 다녀왔는지

    private LocalDateTime visitedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;                  // 추가한 사람

    private LocalDateTime created;

    // 위도,경도 문자열 (Google 지도 주소에 넣을 때)
    public String getLatLng() {
        return lat + "," + lng;
    }

    // 이 장소를 Google 지도에서 열기
    public String getGoogleUrl() {
        String url = "https://www.google.com/maps/search/?api=1&query=" + getLatLng();
        if (placeId != null && !placeId.isBlank()) {
            url += "&query_place_id=" + URLEncoder.encode(placeId, StandardCharsets.UTF_8);
        }
        return url;
    }
}
