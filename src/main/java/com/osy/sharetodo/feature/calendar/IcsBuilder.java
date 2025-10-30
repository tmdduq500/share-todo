package com.osy.sharetodo.feature.calendar;

import com.osy.sharetodo.feature.event.domain.Event;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class IcsBuilder {
    private static final DateTimeFormatter ICS = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace(",", "\\,").replace(";", "\\;").replace("\n", "\\n");
    }

    private static String dt(LocalDateTime utc) {
        return utc.atOffset(ZoneOffset.UTC).format(ICS);
    }

    public static String singleEvent(Event e) {
        String uid = e.getUid() + "@share-todo";
        StringBuilder sb = new StringBuilder(512);
        sb.append("BEGIN:VCALENDAR\r\n")
                .append("VERSION:2.0\r\n")
                .append("PRODID:-//share-todo//EN\r\n")
                .append("CALSCALE:GREGORIAN\r\n")
                .append("METHOD:PUBLISH\r\n")
                .append("BEGIN:VEVENT\r\n")
                .append("UID:").append(uid).append("\r\n")
                .append("DTSTAMP:").append(dt(LocalDateTime.now(ZoneOffset.UTC))).append("\r\n")
                .append("DTSTART:").append(dt(e.getStartsAtUtc())).append("\r\n")
                .append("DTEND:").append(dt(e.getEndsAtUtc())).append("\r\n")
                .append("SUMMARY:").append(esc(e.getTitle())).append("\r\n");
        if (e.getDescription() != null) sb.append("DESCRIPTION:").append(esc(e.getDescription())).append("\r\n");
        if (e.getLocation() != null) sb.append("LOCATION:").append(esc(e.getLocation())).append("\r\n");
        sb.append("END:VEVENT\r\n")
                .append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    public static String singleError(String message) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("BEGIN:VCALENDAR\r\n")
                .append("VERSION:2.0\r\n")
                .append("PRODID:-//share-todo//EN\r\n")
                .append("CALSCALE:GREGORIAN\r\n")
                .append("BEGIN:VEVENT\r\n")
                .append("SUMMARY:초대 오류\r\n")
                .append("DESCRIPTION:").append(esc(message)).append("\r\n")
                .append("DTSTART:20240101T000000Z\r\n")
                .append("DTEND:20240101T000100Z\r\n")
                .append("END:VEVENT\r\n")
                .append("END:VCALENDAR\r\n");
        return sb.toString();
    }
}