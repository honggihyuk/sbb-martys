package com.mysite.sbb.study.repository;

import com.mysite.sbb.member.entity.Member;
import com.mysite.sbb.study.entity.StudyCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyCheckRepository extends JpaRepository<StudyCheck, Long> {

    List<StudyCheck> findByMember(Member member);

    Optional<StudyCheck> findByMemberAndWeekNoAndSlot(Member member, Integer weekNo, Integer slot);

    void deleteByMember(Member member);
}
