package com.mysite.sbb.common.text;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 모임 기록 글의 간단한 서식을 HTML 로 바꾼다 (글쓰기 화면의 서식 도구와 짝)
//   # 제목   ## 소제목   - 목록   1. 목록   **굵게**
// 먼저 HTML 특수문자를 모두 바꿔(escape) 두므로, 글에 <script> 를 써도 실행되지 않는다.
// 화면에서 쓰는 법 : 컨트롤러에서 toHtml() 결과를 model 에 넣고 <div th:utext="${contentHtml}"></div>
@Component("markdown")
public class SimpleMarkdown {

    private static final Pattern BOLD = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern OL = Pattern.compile("^\\d+\\. (.*)");

    public String toHtml(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        StringBuilder html = new StringBuilder();
        String list = null;                 // 지금 열려 있는 목록 (ul / ol)
        boolean paragraph = false;

        for (String raw : text.replace("\r", "").split("\n")) {
            String line = HtmlUtils.htmlEscape(raw.strip());
            String item = null, listType = null;
            if (line.startsWith("- ")) {
                item = line.substring(2);
                listType = "ul";
            } else {
                Matcher m = OL.matcher(line);
                if (m.matches()) {
                    item = m.group(1);
                    listType = "ol";
                }
            }

            // 목록이 끝났으면 닫기
            if (list != null && !list.equals(listType)) {
                html.append("</").append(list).append('>');
                list = null;
            }
            if (paragraph && (line.isEmpty() || line.startsWith("#") || item != null)) {
                html.append("</p>");
                paragraph = false;
            }

            if (item != null) {
                if (list == null) {
                    html.append('<').append(listType).append('>');
                    list = listType;
                }
                html.append("<li>").append(inline(item)).append("</li>");
            } else if (line.startsWith("## ")) {
                html.append("<h4>").append(inline(line.substring(3))).append("</h4>");
            } else if (line.startsWith("# ")) {
                html.append("<h3>").append(inline(line.substring(2))).append("</h3>");
            } else if (!line.isEmpty()) {
                html.append(paragraph ? "<br>" : "<p>").append(inline(line));
                paragraph = true;
            }
        }
        if (list != null) html.append("</").append(list).append('>');
        if (paragraph) html.append("</p>");
        return html.toString();
    }

    // 목록 미리보기용 : 서식 기호를 뺀 글
    public String toPlain(String text) {
        if (text == null) {
            return "";
        }
        return BOLD.matcher(text.replaceAll("(?m)^(#{1,2} |- |\\d+\\. )", "")).replaceAll("$1");
    }

    private String inline(String s) {
        return BOLD.matcher(s).replaceAll("<strong>$1</strong>");
    }
}
