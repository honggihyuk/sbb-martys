package com.mysite.sbb.member.service;

import com.mysite.sbb.common.TripInfo;
import com.mysite.sbb.member.constant.Community;
import com.mysite.sbb.member.constant.MemberRole;
import com.mysite.sbb.member.dto.MemberDto;
import com.mysite.sbb.member.entity.Member;
import com.mysite.sbb.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    public boolean isUsernameTaken(String username) {
        return memberRepository.existsByUsername(username);
    }

    public boolean isEmailTaken(String email) {
        return memberRepository.existsByEmail(email);
    }

    public void createMember(MemberDto memberDto) {
        Member member = Member.builder()
                .username(memberDto.getUsername())
                .name(memberDto.getName())
                .teamNo(resolveTeam(memberDto.getCommunity(), memberDto.getTeamNo()))
                .community(memberDto.getCommunity())
                .password(passwordEncoder.encode(memberDto.getPassword1()))
                .email(emptyToNull(memberDto.getEmail()))
                .gender(memberDto.getGender())
                .registration(memberDto.getRegistration())
                .role(MemberRole.USER)              // 회원가입으로 만든 계정은 항상 이용자
                .created(LocalDateTime.now())
                .build();

        memberRepository.save(member);
    }

    // 로그인한 사용자 아이디(Principal.getName())로 회원 찾기
    public Member getMember(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
    }

    public List<Member> getTeamMembers(Integer teamNo) {
        return memberRepository.findByTeamNoOrderByNameAsc(teamNo);
    }

    // 관리자 화면 : 전체 회원 목록 (최근 가입순)
    public List<Member> getMemberList() {
        return memberRepository.findAll(Sort.by(Sort.Order.desc("id")));
    }

    // 내 정보 : 프로필 사진 변경 / 삭제 (path 가 null 이면 삭제)
    public void updatePhoto(String username, String path) {
        Member member = getMember(username);
        member.setPhotoPath(path);
        memberRepository.save(member);
    }

    // 공지사항을 봤다고 기록 (메뉴의 새 공지 파란 점을 끈다)
    public void markNoticeSeen(String username) {
        Member member = getMember(username);
        member.setNoticeSeen(LocalDateTime.now());
        memberRepository.save(member);
    }

    // 관리자 화면 : 권한 / 조 변경
    public void updateRoleAndTeam(Long id, MemberRole role, Integer teamNo) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
        member.setRole(role);
        member.setTeamNo(resolveTeam(member.getCommunity(), teamNo));
        memberRepository.save(member);
    }

    // 소속이 SL·ED 면 고른 조와 상관없이 SLED조, 아니면 고른 조 그대로
    public static Integer resolveTeam(Community community, Integer teamNo) {
        return (community != null && community.isSled()) ? TripInfo.SLED_TEAM_NO : teamNo;
    }

    // 빈 칸으로 온 선택 항목은 null 로 저장한다 (이메일은 unique 라서 빈 문자열이 여러 개면 겹친다)
    public static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.strip();
    }
}
