package com.mysite.sbb.study.dto;

// 읽은 만큼 바울의 경로를 따라간 위치
// weekNo : 몇 주차 경로 위에 있는지 (0 = 아직 출발 전)
// stopIndex : 그 주 경로(getStops)의 몇 번째 장소인지
public record StudyPosition(int weekNo, int stopIndex, String place) {
}
