package com.mysite.sbb.study.dto;

import com.mysite.sbb.common.TripInfo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// 16주 말씀 묵상 계획표 (사도바울의 전도여행 순서)
public final class StudyPlan {

    private StudyPlan() {}

    public static final String[] JOURNEY_NAMES = {"", "제1차 전도여행", "제2차 전도여행", "제3차 전도여행", "로마 호송 여정"};

    public static final List<StudyWeek> WEEKS = build();

    public static final int TOTAL_READINGS = WEEKS.size() * 2;   // 주마다 사도행전 + 서신서 = 32

    private static List<StudyWeek> build() {
        List<StudyWeek> list = new ArrayList<>();
        add(list, 1, "수리아 안디옥,실루기아,구브로(살라미·바보)", "9–12장", "갈라디아서", "갈", "1–3장");
        add(list, 1, "바보,버가,비시디아 안디옥,이고니온", "13장", "갈라디아서", "갈", "4–6장");
        add(list, 1, "이고니온,루스드라,더베,다시 돌아감", "14장", "데살로니가전서", "살전", "1–5장");
        add(list, 1, "더베,루스드라,이고니온,버가,앗달리아,안디옥", "15장", "데살로니가후서", "살후", "1–3장");
        add(list, 2, "안디옥,다소,더베,루스드라,이고니온,갈라디아·브루기아", "16장", "고린도전서", "고전", "1–5장");
        add(list, 2, "무시아,드로아,사모드라게,네압볼리,빌립보,데살로니가", "16–17장", "고린도전서", "고전", "6–10장");
        add(list, 2, "데살로니가,베뢰아,아덴,고린도,겐그레아", "17–18장", "고린도전서", "고전", "11–16장");
        add(list, 2, "에베소,가이사랴,예루살렘,수리아 안디옥", "18장", "고린도후서", "고후", "1–6장");
        add(list, 3, "안디옥,갈라디아·브루기아,에베소", "18–19장", "고린도후서", "고후", "7–13장");
        add(list, 3, "에베소,마게도냐,헬라·고린도", "19–20장", "로마서", "롬", "1–8장");
        add(list, 3, "고린도,마게도냐,빌립보,드로아,앗소,미둘레네,기오,사모,밀레도", "20장", "로마서", "롬", "9–16장");
        add(list, 3, "밀레도,고스,로도,바다라,두로,돌레마이,가이사랴,예루살렘", "20–21장", "에베소서", "엡", "1–6장");
        add(list, 4, "예루살렘,가이사랴,시돈,무라", "21–24장", "빌립보서", "빌", "1–4장");
        add(list, 4, "무라,미항(그레데),유라굴로 광풍,멜리데", "27장", "골로새서·빌레몬서", "골", "1–4장 + 몬 1장");
        add(list, 4, "멜리데,수라구사,레기온,보디올", "28장", "디모데전서·디도서", "딤전", "1–6장 + 딛 1–3장");
        add(list, 4, "보디올,압비오 광장,세 여관(트레이스 타베르네),로마", "28장", "디모데후서", "딤후", "1–4장");
        return List.copyOf(list);
    }

    private static void add(List<StudyWeek> list, int journey, String route, String acts,
                            String letter, String letterShort, String letterRange) {
        int weekNo = list.size() + 1;
        LocalDate start = TripInfo.STUDY_START.plusWeeks(weekNo - 1);
        list.add(new StudyWeek(weekNo, journey, List.of(route.split(",")), start, start.plusDays(6),
                acts, letter, letterShort, letterRange));
    }

    // 오늘이 몇 주차인지 (시작 전이면 1, 끝났으면 16)
    public static int currentWeekNo() {
        LocalDate today = LocalDate.now();
        for (StudyWeek w : WEEKS) {
            if (!today.isAfter(w.end())) {
                return w.weekNo();
            }
        }
        return WEEKS.size();
    }

    public static StudyWeek week(int weekNo) {
        int idx = Math.max(1, Math.min(weekNo, WEEKS.size())) - 1;
        return WEEKS.get(idx);
    }
}
