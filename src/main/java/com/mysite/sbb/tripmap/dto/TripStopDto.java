package com.mysite.sbb.tripmap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 장소 추가 폼 : 지도 검색 / 지도 클릭 / 성경 지명 중 하나로 좌표가 채워진다
@Getter
@Setter
@NoArgsConstructor
public class TripStopDto {

    @NotNull
    private Long dayId;

    @NotBlank(message = "장소 이름을 입력해주세요.")
    @Size(max = 100)
    private String name;

    @NotNull(message = "지도에서 위치를 골라주세요.")
    private Double lat;

    @NotNull(message = "지도에서 위치를 골라주세요.")
    private Double lng;

    private String placeId;
    private String address;
    private String time;
    private String memo;
}
