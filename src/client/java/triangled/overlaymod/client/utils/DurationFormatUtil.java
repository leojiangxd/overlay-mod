package triangled.overlaymod.client.utils;

public class DurationFormatUtil {
    private DurationFormatUtil() {
    }

    public static String format(long totalSeconds, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return "";
        }

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder result = new StringBuilder();
        int i = 0;
        int len = pattern.length();
        while (i < len) {
            char c = pattern.charAt(i);

            if (c == '\\' && i + 1 < len) {
                result.append(pattern.charAt(i + 1));
                i += 2;
                continue;
            }

            if (c == 'd' || c == 'h' || c == 'm' || c == 's') {
                int runStart = i;
                while (i < len && pattern.charAt(i) == c) {
                    i++;
                }
                long value = switch (c) {
                    case 'd' -> days;
                    case 'h' -> hours;
                    case 'm' -> minutes;
                    default -> seconds;
                };
                result.append(padded(value, i - runStart));
                continue;
            }

            result.append(c);
            i++;
        }

        return result.toString();
    }

    private static String padded(long value, int width) {
        String s = String.valueOf(value);
        return s.length() >= width ? s : "0".repeat(width - s.length()) + s;
    }
}
