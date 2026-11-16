package com.mysite.sbb.moment.service;

import com.mysite.sbb.member.entity.Member;
import com.mysite.sbb.moment.dto.MomentDto;
import com.mysite.sbb.moment.entity.Moment;
import com.mysite.sbb.moment.repository.MomentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class MomentService {

    private final MomentRepository momentRepository;

    // 월별로 묶기 (예: "2026년 11월" → [기록들])
    public Map<String, List<Moment>> getGroupedByMonth() {
        Map<String, List<Moment>> map = new LinkedHashMap<>();
        for (Moment m : momentRepository.findAllByOrderByMeetDateDescIdDesc()) {
            YearMonth ym = YearMonth.from(m.getMeetDate());
            String key = ym.getYear() + "년 " + ym.getMonthValue() + "월";
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(m);
        }
        return map;
    }

    public Moment getMoment(Long id) {
        return momentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("모임 기록이 없습니다."));
    }

    // photoPaths : 올린 사진들 (첫 장이 대표 사진)
    public void create(MomentDto dto, List<String> photoPaths, Member author) {
        momentRepository.save(Moment.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .meetDate(dto.getMeetDate())
                .photoPath(photoPaths.isEmpty() ? null : photoPaths.get(0))
                .photos(new ArrayList<>(photoPaths))
                .author(author)
                .created(LocalDateTime.now())
                .build());
    }

    // 삭제 : 작성자 본인 또는 관리자
    public void delete(Long id, Member member) {
        Moment moment = getMoment(id);
        if (member.isAdmin() || (moment.getAuthor() != null && moment.getAuthor().getId().equals(member.getId()))) {
            momentRepository.delete(moment);
        }
    }
}
