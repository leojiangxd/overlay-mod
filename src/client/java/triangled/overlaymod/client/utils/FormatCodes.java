package triangled.overlaymod.client.utils;

public final class FormatCodes {
    private FormatCodes() {
    }

    public static String replaceAnd(String input) {
        if (input == null) {
            return "";
        }
        return input.replaceAll("(?<!\\\\)&", "§").replace("\\&", "&");
    }

    public static String toSubscript(String input) {
        StringBuilder result = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            result.append(c >= '0' && c <= '9' ? (char) ('₀' + (c - '0')) : c);
        }
        return result.toString();
    }
}
