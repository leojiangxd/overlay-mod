package triangled.overlaymod.client.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import triangled.overlaymod.client.hud.StatusEffectsHud;

@Mixin(Hud.class)
public class HideVanillaEffectsMixin {
    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void hideVanillaEffects(GuiGraphicsExtractor graphics, DeltaTracker tickCounter, CallbackInfo ci) {
        if (StatusEffectsHud.replacesVanilla()) {
            ci.cancel();
        }
    }
}
