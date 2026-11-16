package com.mysite.sbb.member.repository;

import com.mysite.sbb.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 로그인할 때 사용자 ID 로 회원 조회
    Optional<Member> findByUsername(String username);

    // 회원가입할 때 중복 확인
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // 관리자 : 회원 수정할 때 이메일 중복 확인
    Optional<Member> findByEmail(String email);

    // 조별 활동 : 같은 조 회원 (이름순)
    List<Member> findByTeamNoOrderByNameAsc(Integer teamNo);
}
