package triangled.overlaymod.client.utils;

/**
 * Shared yaw maths for the HUDs that care which way the player is facing.
 */
public final class Facing {
    private Facing() {
    }

    /**
     * Maps a Minecraft yaw (0 = south, 90 = west, 180 = north, 270 = east)
     * onto an index into a direction table that starts at north and runs
     * clockwise with {@code directions} evenly spaced entries.
     */
    public static int index(float yaw, int directions) {
        int index = Math.round((yaw + 180.0F) / (360.0F / directions)) % directions;
        return index < 0 ? index + directions : index;
    }
}
