package com.mysite.sbb.fitness.dto;

import java.time.LocalDate;

// 달력 한 칸 (date 가 null 이면 빈칸)
public record CalendarDay(LocalDate date, boolean checked, boolean checkable, boolean today) {

    public static CalendarDay blank() {
        return new CalendarDay(null, false, false, false);
    }
}
