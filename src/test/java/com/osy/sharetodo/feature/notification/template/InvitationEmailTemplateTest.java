package com.osy.sharetodo.feature.notification.template;

import com.osy.sharetodo.feature.invitation.template.InvitationEmailTemplate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class InvitationEmailTemplateTest {

    @Test
    void subject_and_body() {
        InvitationEmailTemplate t = new InvitationEmailTemplate();
        String subject = t.subject("회의");
        String body = t.body("http://localhost:8080", "TOKEN123", "회의", "설명");

        assertThat(subject).contains("회의");
        assertThat(body).contains("초대 수락하기");
        assertThat(body).contains("/invite?token=TOKEN123");
        assertThat(body).contains("/ics/TOKEN123.ics");
        assertThat(body).contains("회의");
        assertThat(body).contains("설명");
    }
}
