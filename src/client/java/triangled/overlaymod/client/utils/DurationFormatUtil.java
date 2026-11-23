package triangled.overlaymod.client.utils;

import java.util.Locale;

// Formats seconds using a pattern like m:ss, where d/h/m/s are time units and \ escapes the next character.
public final class DurationFormatUtil {
    private DurationFormatUtil() {
    }

    public static String format(long seconds, String pattern) {
        if (pattern == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '\\' && i + 1 < pattern.length()) {
                result.append(pattern.charAt(++i));
                continue;
            }
            long value = switch (c) {
                case 'd' -> seconds / 86400;
                case 'h' -> seconds / 3600 % 24;
                case 'm' -> seconds / 60 % 60;
                case 's' -> seconds % 60;
                default -> -1;
            };
            if (value < 0) {
                result.append(c);
                continue;
            }
            int run = 1;
            while (i + 1 < pattern.length() && pattern.charAt(i + 1) == c) {
                run++;
                i++;
            }
            result.append(String.format(Locale.ROOT, "%0" + run + "d", value));
        }
        return result.toString();
    }
}
