package com.mysite.sbb.group.service;

import com.mysite.sbb.common.TripInfo;
import com.mysite.sbb.group.dto.TeamView;
import com.mysite.sbb.group.entity.TeamInfo;
import com.mysite.sbb.group.repository.TeamInfoRepository;
import com.mysite.sbb.member.entity.Member;
import com.mysite.sbb.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final TeamInfoRepository teamInfoRepository;
    private final MemberService memberService;

    public TeamInfo getInfo(int teamNo) {
        return teamInfoRepository.findById(teamNo)
                .orElseGet(() -> TeamInfo.builder().teamNo(teamNo).build());
    }

    public TeamView getTeam(int teamNo) {
        // 조장·리더를 맨 앞(왼쪽 위)에, 나머지는 이름순
        List<Member> members = memberService.getTeamMembers(teamNo).stream()
                .sorted(Comparator.comparing((Member m) -> !m.isLeader()))
                .toList();
        return new TeamView(teamNo, getInfo(teamNo), members);
    }

    // 전체 조 (내 조는 빼고 싶으면 exceptTeamNo 사용)
    public List<TeamView> getTeams(Integer exceptTeamNo) {
        List<TeamView> list = new ArrayList<>();
        for (int no : TripInfo.teams(true).keySet()) {      // 1조 ~ 6조, SLED조
            if (exceptTeamNo != null && exceptTeamNo == no) {
                continue;
            }
            list.add(getTeam(no));
        }
        return list;
    }

    public long countNotes() {
        return getTeams(null).stream().filter(TeamView::hasNote).count();
    }

    // 사진은 그 조의 조장(리더)이나 관리자만 바꿀 수 있다
    public boolean canEditPhoto(Member member, int teamNo) {
        return member.isAdmin() || (member.isLeader() && Integer.valueOf(teamNo).equals(member.getTeamNo()));
    }

    // 의견은 그 조 조원이면 누구나 수정
    public boolean canEditNote(Member member, int teamNo) {
        return member.isAdmin() || Integer.valueOf(teamNo).equals(member.getTeamNo());
    }

    public void updatePhoto(int teamNo, String photoPath) {
        TeamInfo info = getInfo(teamNo);
        info.setPhotoPath(photoPath);
        teamInfoRepository.save(info);
    }

    public void updateNote(int teamNo, String note, Member member) {
        TeamInfo info = getInfo(teamNo);
        info.setNote(note);
        info.setNoteUpdatedBy(member);
        info.setNoteUpdated(LocalDateTime.now());
        teamInfoRepository.save(info);
    }
}
