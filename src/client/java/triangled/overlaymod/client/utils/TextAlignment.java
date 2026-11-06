package triangled.overlaymod.client.utils;

public enum TextAlignment {
    LEFT, MIDDLE, RIGHT;

    public float align(float slack) {
        return switch (this) {
            case LEFT -> 0;
            case MIDDLE -> slack / 2f;
            case RIGHT -> slack;
        };
    }
}
