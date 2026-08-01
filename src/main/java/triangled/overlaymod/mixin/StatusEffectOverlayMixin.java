package triangled.overlaymod.mixin;

import com.google.common.collect.Ordering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.Mth;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import net.minecraft.resources.Identifier;

import triangled.overlaymod.OverlayMod;
import triangled.overlaymod.util.BossBarUtil;
import triangled.overlaymod.util.TextRenderUtil;
import triangled.overlaymod.config.OverlayModConfig;

import static triangled.overlaymod.config.OverlayModConfig.replaceAnd;

@Mixin(Hud.class)
public class StatusEffectOverlayMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private OverlayModConfig.StatusEffectsCategory statusEffectConfig;

    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void extractCenteredStatusEffectOverlay(GuiGraphicsExtractor graphics, DeltaTracker tickCounter,
            CallbackInfo ci) {
        if (statusEffectConfig == null) {
            statusEffectConfig = OverlayMod.config.statusEffects;
        }

        if (minecraft.player == null || !statusEffectConfig.visibility.showStatusEffects) {
            return;
        }

        Collection<MobEffectInstance> collection = minecraft.player.getActiveEffects();
        if (collection.isEmpty()) {
            return;
        }

        List<MobEffectInstance> effects = Ordering.natural().sortedCopy(collection)
                .stream()
                .toList();

        int screenWidth = graphics.guiWidth();
        int effectWidth = statusEffectConfig.positioning.effectWidth;
        int bossBarOffset = BossBarUtil.getBossBarOffset(graphics, minecraft);
        int statusEffectOffsetY = bossBarOffset -
                (bossBarOffset + statusEffectConfig.positioning.bossBarInitialYOffset > 0 ? statusEffectConfig.positioning.bossBarInitialYOffset
                        : 0);
        List<Runnable> renderTasks = new ArrayList<>();

        graphics.pose().pushMatrix();
        graphics.pose().translate(statusEffectConfig.positioning.statusEffectXOffset, statusEffectConfig.positioning.statusEffectYOffset);

        if (statusEffectConfig.visibility.separateNegativeEffects) {
            List<MobEffectInstance> beneficialEffects = effects.stream()
                    .filter(effect -> effect.getEffect().value().isBeneficial())
                    .collect(Collectors.toList());

            List<MobEffectInstance> nonBeneficialEffects = effects.stream()
                    .filter(effect -> !effect.getEffect().value().isBeneficial())
                    .collect(Collectors.toList());

            int beneficialOffsetX = calculateOffsetX(screenWidth, beneficialEffects.size(), effectWidth);
            int nonBeneficialOffsetX = calculateOffsetX(screenWidth, nonBeneficialEffects.size(), effectWidth);

            int nonBeneficialOffsetY = beneficialEffects.isEmpty()
                    ? statusEffectOffsetY
                    : statusEffectConfig.positioning.negativeEffectYOffset + statusEffectOffsetY;

            renderEffects(minecraft, graphics, beneficialEffects, beneficialOffsetX, statusEffectOffsetY, renderTasks);
            renderEffects(minecraft, graphics, nonBeneficialEffects, nonBeneficialOffsetX, nonBeneficialOffsetY,
                    renderTasks);
            renderTimers(minecraft, graphics, beneficialEffects, beneficialOffsetX, statusEffectOffsetY, renderTasks);
            renderTimers(minecraft, graphics, nonBeneficialEffects, nonBeneficialOffsetX, nonBeneficialOffsetY,
                    renderTasks);
        } else {
            int combinedOffsetX = calculateOffsetX(screenWidth, effects.size(), effectWidth);

            renderEffects(minecraft, graphics, effects, combinedOffsetX, statusEffectOffsetY, renderTasks);
            renderTimers(minecraft, graphics, effects, combinedOffsetX, statusEffectOffsetY, renderTasks);
        }

        renderTasks.forEach(Runnable::run);
        graphics.pose().popMatrix();
        ci.cancel();
    }

    @Unique
    private static final int EFFECT_ICON_WIDTH = 24;

    @Unique
    private int calculateOffsetX(int screenWidth, int effectCount, int effectWidth) {
        if (effectCount <= 0) {
            return screenWidth / 2;
        }

        int totalEffectsWidth = (effectCount - 1) * effectWidth + EFFECT_ICON_WIDTH;
        return (screenWidth - totalEffectsWidth) / 2;
    }

    @Unique
    private void renderEffects(Minecraft client, GuiGraphicsExtractor graphics, List<MobEffectInstance> effects,
            int OffsetX, int verticalOffset, List<Runnable> renderTasks) {
        for (int i = 0; i < effects.size(); i++) {
            MobEffectInstance statusEffectInstance = effects.get(i);
            Holder<MobEffect> effectHolder = statusEffectInstance.getEffect();
            int currentX = OffsetX + i * statusEffectConfig.positioning.effectWidth;
            int currentY = verticalOffset;

            if (client.isDemo()) {
                currentY += 15;
            }

            float f = 1.0F;
            int finalY = currentY;
            if (statusEffectInstance.endsWithin((statusEffectConfig.text.expirationDuration + 1) * 20)) {
                int m = statusEffectInstance.getDuration();
                int n = 10 - m / 20;
                f = Mth.clamp((float) m / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
                        + Mth.cos((float) m * (float) Math.PI / 5.0F)
                                * Mth.clamp((float) n / 10.0F * 0.25F, 0.0F, 0.25F);
            }

            if (statusEffectConfig.visibility.renderBackground) {
                if (statusEffectInstance.isAmbient()) {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                            Identifier.withDefaultNamespace("hud/effect_background_ambient"), currentX, finalY, 24, 24);
                } else {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                            Identifier.withDefaultNamespace("hud/effect_background"), currentX, finalY, 24, 24);
                }
            }

            Identifier spriteId = Hud.getMobEffectSprite(effectHolder);
            float finalAlpha = f;
            renderTasks.add(() -> {
                int k = ARGB.white(finalAlpha);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, spriteId, currentX + 3, finalY + 3, 18, 18, k);
            });
        }
    }

    @Unique
    private void renderTimers(Minecraft client, GuiGraphicsExtractor graphics, List<MobEffectInstance> effects,
            int OffsetX, int verticalOffset, List<Runnable> renderTasks) {
        for (int i = 0; i < effects.size(); i++) {
            MobEffectInstance statusEffectInstance = effects.get(i);
            int currentX = OffsetX + i * statusEffectConfig.positioning.effectWidth;
            int currentY = verticalOffset - 3;

            if (client.isDemo()) {
                currentY += 15;
            }

            int finalY = currentY;
            renderTasks.add(() -> {
                if (statusEffectConfig.visibility.renderAmplifier) {
                    String amplifier = replaceAnd(statusEffectInstance.getAmplifier() > 0
                            ? (statusEffectConfig.visibility.superScriptAmplifiers
                                    ? convertToSuperscript(String.valueOf(statusEffectInstance.getAmplifier() + 1))
                                    : String.valueOf(statusEffectInstance.getAmplifier() + 1))
                            : "");
                    if (!amplifier.isEmpty()) {
                        int amplifierLength = client.font.width(amplifier);
                        int amplifierX = currentX + (24 - amplifierLength) / 2;
                        int amplifierY = finalY + 11;

                        graphics.pose().pushMatrix();
                        graphics.pose().translate(amplifierX + amplifierLength / 2.0f,
                                amplifierY + client.font.lineHeight / 2.0f);
                        graphics.pose().scale(statusEffectConfig.positioning.amplifierScale, statusEffectConfig.positioning.amplifierScale);
                        graphics.pose().translate(statusEffectConfig.positioning.amplifierXOffset,
                                statusEffectConfig.positioning.amplifierYOffset);
                        graphics.pose().translate(-(amplifierX + amplifierLength / 2.0f),
                                -(amplifierY + client.font.lineHeight / 2.0f));

                        String finalAmplifier = replaceAnd(
                                statusEffectInstance.isAmbient() ? statusEffectConfig.text.ambientAmplifierText
                                        : statusEffectConfig.text.amplifierText)
                                + amplifier;
                        TextRenderUtil.drawText(graphics, client.font, finalAmplifier, amplifierX, amplifierY, 0xFFFFFFFF, statusEffectConfig.style.amplifierTextShadow);

                        graphics.pose().popMatrix();
                    }
                }

                if (statusEffectConfig.visibility.renderDuration) {
                    String duration = replaceAnd(
                            statusEffectConfig.text.durationText + getDurationAsString(statusEffectInstance));
                    int durationLength = client.font.width(duration);
                    int durationX = currentX + (24 - durationLength) / 2;
                    int durationY = finalY + 26;

                    graphics.pose().pushMatrix();
                    graphics.pose().translate(durationX + durationLength / 2.0f,
                            durationY + client.font.lineHeight / 2.0f);
                    graphics.pose().scale(statusEffectConfig.positioning.durationScale, statusEffectConfig.positioning.durationScale);
                    graphics.pose().translate(statusEffectConfig.positioning.durationXOffset, statusEffectConfig.positioning.durationYOffset);
                    graphics.pose().translate(-(durationX + durationLength / 2.0f),
                            -(durationY + client.font.lineHeight / 2.0f));

                    TextRenderUtil.drawText(graphics, client.font, duration, durationX, durationY, 0xFFFFFFFF, statusEffectConfig.style.durationTextShadow);

                    graphics.pose().popMatrix();
                }
            });
        }
    }

    @Unique
    private String getDurationAsString(MobEffectInstance effect) {
        long totalSeconds = effect.getDuration() / 20;
        String ambientColor = effect.isAmbient() ? String.valueOf(statusEffectConfig.text.ambientDurationText) : "";

        if (effect.getDuration() <= -1) {
            return ambientColor + "∞";
        } else if (totalSeconds / (86400 * 99) > 0) {
            return "";
        } else if (totalSeconds / 86400 > 0) {
            return ambientColor + totalSeconds / 86400 + statusEffectConfig.text.dayText;
        } else if (totalSeconds / 3600 > 0) {
            return ambientColor + totalSeconds / 3600 + statusEffectConfig.text.hourText;
        } else if ((totalSeconds % 3600) / 60 > 0) {
            return ambientColor + String.format("%d:%02d", (totalSeconds % 3600) / 60, totalSeconds % 60);
        } else {
            return totalSeconds < (statusEffectConfig.text.expirationDuration + 1)
                    ? ambientColor + statusEffectConfig.text.expirationText + String.format("0:%02d", totalSeconds % 60)
                    : ambientColor + String.format("0:%02d", totalSeconds % 60);
        }
    }

    @Unique
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
