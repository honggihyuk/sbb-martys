package com.mysite.sbb.common.advice;

import com.mysite.sbb.member.entity.Member;
import com.mysite.sbb.member.repository.MemberRepository;
import com.mysite.sbb.notice.service.NoticeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

// 모든 화면(헤더)에서 공통으로 쓰는 값을 model 에 넣어준다.
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final MemberRepository memberRepository;
    private final NoticeService noticeService;

    // 로그인한 회원 (헤더의 이름, 프로필 사진)
    @ModelAttribute("loginMember")
    public Member loginMember(Principal principal) {
        if (principal == null) {
            return null;
        }
        return memberRepository.findByUsername(principal.getName()).orElse(null);
    }

    // 지금 주소 (헤더에서 현재 메뉴에 밑줄 표시)
    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    // 새 공지 표시 (공지사항 메뉴 옆 파란 점)
    //  - 내가 마지막으로 공지사항을 본 뒤에 새 공지가 올라왔을 때만 켠다
    //  - 공지사항 화면에 있는 동안은 끈다 (들어오는 순간 NoticeController 가 '봤음'으로 기록)
    @ModelAttribute("hasNewNotice")
    public boolean hasNewNotice(Principal principal, HttpServletRequest request) {
        if (principal == null || request.getRequestURI().startsWith("/notice")) {
            return false;
        }
        return memberRepository.findByUsername(principal.getName())
                .map(m -> noticeService.hasNewSince(m.getNoticeSeen()))
                .orElse(false);
    }
}
