package com.habittracker.api.core.utils;

import java.time.ZoneId;
import java.util.Set;

public final class TimezoneUtils {

    private TimezoneUtils() {

    }

    private static final Set<String> VALID_ZONE_IDS = ZoneId.getAvailableZoneIds();


    public static boolean isValidTimezone(String timezone) {
        if (timezone == null) return false;
        return VALID_ZONE_IDS.contains(timezone.trim());
    }
}
