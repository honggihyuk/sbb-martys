package com.mysite.sbb.member.dto;

import com.mysite.sbb.common.TripInfo;
import com.mysite.sbb.member.constant.Community;
import com.mysite.sbb.member.constant.Gender;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {
    //DTO for validation check!

    @NotEmpty(message = "사용자 ID는 필수 항목입니다.")
    @Size(min = 3, max = 30, message = "사용자ID는 3~30자로 입력하세요.")
    private String username;

    @NotEmpty(message = "이름은 필수 항목입니다.")
    @Size(max = 20, message = "이름은 20자 이내로 입력하세요.")
    private String name;

    // 소속이 SL·ED 면 비워도 된다 (SLED조로 자동 배정). 나머지는 컨트롤러에서 필수 검사
    @Min(value = 1, message = "조를 선택하세요.")
    @Max(value = TripInfo.TEAM_COUNT, message = "조를 선택하세요.")
    private Integer teamNo;

    @NotNull(message = "소속을 선택하세요.")
    private Community community;

    @NotEmpty(message = "비밀번호는 필수 항목입니다.")
    @Size(min = 4, message = "비밀번호는 4자 이상이어야 합니다.")
    private String password1;

    @NotEmpty(message = "비밀번호 확인은 필수 항목입니다.")
    private String password2;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;       // 선택 항목 (비워 두면 저장하지 않는다)

    @NotNull(message = "성별을 선택하세요.")
    private Gender gender;

    // 등록 확인(체크박스): 미체크 시 null 이므로 Boolean 권장
    @AssertTrue(message = "참가 등록 확인에 동의해야 가입할 수 있습니다.")
    private Boolean registration;
}
