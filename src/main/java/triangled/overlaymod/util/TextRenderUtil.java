package triangled.overlaymod.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import triangled.overlaymod.config.OverlayModConfig.TextShadow;

import java.util.ArrayList;
import java.util.List;

public class TextRenderUtil {
    public static void drawText(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color, TextShadow shadow) {
        switch (shadow == null ? TextShadow.SHADOW : shadow) {
            case FOUR_DIRECTION -> {
                drawOutline(graphics, font, segments(text, color), x, y, false);
                graphics.text(font, text, x, y, color, false);
            }
            case EIGHT_DIRECTION -> {
                drawOutline(graphics, font, segments(text, color), x, y, true);
                graphics.text(font, text, x, y, color, false);
            }
            case SHADOW -> graphics.text(font, text, x, y, color, true);
            case NONE -> graphics.text(font, text, x, y, color, false);
        }
    }

    public static void drawText(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color, TextShadow shadow) {
        switch (shadow == null ? TextShadow.SHADOW : shadow) {
            case FOUR_DIRECTION -> {
                drawOutline(graphics, font, segments(text, color), x, y, false);
                graphics.text(font, text, x, y, color, false);
            }
            case EIGHT_DIRECTION -> {
                drawOutline(graphics, font, segments(text, color), x, y, true);
                graphics.text(font, text, x, y, color, false);
            }
            case SHADOW -> graphics.text(font, text, x, y, color, true);
            case NONE -> graphics.text(font, text, x, y, color, false);
        }
    }

    private static void drawOutline(GuiGraphicsExtractor graphics, Font font, List<ColorSegment> segments, int x, int y, boolean diagonals) {
        int cursorX = x;
        for (ColorSegment segment : segments) {
            int shadowColor = shadowColor(segment.color());
            FormattedCharSequence s = segment.text();

            graphics.text(font, s, cursorX - 1, y, shadowColor, false);
            graphics.text(font, s, cursorX + 1, y, shadowColor, false);
            graphics.text(font, s, cursorX, y - 1, shadowColor, false);
            graphics.text(font, s, cursorX, y + 1, shadowColor, false);
            if (diagonals) {
                graphics.text(font, s, cursorX - 1, y - 1, shadowColor, false);
                graphics.text(font, s, cursorX + 1, y - 1, shadowColor, false);
                graphics.text(font, s, cursorX - 1, y + 1, shadowColor, false);
                graphics.text(font, s, cursorX + 1, y + 1, shadowColor, false);
            }

            cursorX += font.width(s);
        }
    }

    private static int shadowColor(int color) {
        return (color & 0xFCFCFC) >> 2 | (color & 0xFF000000);
    }

    private static List<ColorSegment> segments(String text, int baseColor) {
        RunCollector collector = new RunCollector(baseColor);
        StringDecomposer.iterateFormatted(text, Style.EMPTY, collector);
        return collector.finish();
    }

    private static List<ColorSegment> segments(Component text, int baseColor) {
        RunCollector collector = new RunCollector(baseColor);
        StringDecomposer.iterateFormatted(text, Style.EMPTY, collector);
        return collector.finish();
    }

    private static int resolveColor(Style style, int baseColor) {
        TextColor textColor = style.getColor();
        return textColor != null ? ARGB.color(ARGB.alpha(baseColor), textColor.getValue()) : baseColor;
    }

    private static Style formatOnly(Style style) {
        return Style.EMPTY
                .withBold(style.isBold())
                .withItalic(style.isItalic())
                .withUnderlined(style.isUnderlined())
                .withStrikethrough(style.isStrikethrough())
                .withObfuscated(style.isObfuscated());
    }

    private record ColorSegment(FormattedCharSequence text, int color) {
    }

    private static final class RunCollector implements FormattedCharSink {
        private final int baseColor;
        private final List<ColorSegment> segments = new ArrayList<>();
        private final StringBuilder current = new StringBuilder();
        private int currentColor;
        private Style currentFormat;
        private boolean hasCurrent;

        private RunCollector(int baseColor) {
            this.baseColor = baseColor;
        }

        @Override
        public boolean accept(int position, Style style, int codepoint) {
            int color = resolveColor(style, baseColor);
            Style format = formatOnly(style);
            if (hasCurrent && (color != currentColor || !format.equals(currentFormat))) {
                flush();
            }
            if (!hasCurrent) {
                currentColor = color;
                currentFormat = format;
                hasCurrent = true;
            }
            current.appendCodePoint(codepoint);
            return true;
        }

        private void flush() {
            if (!current.isEmpty()) {
                FormattedCharSequence sequence = Language.getInstance().getVisualOrder(FormattedText.of(current.toString(), currentFormat));
                segments.add(new ColorSegment(sequence, currentColor));
                current.setLength(0);
            }
            hasCurrent = false;
        }

        private List<ColorSegment> finish() {
            flush();
            return segments;
        }
    }
}
