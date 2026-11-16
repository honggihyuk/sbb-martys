package com.mysite.sbb.minutes.controller;

import com.mysite.sbb.member.service.MemberService;
import com.mysite.sbb.minutes.dto.MinutesDto;
import com.mysite.sbb.minutes.service.MinutesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;

// 회의록 : SecurityConfig 에서 LEADER, ADMIN 만 /minutes/** 에 들어올 수 있게 막는다
@Controller
@RequiredArgsConstructor
@RequestMapping("/minutes")
public class MinutesController {

    private final MinutesService minutesService;
    private final MemberService memberService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("minutesList", minutesService.getList());
        return "minutes/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("minutes", minutesService.getMinutes(id));
        return "minutes/detail";
    }

    @GetMapping("/create")
    public String createForm(MinutesDto minutesDto) {
        minutesDto.setMeetingDate(LocalDate.now());
        return "minutes/form";
    }

    @PostMapping("/create")
    public String create(@Valid MinutesDto minutesDto, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "minutes/form";
        }
        minutesService.create(minutesDto, memberService.getMember(principal.getName()));
        return "redirect:/minutes";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id) {
        minutesService.delete(id);
        return "redirect:/minutes";
    }
}
