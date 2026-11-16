package com.mysite.sbb.tripmap.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 하루 일정 만들기 / 고치기 폼
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripDayDto {

    @NotNull(message = "며칠차인지 입력해주세요.")
    @Min(value = 1, message = "1일차부터 입력할 수 있어요.")
    @Max(value = 30, message = "30일차까지 입력할 수 있어요.")
    private Integer dayNo;

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 50, message = "제목은 50자까지 쓸 수 있어요.")
    private String title;

    private String memo;
}
