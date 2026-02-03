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

}
