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
import triangled.overlaymod.util.DurationFormatUtil;
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
        int effectStride = EFFECT_ICON_SIZE + statusEffectConfig.positioning.effectGap;
        int bossBarOffset = BossBarUtil.getBossBarOffset(graphics, minecraft);
        float bossBarInitialYOffset = BOSS_BAR_INITIAL_Y_OFFSET + statusEffectConfig.positioning.bossBarInitialYOffset;
        float statusEffectOffsetY = bossBarOffset -
                (bossBarOffset + bossBarInitialYOffset > 0 ? bossBarInitialYOffset : 0);
        List<Runnable> renderTasks = new ArrayList<>();

        graphics.pose().pushMatrix();
        graphics.pose().translate(statusEffectConfig.positioning.statusEffectXOffset,
                -statusEffectConfig.positioning.statusEffectYOffset);

        if (statusEffectConfig.visibility.separateNegativeEffects) {
            List<MobEffectInstance> beneficialEffects = effects.stream()
                    .filter(effect -> effect.getEffect().value().isBeneficial())
                    .collect(Collectors.toList());

            List<MobEffectInstance> nonBeneficialEffects = effects.stream()
                    .filter(effect -> !effect.getEffect().value().isBeneficial())
                    .collect(Collectors.toList());

            float beneficialOffsetX = calculateOffsetX(screenWidth, beneficialEffects.size(), effectStride);
            float nonBeneficialOffsetX = calculateOffsetX(screenWidth, nonBeneficialEffects.size(), effectStride);

            float nonBeneficialOffsetY = beneficialEffects.isEmpty()
                    ? statusEffectOffsetY
                    : statusEffectOffsetY + EFFECT_ICON_SIZE;
            float nonBeneficialGap = beneficialEffects.isEmpty() ? 0f
                    : -statusEffectConfig.positioning.negativeEffectYOffset;

            renderEffects(minecraft, graphics, beneficialEffects, beneficialOffsetX, statusEffectOffsetY, 0f,
                    effectStride, renderTasks);
            renderEffects(minecraft, graphics, nonBeneficialEffects, nonBeneficialOffsetX, nonBeneficialOffsetY,
                    nonBeneficialGap, effectStride, renderTasks);
            renderTimers(minecraft, graphics, beneficialEffects, beneficialOffsetX, statusEffectOffsetY, 0f,
                    effectStride, renderTasks);
            renderTimers(minecraft, graphics, nonBeneficialEffects, nonBeneficialOffsetX, nonBeneficialOffsetY,
                    nonBeneficialGap, effectStride, renderTasks);
        } else {
            float combinedOffsetX = calculateOffsetX(screenWidth, effects.size(), effectStride);

            renderEffects(minecraft, graphics, effects, combinedOffsetX, statusEffectOffsetY, 0f, effectStride,
                    renderTasks);
            renderTimers(minecraft, graphics, effects, combinedOffsetX, statusEffectOffsetY, 0f, effectStride,
                    renderTasks);
        }

        renderTasks.forEach(Runnable::run);
        graphics.pose().popMatrix();
        ci.cancel();
    }

    @Unique
    private static final int EFFECT_ICON_SIZE = 24;

    @Unique
    private static final float BOSS_BAR_INITIAL_Y_OFFSET = -1f;

    @Unique
    private float calculateOffsetX(int screenWidth, int effectCount, int effectStride) {
        if (effectCount <= 0) {
            return screenWidth / 2f;
        }

        int totalEffectsWidth = (effectCount - 1) * effectStride + EFFECT_ICON_SIZE;
        return (screenWidth - totalEffectsWidth) / 2f;
    }

    @Unique
    private void renderEffects(Minecraft client, GuiGraphicsExtractor graphics, List<MobEffectInstance> effects,
            float OffsetX, float verticalOffset, float yGap, int effectStride, List<Runnable> renderTasks) {
        for (int i = 0; i < effects.size(); i++) {
            MobEffectInstance statusEffectInstance = effects.get(i);
            Holder<MobEffect> effectHolder = statusEffectInstance.getEffect();
            float currentX = OffsetX + i * effectStride;
            float currentY = verticalOffset;

            float f = 1.0F;
            float finalY = currentY;
            if (statusEffectInstance.endsWithin((statusEffectConfig.text.expirationDuration + 1) * 20)) {
                int m = statusEffectInstance.getDuration();
                int n = 10 - m / 20;
                f = Mth.clamp((float) m / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
                        + Mth.cos((float) m * (float) Math.PI / 5.0F)
                                * Mth.clamp((float) n / 10.0F * 0.25F, 0.0F, 0.25F);
            }

            int blitX = Math.round(currentX);
            int blitY = Math.round(finalY);

            graphics.pose().pushMatrix();
            graphics.pose().translate(0, yGap);

            if (statusEffectConfig.visibility.renderBackground) {
                if (statusEffectInstance.isAmbient()) {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                            Identifier.withDefaultNamespace("hud/effect_background_ambient"), blitX, blitY, 24, 24);
                } else {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                            Identifier.withDefaultNamespace("hud/effect_background"), blitX, blitY, 24, 24);
                }
            }

            graphics.pose().popMatrix();

            Identifier spriteId = Hud.getMobEffectSprite(effectHolder);
            float finalAlpha = f;
            renderTasks.add(() -> {
                graphics.pose().pushMatrix();
                graphics.pose().translate(0, yGap);
                int k = ARGB.white(finalAlpha);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, spriteId, blitX + 3, blitY + 3, 18, 18, k);
                graphics.pose().popMatrix();
            });
        }
    }

    @Unique
    private void renderTimers(Minecraft client, GuiGraphicsExtractor graphics, List<MobEffectInstance> effects,
            float OffsetX, float verticalOffset, float yGap, int effectStride, List<Runnable> renderTasks) {
        for (int i = 0; i < effects.size(); i++) {
            MobEffectInstance statusEffectInstance = effects.get(i);
            float currentX = OffsetX + i * effectStride;
            float currentY = verticalOffset - 3;

            float finalY = currentY;
            renderTasks.add(() -> {
                graphics.pose().pushMatrix();
                graphics.pose().translate(0, yGap);

                if (statusEffectConfig.visibility.renderAmplifier) {
                    boolean subscriptAmplifiers = statusEffectConfig.visibility.subscriptAmplifiers;
                    String amplifierDigits = String.valueOf(statusEffectInstance.getAmplifier() + 1);
                    String amplifier = replaceAnd(statusEffectInstance.getAmplifier() > 0
                            ? (subscriptAmplifiers ? convertToSubscript(amplifierDigits) : amplifierDigits)
                            : "");
                    if (!amplifier.isEmpty()) {
                        int amplifierLength = client.font.width(amplifier);
                        float scale = statusEffectConfig.positioning.amplifierScale;
                        float styleYOffset = subscriptAmplifiers ? -1.5f : 0f;
                        float amplifierX = currentX + (24 - scale * amplifierLength) / 2f
                                + scale * (0.5f + statusEffectConfig.positioning.amplifierXOffset);
                        float amplifierY = finalY + 11 + (client.font.lineHeight - scale * client.font.lineHeight) / 2f
                                + scale * (styleYOffset - statusEffectConfig.positioning.amplifierYOffset);

                        graphics.pose().pushMatrix();
                        graphics.pose().translate(amplifierX, amplifierY);
                        graphics.pose().scale(scale, scale);

                        String finalAmplifier = replaceAnd(
                                statusEffectInstance.isAmbient() ? statusEffectConfig.text.ambientAmplifierText
                                        : statusEffectConfig.text.amplifierText)
                                + amplifier;
                        int amplifierColor = statusEffectInstance.isAmbient()
                                ? statusEffectConfig.style.ambientAmplifierColor
                                : statusEffectConfig.style.amplifierColor;
                        TextRenderUtil.drawText(graphics, client.font, finalAmplifier, 0, 0, amplifierColor,
                                statusEffectConfig.style.amplifierTextShadow);

                        graphics.pose().popMatrix();
                    }
                }

                if (statusEffectConfig.visibility.renderDuration) {
                    String duration = replaceAnd(
                            statusEffectConfig.text.durationText + getDurationAsString(statusEffectInstance));
                    int durationLength = client.font.width(duration);
                    float scale = statusEffectConfig.positioning.durationScale;
                    float durationX = currentX + (24 - scale * durationLength) / 2f
                            + scale * (0.5f + statusEffectConfig.positioning.durationXOffset);
                    float durationY = finalY + 26 + (client.font.lineHeight - scale * client.font.lineHeight) / 2f
                            + scale * (1.0f - statusEffectConfig.positioning.durationYOffset);

                    graphics.pose().pushMatrix();
                    graphics.pose().translate(durationX, durationY);
                    graphics.pose().scale(scale, scale);

                    int durationColor = isExpiringSoon(statusEffectInstance)
                            ? statusEffectConfig.style.expirationColor
                            : statusEffectInstance.isAmbient()
                                    ? statusEffectConfig.style.ambientDurationColor
                                    : statusEffectConfig.style.durationColor;
                    TextRenderUtil.drawText(graphics, client.font, duration, 0, 0, durationColor,
                            statusEffectConfig.style.durationTextShadow);

                    graphics.pose().popMatrix();
                }

                graphics.pose().popMatrix();
            });
        }
    }

    @Unique
    private boolean isExpiringSoon(MobEffectInstance effect) {
        long totalSeconds = effect.getDuration() / 20;
        if (effect.getDuration() <= -1)
            return false;
        if (totalSeconds / (86400 * 99) > 0)
            return false;
        if (totalSeconds / 86400 > 0)
            return false;
        if (totalSeconds / 3600 > 0)
            return false;
        if ((totalSeconds % 3600) / 60 > 0)
            return false;
        return totalSeconds < (statusEffectConfig.text.expirationDuration + 1);
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
            return ambientColor + DurationFormatUtil.format(totalSeconds, statusEffectConfig.text.dayFormat);
        } else if (totalSeconds / 3600 > 0) {
            return ambientColor + DurationFormatUtil.format(totalSeconds, statusEffectConfig.text.hourFormat);
        } else if (totalSeconds / 60 >= 10) {
            return ambientColor + DurationFormatUtil.format(totalSeconds, statusEffectConfig.text.minutesFormatLong);
        } else if (totalSeconds / 60 > 0) {
            return ambientColor + DurationFormatUtil.format(totalSeconds, statusEffectConfig.text.minutesFormat);
        } else {
            return totalSeconds < (statusEffectConfig.text.expirationDuration + 1)
                    ? ambientColor + statusEffectConfig.text.expirationText
                            + DurationFormatUtil.format(totalSeconds, statusEffectConfig.text.secondsFormat)
                    : ambientColor + DurationFormatUtil.format(totalSeconds, statusEffectConfig.text.secondsFormat);
        }
    }

    @Unique
    private String convertToSubscript(String input) {
        String[] subscriptDigits = { "₀", "₁", "₂", "₃", "₄", "₅", "₆", "₇", "₈", "₉" };
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                int digit = c - '0';
                result.append(subscriptDigits[digit]);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

}
