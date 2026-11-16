package com.mysite.sbb.study.dto;

import java.util.List;
import java.util.Map;

// GET /study/journey-map 응답 (JSON) : 묵상 여정 지도를 그리는 데 필요한 모든 것
public record JourneyMapData(List<String> journeyNames,
                             List<JourneyWeek> weeks,
                             Map<String, BiblePlace> places,
                             List<MapPin> pins,
                             int readCount,
                             int total) {
}
