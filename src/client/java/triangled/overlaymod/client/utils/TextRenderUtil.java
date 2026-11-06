package triangled.overlaymod.client.utils;

import java.util.ArrayList;
import java.util.List;
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

public final class TextRenderUtil {
    private TextRenderUtil() {
    }

    public static void drawText(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int color, TextShadow shadow) {
        TextShadow style = TextShadow.orDefault(shadow);
        if (style.hasOutline()) {
            RunCollector collector = new RunCollector(color);
            StringDecomposer.iterateFormatted(text, Style.EMPTY, collector);
            drawOutline(graphics, font, collector.finish(), x, y, style);
        }
        graphics.text(font, text, x, y, color, style.dropShadow());
    }

    public static void drawText(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color, TextShadow shadow) {
        TextShadow style = TextShadow.orDefault(shadow);
        if (style.hasOutline()) {
            RunCollector collector = new RunCollector(color);
            text.getVisualOrderText().accept(collector);
            drawOutline(graphics, font, collector.finish(), x, y, style);
        }
        graphics.text(font, text, x, y, color, style.dropShadow());
    }

    public static Bounds textBounds(Font font, int width, float x, float y, float scale, TextShadow shadow) {
        Insets insets = TextShadow.orDefault(shadow).insets();
        return new Bounds(
                x - insets.left() * scale,
                y - insets.top() * scale,
                x + (width + insets.right()) * scale,
                y + (font.lineHeight - 1 + insets.bottom()) * scale);
    }

    private static void drawOutline(GuiGraphicsExtractor graphics, Font font, List<ColorSegment> segments, int x, int y, TextShadow shadow) {
        int cursorX = x;
        for (ColorSegment segment : segments) {
            int outlineColor = outlineColor(segment.color());
            for (TextShadow.Offset offset : shadow.outline()) {
                graphics.text(font, segment.text(), cursorX + offset.dx(), y + offset.dy(), outlineColor, false);
            }
            cursorX += font.width(segment.text());
        }
    }

    private static int outlineColor(int color) {
        return (color & 0xFCFCFC) >> 2 | (color & 0xFF000000);
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
