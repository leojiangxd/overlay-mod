package triangled.overlaymod.client;

import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;

public class OverlayModConfig extends Config {
    public static final OverlayModConfig INSTANCE = new OverlayModConfig();

    @Switch(title = "Enable Feature", description = "Turns the feature on or off")
    public static boolean enabled = true;

    public OverlayModConfig() {
        super(
                "overlay-mod.json",
                "/assets/overlay-mod/icon.png",
                "Overlay Mod",
                Category.HUD
        );
    }
}