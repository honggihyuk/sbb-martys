package com.mysite.sbb.fitness.dto;

import java.util.List;

// 한 달 달력 : 7칸씩 끊어서 weeks 로 보여준다
public record CalendarMonth(String title, List<List<CalendarDay>> weeks, long checkedCount, int dayCount) {
}
