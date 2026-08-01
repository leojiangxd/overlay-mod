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
        String deliminator = (!sprinting.isEmpty() && !formattedTime.isEmpty())
                ? replaceAnd(clockConfig.text.deliminator)
                : "";

        int combinedWidth = client.font.width(sprinting) + client.font.width(deliminator) + client.font.width(formattedTime);
        int x = graphics.guiWidth() - combinedWidth - X_PADDING;

        graphics.pose().pushMatrix();
        graphics.pose().translate(x + clockConfig.positioning.xOffset, Y_PADDING - clockConfig.positioning.yOffset);

        int cursorX = 0;
        cursorX = drawSprintPiece(graphics, client, sprinting, sprintingConfig, clockConfig, cursorX);
        cursorX = drawPiece(graphics, client, deliminator, clockConfig.style.deliminatorColor, clockConfig.style.textShadow, cursorX);
        drawPiece(graphics, client, formattedTime, clockConfig.style.color, clockConfig.style.textShadow, cursorX);

        graphics.pose().popMatrix();
    }

    private static int drawSprintPiece(GuiGraphicsExtractor graphics, Minecraft client, String text,
            OverlayModConfig.SprintingCategory sprintingConfig, OverlayModConfig.ClockCategory clockConfig, int cursorX) {
        if (text.isEmpty()) return cursorX;

        graphics.pose().pushMatrix();
        graphics.pose().translate(cursorX + sprintingConfig.positioning.xOffset, -sprintingConfig.positioning.yOffset);
        TextRenderUtil.drawText(graphics, client.font, text, 0, 0, sprintingConfig.style.color, clockConfig.style.textShadow);
        graphics.pose().popMatrix();

        return cursorX + client.font.width(text);
    }

    private static int drawPiece(GuiGraphicsExtractor graphics, Minecraft client, String text, int color,
            OverlayModConfig.TextShadow shadow, int cursorX) {
        if (text.isEmpty()) return cursorX;
        TextRenderUtil.drawText(graphics, client.font, text, cursorX, 0, color, shadow);
        return cursorX + client.font.width(text);
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
