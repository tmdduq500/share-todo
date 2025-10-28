package com.osy.sharetodo.feature.invitation.template;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvitationEmailTemplate {

    public String subject(String eventTitle) {
        String safeTitle = eventTitle == null ? "일정" : eventTitle;
        return "[ShareTodo] 초대: " + safeTitle;
    }

    public String body(String baseUrl, String token, String eventTitle, String eventDesc) {
        String safeTitle = StringEscapeUtils.escapeHtml4(eventTitle == null ? "일정" : eventTitle);
        String safeDesc  = StringEscapeUtils.escapeHtml4(eventDesc == null ? "" : eventDesc);

        String acceptUrl = baseUrl + "/invite?token=" + token;
        String icsUrl    = baseUrl + "/ics/" + token + ".ics";

        return """
               <div style="font-family:system-ui,Segoe UI,Apple SD Gothic Neo,Malgun Gothic,sans-serif;font-size:14px;color:#111">
                 <h2 style="margin:0 0 12px 0">ShareTodo 초대</h2>
                 <p><strong>제목:</strong> %s</p>
                 %s
                 <p style="margin-top:16px">
                   <a href="%s" style="background:#586AEA;color:#fff;padding:10px 16px;border-radius:6px;text-decoration:none;">초대 수락하기</a>
                 </p>
                 <p style="margin-top:8px">
                   또는 <a href="%s">캘린더에 추가(.ics)</a>
                 </p>
                 <hr style="margin-top:20px;border:none;border-top:1px solid #eee"/>
                 <p style="color:#666">본 메일은 발신 전용입니다.</p>
               </div>
               """.formatted(
                safeTitle,
                safeDesc.isBlank() ? "" : "<p><strong>설명:</strong> " + safeDesc + "</p>",
                acceptUrl,
                icsUrl
        );
    }
}