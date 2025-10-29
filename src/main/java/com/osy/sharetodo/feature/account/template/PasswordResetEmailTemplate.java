package com.osy.sharetodo.feature.account.template;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetEmailTemplate {

    public String subject() {
        return "[ShareTodo] 비밀번호 재설정 안내";
    }

    public String body(String baseUrl, String token, String email) {
        String safeEmail = StringEscapeUtils.escapeHtml4(email);
        String link = baseUrl + "/reset-password?token=" + token; // 프론트 페이지

        return """
                  <div style="font-family:system-ui,Segoe UI,Apple SD Gothic Neo,Malgun Gothic,sans-serif;font-size:14px;color:#111">
                    <h2 style="margin:0 0 12px 0">비밀번호 재설정</h2>
                    <p><strong>%s</strong> 계정의 비밀번호 재설정을 요청하셨습니다.</p>
                    <p style="margin-top:16px">
                      <a href="%s" style="background:#586AEA;color:#fff;padding:10px 16px;border-radius:6px;text-decoration:none;">비밀번호 재설정하기</a>
                    </p>
                    <p style="color:#666;margin-top:12px">이 요청을 본인이 하지 않았다면 이 메일을 무시하셔도 됩니다.</p>
                    <hr style="margin-top:20px;border:none;border-top:1px solid #eee"/>
                    <p style="color:#666">본 메일은 발신 전용입니다.</p>
                  </div>
                """.formatted(safeEmail, link);
    }
}