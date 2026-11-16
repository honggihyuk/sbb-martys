package com.mysite.sbb.fitness.service;

import com.mysite.sbb.common.TripInfo;
import com.mysite.sbb.fitness.dto.CalendarDay;
import com.mysite.sbb.fitness.dto.CalendarMonth;
import com.mysite.sbb.fitness.entity.FitnessCheck;
import com.mysite.sbb.fitness.repository.FitnessCheckRepository;
import com.mysite.sbb.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FitnessService {

    private final FitnessCheckRepository fitnessCheckRepository;

    public Set<LocalDate> getCheckedDates(Member member) {
        return fitnessCheckRepository.findByMember(member).stream()
                .map(FitnessCheck::getCheckDate)
                .collect(Collectors.toSet());
    }

    // 체크할 수 있는 날 : 기간 안이고, 오늘이거나 지난 날
    public boolean isCheckable(LocalDate date) {
        LocalDate today = LocalDate.now();
        return !date.isBefore(TripInfo.FITNESS_START)
                && !date.isAfter(TripInfo.FITNESS_END)
                && !date.isAfter(today);
    }

    @Transactional
    public void toggle(Member member, LocalDate date) {
        if (!isCheckable(date)) {
            return;
        }
        fitnessCheckRepository.findByMemberAndCheckDate(member, date)
                .ifPresentOrElse(fitnessCheckRepository::delete,
                        () -> fitnessCheckRepository.save(FitnessCheck.builder()
                                .member(member)
                                .checkDate(date)
                                .build()));
    }

    // 12월, 1월 달력 만들기
    public List<CalendarMonth> getCalendars(Set<LocalDate> checked) {
        List<CalendarMonth> months = new ArrayList<>();
        YearMonth ym = YearMonth.from(TripInfo.FITNESS_START);
        YearMonth last = YearMonth.from(TripInfo.FITNESS_END);
        while (!ym.isAfter(last)) {
            months.add(buildMonth(ym, checked));
            ym = ym.plusMonths(1);
        }
        return months;
    }

    private CalendarMonth buildMonth(YearMonth ym, Set<LocalDate> checked) {
        LocalDate today = LocalDate.now();
        List<CalendarDay> cells = new ArrayList<>();
        // 일요일 시작 달력 : 1일 앞의 빈칸 개수 (일=0, 월=1, ... 토=6)
        int blanks = ym.atDay(1).getDayOfWeek().getValue() % 7;
        for (int i = 0; i < blanks; i++) {
            cells.add(CalendarDay.blank());
        }
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            LocalDate date = ym.atDay(d);
            cells.add(new CalendarDay(date, checked.contains(date), isCheckable(date), date.equals(today)));
        }
        long count = checked.stream().filter(date -> YearMonth.from(date).equals(ym)).count();
        return new CalendarMonth(ym.getMonthValue() + "월", chunk(cells), count, ym.lengthOfMonth());
    }

    // 메인 화면 미니 달력 : 이번 주 (시작 전이면 시작하는 주)
    public List<CalendarDay> getWeekStrip(Set<LocalDate> checked) {
        LocalDate today = LocalDate.now();
        LocalDate base = today.isBefore(TripInfo.FITNESS_START) ? TripInfo.FITNESS_START : today;
        LocalDate sunday = base.minusDays(base.getDayOfWeek().getValue() % 7);
        List<CalendarDay> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = sunday.plusDays(i);
            boolean inRange = !date.isBefore(TripInfo.FITNESS_START) && !date.isAfter(TripInfo.FITNESS_END);
            days.add(inRange ? new CalendarDay(date, checked.contains(date), isCheckable(date), date.equals(today))
                    : CalendarDay.blank());
        }
        return days;
    }

    private List<List<CalendarDay>> chunk(List<CalendarDay> cells) {
        List<List<CalendarDay>> weeks = new ArrayList<>();
        for (int i = 0; i < cells.size(); i += 7) {
            List<CalendarDay> week = new ArrayList<>(cells.subList(i, Math.min(i + 7, cells.size())));
            while (week.size() < 7) {
                week.add(CalendarDay.blank());
            }
            weeks.add(week);
        }
        return weeks;
    }

}
