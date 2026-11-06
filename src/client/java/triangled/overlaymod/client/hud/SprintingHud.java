package triangled.overlaymod.client.hud;

import kotlin.Pair;
import net.minecraft.client.Minecraft;
import org.polyfrost.oneconfig.api.config.v1.annotations.Text;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;

public class SprintingHud extends MinecraftTextHud {
    @Text(title = "Sprinting Text", description = "Text shown while sprinting. & starts a formatting code, \\& is a literal &")
    public String sprintingText = "ꜱᴘʀɪɴᴛɪɴɢ";

    private transient boolean sprintIntentActive;

    public SprintingHud() {
        super("overlaymod_sprinting.yml", "Sprinting", Hud.Category.Companion.getPLAYER());
    }

    @Override
    protected String getText() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return "";
        }
        if (mc.gui.screen() == null) {
            sprintIntentActive = mc.options.keySprint.isDown();
        }
        return sprintIntentActive ? sprintingText : "";
    }

    @Override
    protected String previewText() {
        return sprintingText;
    }

    @Override
    public Pair<Float, Float> defaultPosition() {
        return new Pair<>(HudManager.guiScreenWidth - getScaledWidth() - 3.0F, 14.0F);
    }
}
