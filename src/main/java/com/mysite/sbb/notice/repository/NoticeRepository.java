package com.mysite.sbb.notice.repository;

import com.mysite.sbb.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findAllByOrderByCreatedDesc();

    // 공지 목록 : 고정한 공지가 맨 위, 그다음 최신순
    @Query("select n from Notice n order by case when n.pinned = true then 0 else 1 end, n.created desc")
    List<Notice> findAllPinnedFirst();

    List<Notice> findTop4ByOrderByCreatedDesc();      // 메인 화면 공지 4개

    Optional<Notice> findTopByOrderByCreatedDesc();   // 가장 최근 공지 (새 글 표시용)
}
