package triangled.overlaymod.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import triangled.overlaymod.config.OverlayModConfig.TextShadow;

public class TextRenderUtil {
    public static void drawText(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color, TextShadow shadow) {
        switch (shadow) {
            case FULL -> {
                drawOutline(graphics, font, stripFormatting(text), x, y, color);
                graphics.text(font, text, x, y, color, false);
            }
            case SHADOW -> graphics.text(font, text, x, y, color, true);
            case NONE -> graphics.text(font, text, x, y, color, false);
        }
    }

    public static void drawText(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color, TextShadow shadow) {
        switch (shadow) {
            case FULL -> {
                drawOutline(graphics, font, text.getString(), x, y, color);
                graphics.text(font, text, x, y, color, false);
            }
            case SHADOW -> graphics.text(font, text, x, y, color, true);
            case NONE -> graphics.text(font, text, x, y, color, false);
        }
    }

    private static void drawOutline(GuiGraphicsExtractor graphics, Font font, String outline, int x, int y, int color) {
        int shadowColor = shadowColor(color);
        graphics.text(font, outline, x - 1, y, shadowColor, false);
        graphics.text(font, outline, x + 1, y, shadowColor, false);
        graphics.text(font, outline, x, y - 1, shadowColor, false);
        graphics.text(font, outline, x, y + 1, shadowColor, false);
    }

    private static int shadowColor(int color) {
        return (color & 0xFCFCFC) >> 2 | (color & 0xFF000000);
    }

    private static String stripFormatting(String text) {
        return text.replaceAll("§.", "");
    }
}
