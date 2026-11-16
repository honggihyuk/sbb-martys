package com.mysite.sbb.group.entity;

import com.mysite.sbb.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 조별 정보 : 단체 사진 + 조별 의견 (조 하나에 한 줄)
@Entity
@Getter
@Setter
@ToString(exclude = "noteUpdatedBy")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamInfo {

    @Id
    private Integer teamNo;             // 1 ~ TEAM_COUNT, SLED조는 0 (조 번호를 그대로 기본키로 사용)

    private String photoPath;           // 단체 사진 (조장이 올림)

    @Column(columnDefinition = "TEXT")
    private String note;                // 조별 의견

    @ManyToOne(fetch = FetchType.LAZY)
    private Member noteUpdatedBy;       // 마지막으로 의견을 수정한 사람

    private LocalDateTime noteUpdated;
}
