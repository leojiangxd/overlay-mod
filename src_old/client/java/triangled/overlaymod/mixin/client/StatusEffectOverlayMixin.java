package triangled.overlaymod.mixin.client;

import com.google.common.collect.Ordering;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.texture.StatusEffectSpriteManager;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import net.minecraft.util.Identifier;
import me.shedaniel.autoconfig.AutoConfig;

import triangled.overlaymod.config.OverlayModConfig;

import static triangled.overlaymod.config.OverlayModConfig.replaceAnd;

@Mixin(InGameHud.class)
public class StatusEffectOverlayMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Unique
    private OverlayModConfig.StatusEffectsCategory statusEffectConfig;
    @Unique
    private OverlayModConfig.BossBarCategory scaleBossBarConfig;

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void renderCenteredStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (statusEffectConfig == null) {
            OverlayModConfig config = AutoConfig.getConfigHolder(OverlayModConfig.class).getConfig();
            statusEffectConfig = config.statusEffects;
            scaleBossBarConfig = config.bossbar;
        }

        if (client.player == null || !statusEffectConfig.showStatusEffects) {
            return;
        }

        Collection<StatusEffectInstance> collection = client.player.getStatusEffects();
        if (collection.isEmpty()) {
            return;
        }

        List<StatusEffectInstance> effects = Ordering.natural().sortedCopy(collection)
                .stream()
                .toList();

        int screenWidth = context.getScaledWindowWidth();
        int effectWidth = statusEffectConfig.effectWidth;
        int statusEffectOffsetY = getBossBarOffset(context, client) -
                (getBossBarOffset(context, client) + statusEffectConfig.bossBarInitialYOffset > 0 ?
                        statusEffectConfig.bossBarInitialYOffset : 0) + statusEffectConfig.statusEffectYOffset;
        List<Runnable> renderTasks = new ArrayList<>();

        if (statusEffectConfig.separateNegativeEffects) {
            List<StatusEffectInstance> beneficialEffects = effects.stream()
                    .filter(effect -> effect.getEffectType().value().isBeneficial())
                    .collect(Collectors.toList());

            List<StatusEffectInstance> nonBeneficialEffects = effects.stream()
                    .filter(effect -> !effect.getEffectType().value().isBeneficial())
                    .collect(Collectors.toList());

            int beneficialOffsetX = calculateOffsetX(screenWidth, beneficialEffects.size(), effectWidth) + 2;
            int nonBeneficialOffsetX = calculateOffsetX(screenWidth, nonBeneficialEffects.size(), effectWidth) + 2;

            int nonBeneficialOffsetY = beneficialEffects.isEmpty()
                    ? statusEffectOffsetY
                    : statusEffectConfig.negativeEffectYOffset + statusEffectOffsetY;

            renderEffects(client, context, beneficialEffects, beneficialOffsetX, statusEffectOffsetY, renderTasks);
            renderEffects(client, context, nonBeneficialEffects, nonBeneficialOffsetX, nonBeneficialOffsetY, renderTasks);
            renderTimers(client, context, beneficialEffects, beneficialOffsetX, statusEffectOffsetY, renderTasks);
            renderTimers(client, context, nonBeneficialEffects, nonBeneficialOffsetX, nonBeneficialOffsetY, renderTasks);
        } else {
            int combinedOffsetX = calculateOffsetX(screenWidth, effects.size(), effectWidth) + 2;

            renderEffects(client, context, effects, combinedOffsetX, statusEffectOffsetY, renderTasks);
            renderTimers(client, context, effects, combinedOffsetX, statusEffectOffsetY, renderTasks);
        }

        renderTasks.forEach(Runnable::run);
        ci.cancel();
    }


    @Unique
    private int getBossBarOffset(DrawContext context, MinecraftClient client) {
        int numberOfBossBars;
        BossBarHud bossBarHud = client.inGameHud.getBossBarHud();
        try {
            Method getNumberOfBossBarsMethod = BossBarHud.class.getDeclaredMethod("getNumberOfBossBars");
            getNumberOfBossBarsMethod.setAccessible(true);
            numberOfBossBars = (int) getNumberOfBossBarsMethod.invoke(bossBarHud);
        } catch (Exception e) {
            numberOfBossBars = 0;
        }
        if (numberOfBossBars <= 0)
            return 0;
        int bossBarOffset = 12;
        float scale = scaleBossBarConfig.shouldScaleBossBars ? scaleBossBarConfig.scale : 1.0F;
        int maxHeight = scaleBossBarConfig.shouldScaleBossBars ? scaleBossBarConfig.maxHeight : 3;
        for (int i = 0; i < numberOfBossBars; i++) {
            bossBarOffset += 19;
            if (bossBarOffset >= (context.getScaledWindowHeight() / scale) / maxHeight) {
                break;
            }
        }
        return (int) ((bossBarOffset - 12) * scale);
    }

    @Unique
    private int calculateOffsetX(int screenWidth, int effectCount, int effectWidth) {
        int totalEffectsWidth = effectCount * effectWidth;
        return (screenWidth - totalEffectsWidth) / 2;
    }

    @Unique
    private void renderEffects(MinecraftClient client, DrawContext context, List<StatusEffectInstance> effects,
                               int OffsetX, int verticalOffset, List<Runnable> renderTasks) {
        StatusEffectSpriteManager statusEffectSpriteManager = this.client.getStatusEffectSpriteManager();
        for (int i = 0; i < effects.size(); i++) {
            StatusEffectInstance statusEffectInstance = effects.get(i);
            RegistryEntry<StatusEffect> registryEntry = statusEffectInstance.getEffectType();
            int currentX = OffsetX + i * statusEffectConfig.effectWidth;
            int currentY = verticalOffset;

            if (client.isDemo()) {
                currentY += 15;
            }

            float f = 1.0F;
            int finalY = currentY;
            if (statusEffectInstance.isDurationBelow((statusEffectConfig.expirationDuration + 1) * 20)) {
                int m = statusEffectInstance.getDuration();
                int n = 10 - m / 20;
                f = MathHelper.clamp((float) m / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
                        + MathHelper.cos((float) m * (float) Math.PI / 5.0F) * MathHelper.clamp((float) n / 10.0F * 0.25F, 0.0F, 0.25F);
            }

            if (statusEffectConfig.renderBackground) {
                if (statusEffectInstance.isAmbient()) {
                    context.drawGuiTexture(RenderLayer::getGuiTextured, Identifier.ofVanilla("hud/effect_background_ambient"), currentX, finalY, 24, 24);
                } else {
                    context.drawGuiTexture(RenderLayer::getGuiTextured, Identifier.ofVanilla("hud/effect_background"), currentX, finalY, 24, 24);
                }
            }

            Sprite sprite = statusEffectSpriteManager.getSprite(registryEntry);
            float finalAlpha = f;
            renderTasks.add(() -> {
                int k = ColorHelper.getWhite(finalAlpha);
                context.drawSpriteStretched(RenderLayer::getGuiTextured, sprite, currentX + 3, finalY + 3,  18, 18, k);
            });
        }
    }

    @Unique
    private void renderTimers(MinecraftClient client, DrawContext context, List<StatusEffectInstance> effects,
                              int OffsetX, int verticalOffset, List<Runnable> renderTasks) {
        for (int i = 0; i < effects.size(); i++) {
            StatusEffectInstance statusEffectInstance = effects.get(i);
            int currentX = OffsetX + i * statusEffectConfig.effectWidth;
            int currentY = verticalOffset - 3;

            if (client.isDemo()) {
                currentY += 15;
            }

            int finalY = currentY;
            renderTasks.add(() -> {
                // Draw amplifier
                if (statusEffectConfig.renderAmplifier) {
                    String amplifier = replaceAnd(statusEffectInstance.getAmplifier() > 0
                            ? (statusEffectConfig.superScriptAmplifiers
                            ? convertToSuperscript(String.valueOf(statusEffectInstance.getAmplifier() + 1))
                            : String.valueOf(statusEffectInstance.getAmplifier() + 1))
                            : "");
                    if (!amplifier.isEmpty()) {
                        int amplifierLength = client.textRenderer.getWidth(amplifier);
                        int amplifierX = currentX + (24 - amplifierLength) / 2;
                        int amplifierY = finalY + 11;

                        context.getMatrices().push(); // Save the current matrix state
                        context.getMatrices().translate(amplifierX + amplifierLength / 2.0, amplifierY + client.textRenderer.fontHeight / 2.0, 0);
                        context.getMatrices().scale(statusEffectConfig.amplifierScale, statusEffectConfig.amplifierScale, 1.0f);
                        context.getMatrices().translate(statusEffectConfig.amplifierXOffset, statusEffectConfig.amplifierYOffset, 0);
                        context.getMatrices().translate(-(amplifierX + amplifierLength / 2.0), -(amplifierY + client.textRenderer.fontHeight / 2.0), 0);

                        context.drawText(client.textRenderer, amplifier, amplifierX - 1, amplifierY, 0xFF000000, false);
                        context.drawText(client.textRenderer, amplifier, amplifierX + 1, amplifierY, 0xFF000000, false);
                        context.drawText(client.textRenderer, amplifier, amplifierX, amplifierY - 1, 0xFF000000, false);
                        context.drawText(client.textRenderer, amplifier, amplifierX, amplifierY + 1, 0xFF000000, false);
                        amplifier = replaceAnd(statusEffectInstance.isAmbient() ? statusEffectConfig.ambientAmplifierText : statusEffectConfig.amplifierText) + amplifier;
                        context.drawText(client.textRenderer, amplifier, amplifierX, amplifierY, 0xFFFFFFFF, false);

                        context.getMatrices().pop();
                    }
                }

                // Draw Duration
                if (statusEffectConfig.renderDuration) {
                    String duration = replaceAnd(statusEffectConfig.durationText + getDurationAsString(statusEffectInstance));
                    int durationLength = client.textRenderer.getWidth(duration);
                    int durationX = currentX + (24 - durationLength) / 2;
                    int durationY = finalY + 26;

                    context.getMatrices().push();
                    context.getMatrices().translate(durationX + durationLength / 2.0, durationY + client.textRenderer.fontHeight / 2.0, 0);
                    context.getMatrices().scale(statusEffectConfig.durationScale, statusEffectConfig.durationScale, 1.0f);
                    context.getMatrices().translate(statusEffectConfig.durationXOffset, statusEffectConfig.durationYOffset, 0);
                    context.getMatrices().translate(-(durationX + durationLength / 2.0), -(durationY + client.textRenderer.fontHeight / 2.0), 0);

                    context.drawText(client.textRenderer, duration, durationX, durationY, 0xFFFFFFFF, true);

                    context.getMatrices().pop();
                }
            });
        }
    }

    @Unique
    private String getDurationAsString(StatusEffectInstance effect) {
        long totalSeconds = effect.getDuration() / 20;
        String ambientColor = effect.isAmbient() ? String.valueOf(statusEffectConfig.ambientDurationText) : "";

        if (effect.getDuration() <= -1) {
            return ambientColor + "∞";
        } else if (totalSeconds / (86400 * 99) > 0) {
            return "";
        } else if (totalSeconds / 86400 > 0) {
            return ambientColor + totalSeconds / 86400 + statusEffectConfig.dayText;
        } else if (totalSeconds / 3600 > 0) {
            return ambientColor + totalSeconds / 3600 + statusEffectConfig.hourText;
        } else if ((totalSeconds % 3600) / 60 > 0) {
            return ambientColor + String.format("%d:%02d", (totalSeconds % 3600) / 60, totalSeconds % 60);
        } else {
            return totalSeconds < (statusEffectConfig.expirationDuration + 1)
                    ? ambientColor + statusEffectConfig.expirationText + String.format("0:%02d", totalSeconds % 60)
                    : ambientColor + String.format("0:%02d", totalSeconds % 60);
        }
    }

    @Unique
    private String convertToSuperscript(String input) {
        String[] superscriptDigits = {"⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹"};
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                int digit = c - '0';
                result.append(superscriptDigits[digit]);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}