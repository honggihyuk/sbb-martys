package com.mysite.sbb.tripmap.service;

import com.mysite.sbb.member.entity.Member;
import com.mysite.sbb.tripmap.dto.TripDayDto;
import com.mysite.sbb.tripmap.dto.TripStopDto;
import com.mysite.sbb.tripmap.entity.TripDay;
import com.mysite.sbb.tripmap.entity.TripStop;
import com.mysite.sbb.tripmap.repository.TripDayRepository;
import com.mysite.sbb.tripmap.repository.TripStopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

// 지도 메뉴 : 답사 일정(하루 → 장소들) 계획과 기록
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripMapService {

    private final TripDayRepository tripDayRepository;
    private final TripStopRepository tripStopRepository;

    public List<TripDay> getDays() {
        return tripDayRepository.findAllByOrderByDayNoAsc();
    }

    public TripDay getDay(Long id) {
        return tripDayRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "일정을 찾을 수 없어요."));
    }

    public TripStop getStop(Long id) {
        return tripStopRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "장소를 찾을 수 없어요."));
    }

    // 다음에 만들 날 번호 (마지막 날 + 1)
    public int nextDayNo() {
        List<TripDay> days = getDays();
        return days.isEmpty() ? 1 : days.get(days.size() - 1).getDayNo() + 1;
    }

    public boolean existsDayNo(Integer dayNo, Long exceptId) {
        return tripDayRepository.findByDayNo(dayNo)
                .filter(d -> !d.getId().equals(exceptId))
                .isPresent();
    }

    // 고치기·지우기 : 만든 사람, 조장, 관리자
    public boolean canEdit(Member member, Member author) {
        return member.isAdmin() || member.isLeader()
                || (author != null && author.getId().equals(member.getId()));
    }

    // ---------- 하루 일정 ----------

    @Transactional
    public TripDay createDay(TripDayDto dto, Member author) {
        return tripDayRepository.save(TripDay.builder()
                .dayNo(dto.getDayNo())
                .title(dto.getTitle().strip())
                .memo(dto.getMemo())
                .author(author)
                .created(LocalDateTime.now())
                .build());
    }

    @Transactional
    public void updateDay(Long id, TripDayDto dto) {
        TripDay day = getDay(id);
        day.setDayNo(dto.getDayNo());
        day.setTitle(dto.getTitle().strip());
        day.setMemo(dto.getMemo());
    }

    @Transactional
    public void deleteDay(Long id) {
        tripDayRepository.delete(getDay(id));
    }

    // ---------- 장소 ----------

    @Transactional
    public void addStop(TripStopDto dto, Member author) {
        TripDay day = getDay(dto.getDayId());
        TripStop stop = TripStop.builder()
                .day(day)
                .sortOrder(day.getStops().size())     // 맨 뒤에 붙인다
                .name(dto.getName().strip())
                .lat(dto.getLat())
                .lng(dto.getLng())
                .placeId(blankToNull(dto.getPlaceId()))
                .address(blankToNull(dto.getAddress()))
                .time(blankToNull(dto.getTime()))
                .memo(blankToNull(dto.getMemo()))
                .author(author)
                .created(LocalDateTime.now())
                .build();
        day.getStops().add(stop);
    }

    // 순서 바꾸기 : up = 앞으로, 아니면 뒤로
    @Transactional
    public void move(Long stopId, boolean up) {
        TripStop stop = getStop(stopId);
        List<TripStop> stops = stop.getDay().getStops();
        int i = stops.indexOf(stop);
        int j = up ? i - 1 : i + 1;
        if (j < 0 || j >= stops.size()) {
            return;
        }
        stops.set(i, stops.get(j));
        stops.set(j, stop);
        renumber(stops);
    }

    // '다녀왔어요' 체크 (누구나)
    @Transactional
    public void toggleVisited(Long stopId) {
        TripStop stop = getStop(stopId);
        boolean visited = !Boolean.TRUE.equals(stop.getVisited());
        stop.setVisited(visited);
        stop.setVisitedAt(visited ? LocalDateTime.now() : null);
    }

    @Transactional
    public void updateStop(Long stopId, String time, String memo) {
        TripStop stop = getStop(stopId);
        stop.setTime(blankToNull(time));
        stop.setMemo(blankToNull(memo));
    }

    @Transactional
    public void deleteStop(Long stopId) {
        TripStop stop = getStop(stopId);
        List<TripStop> stops = stop.getDay().getStops();
        stops.remove(stop);       // orphanRemoval 로 DB 에서도 지워진다
        renumber(stops);
    }

    private void renumber(List<TripStop> stops) {
        for (int k = 0; k < stops.size(); k++) {
            stops.get(k).setSortOrder(k);
        }
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.strip();
    }
}
