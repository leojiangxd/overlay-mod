package triangled.overlaymod.client.utils;

public final class Facing {
    private Facing() {
    }

    public static int index(float yaw, int directions) {
        int index = Math.round((yaw + 180.0F) / (360.0F / directions)) % directions;
        return index < 0 ? index + directions : index;
    }
}
