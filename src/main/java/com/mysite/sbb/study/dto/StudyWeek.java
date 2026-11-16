package com.mysite.sbb.study.dto;

import java.time.LocalDate;
import java.util.List;

// 말씀 묵상 한 주 분량
// journey : 1 = 1차, 2 = 2차, 3 = 3차, 4 = 로마 호송
public record StudyWeek(int weekNo,
                        int journey,
                        List<String> route,       // 바울의 이동 경로
                        LocalDate start,
                        LocalDate end,
                        String acts,              // 사도행전 범위   (예: 16–17장)
                        String letter,            // 서신서 이름     (예: 고린도전서)
                        String letterShort,       // 서신서 줄임말   (예: 고전)
                        String letterRange) {     // 서신서 범위     (예: 6–10장)

    // 지도에 찍을 수 있는 경로 ('다시 돌아감' 같은 설명은 뺀다)
    public List<String> getStops() {
        return route.stream().filter(name -> BiblePlaces.find(name) != null).toList();
    }

    // 이 주의 도착지 (메인 '묵상 여정' 표시용)
    public String getStop() {
        List<String> stops = getStops();
        return stops.get(stops.size() - 1);
    }

    public String getRouteText() {
        return String.join(" → ", route);
    }

    // 목록 화면 아랫줄 (예: 11.9 – 11.15 · 행 16–17장 · 고전 6–10장)
    public String getSummary() {
        return start.getMonthValue() + "." + start.getDayOfMonth() + " – "
                + end.getMonthValue() + "." + end.getDayOfMonth()
                + " · 행 " + acts + " · " + letterShort + " " + letterRange;
    }

    public String getPeriod() {
        return start.getMonthValue() + "." + start.getDayOfMonth() + " – "
                + end.getMonthValue() + "." + end.getDayOfMonth();
    }
}
