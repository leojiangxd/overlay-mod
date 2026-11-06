package triangled.overlaymod.client.utils;

public final class DurationFormatUtil {
    private DurationFormatUtil() {
    }

    private enum Field {
        DAYS('d', 86400, 0),
        HOURS('h', 3600, 24),
        MINUTES('m', 60, 60),
        SECONDS('s', 1, 60);

        private final char symbol;
        private final long unitSeconds;
        private final long wrap;

        Field(char symbol, long unitSeconds, long wrap) {
            this.symbol = symbol;
            this.unitSeconds = unitSeconds;
            this.wrap = wrap;
        }

        long valueOf(long totalSeconds) {
            long value = totalSeconds / unitSeconds;
            return wrap == 0 ? value : value % wrap;
        }

        static Field of(char symbol) {
            for (Field field : values()) {
                if (field.symbol == symbol) {
                    return field;
                }
            }
            return null;
        }
    }

    public static String format(long totalSeconds, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        int length = pattern.length();
        while (i < length) {
            char c = pattern.charAt(i);

            if (c == '\\' && i + 1 < length) {
                result.append(pattern.charAt(i + 1));
                i += 2;
                continue;
            }

            Field field = Field.of(c);
            if (field == null) {
                result.append(c);
                i++;
                continue;
            }

            int runStart = i;
            while (i < length && pattern.charAt(i) == c) {
                i++;
            }
            result.append(padded(field.valueOf(totalSeconds), i - runStart));
        }

        return result.toString();
    }

    private static String padded(long value, int width) {
        String digits = String.valueOf(value);
        return digits.length() >= width ? digits : "0".repeat(width - digits.length()) + digits;
    }
}
