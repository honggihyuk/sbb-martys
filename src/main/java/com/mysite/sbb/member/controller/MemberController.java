package com.mysite.sbb.member.controller;


import com.mysite.sbb.common.TripInfo;
import com.mysite.sbb.member.constant.Community;
import com.mysite.sbb.member.dto.MemberDto;
import com.mysite.sbb.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    // 회원가입 화면의 소속 선택 목록 (이 컨트롤러의 모든 화면에 model 로 들어간다)
    // 회원가입 화면의 조 선택 목록 (SLED조는 소속으로 자동 배정되므로 빼고)
    @ModelAttribute("teams")
    public Map<Integer, String> teams() {
        return TripInfo.teams(false);
    }

    @ModelAttribute("communities")
    public Community[] communities() {
        return Community.values();
    }

    // 로그인 화면 (POST /member/login 은 Spring Security 가 처리하므로 GET 만 만든다)
    @GetMapping("/login")
    public String login() {
        return "member/login";
    }

    @GetMapping("/signup")
    public String signup(MemberDto memberDto) {
        return "member/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid MemberDto memberDto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        log.info("=======> memberDto = {}", memberDto);
        // 조는 소속이 SL·ED 가 아닐 때만 필수 (SL·ED 는 SLED조로 자동 배정)
        if (memberDto.getCommunity() != null && !memberDto.getCommunity().isSled() && memberDto.getTeamNo() == null) {
            bindingResult.rejectValue("teamNo", "required", "조를 선택하세요.");
        }
        if(bindingResult.hasErrors()) {
            return "member/signup";
        }

        if(!memberDto.getPassword1().equals(memberDto.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordIncorrect", "2개의 패스워드가 일치하지 않습니다.");
            return "member/signup";
        }

        if(memberService.isUsernameTaken(memberDto.getUsername())) {
            bindingResult.rejectValue("username", "duplicated", "이미 사용 중인 사용자ID 입니다.");
            return "member/signup";
        }

        String email = MemberService.emptyToNull(memberDto.getEmail());
        if(email != null && memberService.isEmailTaken(email)) {
            bindingResult.rejectValue("email", "duplicated", "이미 가입된 이메일입니다.");
            return "member/signup";
        }

        memberService.createMember(memberDto);

        // 가입 후 로그인 화면으로 이동하면서 안내 메시지 전달
        redirectAttributes.addFlashAttribute("signupSuccess", "회원가입이 완료되었습니다. 로그인해 주세요.");
        return "redirect:/member/login";
    }

}
