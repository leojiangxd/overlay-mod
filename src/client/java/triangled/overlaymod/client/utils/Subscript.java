package triangled.overlaymod.client.utils;

public final class Subscript {
    private Subscript() {
    }

    public static String of(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (char character : text.toCharArray()) {
            boolean isDigit = character >= '0' && character <= '9';
            result.append(isDigit ? (char) ('₀' + (character - '0')) : character);
        }
        return result.toString();
    }
}
