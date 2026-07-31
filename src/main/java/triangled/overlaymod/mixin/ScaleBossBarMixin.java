package triangled.overlaymod.mixin;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import triangled.overlaymod.config.OverlayModConfig;

import java.util.Map;
import java.util.UUID;

@Mixin(BossHealthOverlay.class)
public abstract class ScaleBossBarMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Final
    @Shadow
    private Map<UUID, LerpingBossEvent> events;

    @Shadow
    abstract void extractBar(GuiGraphicsExtractor graphics, int x, int y, BossEvent bossEvent);

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void onExtractRenderState(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        OverlayModConfig.BossBarCategory config =
                AutoConfig.getConfigHolder(OverlayModConfig.class).getConfig().bossbar;

        if (config.shouldScaleBossBars) {
            extractScaledBossBars(graphics, config);
            ci.cancel();
        }
    }

    @Unique
    private void extractScaledBossBars(GuiGraphicsExtractor graphics, OverlayModConfig.BossBarCategory config) {
        if (this.events.isEmpty()) {
            return;
        }

        Matrix3x2fStack matrixStack = graphics.pose();
        matrixStack.pushMatrix();

        float centerX = graphics.guiWidth() / 2.0F;
        matrixStack.translate(centerX, 0.0F);
        float scale = config.scale;
        matrixStack.scale(scale, scale);
        matrixStack.translate(-centerX, config.yOffset);

        int screenWidth = graphics.guiWidth();
        int j = 12;

        for (LerpingBossEvent bossEvent : this.events.values()) {
            int x = screenWidth / 2 - 91;
            int y = j;
            extractBar(graphics, x, y, bossEvent);

            Component name = bossEvent.getName();
            int nameWidth = minecraft.font.width(name);
            int nameX = screenWidth / 2 - nameWidth / 2;
            int nameY = y - 9;
            graphics.text(minecraft.font, name, nameX, nameY, 0xFFFFFFFF, true);

            j += 10 + 9;
            if (j >= (graphics.guiHeight() / scale) / config.maxHeight) {
                break;
            }
        }

        matrixStack.popMatrix();
    }
}
