package com.mysite.sbb.notice.entity;

import com.mysite.sbb.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(exclude = {"author", "commentList", "pollOptions"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;

    private LocalDateTime created;

    @Builder.Default
    private Boolean pinned = false;         // 중요 공지 : 맨 위에 고정

    @Builder.Default
    private Boolean multiple = false;       // 투표 : 여러 개 고를 수 있게

    @Builder.Default
    @OrderBy("id asc")
    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticeComment> commentList = new ArrayList<>();

    // 투표 항목 (없으면 투표 없는 공지)
    @Builder.Default
    @OrderBy("sortOrder asc")
    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoticePollOption> pollOptions = new ArrayList<>();

    public boolean isPinnedNotice() {
        return Boolean.TRUE.equals(pinned);
    }

    public boolean hasPoll() {
        return !pollOptions.isEmpty();
    }

    public boolean isMultipleChoice() {
        return Boolean.TRUE.equals(multiple);
    }

    // 투표한 사람 수 (여러 개 고르기여도 한 사람은 한 명으로 센다)
    public int getVoteCount() {
        return (int) pollOptions.stream()
                .flatMap(o -> o.getVotes().stream())
                .filter(v -> v.getMember() != null)
                .map(v -> v.getMember().getId())
                .distinct()
                .count();
    }

    // 이 회원이 투표했는지
    public boolean hasVoted(Long memberId) {
        return pollOptions.stream().anyMatch(o -> o.votedBy(memberId));
    }
}
