package com.mysite.sbb.member.constant;

import lombok.Getter;

// 소속 공동체 (교회) : 회원가입 / 관리자 회원 등록 화면의 선택 목록
// DB 에는 이름(SONGDO ...)이 저장되고, 화면에는 label(송도 ...)을 보여준다.
// sled 가 true 인 소속(SL, ED)은 조를 고르지 않고 SLED조로 자동 배정된다 (TripInfo.SLED_TEAM_NO).
@Getter
public enum Community {
    SONGDO("송도", false),
    TTORAE("또래", false),
    MIDDLE("미들", false),
    IEUM("이음", false),
    SL("SL", true),
    ED("ED", true);

    private final String label;
    private final boolean sled;

    Community(String label, boolean sled) {
        this.label = label;
        this.sled = sled;
    }
}
