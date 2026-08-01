package triangled.overlaymod.HUD;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import triangled.overlaymod.OverlayMod;
import triangled.overlaymod.config.OverlayModConfig;
import triangled.overlaymod.util.TextRenderUtil;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static triangled.overlaymod.config.OverlayModConfig.replaceAnd;

public class ClockHUD {
    private static final int X_PADDING = 3;
    private static final int Y_PADDING = 3;
    private static final int COLOR = 0xFFFFFFFF;

    private static boolean sprintIntentActive = false;

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        OverlayModConfig config = OverlayMod.config;
        if (config == null)
            return;

        OverlayModConfig.ClockCategory clockConfig = config.clock;
        OverlayModConfig.SprintingCategory sprintingConfig = config.sprinting;

        if (!(sprintingConfig.visibility.showSprinting || clockConfig.visibility.showClock) || client.gui.hud.isHidden()) {
            return;
        }
        if (client.player == null)
            return;

        String sprinting = getSprintText(client, sprintingConfig);
        String formattedTime = getFormattedTime(clockConfig);

        String combined = sprinting + formattedTime;
        int x = graphics.guiWidth() - client.font.width(combined) - X_PADDING;

        graphics.pose().pushMatrix();
        graphics.pose().translate(x + clockConfig.positioning.xOffset, Y_PADDING + clockConfig.positioning.yOffset);
        TextRenderUtil.drawText(graphics, client.font, combined, 0, 0, COLOR, clockConfig.style.textShadow);
        graphics.pose().popMatrix();
    }

    private static String getFormattedTime(OverlayModConfig.ClockCategory clockConfig) {
        if (!clockConfig.visibility.showClock)
            return "";
        try {
            LocalTime time = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(clockConfig.text.clockFormat);
            return replaceAnd(clockConfig.text.clockText + time.format(formatter));
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String getSprintText(Minecraft client, OverlayModConfig.SprintingCategory sprintingConfig) {
        if (!sprintingConfig.visibility.showSprinting)
            return "";

        boolean hasGuiOpen = client.gui.screen() != null;
        if (!hasGuiOpen) {
            sprintIntentActive = client.options.keySprint.isDown();
        }

        return replaceAnd(sprintIntentActive ? sprintingConfig.text.sprintingText : "");
    }
}
