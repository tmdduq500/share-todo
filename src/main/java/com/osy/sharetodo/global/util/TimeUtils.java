package com.osy.sharetodo.global.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class TimeUtils {
    private TimeUtils() {
    }

    public static LocalDateTime toUtc(String localIso, String zoneId) {
        ZoneId zid = ZoneId.of(zoneId);
        LocalDateTime ldt = LocalDateTime.parse(localIso);
        Instant instant = ldt.atZone(zid).toInstant();
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    public static String toIsoZ(LocalDateTime utc) {
        return utc.atOffset(ZoneOffset.UTC).toString();
    }

    public static LocalDateTime toUtcFromIsoZ(String isoZ, String zoneId) {
        ZoneId zid = ZoneId.of(zoneId);
        Instant instant = Instant.parse(isoZ);
        return LocalDateTime.ofInstant(instant, zid).atZone(zid).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public static LocalDateTime toUtcFlexible(String input, String zoneId) {
        ZoneId zid = ZoneId.of(zoneId);

        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("time input is blank");
        }

        // Z 또는 +09:00 같은 offset 포함
        if (input.endsWith("Z") || input.matches(".*[+-]\\d{2}:\\d{2}$")) {
            Instant instant = Instant.parse(input);
            return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        }

        // offset 없는 local datetime
        LocalDateTime ldt = LocalDateTime.parse(input);
        Instant instant = ldt.atZone(zid).toInstant();
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

}
