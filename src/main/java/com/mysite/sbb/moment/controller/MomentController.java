package com.mysite.sbb.moment.controller;

import com.mysite.sbb.common.file.FileStore;
import com.mysite.sbb.common.text.SimpleMarkdown;
import com.mysite.sbb.member.service.MemberService;
import com.mysite.sbb.moment.dto.MomentDto;
import com.mysite.sbb.moment.entity.Moment;
import com.mysite.sbb.moment.service.MomentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// 모임 기록 : 함께한 모임을 사진과 글로 남기기
@Controller
@RequiredArgsConstructor
@RequestMapping("/moments")
public class MomentController {

    private final MomentService momentService;
    private final MemberService memberService;
    private final FileStore fileStore;
    private final SimpleMarkdown markdown;

    private static final int MAX_PHOTOS = 10;

    // 목록 (?write 이면 맨 위에 기록하기 폼을 펼친다)
    @GetMapping
    public String list(@RequestParam(value = "write", required = false) String write, Model model) {
        if (write != null) {
            MomentDto momentDto = new MomentDto();
            momentDto.setMeetDate(LocalDate.now());
            model.addAttribute("momentDto", momentDto);
            model.addAttribute("showForm", true);
        }
        model.addAttribute("momentMap", momentService.getGroupedByMonth());
        return "moment/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Moment moment = momentService.getMoment(id);
        model.addAttribute("moment", moment);
        // 본문의 # 제목, - 목록, **굵게** 를 HTML 로 바꿔서 넘긴다 (화면에서는 th:utext 로 그대로 출력)
        model.addAttribute("contentHtml", markdown.toHtml(moment.getContent()));
        return "moment/detail";
    }

    @GetMapping("/create")
    public String createForm() {
        return "redirect:/moments?write";
    }

    // 사진은 여러 장 (input name="photos" multiple)
    @PostMapping("/create")
    public String create(@Valid MomentDto momentDto, BindingResult bindingResult,
                         @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
                         Principal principal, Model model) {
        List<MultipartFile> files = (photos == null) ? List.of()
                : photos.stream().filter(f -> f != null && !f.isEmpty()).toList();
        if (files.size() > MAX_PHOTOS) {
            bindingResult.reject("photos", "사진은 " + MAX_PHOTOS + "장까지 올릴 수 있어요.");
        }
        if (bindingResult.hasErrors()) {
            return showForm(model);
        }
        List<String> paths = new ArrayList<>();
        try {
            for (MultipartFile f : files) {
                paths.add(fileStore.store(f));
            }
        } catch (IllegalArgumentException e) {
            bindingResult.reject("photos", e.getMessage());
            return showForm(model);
        }
        momentService.create(momentDto, paths, memberService.getMember(principal.getName()));
        return "redirect:/moments";
    }

    private String showForm(Model model) {
        model.addAttribute("showForm", true);
        model.addAttribute("momentMap", momentService.getGroupedByMonth());
        return "moment/list";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, Principal principal) {
        momentService.delete(id, memberService.getMember(principal.getName()));
        return "redirect:/moments";
    }
}
