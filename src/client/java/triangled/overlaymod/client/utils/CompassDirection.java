package triangled.overlaymod.client.utils;

import static triangled.overlaymod.client.utils.AxisSign.NEGATIVE;
import static triangled.overlaymod.client.utils.AxisSign.NONE;
import static triangled.overlaymod.client.utils.AxisSign.POSITIVE;

public enum CompassDirection {
    NORTH(NONE, NEGATIVE),
    NORTH_EAST(POSITIVE, NEGATIVE),
    EAST(POSITIVE, NONE),
    SOUTH_EAST(POSITIVE, POSITIVE),
    SOUTH(NONE, POSITIVE),
    SOUTH_WEST(NEGATIVE, POSITIVE),
    WEST(NEGATIVE, NONE),
    NORTH_WEST(NEGATIVE, NEGATIVE);

    private static final CompassDirection[] VALUES = values();

    private final AxisSign xSign;
    private final AxisSign zSign;

    CompassDirection(AxisSign xSign, AxisSign zSign) {
        this.xSign = xSign;
        this.zSign = zSign;
    }

    public static CompassDirection fromYaw(float yaw, boolean includeOrdinals) {
        int sectors = includeOrdinals ? VALUES.length : VALUES.length / 2;
        int sector = Math.floorMod(Math.round((yaw + 180.0F) / (360.0F / sectors)), sectors);
        return VALUES[sector * (VALUES.length / sectors)];
    }

    public AxisSign xSign() {
        return xSign;
    }

    public AxisSign zSign() {
        return zSign;
    }
}
