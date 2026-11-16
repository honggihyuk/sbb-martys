package com.mysite.sbb.moment.entity;

import com.mysite.sbb.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 모임 기록 : 사진 + 글
@Entity
@Getter
@Setter
@ToString(exclude = "author")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Moment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDate meetDate;     // 모임한 날

    private String photoPath;       // 대표 사진 경로 (/uploads/...) : 목록 카드에 보인다

    // 사진 여러 장 (순서대로) : moment_photo 테이블에 저장된다
    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "moment_photo", joinColumns = @JoinColumn(name = "moment_id"))
    @OrderColumn(name = "sort_order")
    @Column(name = "path")
    private List<String> photos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;

    private LocalDateTime created;

    // 화면에 보여줄 사진 전부 (예전에 한 장만 올린 기록도 포함)
    public List<String> getAllPhotos() {
        if (!photos.isEmpty()) {
            return photos;
        }
        return photoPath == null ? List.of() : List.of(photoPath);
    }
}
