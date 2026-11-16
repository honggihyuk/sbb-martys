package com.mysite.sbb.fitness.repository;

import com.mysite.sbb.fitness.entity.FitnessCheck;
import com.mysite.sbb.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FitnessCheckRepository extends JpaRepository<FitnessCheck, Long> {

    List<FitnessCheck> findByMember(Member member);

    Optional<FitnessCheck> findByMemberAndCheckDate(Member member, LocalDate checkDate);
}
