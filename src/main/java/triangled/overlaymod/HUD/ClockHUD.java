package triangled.overlaymod.HUD;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import triangled.overlaymod.OverlayMod;
import triangled.overlaymod.config.OverlayModConfig;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static triangled.overlaymod.config.OverlayModConfig.replaceAnd;

public class ClockHUD {
    private static final int X_PADDING = 3;
    private static final int Y_PADDING = 3;
    private static final int COLOR = 0xFFFFFFFF;
    private static final boolean TEXT_SHADOW = true;

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        OverlayModConfig config = OverlayMod.config;
        if (config == null) return;

        OverlayModConfig.ClockCategory clockConfig = config.clock;
        OverlayModConfig.SprintingCategory sprintingConfig = config.sprinting;

        if (!(sprintingConfig.showSprinting || clockConfig.showClock) || client.gui.hud.isHidden()) {
            return;
        }
        if (client.player == null) return;

        String sprinting = getSprintText(client, sprintingConfig);
        String formattedTime = getFormattedTime(clockConfig);

        String combined = sprinting + formattedTime;
        int x = graphics.guiWidth() - client.font.width(combined) - X_PADDING;
        graphics.text(client.font, combined, x, Y_PADDING, COLOR, TEXT_SHADOW);
    }

    private static String getFormattedTime(OverlayModConfig.ClockCategory clockConfig) {
        if (!clockConfig.showClock) return "";
        try {
            LocalTime time = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(clockConfig.clockFormat);
            return replaceAnd(clockConfig.clockText + time.format(formatter));
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String getSprintText(Minecraft client, OverlayModConfig.SprintingCategory sprintingConfig) {
        if (!sprintingConfig.showSprinting) return "";
        return replaceAnd(client.options.keySprint.isDown() ? sprintingConfig.sprintingText : "");
    }
}
