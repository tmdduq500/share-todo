package com.osy.sharetodo.feature.calendar;

import com.osy.sharetodo.feature.event.domain.Event;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@NoArgsConstructor
public final class IcsBuilder {

    private static final DateTimeFormatter ICS_UTC = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private static String esc(String s) {
        if (s == null) return "";
        String normalized = s.replace("\r\n", "\n").replace("\r", "\n");
        return normalized
                .replace("\\", "\\\\")
                .replace(",", "\\,")
                .replace(";", "\\;")
                .replace("\n", "\\n");
    }

    private static String dtUtc(LocalDateTime utc) {
        return utc.atOffset(ZoneOffset.UTC).format(ICS_UTC);
    }

    private static String foldLine(String line) {
        if (line == null || line.isEmpty()) return "\r\n";

        byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= 75) return line + "\r\n";

        StringBuilder out = new StringBuilder(line.length() + 32);
        int i = 0;
        boolean first = true;

        while (i < line.length()) {
            int limit = first ? 75 : 74;
            int start = i;
            int used = 0;

            while (i < line.length()) {
                int cp = line.codePointAt(i);
                String ch = new String(Character.toChars(cp));
                int blen = ch.getBytes(StandardCharsets.UTF_8).length;

                if (used + blen > limit) break;

                used += blen;
                i += Character.charCount(cp);
            }

            String chunk = line.substring(start, i);

            if (first) {
                out.append(chunk).append("\r\n");
                first = false;
            } else {
                out.append(" ").append(chunk).append("\r\n");
            }
        }

        return out.toString();
    }

    private static String prop(String name, String value) {
        return foldLine(name + ":" + value);
    }

    public static String singleEvent(Event e) {
        String uid = e.getUid() + "@share-todo.ohsy.store";

        StringBuilder sb = new StringBuilder(768);
        sb.append("BEGIN:VCALENDAR\r\n")
                .append("VERSION:2.0\r\n")
                .append("PRODID:-//share-todo//EN\r\n")
                .append("CALSCALE:GREGORIAN\r\n")
                .append("METHOD:PUBLISH\r\n")
                .append("BEGIN:VEVENT\r\n")
                .append(prop("UID", uid))
                .append(prop("DTSTAMP", dtUtc(LocalDateTime.now(ZoneOffset.UTC))))
                .append(prop("DTSTART", dtUtc(e.getStartsAtUtc())))
                .append(prop("DTEND", dtUtc(e.getEndsAtUtc())))
                .append(prop("SUMMARY", esc(e.getTitle())));

        if (e.getDescription() != null && !e.getDescription().isBlank()) {
            sb.append(prop("DESCRIPTION", esc(e.getDescription())));
        }
        if (e.getLocation() != null && !e.getLocation().isBlank()) {
            sb.append(prop("LOCATION", esc(e.getLocation())));
        }

        sb.append("END:VEVENT\r\n")
                .append("END:VCALENDAR\r\n");

        return sb.toString();
    }

    public static String singleError(String message) {
        String uid = "error-" + UUID.randomUUID() + "@share-todo";
        String now = dtUtc(LocalDateTime.now(ZoneOffset.UTC));
        String desc = (message == null || message.isBlank())
                ? "요청한 캘린더 데이터를 생성할 수 없습니다."
                : message;

        StringBuilder sb = new StringBuilder(512);
        sb.append("BEGIN:VCALENDAR\r\n")
                .append("VERSION:2.0\r\n")
                .append("PRODID:-//share-todo//EN\r\n")
                .append("CALSCALE:GREGORIAN\r\n")
                .append("METHOD:PUBLISH\r\n")
                .append("BEGIN:VEVENT\r\n")
                .append(prop("UID", uid))
                .append(prop("DTSTAMP", now))
                .append("STATUS:CANCELLED\r\n")
                .append("TRANSP:TRANSPARENT\r\n")
                .append(prop("SUMMARY", "초대 오류"))
                .append(prop("DESCRIPTION", esc(desc)))
                .append(prop("DTSTART", now))
                .append(prop("DTEND", now))
                .append("END:VEVENT\r\n")
                .append("END:VCALENDAR\r\n");

        return sb.toString();
    }
}
