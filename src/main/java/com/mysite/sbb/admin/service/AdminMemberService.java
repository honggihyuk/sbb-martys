package com.mysite.sbb.admin.service;

import com.mysite.sbb.admin.dto.AdminMemberDto;
import com.mysite.sbb.member.entity.Member;
import com.mysite.sbb.member.repository.MemberRepository;
import com.mysite.sbb.member.service.MemberService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// 관리자 : 회원 등록 / 수정 / 삭제
@Service
@RequiredArgsConstructor
@Transactional
public class AdminMemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager em;

    @Transactional(readOnly = true)
    public Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
    }

    // 중복 확인 (수정할 때는 자기 자신은 빼고)
    @Transactional(readOnly = true)
    public boolean isUsernameTaken(String username, Long exceptId) {
        return memberRepository.findByUsername(username)
                .filter(m -> !m.getId().equals(exceptId))
                .isPresent();
    }

    @Transactional(readOnly = true)
    public boolean isEmailTaken(String email, Long exceptId) {
        return memberRepository.findByEmail(email)
                .filter(m -> !m.getId().equals(exceptId))
                .isPresent();
    }

    public Member create(AdminMemberDto dto) {
        Member member = Member.builder()
                .password(passwordEncoder.encode(dto.getPassword()))
                .created(LocalDateTime.now())
                .build();
        copy(dto, member);
        return memberRepository.save(member);
    }

    public void update(Long id, AdminMemberDto dto) {
        Member member = getMember(id);
        copy(dto, member);
        // 비밀번호 칸을 채웠을 때만 바꾼다 (비밀번호 초기화)
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            member.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
    }

    private void copy(AdminMemberDto dto, Member member) {
        member.setUsername(dto.getUsername().strip());
        member.setName(dto.getName().strip());
        member.setEmail(MemberService.emptyToNull(dto.getEmail()));
        member.setTeamNo(MemberService.resolveTeam(dto.getCommunity(), dto.getTeamNo()));
        member.setCommunity(dto.getCommunity());
        member.setGender(dto.getGender());
        member.setRole(dto.getRole());
        member.setRegistration(dto.isRegistration());
    }

    // 회원 삭제
    //  - 개인 기록(말씀 체크, 운동 체크, 공지 투표)은 같이 지운다
    //  - 공지·댓글·모임 기록·회의록·지도 일정처럼 다른 사람도 보는 글은 남기고, 작성자만 비운다 (화면에는 '탈퇴한 회원')
    public void delete(Long id) {
        Member member = getMember(id);

        em.createQuery("delete from StudyCheck c where c.member = :m").setParameter("m", member).executeUpdate();
        em.createQuery("delete from FitnessCheck c where c.member = :m").setParameter("m", member).executeUpdate();
        em.createQuery("delete from NoticeVote v where v.member = :m").setParameter("m", member).executeUpdate();

        String[] authored = {"Notice", "NoticeComment", "Moment", "Minutes", "TripDay", "TripStop"};
        for (String entity : authored) {
            em.createQuery("update " + entity + " e set e.author = null where e.author = :m")
                    .setParameter("m", member).executeUpdate();
        }
        em.createQuery("update TeamInfo t set t.noteUpdatedBy = null where t.noteUpdatedBy = :m")
                .setParameter("m", member).executeUpdate();

        memberRepository.delete(member);
    }
}
