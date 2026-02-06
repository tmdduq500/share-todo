package com.osy.sharetodo.feature.calendar.api;

import com.osy.sharetodo.feature.calendar.IcsBuilder;
import com.osy.sharetodo.feature.event.domain.Event;
import com.osy.sharetodo.feature.invitation.service.InvitationService;
import com.osy.sharetodo.global.exception.ApiException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ics")
public class IcsController {

    private final InvitationService invitationService;

    private static final MediaType TEXT_CALENDAR_UTF8 = new MediaType("text", "calendar", StandardCharsets.UTF_8);

    /**
     * GET /ics/{token}.ics  (초대 토큰으로 단일 이벤트 ICS 다운로드)
     */
    @GetMapping(produces = "text/calendar; charset=utf-8")
    public void download(@RequestParam String token, HttpServletResponse res) throws IOException {
        var event = invitationService.getEventByToken(token);
        String ics = IcsBuilder.singleEvent(event);

        String filename = StringUtils.defaultIfBlank(event.getTitle(), "event")
                .replaceAll("[^a-zA-Z0-9._-]", "_") + ".ics";

        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.setContentType("text/calendar; charset=utf-8");
        res.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        res.getWriter().write(ics);
    }

    @GetMapping(value = "/invitations", produces = "text/calendar")
    public ResponseEntity<String> getIcs(@RequestParam String token) {
        try {
            Event event = invitationService.getEventByToken(token);
            String ics = IcsBuilder.singleEvent(event);

            return ResponseEntity.ok()
                    .contentType(TEXT_CALENDAR_UTF8)
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename("invite.ics").build().toString())
                    .body(ics);

        } catch (ApiException e) {
            String ics = IcsBuilder.singleError("초대가 유효하지 않거나 만료되었습니다.");

            return ResponseEntity.status(404)
                    .contentType(TEXT_CALENDAR_UTF8)
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename("invite-error.ics").build().toString())
                    .body(ics);
        }
    }

    @GetMapping(value = "/me", produces = "text/calendar")
    public ResponseEntity<String> getIcsForMe(@RequestParam String eventUid) {
        String accountUid = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            Event event = invitationService.getIcsForMe(eventUid, accountUid);
            String ics = IcsBuilder.singleEvent(event);

            return ResponseEntity.ok()
                    .contentType(TEXT_CALENDAR_UTF8)
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename("my-event.ics").build().toString())
                    .body(ics);

        } catch (ApiException e) {
            String ics = IcsBuilder.singleError("등록한 일정이 유효하지 않습니다.");

            return ResponseEntity.status(404)
                    .contentType(TEXT_CALENDAR_UTF8)
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename("my-event-error.ics").build().toString())
                    .body(ics);
        }
    }
}
