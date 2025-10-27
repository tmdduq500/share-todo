package com.osy.sharetodo.feature.calendar.api;

import com.osy.sharetodo.feature.calendar.IcsBuilder;
import com.osy.sharetodo.feature.invitation.service.InvitationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
public class IcsController {

    private final InvitationService invitationService;

    /**
     * GET /ics/{token}.ics  (초대 토큰으로 단일 이벤트 ICS 다운로드)
     */
    @GetMapping(value = "/ics/{token}.ics", produces = "text/calendar")
    public void download(@PathVariable String token, HttpServletResponse res) throws IOException {
        var event = invitationService.getEventByToken(token);

        String ics = IcsBuilder.singleEvent(event);

        String filename = StringUtils.defaultIfBlank(event.getTitle(), "event")
                .replaceAll("[^a-zA-Z0-9._-]", "_") + ".ics";

        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.setContentType("text/calendar; charset=utf-8");
        res.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        res.getWriter().write(ics);
    }
}
