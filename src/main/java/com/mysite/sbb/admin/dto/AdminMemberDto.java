package com.mysite.sbb.admin.dto;

import com.mysite.sbb.common.TripInfo;
import com.mysite.sbb.member.constant.Community;
import com.mysite.sbb.member.constant.Gender;
import com.mysite.sbb.member.constant.MemberRole;
import com.mysite.sbb.member.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

// 관리자 화면 : 회원 등록 / 수정 폼
// 비밀번호는 등록할 때만 필수, 수정할 때는 비워 두면 그대로 둔다.
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "password")
public class AdminMemberDto {

    @NotBlank(message = "사용자ID는 필수항목입니다.")
    @Size(min = 3, max = 30, message = "사용자ID는 3~30자로 입력해주세요.")
    private String username;

    @NotBlank(message = "이름은 필수항목입니다.")
    @Size(max = 20)
    private String name;

    private String password;

    @Email(message = "이메일 형식이 아닙니다.")
    private String email;                   // 선택 항목

    @Min(TripInfo.SLED_TEAM_NO)
    @Max(TripInfo.TEAM_COUNT)
    private Integer teamNo;                 // 비워 두면 '조 없음', 0 은 SLED조 (소속이 SL·ED 면 저장할 때 자동으로 SLED조)

    @NotNull(message = "소속을 선택해주세요.")
    private Community community;

    @NotNull(message = "성별을 선택해주세요.")
    private Gender gender;

    @NotNull(message = "권한을 선택해주세요.")
    private MemberRole role = MemberRole.USER;

    private boolean registration = true;

    // 수정 화면 : 회원 정보로 폼 채우기
    public static AdminMemberDto of(Member m) {
        AdminMemberDto dto = new AdminMemberDto();
        dto.setUsername(m.getUsername());
        dto.setName(m.getName());
        dto.setEmail(m.getEmail());
        dto.setTeamNo(m.getTeamNo());
        dto.setCommunity(m.getCommunity());
        dto.setGender(m.getGender());
        dto.setRole(m.getRole() == null ? MemberRole.USER : m.getRole());
        dto.setRegistration(Boolean.TRUE.equals(m.getRegistration()));
        return dto;
    }
}
