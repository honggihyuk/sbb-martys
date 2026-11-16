package com.mysite.sbb.study.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// 바울의 여정에 나오는 지명과 좌표 (대략적인 위치)
// 묵상 여정 지도(Natural Earth)와 '지도' 메뉴(Google Maps)에서 같이 쓴다.
public final class BiblePlaces {

    private BiblePlaces() {}

    private static final Map<String, BiblePlace> PLACES = new LinkedHashMap<>();

    static {
        // 1차 전도여행
        put("수리아 안디옥", "Antakya, 튀르키예", 36.20, 36.16, true);
        put("실루기아", "Samandağ, 튀르키예", 36.12, 35.93, false);
        put("구브로(살라미·바보)", "Cyprus 키프로스", 35.18, 33.90, true);
        put("바보", "Paphos, 키프로스", 34.76, 32.41, false);
        put("버가", "Perge, 튀르키예", 36.96, 30.85, false);
        put("앗달리아", "Antalya, 튀르키예", 36.88, 30.70, false);
        put("비시디아 안디옥", "Yalvaç, 튀르키예", 38.31, 31.19, true);
        put("이고니온", "Konya, 튀르키예", 37.87, 32.49, true);
        put("루스드라", "Lystra (Hatunsaray), 튀르키예", 37.58, 32.33, false);
        put("더베", "Derbe (Ekinözü), 튀르키예", 37.35, 33.36, false);
        put("다소", "Tarsus, 튀르키예", 36.92, 34.90, true);
        // 2차 전도여행
        put("갈라디아·브루기아", "Galatia · Phrygia 지방", 39.00, 32.00, false);
        put("무시아", "Mysia 지방", 39.60, 27.50, false);
        put("드로아", "Troas, 튀르키예", 39.75, 26.16, true);
        put("사모드라게", "Samothrace, 그리스", 40.47, 25.53, false);
        put("네압볼리", "Kavala, 그리스", 40.94, 24.41, false);
        put("빌립보", "Philippi, 그리스", 41.01, 24.29, true);
        put("데살로니가", "Thessaloniki, 그리스", 40.64, 22.94, true);
        put("베뢰아", "Veria, 그리스", 40.52, 22.20, false);
        put("아덴", "Athens, 그리스", 37.98, 23.73, true);
        put("고린도", "Corinth, 그리스", 37.91, 22.88, true);
        put("겐그레아", "Kechries, 그리스", 37.88, 22.99, false);
        put("에베소", "Ephesus (Selçuk), 튀르키예", 37.94, 27.34, true);
        put("가이사랴", "Caesarea, 이스라엘", 32.50, 34.89, true);
        put("예루살렘", "Jerusalem", 31.78, 35.23, true);
        // 3차 전도여행
        put("마게도냐", "Macedonia 지방", 40.80, 22.50, false);
        put("앗소", "Assos (Behramkale), 튀르키예", 39.49, 26.34, false);
        put("미둘레네", "Mytilene, 그리스", 39.11, 26.55, false);
        put("기오", "Chios, 그리스", 38.37, 26.14, false);
        put("사모", "Samos, 그리스", 37.75, 26.97, false);
        put("밀레도", "Miletus, 튀르키예", 37.53, 27.28, false);
        put("고스", "Kos, 그리스", 36.89, 27.29, false);
        put("로도", "Rhodes, 그리스", 36.44, 28.22, false);
        put("바다라", "Patara, 튀르키예", 36.26, 29.32, false);
        put("두로", "Tyre, 레바논", 33.27, 35.20, false);
        put("돌레마이", "Akko, 이스라엘", 32.92, 35.07, false);
        // 로마 호송
        put("시돈", "Sidon, 레바논", 33.56, 35.37, false);
        put("무라", "Myra (Demre), 튀르키예", 36.26, 29.98, false);
        put("미항(그레데)", "Kali Limenes, 크레타", 34.93, 24.78, false);
        put("유라굴로 광풍", "지중해 한가운데 (표류)", 35.50, 18.50, false);
        put("멜리데", "Malta 몰타", 35.95, 14.40, true);
        put("수라구사", "Syracuse, 이탈리아", 37.08, 15.29, false);
        put("레기온", "Reggio Calabria, 이탈리아", 38.11, 15.65, false);
        put("보디올", "Pozzuoli, 이탈리아", 40.82, 14.12, false);
        put("압비오 광장", "Forum Appii, 이탈리아", 41.41, 13.08, false);
        put("세 여관(트레이스 타베르네)", "Tres Tabernae, 이탈리아", 41.62, 12.88, false);
        put("로마", "Rome, 이탈리아", 41.89, 12.48, true);

        // 계획표에서 다르게 부르는 이름
        alias("안디옥", "수리아 안디옥");
        alias("헬라·고린도", "고린도");
    }

    private static void put(String name, String modern, double lat, double lng, boolean major) {
        PLACES.put(name, new BiblePlace(name, modern, lat, lng, major));
    }

    private static void alias(String alias, String name) {
        BiblePlace p = PLACES.get(name);
        PLACES.put(alias, new BiblePlace(alias, p.modern(), p.lat(), p.lng(), false));
    }

    // 이름으로 찾기 (없으면 null : 예) '다시 돌아감')
    public static BiblePlace find(String name) {
        return PLACES.get(name);
    }

    // 전체 목록 (같은 장소를 가리키는 다른 이름은 빼고)
    public static List<BiblePlace> list() {
        List<BiblePlace> list = new ArrayList<>();
        for (Map.Entry<String, BiblePlace> e : PLACES.entrySet()) {
            if (!e.getKey().equals("안디옥") && !e.getKey().equals("헬라·고린도")) {
                list.add(e.getValue());
            }
        }
        return list;
    }

    // 이름 → 장소 전체 (지도 스크립트에 넘겨줄 때)
    public static Map<String, BiblePlace> all() {
        return PLACES;
    }
}
