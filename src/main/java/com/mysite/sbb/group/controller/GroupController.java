package com.mysite.sbb.group.controller;

import com.mysite.sbb.common.TripInfo;
import com.mysite.sbb.common.file.FileStore;
import com.mysite.sbb.group.service.GroupService;
import com.mysite.sbb.member.entity.Member;
import com.mysite.sbb.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

// 조별 활동 (조원·사진, 조별 의견)
@Controller
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final MemberService memberService;
    private final FileStore fileStore;

    // 예전 '팀 구성' 주소 : 조원 명단은 조별 활동으로 옮겼다
    @GetMapping("/team")
    public String team() {
        return "redirect:/group";
    }

    // 조별 활동 : view=photos(기본) 또는 view=notes
    @GetMapping("/group")
    public String group(@RequestParam(value = "view", defaultValue = "photos") String view,
                        @RequestParam(value = "edit", defaultValue = "false") boolean edit,
                        Principal principal, Model model) {
        Member me = memberService.getMember(principal.getName());
        Integer myTeamNo = me.getTeamNo();

        model.addAttribute("view", view);
        model.addAttribute("edit", edit);
        model.addAttribute("teamCount", TripInfo.teams(true).size());
        model.addAttribute("myTeam", myTeamNo == null ? null : groupService.getTeam(myTeamNo));
        model.addAttribute("otherTeams", groupService.getTeams(myTeamNo));
        model.addAttribute("noteCount", groupService.countNotes());
        model.addAttribute("canEditPhoto", myTeamNo != null && groupService.canEditPhoto(me, myTeamNo));
        return "notes".equals(view) ? "group/notes" : "group/photos";
    }

    @PostMapping("/group/{teamNo}/photo")
    public String photo(@PathVariable("teamNo") int teamNo,
                        @RequestParam("photo") MultipartFile photo,
                        Principal principal, RedirectAttributes redirectAttributes) {
        Member me = memberService.getMember(principal.getName());
        if (groupService.canEditPhoto(me, teamNo)) {
            try {
                String path = fileStore.store(photo);
                if (path != null) {
                    groupService.updatePhoto(teamNo, path);
                }
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            }
        }
        return "redirect:/group";
    }

    @PostMapping("/group/{teamNo}/photo/delete")
    public String deletePhoto(@PathVariable("teamNo") int teamNo, Principal principal) {
        if (groupService.canEditPhoto(memberService.getMember(principal.getName()), teamNo)) {
            groupService.updatePhoto(teamNo, null);
        }
        return "redirect:/group";
    }

    @PostMapping("/group/{teamNo}/note")
    public String note(@PathVariable("teamNo") int teamNo,
                       @RequestParam("note") String note,
                       Principal principal) {
        Member me = memberService.getMember(principal.getName());
        if (groupService.canEditNote(me, teamNo)) {
            groupService.updateNote(teamNo, note.strip(), me);
        }
        return "redirect:/group?view=notes";
    }
}
