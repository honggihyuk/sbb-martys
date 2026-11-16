package com.mysite.sbb.group.dto;

import com.mysite.sbb.common.TripInfo;
import com.mysite.sbb.group.entity.TeamInfo;
import com.mysite.sbb.member.entity.Member;

import java.util.List;

// 화면에 보여줄 조 하나 (조 정보 + 조원 목록)
public record TeamView(int teamNo, TeamInfo info, List<Member> members) {

    public String getName() {
        return TripInfo.teamName(teamNo);
    }

    public boolean hasNote() {
        return info.getNote() != null && !info.getNote().isBlank();
    }
}
