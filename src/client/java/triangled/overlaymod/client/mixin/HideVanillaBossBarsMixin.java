package triangled.overlaymod.client.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import triangled.overlaymod.client.hud.BossBarHud;

// Hides vanilla boss bars when the custom boss bar HUD replaces them.
@Mixin(BossHealthOverlay.class)
public class HideVanillaBossBarsMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void hideVanillaBossBars(GuiGraphicsExtractor graphics, CallbackInfo callback) {
        if (BossBarHud.replacesVanilla()) {
            callback.cancel();
        }
    }
}
