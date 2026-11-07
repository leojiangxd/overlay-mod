package triangled.overlaymod.client.hud;

import kotlin.Pair;
import net.minecraft.client.player.LocalPlayer;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.config.v1.annotations.Text;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import triangled.overlaymod.client.utils.CompassDirection;

public class CompassHud extends MinecraftTextHud {
    // --------------- CONFIGS ---------------
    @Switch(title = "Ordinal Directions", description = "Show NE/SE/SW/NW instead of N/E/S/W")
    public boolean ordinalDirections = true;

    @Text(title = "North", description = "Text shown when facing north")
    public String dirNorth = "ɴ";

    @Text(title = "Northeast", description = "Text shown when facing northeast")
    public String dirNorthEast = "ɴᴇ";

    @Text(title = "East", description = "Text shown when facing east")
    public String dirEast = "ᴇ";

    @Text(title = "Southeast", description = "Text shown when facing southeast")
    public String dirSouthEast = "sᴇ";

    @Text(title = "South", description = "Text shown when facing south")
    public String dirSouth = "s";

    @Text(title = "Southwest", description = "Text shown when facing southwest")
    public String dirSouthWest = "sᴡ";

    @Text(title = "West", description = "Text shown when facing west")
    public String dirWest = "ᴡ";

    @Text(title = "Northwest", description = "Text shown when facing northwest")
    public String dirNorthWest = "ɴᴡ";
    // ---------------------------------------

    public CompassHud() {
        super("overlaymod_compass.yml", "Compass", Hud.Category.Companion.getINFO());
    }

    // Returns the text for the direction the player is facing.
    @Override
    protected String getText() {
        LocalPlayer player = player();
        if (player == null) {
            return "";
        }
        return directionText(CompassDirection.fromYaw(player.getYRot(1.0F), ordinalDirections));
    }

    // Shows north in the HUD editor when there's no direction to show.
    @Override
    protected String previewText() {
        return directionText(CompassDirection.NORTH);
    }

    // Returns the text set for the given direction.
    private String directionText(CompassDirection direction) {
        return switch (direction) {
            case NORTH -> dirNorth;
            case NORTH_EAST -> dirNorthEast;
            case EAST -> dirEast;
            case SOUTH_EAST -> dirSouthEast;
            case SOUTH -> dirSouth;
            case SOUTH_WEST -> dirSouthWest;
            case WEST -> dirWest;
            case NORTH_WEST -> dirNorthWest;
        };
    }

    // Default position of HUD in OneConfig's HUD editor
    @Override
    public Pair<Float, Float> defaultPosition() {
        return new Pair<>(3.0F, 14.0F);
    }
}
