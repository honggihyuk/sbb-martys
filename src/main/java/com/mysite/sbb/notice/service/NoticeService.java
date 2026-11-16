package com.mysite.sbb.notice.service;

import com.mysite.sbb.member.entity.Member;
import com.mysite.sbb.notice.dto.NoticeDto;
import com.mysite.sbb.notice.entity.Notice;
import com.mysite.sbb.notice.entity.NoticeComment;
import com.mysite.sbb.notice.repository.NoticeCommentRepository;
import com.mysite.sbb.notice.repository.NoticeRepository;
import com.mysite.sbb.notice.entity.NoticePollOption;
import com.mysite.sbb.notice.entity.NoticeVote;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeCommentRepository noticeCommentRepository;

    public List<Notice> getList() {
        return noticeRepository.findAllPinnedFirst();
    }

    public List<Notice> getRecent() {
        return noticeRepository.findTop4ByOrderByCreatedDesc();
    }

    // 마지막으로 공지사항을 본 뒤(seen)에 새 공지가 올라왔으면 true → 메뉴에 파란 점
    // seen 이 null 이면 아직 한 번도 안 본 것이므로 공지가 하나라도 있으면 true
    public boolean hasNewSince(LocalDateTime seen) {
        return noticeRepository.findTopByOrderByCreatedDesc()
                .map(n -> seen == null || (n.getCreated() != null && n.getCreated().isAfter(seen)))
                .orElse(false);
    }

    public Notice getNotice(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공지사항이 없습니다."));
    }

    @Transactional
    public void create(NoticeDto dto, Member author) {
        Notice notice = Notice.builder()
                .subject(dto.getSubject())
                .content(dto.getContent())
                .pinned(dto.isPinned())
                .multiple(dto.isPoll() && dto.isMultiple())
                .author(author)
                .created(LocalDateTime.now())
                .build();
        if (dto.isPoll()) {
            setOptions(notice, dto.cleanOptions());
        }
        noticeRepository.save(notice);
    }

    @Transactional
    public void update(Long id, NoticeDto dto) {
        Notice notice = getNotice(id);
        notice.setSubject(dto.getSubject());
        notice.setContent(dto.getContent());
        notice.setPinned(dto.isPinned());
        // 선택지와 '여러 개 고르기'는 아직 아무도 투표하지 않았을 때만 바꿀 수 있다
        if (notice.getVoteCount() == 0) {
            notice.getPollOptions().clear();
            notice.setMultiple(dto.isPoll() && dto.isMultiple());
            if (dto.isPoll()) {
                setOptions(notice, dto.cleanOptions());
            }
        } else if (!dto.isPoll()) {
            notice.getPollOptions().clear();      // 투표 빼기 (표도 같이 지워진다)
            notice.setMultiple(false);
        }
    }

    private void setOptions(Notice notice, List<NoticeDto.Option> options) {
        for (int i = 0; i < options.size(); i++) {
            NoticeDto.Option o = options.get(i);
            notice.getPollOptions().add(NoticePollOption.builder()
                    .notice(notice).text(o.text()).description(o.description()).sortOrder(i).build());
        }
    }

    // 고치기 화면 : 공지 → 폼
    public NoticeDto toDto(Notice notice) {
        List<String> options = new ArrayList<>(notice.getPollOptions().stream().map(NoticePollOption::getText).toList());
        List<String> descriptions = new ArrayList<>(notice.getPollOptions().stream()
                .map(o -> o.getDescription() == null ? "" : o.getDescription()).toList());
        while (options.size() < 2) {
            options.add("");
            descriptions.add("");
        }
        return NoticeDto.builder()
                .subject(notice.getSubject())
                .content(notice.getContent())
                .pinned(notice.isPinnedNotice())
                .poll(notice.hasPoll())
                .multiple(notice.isMultipleChoice())
                .pollOptions(options)
                .pollDescriptions(descriptions)
                .build();
    }

    // 투표 : 같은 항목을 다시 누르면 취소
    //  - 한 개만 고르기 : 다른 항목을 누르면 그쪽으로 바꾼다
    //  - 여러 개 고르기 : 누르는 항목마다 따로 켜고 끈다
    @Transactional
    public void vote(Long noticeId, Long optionId, Member member) {
        Notice notice = getNotice(noticeId);
        NoticePollOption chosen = notice.getPollOptions().stream()
                .filter(o -> o.getId().equals(optionId)).findFirst()
                .orElseThrow(() -> new EntityNotFoundException("투표 항목이 없습니다."));
        boolean wasChosen = chosen.votedBy(member.getId());
        if (notice.isMultipleChoice()) {
            chosen.getVotes().removeIf(v -> v.getMember() != null && v.getMember().getId().equals(member.getId()));
        } else {
            for (NoticePollOption o : notice.getPollOptions()) {
                o.getVotes().removeIf(v -> v.getMember() != null && v.getMember().getId().equals(member.getId()));
            }
        }
        if (!wasChosen) {
            chosen.getVotes().add(NoticeVote.builder().option(chosen).member(member).created(LocalDateTime.now()).build());
        }
    }

    public void delete(Long id) {
        noticeRepository.delete(getNotice(id));
    }

    public void addComment(Long noticeId, String content, Member author) {
        noticeCommentRepository.save(NoticeComment.builder()
                .notice(getNotice(noticeId))
                .author(author)
                .content(content)
                .created(LocalDateTime.now())
                .build());
    }

    // 댓글 삭제 : 작성자 본인 또는 관리자만
    public void deleteComment(Long commentId, Member member) {
        NoticeComment comment = noticeCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글이 없습니다."));
        if (member.isAdmin() || (comment.getAuthor() != null && comment.getAuthor().getId().equals(member.getId()))) {
            noticeCommentRepository.delete(comment);
        }
    }
}
