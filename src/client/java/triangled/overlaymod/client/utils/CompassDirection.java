package triangled.overlaymod.client.utils;

// dx/dz: which way facing this direction moves along X and Z (-1, 0 or 1).
public enum CompassDirection {
    NORTH(0, -1),
    NORTH_EAST(1, -1),
    EAST(1, 0),
    SOUTH_EAST(1, 1),
    SOUTH(0, 1),
    SOUTH_WEST(-1, 1),
    WEST(-1, 0),
    NORTH_WEST(-1, -1);

    private static final CompassDirection[] DIRECTIONS = values();

    public final int dx;
    public final int dz;

    CompassDirection(int dx, int dz) {
        this.dx = dx;
        this.dz = dz;
    }

    public static CompassDirection fromYaw(float yaw, boolean includeOrdinals) {
        int sectorCount = includeOrdinals ? DIRECTIONS.length : DIRECTIONS.length / 2;
        int sectorIndex = Math.floorMod(Math.round((yaw + 180.0F) / (360.0F / sectorCount)), sectorCount);
        return DIRECTIONS[sectorIndex * (DIRECTIONS.length / sectorCount)];
    }
}
