package com.mysite.sbb.notice.repository;

import com.mysite.sbb.notice.entity.NoticeComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeCommentRepository extends JpaRepository<NoticeComment, Long> {
}
