package com.mysite.sbb.fitness.controller;

import com.mysite.sbb.common.TripInfo;
import com.mysite.sbb.fitness.service.FitnessService;
import com.mysite.sbb.member.entity.Member;
import com.mysite.sbb.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Set;

// 체력 증진 : 12월 ~ 1월 매일 운동 체크
@Controller
@RequiredArgsConstructor
@RequestMapping("/fitness")
public class FitnessController {

    private final FitnessService fitnessService;
    private final MemberService memberService;

    @GetMapping
    public String fitness(Principal principal, Model model) {
        Member member = memberService.getMember(principal.getName());
        Set<LocalDate> checked = fitnessService.getCheckedDates(member);

        model.addAttribute("months", fitnessService.getCalendars(checked));
        model.addAttribute("dday", TripInfo.daysUntil(TripInfo.FITNESS_START));
        model.addAttribute("start", TripInfo.FITNESS_START);
        model.addAttribute("end", TripInfo.FITNESS_END);
        model.addAttribute("checkedCount", checked.size());
        return "fitness/calendar";
    }

    @PostMapping("/check")
    public String check(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        Principal principal) {
        fitnessService.toggle(memberService.getMember(principal.getName()), date);
        return "redirect:/fitness";
    }
}
