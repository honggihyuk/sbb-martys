package com.mysite.sbb.notice.controller;

import com.mysite.sbb.member.service.MemberService;
import com.mysite.sbb.notice.dto.NoticeDto;
import com.mysite.sbb.notice.entity.Notice;
import com.mysite.sbb.notice.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

// 공지사항 : 누구나 읽고 댓글, 작성/수정/삭제는 관리자만 (SecurityConfig 에서 막음)
@Controller
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {

    private final NoticeService noticeService;
    private final MemberService memberService;

    // 목록 (?write 이면 맨 위에 공지 쓰기 폼을 펼친다 : 관리자만 보인다)
    @GetMapping
    public String list(@RequestParam(value = "write", required = false) String write,
                       Principal principal, Model model) {
        // 공지사항을 봤다고 기록 → 메뉴의 새 공지 파란 점이 꺼진다 (GlobalModelAdvice)
        if (principal != null) {
            memberService.markNoticeSeen(principal.getName());
        }
        if (write != null) {
            model.addAttribute("noticeDto", new NoticeDto());
            model.addAttribute("formAction", "/notice/create");
        }
        model.addAttribute("noticeList", noticeService.getList());
        return "notice/list";
    }

    @GetMapping("/create")
    public String createForm() {
        return "redirect:/notice?write";
    }

    @PostMapping("/create")
    public String create(@Valid NoticeDto noticeDto, BindingResult bindingResult,
                         Principal principal, Model model) {
        checkPoll(noticeDto, bindingResult);
        if (bindingResult.hasErrors()) {
            return showForm(model, "/notice/create");
        }
        noticeService.create(noticeDto, memberService.getMember(principal.getName()));
        return "redirect:/notice";
    }

    // 고치기 : 목록 맨 위에 같은 폼을 채워서 보여준다
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Notice notice = noticeService.getNotice(id);
        model.addAttribute("noticeDto", noticeService.toDto(notice));
        model.addAttribute("pollLocked", notice.getVoteCount() > 0);
        return showForm(model, "/notice/" + id + "/edit");
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long id, @Valid NoticeDto noticeDto,
                       BindingResult bindingResult, Model model) {
        Notice notice = noticeService.getNotice(id);
        if (notice.getVoteCount() == 0) {
            checkPoll(noticeDto, bindingResult);
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("pollLocked", notice.getVoteCount() > 0);
            return showForm(model, "/notice/" + id + "/edit");
        }
        noticeService.update(id, noticeDto);
        return "redirect:/notice#notice-" + id;
    }

    // 투표하기 (누구나)
    @PostMapping("/{id}/vote")
    public String vote(@PathVariable("id") Long id, @RequestParam("optionId") Long optionId, Principal principal) {
        noticeService.vote(id, optionId, memberService.getMember(principal.getName()));
        return "redirect:/notice#notice-" + id;
    }

    private String showForm(Model model, String formAction) {
        model.addAttribute("formAction", formAction);
        model.addAttribute("noticeList", noticeService.getList());
        return "notice/list";
    }

    // '투표 넣기'를 골랐으면 선택지가 2개 이상이어야 한다
    private void checkPoll(NoticeDto dto, BindingResult bindingResult) {
        if (dto.isPoll() && dto.cleanOptions().size() < 2) {
            bindingResult.rejectValue("pollOptions", "poll", "선택지를 2개 이상 입력해주세요.");
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id) {
        noticeService.delete(id);
        return "redirect:/notice";
    }

    @PostMapping("/{id}/comment")
    public String comment(@PathVariable("id") Long id,
                          @RequestParam("content") String content,
                          Principal principal) {
        if (!content.isBlank()) {
            noticeService.addComment(id, content.strip(), memberService.getMember(principal.getName()));
        }
        return "redirect:/notice?open=" + id + "#notice-" + id;
    }

    @PostMapping("/comment/{commentId}/delete")
    public String deleteComment(@PathVariable("commentId") Long commentId,
                                @RequestParam("noticeId") Long noticeId,
                                Principal principal) {
        noticeService.deleteComment(commentId, memberService.getMember(principal.getName()));
        return "redirect:/notice?open=" + noticeId + "#notice-" + noticeId;
    }
}
