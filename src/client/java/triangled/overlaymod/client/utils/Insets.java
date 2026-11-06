package triangled.overlaymod.client.utils;

public record Insets(int left, int top, int right, int bottom) {
    public static final Insets NONE = new Insets(0, 0, 0, 0);

    public static Insets uniform(int size) {
        return new Insets(size, size, size, size);
    }

    public int horizontal() {
        return left + right;
    }

    public int vertical() {
        return top + bottom;
    }
}
