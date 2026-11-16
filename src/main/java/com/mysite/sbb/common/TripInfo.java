package com.mysite.sbb.common;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

// 성서지리 답사 일정 (여러 화면에서 같이 쓰는 날짜 상수)
public final class TripInfo {

    private TripInfo() {}

    public static final String TITLE = "2026 청년 신약 성서지리";
    public static final String TEAM_NAME = "마르투스";          // 팀 이름 (헬라어 '증인')
    public static final int TEAM_COUNT = 6;                    // 조 개수 (1조 ~ 6조)
    public static final int SLED_TEAM_NO = 0;                  // SLED조 : 소속이 SL·ED 인 사람은 자동으로 여기 (번호 조와 겹치지 않게 0)

    public static final LocalDate DEPARTURE = LocalDate.of(2027, 2, 1);     // 출발일
    public static final LocalDate STUDY_START = LocalDate.of(2026, 10, 5);  // 말씀 묵상 시작 (월)
    public static final LocalDate FITNESS_START = LocalDate.of(2026, 12, 1);
    public static final LocalDate FITNESS_END = LocalDate.of(2027, 1, 31);

    // 조 이름 (1 → "1조", 0 → "SLED조", null → null)
    public static String teamName(Integer teamNo) {
        if (teamNo == null) {
            return null;
        }
        return teamNo == SLED_TEAM_NO ? "SLED조" : teamNo + "조";
    }

    // 조 선택 목록 { 번호 : 이름 } (1조 ~ TEAM_COUNT조, withSled 이면 맨 뒤에 SLED조)
    public static Map<Integer, String> teams(boolean withSled) {
        Map<Integer, String> map = new LinkedHashMap<>();
        for (int no = 1; no <= TEAM_COUNT; no++) {
            map.put(no, teamName(no));
        }
        if (withSled) {
            map.put(SLED_TEAM_NO, teamName(SLED_TEAM_NO));
        }
        return map;
    }

    // D-day 계산 : 목표일까지 남은 일수
    public static long daysUntil(LocalDate target) {
        return ChronoUnit.DAYS.between(LocalDate.now(), target);
    }
}
