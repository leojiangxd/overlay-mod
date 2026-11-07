package triangled.overlaymod.client.utils;

// Turns digits into their subscript versions, like 12 into ₁₂.
public final class Subscript {
    private Subscript() {
    }

    // Returns the text with every digit 0-9 swapped for its subscript version.
    public static String of(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (char character : text.toCharArray()) {
            boolean isDigit = character >= '0' && character <= '9';
            result.append(isDigit ? (char) ('₀' + (character - '0')) : character);
        }
        return result.toString();
    }
}
