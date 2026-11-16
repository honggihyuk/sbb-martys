package com.mysite.sbb.study.dto;

import java.util.List;

// 지도 스크립트에 넘겨줄 한 주 경로
public record JourneyWeek(int weekNo, int journey, List<String> stops, String period) {

    public static JourneyWeek of(StudyWeek w) {
        return new JourneyWeek(w.weekNo(), w.journey(), w.getStops(), w.getPeriod());
    }
}
