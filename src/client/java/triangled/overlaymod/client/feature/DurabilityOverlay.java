package triangled.overlaymod.client.feature;

import static net.minecraft.core.component.DataComponents.UNBREAKABLE;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import triangled.overlaymod.client.OverlayModConfig;
import triangled.overlaymod.client.utils.Colors;
import triangled.overlaymod.client.utils.FormatCodes;
import triangled.overlaymod.client.utils.TextAlignment;
import triangled.overlaymod.client.utils.TextRenderUtil;
import triangled.overlaymod.client.utils.TextShadow;

public final class DurabilityOverlay {
    private static final int BAR_X_OFFSET = 2;
    private static final int BAR_Y_OFFSET = 13;
    private static final int BAR_WIDTH = 13;
    private static final int BAR_HEIGHT = 2;
    private static final int BAR_BACKGROUND = 0xFF000000;
    private static final float TEXT_SCALE = 0.5f;
    private static final String EXPONENT = "ᴇ";

    private DurabilityOverlay() {
    }

    private record DurabilityText(String text, boolean subscript) {
        static final DurabilityText NONE = new DurabilityText("", false);

        boolean isEmpty() {
            return text.isEmpty();
        }
    }

    public static void render(GuiGraphicsExtractor graphics, Font font, ItemStack stack, int x, int y) {
        int barLeft = x + BAR_X_OFFSET;
        int barTop = y + BAR_Y_OFFSET;
        boolean barVisible = stack.isBarVisible();

        if (OverlayModConfig.durabilityShowBar && barVisible) {
            drawBar(graphics, stack, barLeft, barTop);
        }
        if (!OverlayModConfig.durabilityShowText || (!barVisible && !OverlayModConfig.durabilityShowAtFull)) {
            return;
        }

        DurabilityText durability = durabilityText(stack);
        if (durability.isEmpty()) {
            return;
        }

        float scaledWidth = font.width(durability.text()) * TEXT_SCALE;
        float scaledHeight = font.lineHeight * TEXT_SCALE;

        TextAlignment alignment = OverlayModConfig.durabilityAlignment;
        float anchorX = barLeft + alignment.align(BAR_WIDTH - scaledWidth)
                + (alignment == TextAlignment.MIDDLE ? 0.25f : 0f);
        float anchorY = barTop - scaledHeight + 0.5f;
        if (durability.subscript()) {
            anchorY -= 0.5f + OverlayModConfig.durabilitySubscriptYOffset;
        }
        if (!OverlayModConfig.durabilityShowBar || !barVisible) {
            anchorY += 2f;
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(anchorX + OverlayModConfig.durabilityXOffset, anchorY - OverlayModConfig.durabilityYOffset);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        drawText(graphics, font, durability, textColor(stack));
        graphics.pose().popMatrix();
    }

    private static void drawBar(GuiGraphicsExtractor graphics, ItemStack stack, int left, int top) {
        graphics.fill(RenderPipelines.GUI, left, top, left + BAR_WIDTH, top + BAR_HEIGHT, BAR_BACKGROUND);
        graphics.fill(RenderPipelines.GUI, left, top, left + stack.getBarWidth(), top + 1, ARGB.opaque(stack.getBarColor()));
    }

    private static int textColor(ItemStack stack) {
        int configColor = Colors.argb(OverlayModConfig.durabilityColor);
        int rgb = OverlayModConfig.durabilityUseCustomColor ? (configColor & 0xFFFFFF) : stack.getBarColor();
        return ARGB.color(ARGB.alpha(configColor), rgb);
    }

    private static void drawText(GuiGraphicsExtractor graphics, Font font, DurabilityText durability, int color) {
        TextShadow shadow = OverlayModConfig.durabilityTextShadow;
        String text = durability.text();
        int exponentIndex = durability.subscript() ? text.indexOf(EXPONENT) : -1;
        if (exponentIndex < 0) {
            TextRenderUtil.drawText(graphics, font, text, 0, 0, color, shadow);
            return;
        }

        String mantissa = text.substring(0, exponentIndex);
        String exponent = text.substring(exponentIndex + EXPONENT.length());
        int cursorX = 0;

        if (!mantissa.isEmpty()) {
            TextRenderUtil.drawText(graphics, font, mantissa, cursorX, 0, color, shadow);
            cursorX += font.width(mantissa);
        }

        int exponentY = Math.round(1f - OverlayModConfig.durabilityExponentYOffset);
        TextRenderUtil.drawText(graphics, font, EXPONENT, cursorX, exponentY, color, shadow);
        cursorX += font.width(EXPONENT);

        if (!exponent.isEmpty()) {
            TextRenderUtil.drawText(graphics, font, exponent, cursorX, 0, color, shadow);
        }
    }

    private static DurabilityText durabilityText(ItemStack stack) {
        if (stack.getMaxDamage() == 0 || stack.getComponents().has(UNBREAKABLE)) {
            return DurabilityText.NONE;
        }
        int remaining = stack.getMaxDamage() - stack.getDamageValue();

        if (OverlayModConfig.durabilityAsPercentage) {
            int percentage = (int) (remaining / (double) stack.getMaxDamage() * 100);
            if (percentage >= 100 && !OverlayModConfig.durabilityShowAtFull) {
                return DurabilityText.NONE;
            }
            return new DurabilityText(String.format("%d%%", percentage), false);
        }

        boolean subscript = OverlayModConfig.durabilityUseSubscript;
        int digitLimit = subscript ? OverlayModConfig.durabilitySubscriptDigitLimit : OverlayModConfig.durabilityNormalDigitLimit;
        long threshold = (long) Math.pow(10, digitLimit);

        String text = remaining < threshold
                ? Integer.toString(remaining)
                : String.format("%.0e", (double) remaining).replaceAll("e\\+0", EXPONENT);

        return new DurabilityText(subscript ? FormatCodes.toSubscript(text) : text, subscript);
    }
}
