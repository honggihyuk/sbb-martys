package com.mysite.sbb.study.controller;

import com.mysite.sbb.member.entity.Member;
import com.mysite.sbb.member.service.MemberService;
import com.mysite.sbb.study.dto.JourneyMapData;
import com.mysite.sbb.study.dto.StudyPlan;
import com.mysite.sbb.study.dto.StudyWeek;
import com.mysite.sbb.study.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 말씀 묵상 : 16주 한눈에 / 주차별로 읽기
@Controller
@RequiredArgsConstructor
@RequestMapping("/study")
public class StudyController {

    private final StudyService studyService;
    private final MemberService memberService;

    @GetMapping
    public String study(@RequestParam(value = "view", defaultValue = "all") String view,
                        @RequestParam(value = "week", required = false) Integer week,
                        Principal principal,
                        Model model) {
        Member member = memberService.getMember(principal.getName());
        Set<String> checked = studyService.getCheckedKeys(member);

        // 전도여행별로 묶기 (1차, 2차, 3차, 로마 호송)
        Map<Integer, List<StudyWeek>> journeys = new LinkedHashMap<>();
        for (StudyWeek w : StudyPlan.WEEKS) {
            journeys.computeIfAbsent(w.journey(), k -> new java.util.ArrayList<>()).add(w);
        }

        // 전도여행별 읽은 개수 (예: 1차 8/8)
        Map<Integer, Integer> journeyCounts = new LinkedHashMap<>();
        for (StudyWeek w : StudyPlan.WEEKS) {
            int c = (checked.contains(w.weekNo() + "-0") ? 1 : 0) + (checked.contains(w.weekNo() + "-1") ? 1 : 0);
            journeyCounts.merge(w.journey(), c, Integer::sum);
        }
        model.addAttribute("journeyCounts", journeyCounts);

        int currentWeekNo = StudyPlan.currentWeekNo();
        int selectedWeek = (week == null) ? currentWeekNo : Math.max(1, Math.min(week, StudyPlan.WEEKS.size()));

        model.addAttribute("view", view);
        model.addAttribute("journeys", journeys);
        model.addAttribute("journeyNames", StudyPlan.JOURNEY_NAMES);
        model.addAttribute("checked", checked);
        model.addAttribute("total", StudyPlan.TOTAL_READINGS);
        model.addAttribute("currentWeekNo", currentWeekNo);
        model.addAttribute("selected", StudyPlan.week(selectedWeek));
        model.addAttribute("weeks", StudyPlan.WEEKS);
        model.addAttribute("place", studyService.currentPlace(checked));
        return "study/list";
    }

    // 묵상 여정 지도 데이터 (JSON) : static/js/journey-map.js 가 불러간다
    @GetMapping("/journey-map")
    @ResponseBody
    public JourneyMapData journeyMap(Principal principal) {
        Member me = memberService.getMember(principal.getName());
        return studyService.getJourneyMap(memberService.getMemberList(), me);
    }

    // 동그라미(체크) 누르기
    @PostMapping("/check")
    public String check(@RequestParam("week") int week,
                        @RequestParam("slot") int slot,
                        @RequestParam(value = "redirect", defaultValue = "/study") String redirect,
                        Principal principal) {
        Member member = memberService.getMember(principal.getName());
        studyService.toggle(member, week, slot);
        // 다른 사이트로 넘어가지 않도록 내부 주소만 허용
        return "redirect:" + (redirect.startsWith("/") && !redirect.startsWith("//") ? redirect : "/study");
    }

    @PostMapping("/reset")
    public String reset(Principal principal) {
        studyService.reset(memberService.getMember(principal.getName()));
        return "redirect:/study";
    }
}
