package com.mysite.sbb.member.constant;

import lombok.Getter;

// 로그인한 회원에게 부여할 권한
@Getter
public enum MemberRole {
    ADMIN("ROLE_ADMIN", "관리자"),
    LEADER("ROLE_LEADER", "조장"),   // 조장·리더 : 회의록 열람, 조 사진 등록
    USER("ROLE_USER", "조원");

    private final String value;
    private final String label;

    MemberRole(String value, String label) {
        this.value = value;
        this.label = label;
    }
}
