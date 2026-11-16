package com.mysite.sbb.minutes.entity;

import com.mysite.sbb.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 회의록 : 조장·리더만 보고 쓸 수 있다
@Entity
@Getter
@Setter
@ToString(exclude = "author")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Minutes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    private LocalDate meetingDate;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;

    private LocalDateTime created;
}
