package triangled.overlaymod.mixin.client;

import triangled.overlaymod.config.OverlayModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.entity.boss.BossBar;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.profiler.Profiler;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Mixin(BossBarHud.class)
public abstract class ScaleBossBarMixin {
    OverlayModConfig.BossBarCategory scaleBossBarConfig =
            AutoConfig.getConfigHolder(OverlayModConfig.class).getConfig().bossbar;

    @Final
    @Shadow
    private MinecraftClient client;

    @Final
    @Shadow
    Map<UUID, ClientBossBar> bossBars;

    @Shadow
    abstract void renderBossBar(DrawContext context, int x, int y, BossBar bossBar);

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, CallbackInfo ci) {
        if (scaleBossBarConfig.shouldScaleBossBars) {
            renderScaledBossBars(context);
            ci.cancel();
        }
    }

    @Unique
    private void renderScaledBossBars(DrawContext context) {
        if (!this.bossBars.isEmpty()) {
            MatrixStack matrixStack = context.getMatrices();
            matrixStack.push();
            float centerX = client.getWindow().getScaledWidth() / 2.0F;
            matrixStack.translate(centerX, 0.0F, 0.0F);
            float scale = scaleBossBarConfig.scale;
            matrixStack.scale(scale, scale, 1.0F);
            matrixStack.translate(-centerX, scaleBossBarConfig.yOffset, 0.0F);

            Profiler profiler = Profilers.get();
            profiler.push("bossHealth");
            int i = context.getScaledWindowWidth();
            int j = 12;
            Iterator<ClientBossBar> var4 = bossBars.values().iterator();

            while(var4.hasNext()) {
                ClientBossBar clientBossBar = (ClientBossBar)var4.next();
                int k = i / 2 - 91;
                int l = j;
                renderBossBar(context, k, l, clientBossBar);
                Text text = clientBossBar.getName();
                int m = client.textRenderer.getWidth(text);
                int n = i / 2 - m / 2;
                int o = l - 9;
                context.drawTextWithShadow(this.client.textRenderer, text, n, o, 16777215);
                Objects.requireNonNull(client.textRenderer);
                j += 10 + 9;
                if (j >= (context.getScaledWindowHeight() / scale) / scaleBossBarConfig.maxHeight) {
                    break;
                }
            }

            profiler.pop();
            matrixStack.pop();
        }
    }
}