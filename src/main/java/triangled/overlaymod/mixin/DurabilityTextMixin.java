package triangled.overlaymod.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

        if (!config.visibility.showDurabilityBar) {
            ci.cancel();
        }

        if (!config.visibility.showDurabilityText) {
            return;
        }

        boolean barVisible = itemStack.isBarVisible();
        if (!barVisible && !config.visibility.showFullDurability) {
            return;
        }

        String text = getDurabilityText(config, itemStack);
        if (text.isEmpty()) {
            return;
        }

        GuiGraphicsExtractor graphics = (GuiGraphicsExtractor) (Object) this;

        int barLeft = x + 2;
        int barTop = y + 13;
        int barWidth = 13;
        float scale = 0.5f;

        float scaledWidth = minecraft.font.width(text) * scale;
        float scaledHeight = minecraft.font.lineHeight * scale;

        float anchorX = switch (config.positioning.alignment) {
            case LEFT -> barLeft;
            case RIGHT -> barLeft + barWidth - scaledWidth;
            case MIDDLE -> barLeft + (barWidth - scaledWidth) / 2 + 0.25f;
        };
        float anchorY = barTop - scaledHeight + 0.5f;
        if (!config.visibility.showDurabilityBar || !barVisible) {
            anchorY += 2f;
        }

        int color = 0xFF000000 | itemStack.getBarColor();

        graphics.pose().pushMatrix();
        graphics.pose().translate(anchorX + config.positioning.durabilityXOffset, anchorY - config.positioning.durabilityYOffset);
        graphics.pose().scale(scale, scale);
        TextRenderUtil.drawText(graphics, minecraft.font, text, 0, 0, color, config.style.textShadow);
        graphics.pose().popMatrix();
    }

    @Unique
    private String getDurabilityText(OverlayModConfig.DurabilityCategory config, ItemStack item) {
        int currentDamage = item.getMaxDamage() - item.getDamageValue();
        if (item.getMaxDamage() == 0 || item.getComponents().has(UNBREAKABLE)) {
            return "";
        }

        if (config.visibility.durabilityAsPercentage) {
            int percentage = (int) (currentDamage / (double) item.getMaxDamage() * 100);
            if (percentage >= 100 && !config.visibility.showFullDurability) {
                return "";
            }
            return String.format("%d%%", percentage);
        }

        if (currentDamage < 1000) {
            return Integer.toString(currentDamage);
        } else if (currentDamage < 10000) {
            return String.format("%.1fk", Math.floor(currentDamage / 1000.0 * 10) / 10);
        } else {
            return String.format("%.0e", (double) currentDamage).replaceAll("e\\+0", "ᴇ");
        }
    }
}
