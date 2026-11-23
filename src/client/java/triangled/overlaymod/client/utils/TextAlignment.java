package triangled.overlaymod.client.utils;

public enum TextAlignment {
    LEFT, MIDDLE, RIGHT;

    public float align(float extraSpace) {
        return switch (this) {
            case LEFT -> 0;
            case MIDDLE -> extraSpace / 2f;
            case RIGHT -> extraSpace;
        };
    }
}
