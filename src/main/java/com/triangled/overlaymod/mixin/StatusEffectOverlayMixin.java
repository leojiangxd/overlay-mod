package com.triangled.overlaymod.mixin;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.MathHelper;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import net.minecraft.util.Identifier;
import me.shedaniel.autoconfig.AutoConfig;

import com.triangled.overlaymod.config.OverlayModConfig;

import static com.triangled.overlaymod.config.OverlayModConfig.replaceAnd;

@Mixin(InGameHud.class)
public class StatusEffectOverlayMixin {
    OverlayModConfig.StatusEffectsCategory statusEffectConfig = AutoConfig.getConfigHolder(OverlayModConfig.class)
            .getConfig().statusEffects;
    private static OverlayModConfig.BossBarCategory scaleBossBarConfig = AutoConfig
            .getConfigHolder(OverlayModConfig.class).getConfig().bossbar;

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void renderCenteredStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter,
            CallbackInfo ci) {
        if (!statusEffectConfig.showStatusEffects) {
            return;
        }

        Collection<StatusEffectInstance> collection = client.player.getStatusEffects();
        if (collection.isEmpty()) return;
        
        java.util.Set<String> effectsToFilter = java.util.Arrays.stream(statusEffectConfig.filteredEffects.trim().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        
        if (!effectsToFilter.isEmpty()) {
            collection = collection.stream()
                    .filter(effect -> {
                        String id = net.minecraft.registry.Registries.STATUS_EFFECT.getId(effect.getEffectType().value()).toString();
                        return !effectsToFilter.contains(id);
                    })
                    .toList();
        
            if (collection.isEmpty()) ci.cancel();
        }
        
        
        RenderSystem.enableBlend();
        List<StatusEffectInstance> effects = Ordering.natural().sortedCopy(collection)
                .stream()
                .toList();
        

        int screenWidth = context.getScaledWindowWidth();
        int effectWidth = statusEffectConfig.effectWidth;
        int statusEffectOffsetY = getBossBarOffset(context, client) -
                (getBossBarOffset(context, client) + statusEffectConfig.bossBarInitialYOffset > 0
                        ? statusEffectConfig.bossBarInitialYOffset
                        : 0)
                + statusEffectConfig.statusEffectYOffset;
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
            renderEffects(client, context, nonBeneficialEffects, nonBeneficialOffsetX, nonBeneficialOffsetY,
                    renderTasks);
            renderTimers(client, context, beneficialEffects, beneficialOffsetX, statusEffectOffsetY, renderTasks);
            renderTimers(client, context, nonBeneficialEffects, nonBeneficialOffsetX, nonBeneficialOffsetY,
                    renderTasks);
        } else {
            int combinedOffsetX = calculateOffsetX(screenWidth, effects.size(), effectWidth) + 2;

            renderEffects(client, context, effects, combinedOffsetX, statusEffectOffsetY, renderTasks);
            renderTimers(client, context, effects, combinedOffsetX, statusEffectOffsetY, renderTasks);
        }

        renderTasks.forEach(Runnable::run);
        RenderSystem.disableBlend();
        ci.cancel();
    }

    private static int getBossBarOffset(DrawContext context, MinecraftClient client) {
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

    private int calculateOffsetX(int screenWidth, int effectCount, int effectWidth) {
        int totalEffectsWidth = effectCount * effectWidth;
        return (screenWidth - totalEffectsWidth) / 2;
    }

    private void renderEffects(MinecraftClient client, DrawContext context, List<StatusEffectInstance> effects,
            int OffsetX, int verticalOffset, List<Runnable> renderTasks) {
        for (int i = 0; i < effects.size(); i++) {
            StatusEffectInstance statusEffectInstance = effects.get(i);
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
                        + MathHelper.cos((float) m * (float) Math.PI / 5.0F)
                                * MathHelper.clamp((float) n / 10.0F * 0.25F, 0.0F, 0.25F);
            }

            if (statusEffectConfig.renderBackground) {
                if (statusEffectInstance.isAmbient()) {
                    context.drawGuiTexture(Identifier.ofVanilla("hud/effect_background_ambient"), currentX, finalY, 24,
                            24);
                } else {
                    context.drawGuiTexture(Identifier.ofVanilla("hud/effect_background"), currentX, finalY, 24, 24);
                }
            }

            Sprite sprite = client.getStatusEffectSpriteManager().getSprite(statusEffectInstance.getEffectType());
            float finalAlpha = f;
            renderTasks.add(() -> {
                context.setShaderColor(1.0F, 1.0F, 1.0F, finalAlpha);
                context.drawSprite(currentX + 3, finalY + 3, 0, 18, 18, sprite);
                context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            });
        }
    }

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
                        context.getMatrices().translate(amplifierX + amplifierLength / 2.0,
                                amplifierY + client.textRenderer.fontHeight / 2.0, 0);
                        context.getMatrices().scale(statusEffectConfig.amplifierScale,
                                statusEffectConfig.amplifierScale, 1.0f);
                        context.getMatrices().translate(statusEffectConfig.amplifierXOffset,
                                statusEffectConfig.amplifierYOffset, 0);
                        context.getMatrices().translate(-(amplifierX + amplifierLength / 2.0),
                                -(amplifierY + client.textRenderer.fontHeight / 2.0), 0);

                        context.drawText(client.textRenderer, amplifier, amplifierX - 1, amplifierY, 0xFF000000, false);
                        context.drawText(client.textRenderer, amplifier, amplifierX + 1, amplifierY, 0xFF000000, false);
                        context.drawText(client.textRenderer, amplifier, amplifierX, amplifierY - 1, 0xFF000000, false);
                        context.drawText(client.textRenderer, amplifier, amplifierX, amplifierY + 1, 0xFF000000, false);
                        amplifier = replaceAnd(
                                statusEffectInstance.isAmbient() ? statusEffectConfig.ambientAmplifierText
                                        : statusEffectConfig.amplifierText)
                                + amplifier;
                        context.drawText(client.textRenderer, amplifier, amplifierX, amplifierY, 0xFFFFFFFF, false);

                        context.getMatrices().pop();
                    }
                }

                // Draw Duration
                if (statusEffectConfig.renderDuration) {
                    String duration = replaceAnd(
                            statusEffectConfig.durationText + getDurationAsString(statusEffectInstance));
                    int durationLength = client.textRenderer.getWidth(duration);
                    int durationX = currentX + (24 - durationLength) / 2;
                    int durationY = finalY + 26;

                    context.getMatrices().push();
                    context.getMatrices().translate(durationX + durationLength / 2.0,
                            durationY + client.textRenderer.fontHeight / 2.0, 0);
                    context.getMatrices().scale(statusEffectConfig.durationScale, statusEffectConfig.durationScale,
                            1.0f);
                    context.getMatrices().translate(statusEffectConfig.durationXOffset,
                            statusEffectConfig.durationYOffset, 0);
                    context.getMatrices().translate(-(durationX + durationLength / 2.0),
                            -(durationY + client.textRenderer.fontHeight / 2.0), 0);

                    context.drawText(client.textRenderer, duration, durationX, durationY, 0xFFFFFFFF, true);

                    context.getMatrices().pop();
                }
            });
        }
    }

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

    private String convertToSuperscript(String input) {
        String[] superscriptDigits = { "⁰", "¹", "²", "³", "⁴", "⁵", "⁶", "⁷", "⁸", "⁹" };
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