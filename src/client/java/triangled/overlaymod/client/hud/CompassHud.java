package triangled.overlaymod.client.hud;

import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.config.v1.annotations.Text;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import triangled.overlaymod.client.utils.Facing;

public class CompassHud extends MinecraftTextHud {
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

    public CompassHud() {
        super("overlaymod_compass.yml", "Compass", Hud.Category.Companion.getINFO());
    }

    @Override
    protected String getText() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc == null ? null : mc.player;

        if (player == null) {
            return "";
        }

        float yaw = player.getYRot(1.0F);
        return ordinalDirections
                ? directionText(Facing.index(yaw, 8))
                : directionText(Facing.index(yaw, 4) * 2);
    }

    @Override
    protected String previewText() {
        return dirNorth;
    }

    private String directionText(int facing) {
        switch (facing) {
            case 0: return dirNorth;
            case 1: return dirNorthEast;
            case 2: return dirEast;
            case 3: return dirSouthEast;
            case 4: return dirSouth;
            case 5: return dirSouthWest;
            case 6: return dirWest;
            default: return dirNorthWest;
        }
    }

    @Override
    public Pair<Float, Float> defaultPosition() {
        return new Pair<>(3.0F, 14.0F);
    }
}
