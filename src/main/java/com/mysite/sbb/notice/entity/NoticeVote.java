package com.mysite.sbb.notice.entity;

import com.mysite.sbb.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 투표 한 표 : 회원 한 명은 공지 하나에 한 항목만 고를 수 있다 (NoticeService.vote 에서 확인)
@Entity
@Getter
@Setter
@ToString(exclude = {"option", "member"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"option_id", "member_id"}))
public class NoticeVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private NoticePollOption option;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Member member;

    private LocalDateTime created;
}
