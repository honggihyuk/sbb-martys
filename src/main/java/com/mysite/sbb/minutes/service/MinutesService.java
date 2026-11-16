package com.mysite.sbb.minutes.service;

import com.mysite.sbb.member.entity.Member;
import com.mysite.sbb.minutes.dto.MinutesDto;
import com.mysite.sbb.minutes.entity.Minutes;
import com.mysite.sbb.minutes.repository.MinutesRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MinutesService {

    private final MinutesRepository minutesRepository;

    public List<Minutes> getList() {
        return minutesRepository.findAllByOrderByMeetingDateDescIdDesc();
    }

    public Minutes getMinutes(Long id) {
        return minutesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("회의록이 없습니다."));
    }

    public void create(MinutesDto dto, Member author) {
        minutesRepository.save(Minutes.builder()
                .title(dto.getTitle())
                .meetingDate(dto.getMeetingDate())
                .content(dto.getContent())
                .author(author)
                .created(LocalDateTime.now())
                .build());
    }

    public void delete(Long id) {
        minutesRepository.deleteById(id);
    }
}
