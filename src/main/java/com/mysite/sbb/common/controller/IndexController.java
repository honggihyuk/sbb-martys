package com.mysite.sbb.common.controller;

import com.mysite.sbb.common.TripInfo;
import com.mysite.sbb.fitness.service.FitnessService;
import com.mysite.sbb.member.entity.Member;
import com.mysite.sbb.member.service.MemberService;
import com.mysite.sbb.notice.service.NoticeService;
import com.mysite.sbb.study.dto.StudyPlan;
import com.mysite.sbb.study.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Set;

// 메인 화면 : D-day, 공지, 이번 주 말씀, 체력 증진, 묵상 여정
@Controller
@RequiredArgsConstructor
public class IndexController {

    private final MemberService memberService;
    private final NoticeService noticeService;
    private final StudyService studyService;
    private final FitnessService fitnessService;

    @GetMapping("/")
    public String index(Principal principal, Model model) {
        Member me = memberService.getMember(principal.getName());

        // 1. 출발 D-day
        model.addAttribute("departure", TripInfo.DEPARTURE);
        model.addAttribute("dday", TripInfo.daysUntil(TripInfo.DEPARTURE));

        // 2. 최근 공지 4개
        model.addAttribute("noticeList", noticeService.getRecent());

        // 3. 이번 주 말씀
        Set<String> checked = studyService.getCheckedKeys(me);
        model.addAttribute("week", StudyPlan.week(StudyPlan.currentWeekNo()));
        model.addAttribute("checked", checked);

        // 4. 체력 증진 (이번 주 미니 달력)
        Set<LocalDate> fitnessChecked = fitnessService.getCheckedDates(me);
        model.addAttribute("fitnessDday", TripInfo.daysUntil(TripInfo.FITNESS_START));
        model.addAttribute("fitnessStart", TripInfo.FITNESS_START);
        model.addAttribute("weekStrip", fitnessService.getWeekStrip(fitnessChecked));

        // 5. 묵상 여정 : 지도는 화면에서 /study/journey-map (JSON) 을 불러와 그린다
        model.addAttribute("readCount", checked.size());
        model.addAttribute("total", StudyPlan.TOTAL_READINGS);
        model.addAttribute("place", studyService.currentPlace(checked));
        return "index";
    }

    // 권한이 없는 화면 (예: 이용자가 회의록에 들어온 경우)
    @GetMapping("/denied")
    public String denied() {
        return "denied";
    }
}
