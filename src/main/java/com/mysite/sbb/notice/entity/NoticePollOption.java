package com.mysite.sbb.notice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

// 공지 투표 항목 하나 (예: '마르투스')
@Entity
@Getter
@Setter
@ToString(exclude = {"notice", "votes"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticePollOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Notice notice;

    @Column(length = 100, nullable = false)
    private String text;

    @Column(length = 200)
    private String description;             // 설명 (선택) : 예) 이름에 담긴 뜻

    private Integer sortOrder;

    // 이 항목을 고른 표 (항목이 지워지면 표도 같이 지워진다)
    @Builder.Default
    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticeVote> votes = new ArrayList<>();

    // 이 회원이 이 항목을 골랐는지
    public boolean votedBy(Long memberId) {
        return votes.stream().anyMatch(v -> v.getMember() != null && v.getMember().getId().equals(memberId));
    }

    public boolean hasDescription() {
        return description != null && !description.isBlank();
    }

    // 투표한 사람 중 몇 % 가 골랐는지 (막대 길이)
    public int percentOf(int total) {
        return total == 0 ? 0 : Math.round(votes.size() * 100f / total);
    }
}
