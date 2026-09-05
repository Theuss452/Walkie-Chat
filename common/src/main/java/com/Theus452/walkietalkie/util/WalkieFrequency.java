package com.Theus452.walkietalkie.util;

import java.util.Locale;
import java.util.regex.Pattern;

public final class WalkieFrequency {
    public static final int MAX_WIRE_LENGTH = 10;
    private static final Pattern PUBLIC = Pattern.compile("\\d{1,4}");
    private static final Pattern PRIVATE = Pattern.compile("E\\d{4}");

    private WalkieFrequency() {
    }

    public static String sanitize(String raw) {
        if (raw == null) {
            return "";
        }
        String value = raw.trim().toUpperCase(Locale.ROOT);
        if (value.isEmpty()) {
            return "";
        }
        if (value.length() > MAX_WIRE_LENGTH) {
            value = value.substring(0, MAX_WIRE_LENGTH);
        }
        if (PUBLIC.matcher(value).matches() || PRIVATE.matcher(value).matches()) {
            return value;
        }
        return "";
    }

    public static boolean isValid(String raw) {
        return !sanitize(raw).isEmpty();
    }

    public static boolean isValidOrEmpty(String raw) {
        return raw == null || raw.isBlank() || isValid(raw);
    }

    public static boolean isPrivate(String raw) {
        return PRIVATE.matcher(sanitize(raw)).matches();
    }

    public static String createPrivate(String publicDigits) {
        String digits = publicDigits == null ? "" : publicDigits.trim();
        return PUBLIC.matcher(digits).matches() && digits.length() == 4 ? "E" + digits : "";
    }

    public static String sanitizeChannelName(String raw) {
        if (raw == null) {
            return "";
        }
        String value = raw.trim().replace('\n', ' ').replace('\r', ' ');
        return value.length() > 20 ? value.substring(0, 20) : value;
    }

    public static String sanitizeChatMessage(String raw) {
        if (raw == null) {
            return "";
        }
        String value = raw.strip();
        if (value.length() > 256) {
            value = value.substring(0, 256);
        }
        return value;
    }
}
