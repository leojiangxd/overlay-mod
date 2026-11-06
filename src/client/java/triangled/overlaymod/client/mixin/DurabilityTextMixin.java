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

@Mixin(GuiGraphicsExtractor.class)
public class DurabilityTextMixin {
    @Inject(method = "itemBar", at = @At("HEAD"), cancellable = true)
    private void onItemBar(ItemStack itemStack, int x, int y, CallbackInfo ci) {
        if (!OverlayModConfig.enabled) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        DurabilityOverlay.render((GuiGraphicsExtractor) (Object) this, mc.font, itemStack, x, y);
        ci.cancel();
    }
}
