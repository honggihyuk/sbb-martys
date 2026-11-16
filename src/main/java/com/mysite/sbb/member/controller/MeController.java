package com.mysite.sbb.member.controller;

import com.mysite.sbb.common.file.FileStore;
import com.mysite.sbb.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

// 내 정보 (프로필 사진 바꾸기 / 지우기, 로그아웃)
@Controller
@RequiredArgsConstructor
public class MeController {

    private final MemberService memberService;
    private final FileStore fileStore;

    @GetMapping("/me")
    public String me() {
        return "member/me";   // 회원 정보는 GlobalModelAdvice 의 loginMember 사용
    }

    @PostMapping("/me/photo")
    public String changePhoto(@RequestParam("photo") MultipartFile photo,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            String path = fileStore.store(photo);
            if (path != null) {
                memberService.updatePhoto(principal.getName(), path);
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/me";
    }

    @PostMapping("/me/photo/delete")
    public String deletePhoto(Principal principal) {
        memberService.updatePhoto(principal.getName(), null);
        return "redirect:/me";
    }
}
