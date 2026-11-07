package triangled.overlaymod.client.hud;

import kotlin.Pair;
import net.minecraft.client.Minecraft;
import org.polyfrost.oneconfig.api.config.v1.annotations.Text;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;

public class SprintingHud extends MinecraftTextHud {
    // --------------- CONFIGS ---------------
    @Text(title = "Sprinting Text", description = "Text shown while sprint is toggled on")
    public String sprintingText = "ꜱᴘʀɪɴᴛɪɴɢ";
    // ---------------------------------------

    private transient boolean sprintToggled;

    public SprintingHud() {
        super("overlaymod_sprinting.yml", "Sprinting", Hud.Category.Companion.getPLAYER());
    }

    // Shows the sprint text while sprint is toggled on; keeps its state in menus.
    @Override
    protected String getText() {
        if (player() == null) {
            return "";
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.screen() == null) {
            sprintToggled = minecraft.options.keySprint.isDown();
        }
        return sprintToggled ? sprintingText : "";
    }

    // Shows the sprint text in the HUD editor.
    @Override
    protected String previewText() {
        return sprintingText;
    }

    // Default position of HUD in OneConfig's HUD editor
    @Override
    public Pair<Float, Float> defaultPosition() {
        return new Pair<>(HudManager.guiScreenWidth - getScaledWidth() - 2.0F, 14.0F);
    }
}
