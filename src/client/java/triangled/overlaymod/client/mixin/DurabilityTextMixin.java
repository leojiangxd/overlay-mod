package triangled.overlaymod.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import triangled.overlaymod.client.OverlayModConfig;
import triangled.overlaymod.client.feature.DurabilityOverlay;

// Replaces vanilla's durability bar with the mod's bar and text.
@Mixin(GuiGraphicsExtractor.class)
public class DurabilityTextMixin {
    // Draws the mod's durability bar and text instead of vanilla's bar.
    @Inject(method = "itemBar", at = @At("HEAD"), cancellable = true)
    private void onItemBar(ItemStack itemStack, int x, int y, CallbackInfo callback) {
        if (!OverlayModConfig.enabled) {
            return;
        }
        DurabilityOverlay.render((GuiGraphicsExtractor) (Object) this, Minecraft.getInstance().font, itemStack, x, y);
        callback.cancel();
    }
}
