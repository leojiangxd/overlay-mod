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
import me.shedaniel.autoconfig.AutoConfig;

import triangled.overlaymod.util.BossBarUtil;
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
    private void extractCenteredStatusEffectOverlay(GuiGraphicsExtractor graphics, DeltaTracker tickCounter, CallbackInfo ci) {
        if (statusEffectConfig == null) {
            OverlayModConfig config = AutoConfig.getConfigHolder(OverlayModConfig.class).getConfig();
            statusEffectConfig = config.statusEffects;
        }

        if (minecraft.player == null || !statusEffectConfig.showStatusEffects) {
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
        int effectWidth = statusEffectConfig.effectWidth;
        int bossBarOffset = BossBarUtil.getBossBarOffset(graphics, minecraft);
        int statusEffectOffsetY = bossBarOffset -
                (bossBarOffset + statusEffectConfig.bossBarInitialYOffset > 0 ?
                        statusEffectConfig.bossBarInitialYOffset : 0) + statusEffectConfig.statusEffectYOffset;
        List<Runnable> renderTasks = new ArrayList<>();

        if (statusEffectConfig.separateNegativeEffects) {
            List<MobEffectInstance> beneficialEffects = effects.stream()
                    .filter(effect -> effect.getEffect().value().isBeneficial())
                    .collect(Collectors.toList());

            List<MobEffectInstance> nonBeneficialEffects = effects.stream()
                    .filter(effect -> !effect.getEffect().value().isBeneficial())
                    .collect(Collectors.toList());

            int beneficialOffsetX = calculateOffsetX(screenWidth, beneficialEffects.size(), effectWidth) + 2;
            int nonBeneficialOffsetX = calculateOffsetX(screenWidth, nonBeneficialEffects.size(), effectWidth) + 2;

            int nonBeneficialOffsetY = beneficialEffects.isEmpty()
                    ? statusEffectOffsetY
                    : statusEffectConfig.negativeEffectYOffset + statusEffectOffsetY;

            renderEffects(minecraft, graphics, beneficialEffects, beneficialOffsetX, statusEffectOffsetY, renderTasks);
            renderEffects(minecraft, graphics, nonBeneficialEffects, nonBeneficialOffsetX, nonBeneficialOffsetY, renderTasks);
            renderTimers(minecraft, graphics, beneficialEffects, beneficialOffsetX, statusEffectOffsetY, renderTasks);
            renderTimers(minecraft, graphics, nonBeneficialEffects, nonBeneficialOffsetX, nonBeneficialOffsetY, renderTasks);
        } else {
            int combinedOffsetX = calculateOffsetX(screenWidth, effects.size(), effectWidth) + 2;

            renderEffects(minecraft, graphics, effects, combinedOffsetX, statusEffectOffsetY, renderTasks);
            renderTimers(minecraft, graphics, effects, combinedOffsetX, statusEffectOffsetY, renderTasks);
        }

        renderTasks.forEach(Runnable::run);
        ci.cancel();
    }

    @Unique
    private int calculateOffsetX(int screenWidth, int effectCount, int effectWidth) {
        int totalEffectsWidth = effectCount * effectWidth;
        return (screenWidth - totalEffectsWidth) / 2;
    }

    @Unique
    private void renderEffects(Minecraft client, GuiGraphicsExtractor graphics, List<MobEffectInstance> effects,
                                int OffsetX, int verticalOffset, List<Runnable> renderTasks) {
        for (int i = 0; i < effects.size(); i++) {
            MobEffectInstance statusEffectInstance = effects.get(i);
            Holder<MobEffect> effectHolder = statusEffectInstance.getEffect();
            int currentX = OffsetX + i * statusEffectConfig.effectWidth;
            int currentY = verticalOffset;

            if (client.isDemo()) {
                currentY += 15;
            }

            float f = 1.0F;
            int finalY = currentY;
            if (statusEffectInstance.endsWithin((statusEffectConfig.expirationDuration + 1) * 20)) {
                int m = statusEffectInstance.getDuration();
                int n = 10 - m / 20;
                f = Mth.clamp((float) m / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
                        + Mth.cos((float) m * (float) Math.PI / 5.0F) * Mth.clamp((float) n / 10.0F * 0.25F, 0.0F, 0.25F);
            }

            if (statusEffectConfig.renderBackground) {
                if (statusEffectInstance.isAmbient()) {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("hud/effect_background_ambient"), currentX, finalY, 24, 24);
                } else {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("hud/effect_background"), currentX, finalY, 24, 24);
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
            int currentX = OffsetX + i * statusEffectConfig.effectWidth;
            int currentY = verticalOffset - 3;

            if (client.isDemo()) {
                currentY += 15;
            }

            int finalY = currentY;
            renderTasks.add(() -> {
                if (statusEffectConfig.renderAmplifier) {
                    String amplifier = replaceAnd(statusEffectInstance.getAmplifier() > 0
                            ? (statusEffectConfig.superScriptAmplifiers
                            ? convertToSuperscript(String.valueOf(statusEffectInstance.getAmplifier() + 1))
                            : String.valueOf(statusEffectInstance.getAmplifier() + 1))
                            : "");
                    if (!amplifier.isEmpty()) {
                        int amplifierLength = client.font.width(amplifier);
                        int amplifierX = currentX + (24 - amplifierLength) / 2;
                        int amplifierY = finalY + 11;

                        graphics.pose().pushMatrix();
                        graphics.pose().translate(amplifierX + amplifierLength / 2.0f, amplifierY + client.font.lineHeight / 2.0f);
                        graphics.pose().scale(statusEffectConfig.amplifierScale, statusEffectConfig.amplifierScale);
                        graphics.pose().translate(statusEffectConfig.amplifierXOffset, statusEffectConfig.amplifierYOffset);
                        graphics.pose().translate(-(amplifierX + amplifierLength / 2.0f), -(amplifierY + client.font.lineHeight / 2.0f));

                        graphics.text(client.font, amplifier, amplifierX - 1, amplifierY, 0xFF000000, false);
                        graphics.text(client.font, amplifier, amplifierX + 1, amplifierY, 0xFF000000, false);
                        graphics.text(client.font, amplifier, amplifierX, amplifierY - 1, 0xFF000000, false);
                        graphics.text(client.font, amplifier, amplifierX, amplifierY + 1, 0xFF000000, false);
                        String finalAmplifier = replaceAnd(statusEffectInstance.isAmbient() ? statusEffectConfig.ambientAmplifierText : statusEffectConfig.amplifierText) + amplifier;
                        graphics.text(client.font, finalAmplifier, amplifierX, amplifierY, 0xFFFFFFFF, false);

                        graphics.pose().popMatrix();
                    }
                }

                if (statusEffectConfig.renderDuration) {
                    String duration = replaceAnd(statusEffectConfig.durationText + getDurationAsString(statusEffectInstance));
                    int durationLength = client.font.width(duration);
                    int durationX = currentX + (24 - durationLength) / 2;
                    int durationY = finalY + 26;

                    graphics.pose().pushMatrix();
                    graphics.pose().translate(durationX + durationLength / 2.0f, durationY + client.font.lineHeight / 2.0f);
                    graphics.pose().scale(statusEffectConfig.durationScale, statusEffectConfig.durationScale);
                    graphics.pose().translate(statusEffectConfig.durationXOffset, statusEffectConfig.durationYOffset);
                    graphics.pose().translate(-(durationX + durationLength / 2.0f), -(durationY + client.font.lineHeight / 2.0f));

                    graphics.text(client.font, duration, durationX, durationY, 0xFFFFFFFF, true);

                    graphics.pose().popMatrix();
                }
            });
        }
    }

    @Unique
    private String getDurationAsString(MobEffectInstance effect) {
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
