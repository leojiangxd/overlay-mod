package triangled.overlaymod.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import triangled.overlaymod.client.OverlayModConfig;
import triangled.overlaymod.client.feature.BarrierVisibility;

// Stops the floating barrier markers while barriers are already shown.
@Mixin(ClientLevel.class)
public abstract class BarrierParticlesMixin {
    // Skips barrier markers when the hide-particles option is on.
    @ModifyReturnValue(method = "getMarkerParticleTarget", at = @At("RETURN"))
    private Block overlayMod$hideBarrierMarkers(Block original) {
        if (original == Blocks.BARRIER && OverlayModConfig.barrierHideParticles
                && BarrierVisibility.isVisible()) {
            return null;
        }
        return original;
    }
}
