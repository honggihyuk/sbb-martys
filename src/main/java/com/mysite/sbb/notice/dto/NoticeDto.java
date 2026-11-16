package com.mysite.sbb.notice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

// 공지 쓰기 / 고치기 폼
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDto {

    @NotEmpty(message = "제목은 필수 항목입니다.")
    @Size(max = 100, message = "제목은 100자 이내로 입력하세요.")
    private String subject;

    @NotEmpty(message = "내용은 필수 항목입니다.")
    private String content;

    private boolean pinned;                 // 중요 공지로 맨 위에 고정

    private boolean poll;                   // 투표 넣기

    private boolean multiple;               // 여러 개 고를 수 있게

    // 선택지와 설명 : 화면에서 칸이 짝으로 붙어 있어서 같은 순서로 들어온다 (빈 선택지는 무시)
    @Builder.Default
    private List<String> pollOptions = new ArrayList<>(List.of("", ""));

    @Builder.Default
    private List<String> pollDescriptions = new ArrayList<>(List.of("", ""));

    // 선택지 하나 (이름 + 설명)
    public record Option(String text, String description) {
    }

    // i 번째 설명 (화면에서 칸 채우기용)
    public String descriptionAt(int i) {
        return (pollDescriptions != null && i < pollDescriptions.size()) ? pollDescriptions.get(i) : "";
    }

    // 빈 칸을 뺀 선택지
    public List<Option> cleanOptions() {
        List<Option> list = new ArrayList<>();
        if (pollOptions != null) {
            for (int i = 0; i < pollOptions.size(); i++) {
                String text = pollOptions.get(i);
                if (text != null && !text.isBlank()) {
                    String desc = descriptionAt(i);
                    list.add(new Option(text.strip(), (desc == null || desc.isBlank()) ? null : desc.strip()));
                }
            }
        }
        return list;
    }
}
