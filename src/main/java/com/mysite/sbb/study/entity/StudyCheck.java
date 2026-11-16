package com.mysite.sbb.study.entity;

import com.mysite.sbb.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 말씀 묵상 체크 기록 : 회원이 몇 주차의 어떤 본문(0 = 사도행전, 1 = 서신서)을 읽었는지
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "week_no", "slot"}))
public class StudyCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    private Integer weekNo;     // 1 ~ 16

    private Integer slot;       // 0 : 사도행전, 1 : 서신서

    private LocalDateTime created;
}
