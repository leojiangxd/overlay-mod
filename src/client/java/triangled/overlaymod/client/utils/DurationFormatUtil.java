package triangled.overlaymod.client.utils;

// Formats seconds using a pattern like m:ss, where d/h/m/s are time units.
public final class DurationFormatUtil {
    private DurationFormatUtil() {
    }

    // The time units a pattern can use, and the letter for each.
    private enum Field {
        DAYS('d', 86400, 0),
        HOURS('h', 3600, 24),
        MINUTES('m', 60, 60),
        SECONDS('s', 1, 60);

        private final char patternChar;
        private final long secondsPerUnit;
        private final long rolloverAt;

        Field(char patternChar, long secondsPerUnit, long rolloverAt) {
            this.patternChar = patternChar;
            this.secondsPerUnit = secondsPerUnit;
            this.rolloverAt = rolloverAt;
        }

        // Returns how many of this unit are in the time, after larger units.
        long valueOf(long totalSeconds) {
            long units = totalSeconds / secondsPerUnit;
            return rolloverAt == 0 ? units : units % rolloverAt;
        }

        // Returns the unit for a pattern letter, or null if it isn't one.
        static Field of(char patternChar) {
            for (Field field : values()) {
                if (field.patternChar == patternChar) {
                    return field;
                }
            }
            return null;
        }
    }

    // Builds the text by swapping each run of unit letters for its value.
    public static String format(long totalSeconds, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        int index = 0;
        int patternLength = pattern.length();
        while (index < patternLength) {
            char currentChar = pattern.charAt(index);

            if (currentChar == '\\' && index + 1 < patternLength) {
                result.append(pattern.charAt(index + 1));
                index += 2;
                continue;
            }

            Field field = Field.of(currentChar);
            if (field == null) {
                result.append(currentChar);
                index++;
                continue;
            }

            int runStart = index;
            while (index < patternLength && pattern.charAt(index) == currentChar) {
                index++;
            }
            result.append(padded(field.valueOf(totalSeconds), index - runStart));
        }

        return result.toString();
    }

    // Returns the number padded with leading zeros to the given length.
    private static String padded(long value, int minDigits) {
        String digits = String.valueOf(value);
        return digits.length() >= minDigits ? digits : "0".repeat(minDigits - digits.length()) + digits;
    }
}
