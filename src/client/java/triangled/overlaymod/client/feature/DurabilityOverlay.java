package triangled.overlaymod.client.feature;

import static net.minecraft.core.component.DataComponents.UNBREAKABLE;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import org.polyfrost.compose.render.PolyColor;
import org.polyfrost.compose.render.RenderContext;
import triangled.overlaymod.client.OverlayModConfig;
import triangled.overlaymod.client.utils.Colors;
import triangled.overlaymod.client.utils.Subscript;
import triangled.overlaymod.client.utils.TextAlignment;

public final class DurabilityOverlay {
    private static final int BAR_X_OFFSET = 2;
    private static final int BAR_Y_OFFSET = 13;
    private static final int BAR_WIDTH = 13;
    private static final int BAR_HEIGHT = 2;
    private static final int BAR_BACKGROUND_COLOR = 0xFF000000;
    private static final float TEXT_SCALE = 0.5f;
    private static final String EXPONENT_SYMBOL = "ᴇ";

    private DurabilityOverlay() {
    }

    private interface Target {
        void fill(float left, float top, float width, float height, int color);

        void text(String text, float x, float y, float scale, int color, boolean shadow);
    }

    private record GuiTarget(GuiGraphicsExtractor graphics, Font font) implements Target {
        @Override
        public void fill(float left, float top, float width, float height, int color) {
            graphics.fill(RenderPipelines.GUI, (int) left, (int) top, (int) (left + width), (int) (top + height),
                    color);
        }

        @Override
        public void text(String text, float x, float y, float scale, int color, boolean shadow) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(x, y);
            graphics.pose().scale(scale, scale);
            graphics.text(font, text, 0, 0, color, shadow);
            graphics.pose().popMatrix();
        }
    }

    private record CanvasTarget(RenderContext renderContext) implements Target {
        @Override
        public void fill(float left, float top, float width, float height, int color) {
            renderContext.rect(left, top, width, height, new PolyColor(color, false, 1f), 0f);
        }

        @Override
        public void text(String text, float x, float y, float scale, int color, boolean shadow) {
            renderContext.mcText(text, x, y, color, shadow, scale);
        }
    }

    private record DurabilityText(String text, boolean subscript) {
        static final DurabilityText NONE = new DurabilityText("", false);
    }

    public static void render(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y) {
        draw(new GuiTarget(graphics, font), font, stack, x, y);
    }

    public static void render(RenderContext renderContext, Font font, ItemStack stack, float x, float y) {
        draw(new CanvasTarget(renderContext), font, stack, x, y);
    }

    private static void draw(Target target, Font font, ItemStack stack, float x, float y) {
        float barLeft = x + BAR_X_OFFSET;
        float barTop = y + BAR_Y_OFFSET;
        boolean barVisible = stack.isBarVisible();

        if (OverlayModConfig.durabilityShowBar && barVisible) {
            drawBar(target, stack, barLeft, barTop);
        }
        if (!OverlayModConfig.durabilityShowText || (!barVisible && !OverlayModConfig.durabilityShowAtFull)) {
            return;
        }

        DurabilityText label = durabilityText(stack);
        if (label.text().isEmpty()) {
            return;
        }

        float textWidth = font.width(label.text()) * TEXT_SCALE;
        float textHeight = font.lineHeight * TEXT_SCALE;

        TextAlignment alignment = OverlayModConfig.durabilityAlignment;
        float textX = barLeft + alignment.align(BAR_WIDTH - textWidth)
                + (alignment == TextAlignment.MIDDLE ? 0.25f : 0f);
        float textY = barTop - textHeight + 0.5f;
        if (label.subscript()) {
            textY -= 0.5f + OverlayModConfig.durabilitySubscriptYOffset;
        }
        if (!OverlayModConfig.durabilityShowBar || !barVisible) {
            textY += 2f;
        }

        drawText(target, font, label, textColor(stack),
                textX + OverlayModConfig.durabilityXOffset, textY - OverlayModConfig.durabilityYOffset);
    }

    private static void drawBar(Target target, ItemStack stack, float left, float top) {
        target.fill(left, top, BAR_WIDTH, BAR_HEIGHT, BAR_BACKGROUND_COLOR);
        target.fill(left, top, stack.getBarWidth(), 1, ARGB.opaque(stack.getBarColor()));
    }

    private static int textColor(ItemStack stack) {
        int customColor = Colors.argb(OverlayModConfig.durabilityColor);
        int rgb = OverlayModConfig.durabilityUseCustomColor ? (customColor & 0xFFFFFF) : stack.getBarColor();
        return ARGB.color(ARGB.alpha(customColor), rgb);
    }

    private static void drawText(Target target, Font font, DurabilityText label, int color, float originX,
            float originY) {
        boolean shadow = OverlayModConfig.durabilityShadow;
        String text = label.text();
        int exponentIndex = label.subscript() ? text.indexOf(EXPONENT_SYMBOL) : -1;
        if (exponentIndex < 0) {
            target.text(text, originX, originY, TEXT_SCALE, color, shadow);
            return;
        }

        String baseDigits = text.substring(0, exponentIndex);
        String exponentDigits = text.substring(exponentIndex + EXPONENT_SYMBOL.length());
        float exponentYShift = Math.round(1f - OverlayModConfig.durabilityExponentYOffset) * TEXT_SCALE;
        float cursorX = originX;
        target.text(baseDigits, cursorX, originY, TEXT_SCALE, color, shadow);
        cursorX += font.width(baseDigits) * TEXT_SCALE;
        target.text(EXPONENT_SYMBOL, cursorX, originY + exponentYShift, TEXT_SCALE, color, shadow);
        cursorX += font.width(EXPONENT_SYMBOL) * TEXT_SCALE;
        target.text(exponentDigits, cursorX, originY, TEXT_SCALE, color, shadow);
    }

    private static DurabilityText durabilityText(ItemStack stack) {
        if (stack.getMaxDamage() == 0 || stack.getComponents().has(UNBREAKABLE)) {
            return DurabilityText.NONE;
        }
        int remainingDurability = stack.getMaxDamage() - stack.getDamageValue();

        if (OverlayModConfig.durabilityAsPercentage) {
            int percentage = (int) (remainingDurability / (double) stack.getMaxDamage() * 100);
            if (percentage >= 100 && !OverlayModConfig.durabilityShowAtFull) {
                return DurabilityText.NONE;
            }
            return new DurabilityText(percentage + "%", false);
        }

        boolean subscript = OverlayModConfig.durabilityUseSubscript;
        int digitLimit = subscript ? OverlayModConfig.durabilitySubscriptDigitLimit
                : OverlayModConfig.durabilityNormalDigitLimit;
        long maxPlainValue = (long) Math.pow(10, digitLimit);

        String formatted = remainingDurability < maxPlainValue
                ? Integer.toString(remainingDurability)
                : String.format("%.0e", (double) remainingDurability).replaceAll("e\\+0", EXPONENT_SYMBOL);

        return new DurabilityText(subscript ? Subscript.of(formatted) : formatted, subscript);
    }
}
