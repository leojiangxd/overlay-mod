package triangled.overlaymod.client.utils;

// Left, middle or right alignment.
public enum TextAlignment {
    LEFT, MIDDLE, RIGHT;

    // Returns how far to shift something with the given space left over.
    public float align(float extraSpace) {
        return switch (this) {
            case LEFT -> 0;
            case MIDDLE -> extraSpace / 2f;
            case RIGHT -> extraSpace;
        };
    }
}
