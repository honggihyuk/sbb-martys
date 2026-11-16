package com.mysite.sbb.study.dto;

// 묵상 여정 지도 위에 올릴 회원 한 명
public record MapPin(String name,       // 이름
                     String initials,   // 사진이 없을 때 보여줄 두 글자
                     String photo,      // 사진 경로 (없으면 null)
                     boolean me,        // 나인지
                     int weekNo,
                     int stopIndex,
                     String place) {
}
