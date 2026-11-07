package triangled.overlaymod.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import triangled.overlaymod.client.feature.BarrierVisibility;

// Makes barrier blocks draw like a normal see-through block when shown.
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BarrierVisibilityMixin {
    // Gives access to the block this state belongs to.
    @Shadow
    public abstract Block getBlock();

    // Returns true when this is a barrier and barriers are being shown.
    private boolean overlayMod$isShownBarrier() {
        return BarrierVisibility.isVisible() && this.getBlock() == Blocks.BARRIER;
    }

    // Tells the game to draw barriers with their model instead of nothing.
    @WrapMethod(method = "getRenderShape")
    private RenderShape overlayMod$renderShape(Operation<RenderShape> original) {
        RenderShape result = original.call();
        if (result == RenderShape.INVISIBLE && this.overlayMod$isShownBarrier()) {
            return RenderShape.MODEL;
        }
        return result;
    }

    // Lets light pass through shown barriers so they don't look dark.
    @WrapMethod(method = "propagatesSkylightDown")
    private boolean overlayMod$skylight(Operation<Boolean> original) {
        if (this.overlayMod$isShownBarrier()) {
            return true;
        }
        return original.call();
    }

    // Hides the faces between two touching barriers.
    @WrapMethod(method = "skipRendering")
    private boolean overlayMod$skipFace(BlockState neighbor, Direction direction,
            Operation<Boolean> original) {
        if (this.overlayMod$isShownBarrier() && neighbor.getBlock() == Blocks.BARRIER) {
            return true;
        }
        return original.call(neighbor, direction);
    }
}
