package com.triangled.overlaymod.mixin;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Mixin(InGameHud.class)
public class StatusEffectOverlayMixin {
    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void renderCenteredStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        Collection<StatusEffectInstance> collection = client.player.getStatusEffects();
        if (collection.isEmpty()) {
            return;
        }

        RenderSystem.enableBlend();
        List<StatusEffectInstance> effects = Ordering.natural().sortedCopy(collection)
                .stream()
                .toList();
        List<StatusEffectInstance> beneficialEffects = effects.stream()
                .filter(effect -> effect.getEffectType().value().isBeneficial())
                .collect(Collectors.toList());

        List<StatusEffectInstance> nonBeneficialEffects = effects.stream()
                .filter(effect -> !effect.getEffectType().value().isBeneficial())
                .collect(Collectors.toList());

        int screenWidth = context.getScaledWindowWidth();
        int effectWidth = 29;
        int beneficialStartX = calculateStartX(screenWidth, beneficialEffects.size(), effectWidth) + 2;
        int nonBeneficialStartX = calculateStartX(screenWidth, nonBeneficialEffects.size(), effectWidth) + 2;
        int bossBarOffset = getBossBarOffset(context, client);

        int nonBeneficialOffsetY = beneficialEffects.isEmpty()
                ? 0 + bossBarOffset
                : 32 + bossBarOffset;

        List<Runnable> renderTasks = new ArrayList<>();
        renderEffects(client, context, beneficialEffects, beneficialStartX, 0 + bossBarOffset, renderTasks);
        renderEffects(client, context, nonBeneficialEffects, nonBeneficialStartX, nonBeneficialOffsetY, renderTasks);
        renderTimers(client, context, beneficialEffects, beneficialStartX, 0 + bossBarOffset, renderTasks);
        renderTimers(client, context, nonBeneficialEffects, nonBeneficialStartX, nonBeneficialOffsetY, renderTasks);
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
        int bossBarOffset = 0;
        int maxIterations = Math.min(numberOfBossBars, (int) Math.round((context.getScaledWindowHeight() / 3.0) / 19));

        while (bossBarOffset / 19 + 1 <= maxIterations) {
            bossBarOffset += 19;
        }
        return bossBarOffset;
    }

    private int calculateStartX(int screenWidth, int effectCount, int effectWidth) {
        int totalEffectsWidth = effectCount * effectWidth;
        return (screenWidth - totalEffectsWidth) / 2;
    }

    private void renderEffects(MinecraftClient client, DrawContext context, List<StatusEffectInstance> effects,
                               int startX, int verticalOffset, List<Runnable> renderTasks) {
        for (int i = 0; i < effects.size(); i++) {
            StatusEffectInstance statusEffectInstance = effects.get(i);
            int currentX = startX + i * 29;
            int currentY = verticalOffset;

            if (client.isDemo()) {
                currentY += 15;
            }

            float f = 1.0F;
            int finalY = currentY;
            if (statusEffectInstance.isDurationBelow(200)) {
                int m = statusEffectInstance.getDuration();
                int n = 10 - m / 20;
                f = MathHelper.clamp((float) m / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
                        + MathHelper.cos((float) m * (float) Math.PI / 5.0F) * MathHelper.clamp((float) n / 10.0F * 0.25F, 0.0F, 0.25F);
            }

//            //Draw background
//            if (statusEffectInstance.isAmbient()) {
//                context.drawGuiTexture(Identifier.ofVanilla("hud/effect_background_ambient"), currentX, finalY, 24, 24);
//            } else {
//                context.drawGuiTexture(Identifier.ofVanilla("hud/effect_background"), currentX, finalY, 24, 24);
//            }

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
                               int startX, int verticalOffset, List<Runnable> renderTasks) {
        for (int i = 0; i < effects.size(); i++) {
            StatusEffectInstance statusEffectInstance = effects.get(i);
            int currentX = startX + i * 29;
            int currentY = verticalOffset - 3;

            if (client.isDemo()) {
                currentY += 15;
            }

            int finalY = currentY;
            renderTasks.add(() -> {
                // Draw amplifier
                String amplifier = (statusEffectInstance.getAmplifier() > 0 ? String.valueOf(statusEffectInstance.getAmplifier() + 1) : "");
                if (!amplifier.isEmpty()) {
                    int amplifierLength = client.textRenderer.getWidth(amplifier);
                    int amplifierX = currentX + (24 - amplifierLength) / 2;
                    int amplifierY = finalY + 11;

                    context.getMatrices().push(); // Save the current matrix state
                    context.getMatrices().translate(amplifierX + amplifierLength / 2.0, amplifierY + client.textRenderer.fontHeight / 2.0, 0);
                    context.getMatrices().scale(0.5f, 0.5f, 1.0f);
                    context.getMatrices().translate(0.5, 0, 0);
                    context.getMatrices().translate(-(amplifierX + amplifierLength / 2.0), -(amplifierY + client.textRenderer.fontHeight / 2.0), 0);

                    // Draw the outline by rendering the text slightly offset in all directions
                    context.drawText(client.textRenderer, amplifier, amplifierX - 1, amplifierY, 0xFF000000, false);
                    context.drawText(client.textRenderer, amplifier, amplifierX + 1, amplifierY, 0xFF000000, false);
                    context.drawText(client.textRenderer, amplifier, amplifierX, amplifierY - 1, 0xFF000000, false);
                    context.drawText(client.textRenderer, amplifier, amplifierX, amplifierY + 1, 0xFF000000, false);

                    // Draw the main text
                    amplifier = (statusEffectInstance.isAmbient() ? Formatting.YELLOW : "") + amplifier;
                    context.drawText(client.textRenderer, amplifier, amplifierX, amplifierY, 0xFFFFFFFF, false);

                    context.getMatrices().pop(); // Restore the matrix state
                }

                // Draw Duration
                String duration = getDurationAsString(statusEffectInstance);
                int durationLength = client.textRenderer.getWidth(duration);
                int durationX = currentX + (24 - durationLength) / 2;
                int durationY = finalY + 26;
                context.drawTextWithShadow(client.textRenderer, duration, durationX, durationY, 0xFFFFFFFF);
            });
        }
    }

    private String getDurationAsString(StatusEffectInstance effect) {
        long totalSeconds = effect.getDuration() / 20;
        String formattedDuration = effect.isAmbient() ? String.valueOf(Formatting.YELLOW) : "";

        if (effect.getDuration() <= -1) {
            return formattedDuration + "∞";
        } else if (totalSeconds / (86400 * 99) > 0) {
            return "";
        } else if (totalSeconds / 86400 > 0) {
            return formattedDuration + totalSeconds / 86400 + "ᴅ";
        } else if (totalSeconds / 3600 > 0) {
            return formattedDuration + totalSeconds / 3600 + "ʜ";
        } else if ((totalSeconds % 3600) / 60 > 0) {
            return formattedDuration + String.format("%d:%02d", (totalSeconds % 3600) / 60, totalSeconds % 60);
        } else {
            formattedDuration += String.format("0:%02d", totalSeconds % 60);
            return totalSeconds <= 10 ? Formatting.RED + formattedDuration : formattedDuration;
        }
    }
}