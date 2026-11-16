package com.mysite.sbb.member.entity;


import com.mysite.sbb.common.TripInfo;
import com.mysite.sbb.member.constant.Community;
import com.mysite.sbb.member.constant.Gender;
import com.mysite.sbb.member.constant.MemberRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;                // 사용자 ID (로그인 아이디 → 중복 불가)

    private String password;                // 비밀번호 (BCrypt 로 암호화해서 저장)

    @Column(length = 20)
    private String name;                    // 화면에 보이는 이름 (예: 홍기혁)

    private Integer teamNo;                 // 소속 조 번호 (1 ~ TEAM_COUNT, SLED조는 0)

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Community community;            // 소속 공동체 (송도, 또래, 미들, 이음, SL, ED)

    private String photoPath;               // 프로필 사진 경로 (/uploads/...)

    @Column(unique = true)
    private String email;                   // 이메일 (선택, 안 쓰면 null)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;                  // 성별

    @Column(nullable = false)
    private Boolean registration;           // 등록 확인

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MemberRole role;                // 권한 (USER : 이용자, LEADER : 조장, ADMIN : 관리자)

    private LocalDateTime created;          // 생성일

    private LocalDateTime noticeSeen;       // 공지사항을 마지막으로 본 때 (이후에 올라온 공지가 있으면 메뉴에 파란 점)

    // 화면 표시용 이름 : 이름이 없으면 아이디
    public String getDisplayName() {
        return (name == null || name.isBlank()) ? username : name;
    }

    // 프로필 동그라미에 넣을 글자 (이름 뒤 2글자, 예: 홍기혁 → 기혁)
    public String getInitials() {
        String n = getDisplayName();
        return n.length() <= 2 ? n : n.substring(n.length() - 2);
    }

    // 내 정보 화면 부제 (예: 송도 · 남)
    public String getSubtitle() {
        String g = (gender == null) ? "" : (gender == Gender.MALE ? "남" : "여");
        return (community == null) ? g : community.getLabel() + " · " + g;
    }

    // 조 이름 (예: 3조, SLED조)
    public String getTeamName() {
        return TripInfo.teamName(teamNo);
    }

    // 조장 표시 이름 : SL·ED 소속(SLED조)은 '리더', 나머지는 '조장' (권한은 둘 다 LEADER 로 같다)
    public String getLeaderLabel() {
        return (community != null && community.isSled()) ? "리더" : "조장";
    }

    public boolean isLeader() {
        return role == MemberRole.LEADER;
    }

    public boolean isAdmin() {
        return role == MemberRole.ADMIN;
    }
}
