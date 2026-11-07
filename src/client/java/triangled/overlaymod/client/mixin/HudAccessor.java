package triangled.overlaymod.client.mixin;

import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.BossHealthOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Lets the mod read vanilla's boss bar overlay from the HUD.
@Mixin(Hud.class)
public interface HudAccessor {
    @Accessor("bossOverlay")
    BossHealthOverlay getBossOverlayField();
}
