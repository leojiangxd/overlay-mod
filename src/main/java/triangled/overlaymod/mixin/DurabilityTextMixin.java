package triangled.overlaymod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import triangled.overlaymod.OverlayMod;
import triangled.overlaymod.config.OverlayModConfig;
import triangled.overlaymod.util.TextRenderUtil;

import static net.minecraft.core.component.DataComponents.UNBREAKABLE;

@Mixin(GuiGraphicsExtractor.class)
public class DurabilityTextMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "itemBar", at = @At("HEAD"), cancellable = true)
    private void onItemBar(ItemStack itemStack, int x, int y, CallbackInfo ci) {
        OverlayModConfig.DurabilityCategory config = OverlayMod.config.durability;
        GuiGraphicsExtractor graphics = (GuiGraphicsExtractor) (Object) this;

        boolean barVisible = itemStack.isBarVisible();
        int barLeft = x + 2;
        int barTop = y + 13;
        int barWidth = 13;

        // Replicate vanilla's bar rendering ourselves (instead of letting the original
        // method run after us) so our text always draws on top instead of underneath
        // it.
        if (config.visibility.showBar && barVisible) {
            graphics.fill(RenderPipelines.GUI, barLeft, barTop, barLeft + barWidth, barTop + 2, -16777216);
            graphics.fill(RenderPipelines.GUI, barLeft, barTop, barLeft + itemStack.getBarWidth(), barTop + 1,
                    ARGB.opaque(itemStack.getBarColor()));
        }
        ci.cancel();

        if (!config.visibility.showText) {
            return;
        }

        if (!barVisible && !config.visibility.showAtFullDurability) {
            return;
        }

        DurabilityText durabilityText = getDurabilityText(config, itemStack);
        if (durabilityText.text().isEmpty()) {
            return;
        }
        String text = durabilityText.text();

        float scale = 0.5f;

        float scaledWidth = minecraft.font.width(text) * scale;
        float scaledHeight = minecraft.font.lineHeight * scale;

        float anchorX = switch (config.positioning.alignment) {
            case LEFT -> barLeft;
            case RIGHT -> barLeft + barWidth - scaledWidth;
            case MIDDLE -> barLeft + (barWidth - scaledWidth) / 2 + 0.25f;
        };
        float anchorY = barTop - scaledHeight + 0.5f;
        if (durabilityText.subscript()) {
            anchorY -= 0.5f + config.positioning.subscriptYOffset;
        }
        if (!config.visibility.showBar || !barVisible) {
            anchorY += 2f;
        }

        int alpha = ARGB.alpha(config.style.color);
        int rgb = config.style.useCustomColor ? (config.style.color & 0xFFFFFF) : itemStack.getBarColor();
        int color = ARGB.color(alpha, rgb);

        graphics.pose().pushMatrix();
        graphics.pose().translate(anchorX + config.positioning.xOffset, anchorY - config.positioning.yOffset);
        graphics.pose().scale(scale, scale);
        drawDurabilityText(graphics, config, text, durabilityText.subscript(), color);
        graphics.pose().popMatrix();
    }

    @Unique
    private void drawDurabilityText(GuiGraphicsExtractor graphics, OverlayModConfig.DurabilityCategory config,
            String text, boolean subscript, int color) {
        int eIndex = subscript ? text.indexOf('ᴇ') : -1;
        if (eIndex < 0) {
            TextRenderUtil.drawText(graphics, minecraft.font, text, 0, 0, color, config.style.textShadow);
            return;
        }

        String prefix = text.substring(0, eIndex);
        String suffix = text.substring(eIndex + 1);
        int cursorX = 0;

        if (!prefix.isEmpty()) {
            TextRenderUtil.drawText(graphics, minecraft.font, prefix, cursorX, 0, color, config.style.textShadow);
            cursorX += minecraft.font.width(prefix);
        }

        int exponentY = Math.round(1f - config.positioning.exponentYOffset);
        TextRenderUtil.drawText(graphics, minecraft.font, "ᴇ", cursorX, exponentY, color, config.style.textShadow);
        cursorX += minecraft.font.width("ᴇ");

        if (!suffix.isEmpty()) {
            TextRenderUtil.drawText(graphics, minecraft.font, suffix, cursorX, 0, color, config.style.textShadow);
        }
    }

    @Unique
    private record DurabilityText(String text, boolean subscript) {
    }

    @Unique
    private DurabilityText getDurabilityText(OverlayModConfig.DurabilityCategory config, ItemStack item) {
        int currentDamage = item.getMaxDamage() - item.getDamageValue();
        if (item.getMaxDamage() == 0 || item.getComponents().has(UNBREAKABLE)) {
            return new DurabilityText("", false);
        }

        if (config.format.showAsPercentage) {
            int percentage = (int) (currentDamage / (double) item.getMaxDamage() * 100);
            if (percentage >= 100 && !config.visibility.showAtFullDurability) {
                return new DurabilityText("", false);
            }
            return new DurabilityText(String.format("%d%%", percentage), false);
        }

        boolean subscript = config.format.useSubscript;
        int digitLimit = subscript ? config.format.subscriptDigitLimit : config.format.normalDigitLimit;
        long threshold = (long) Math.pow(10, digitLimit);

        String text = currentDamage < threshold
                ? Integer.toString(currentDamage)
                : String.format("%.0e", (double) currentDamage).replaceAll("e\\+0", "ᴇ");

        return new DurabilityText(subscript ? convertToSubscript(text) : text, subscript);
    }

    @Unique
    private String convertToSubscript(String input) {
        String[] subscriptDigits = { "₀", "₁", "₂", "₃", "₄", "₅", "₆", "₇", "₈", "₉" };
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                int digit = c - '0';
                result.append(subscriptDigits[digit]);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

}
