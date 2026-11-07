package triangled.overlaymod.client.utils;

import static triangled.overlaymod.client.utils.AxisSign.NEGATIVE;
import static triangled.overlaymod.client.utils.AxisSign.NONE;
import static triangled.overlaymod.client.utils.AxisSign.POSITIVE;

// The 8 compass directions, and which way each one moves along X and Z.
public enum CompassDirection {
    NORTH(NONE, NEGATIVE),
    NORTH_EAST(POSITIVE, NEGATIVE),
    EAST(POSITIVE, NONE),
    SOUTH_EAST(POSITIVE, POSITIVE),
    SOUTH(NONE, POSITIVE),
    SOUTH_WEST(NEGATIVE, POSITIVE),
    WEST(NEGATIVE, NONE),
    NORTH_WEST(NEGATIVE, NEGATIVE);

    private static final CompassDirection[] DIRECTIONS = values();

    private final AxisSign xSign;
    private final AxisSign zSign;

    CompassDirection(AxisSign xSign, AxisSign zSign) {
        this.xSign = xSign;
        this.zSign = zSign;
    }

    // Returns the direction for a yaw, using 8 directions or just 4.
    public static CompassDirection fromYaw(float yaw, boolean includeOrdinals) {
        int sectorCount = includeOrdinals ? DIRECTIONS.length : DIRECTIONS.length / 2;
        int sectorIndex = Math.floorMod(Math.round((yaw + 180.0F) / (360.0F / sectorCount)), sectorCount);
        return DIRECTIONS[sectorIndex * (DIRECTIONS.length / sectorCount)];
    }

    // Returns which way this direction moves along X.
    public AxisSign xSign() {
        return xSign;
    }

    // Returns which way this direction moves along Z.
    public AxisSign zSign() {
        return zSign;
    }
}
