package com.mysite.sbb.study.dto;

// 성경 지명 하나 (지도 표시용 좌표)
// major : 전체 지도에서도 이름을 보여줄 주요 도시인지
public record BiblePlace(String name,      // 성경 지명   (예: 드로아)
                         String modern,    // 현재 지명   (예: Troas, 튀르키예)
                         double lat,       // 위도
                         double lng,       // 경도
                         boolean major) {
}
