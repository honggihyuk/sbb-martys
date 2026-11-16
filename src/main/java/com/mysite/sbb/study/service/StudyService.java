package com.mysite.sbb.study.service;

import com.mysite.sbb.member.entity.Member;
import com.mysite.sbb.study.dto.BiblePlaces;
import com.mysite.sbb.study.dto.JourneyMapData;
import com.mysite.sbb.study.dto.JourneyWeek;
import com.mysite.sbb.study.dto.MapPin;
import com.mysite.sbb.study.dto.StudyPlan;
import com.mysite.sbb.study.dto.StudyPosition;
import com.mysite.sbb.study.dto.StudyWeek;
import com.mysite.sbb.study.entity.StudyCheck;
import com.mysite.sbb.study.repository.StudyCheckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyCheckRepository studyCheckRepository;

    // 체크한 본문을 "주차-슬롯" 문자열 집합으로 (예: "6-0" → 6주차 사도행전)
    // 화면(Thymeleaf)에서 ${checked.contains('6-0')} 처럼 쉽게 확인하려고 문자열을 쓴다.
    public Set<String> getCheckedKeys(Member member) {
        Set<String> keys = new HashSet<>();
        for (StudyCheck c : studyCheckRepository.findByMember(member)) {
            keys.add(c.getWeekNo() + "-" + c.getSlot());
        }
        return keys;
    }

    // 체크되어 있으면 해제, 아니면 체크
    @Transactional
    public void toggle(Member member, int weekNo, int slot) {
        studyCheckRepository.findByMemberAndWeekNoAndSlot(member, weekNo, slot)
                .ifPresentOrElse(studyCheckRepository::delete,
                        () -> studyCheckRepository.save(StudyCheck.builder()
                                .member(member)
                                .weekNo(weekNo)
                                .slot(slot)
                                .created(LocalDateTime.now())
                                .build()));
    }

    @Transactional
    public void reset(Member member) {
        studyCheckRepository.deleteByMember(member);
    }

    // 읽은 개수 → 바울의 경로 위 위치
    // 한 주에 두 본문 : 하나만 읽었으면 경로 두 번째 장소쯤, 둘 다 읽었으면 그 주의 마지막 장소
    public StudyPosition positionOf(int count) {
        if (count <= 0) {
            return new StudyPosition(0, 0, StudyPlan.WEEKS.get(0).getStops().get(0));
        }
        int weekNo = Math.min((count + 1) / 2, StudyPlan.WEEKS.size());
        List<String> stops = StudyPlan.week(weekNo).getStops();
        int idx = (count % 2 == 1) ? Math.min(1, stops.size() - 1) : stops.size() - 1;
        return new StudyPosition(weekNo, idx, stops.get(idx));
    }

    // '나는 지금 ○○' 표시용
    public String currentPlace(Set<String> checked) {
        return positionOf(checked.size()).place();
    }

    // 지금 몇 주차 여정 위에 있는지 (읽은 개수 기준, 0 이면 출발 전)
    public int currentWeekOf(Set<String> checked) {
        return positionOf(checked.size()).weekNo();
    }

    // 회원별 읽은 개수 (회원 id → 개수)
    private Map<Long, Integer> countByMember() {
        Map<Long, Integer> counts = new HashMap<>();
        for (StudyCheck c : studyCheckRepository.findAll()) {
            counts.merge(c.getMember().getId(), 1, Integer::sum);
        }
        return counts;
    }

    // 메인 화면 '묵상 여정' : 회원들이 지금 몇 주차 여정에 있는지 (주차 → 회원 목록)
    public Map<Integer, List<Member>> getPositionsOfAll(List<Member> members) {
        Map<Long, Integer> counts = countByMember();
        Map<Integer, List<Member>> positions = new LinkedHashMap<>();
        for (Member m : members) {
            int count = counts.getOrDefault(m.getId(), 0);
            if (count == 0) {
                continue;   // 아직 출발 전
            }
            positions.computeIfAbsent(positionOf(count).weekNo(), k -> new ArrayList<>()).add(m);
        }
        return positions;
    }

    // 묵상 여정 지도 데이터 (경로 + 장소 좌표 + 회원 위치)
    public JourneyMapData getJourneyMap(List<Member> members, Member me) {
        Map<Long, Integer> counts = countByMember();
        List<MapPin> pins = new ArrayList<>();
        for (Member m : members) {
            int count = counts.getOrDefault(m.getId(), 0);
            boolean isMe = m.getId().equals(me.getId());
            if (count == 0 && !isMe) {
                continue;   // 출발 전인 사람은 지도에 올리지 않는다 (나는 항상 표시)
            }
            StudyPosition pos = positionOf(count);
            pins.add(new MapPin(m.getDisplayName(), m.getInitials(), m.getPhotoPath(), isMe,
                    pos.weekNo(), pos.stopIndex(), pos.place()));
        }
        List<JourneyWeek> weeks = StudyPlan.WEEKS.stream().map(JourneyWeek::of).toList();
        return new JourneyMapData(List.of(StudyPlan.JOURNEY_NAMES), weeks, BiblePlaces.all(), pins,
                counts.getOrDefault(me.getId(), 0), StudyPlan.TOTAL_READINGS);
    }

    // 몇 주차까지 (두 본문 모두) 읽었는지
    public boolean isWeekDone(Set<String> checked, StudyWeek week) {
        return checked.contains(week.weekNo() + "-0") && checked.contains(week.weekNo() + "-1");
    }
}
