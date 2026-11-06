package triangled.overlaymod.client.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.polyfrost.compose.render.PolyColor;
import org.polyfrost.oneconfig.api.config.v1.annotations.Color;
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown;
import org.polyfrost.oneconfig.api.config.v1.annotations.Number;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.config.v1.annotations.Text;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud;
import triangled.overlaymod.client.utils.BossBarUtil;
import triangled.overlaymod.client.utils.DurationFormatUtil;
import triangled.overlaymod.client.utils.FormatCodes;
import triangled.overlaymod.client.utils.TextAlignment;
import triangled.overlaymod.client.utils.TextRenderUtil;
import triangled.overlaymod.client.utils.TextShadow;

public class StatusEffectsHud extends LegacyHud {
    private static final int ICON_SIZE = 24;
    private static final Identifier EFFECT_BACKGROUND = Identifier.withDefaultNamespace("hud/effect_background");
    private static final Identifier EFFECT_BACKGROUND_AMBIENT = Identifier.withDefaultNamespace("hud/effect_background_ambient");

    // ---------------------------------------------------------------- Visibility

    @Switch(title = "Hide Vanilla Effects", description = "Hide the vanilla effect icons in the top-right corner while this HUD is shown",
            subcategory = "Visibility")
    public boolean hideVanilla = true;

    @Switch(title = "Move Below Boss Bars", description = "Move the HUD down while boss bars above it would overlap it",
            subcategory = "Visibility")
    public boolean belowBossBars = true;

    @Switch(title = "Render Background", description = "Draw the vanilla effect frame behind each icon",
            subcategory = "Visibility")
    public boolean renderBackground = false;

    @Switch(title = "Separate Negative Effects", description = "Put harmful effects on their own row below beneficial ones",
            subcategory = "Visibility")
    public boolean separateNegative = true;

    @Switch(title = "Show Amplifier", description = "Show the effect level on the icon",
            subcategory = "Visibility")
    public boolean renderAmplifier = true;

    @Switch(title = "Subscript Amplifier", description = "Draw the effect level using subscript digits",
            subcategory = "Visibility")
    public boolean subscriptAmplifier = true;

    @Switch(title = "Show Duration", description = "Show the time remaining under each icon",
            subcategory = "Visibility")
    public boolean renderDuration = true;

    // ---------------------------------------------------------------- Text

    @Text(title = "Seconds Format", description = "Under a minute. d/h/m/s are replaced, repeat a letter to zero-pad, \\ escapes",
            subcategory = "Text")
    public String secondsFormat = "0:ss";

    @Text(title = "Minutes Format", description = "Under ten minutes", subcategory = "Text")
    public String minutesFormat = "m:ss";

    @Text(title = "Long Minutes Format", description = "Ten minutes up to an hour", subcategory = "Text")
    public String minutesFormatLong = "m\\m";

    @Text(title = "Hours Format", description = "An hour up to a day", subcategory = "Text")
    public String hourFormat = "h\\h";

    @Text(title = "Days Format", description = "A day or longer", subcategory = "Text")
    public String dayFormat = "d\\d";

    @Text(title = "Amplifier Prefix", description = "Text before the level. & starts a formatting code, \\& is a literal &",
            subcategory = "Text")
    public String amplifierText = "&f";

    @Text(title = "Ambient Amplifier Prefix", description = "Text before the level of a beacon/conduit effect",
            subcategory = "Text")
    public String ambientAmplifierText = "&e";

    @Text(title = "Duration Prefix", description = "Text before the duration", subcategory = "Text")
    public String durationText = "&f";

    @Text(title = "Ambient Duration Prefix", description = "Text before the duration of a beacon/conduit effect",
            subcategory = "Text")
    public String ambientDurationText = "&e";

    @Text(title = "Expiring Prefix", description = "Text before the duration once the effect is about to run out",
            subcategory = "Text")
    public String expirationText = "&c";

    @Slider(title = "Expiring Threshold", description = "Seconds left at which an effect counts as expiring and starts blinking",
            min = 0, max = 59, step = 1, subcategory = "Text")
    public int expirationDuration = 10;

    // ---------------------------------------------------------------- Style

    @Dropdown(title = "Amplifier Shadow", description = "How the level text is shadowed or outlined", subcategory = "Style")
    public TextShadow amplifierTextShadow = TextShadow.FOUR_DIRECTION;

    @Dropdown(title = "Duration Shadow", description = "How the duration text is shadowed or outlined", subcategory = "Style")
    public TextShadow durationTextShadow = TextShadow.SHADOW;

    @Color(title = "Amplifier Color", subcategory = "Style")
    public PolyColor amplifierColor = new PolyColor(0xFFFFFFFF, false, 1.0F);

    @Color(title = "Ambient Amplifier Color", subcategory = "Style")
    public PolyColor ambientAmplifierColor = new PolyColor(0xFFFFFFFF, false, 1.0F);

    @Color(title = "Duration Color", subcategory = "Style")
    public PolyColor durationColor = new PolyColor(0xFFFFFFFF, false, 1.0F);

    @Color(title = "Ambient Duration Color", subcategory = "Style")
    public PolyColor ambientDurationColor = new PolyColor(0xFFFFFFFF, false, 1.0F);

    @Color(title = "Expiring Color", subcategory = "Style")
    public PolyColor expirationColor = new PolyColor(0xFFFFFFFF, false, 1.0F);

    // ---------------------------------------------------------------- Layout

    @Dropdown(title = "Row Alignment", description = "How a shorter row lines up against the longer one", subcategory = "Layout")
    public TextAlignment rowAlignment = TextAlignment.MIDDLE;

    @Slider(title = "Effect Gap", description = "Space between icons", min = -8, max = 16, step = 1, subcategory = "Layout")
    public int effectGap = 1;

    @Number(title = "Row Gap", description = "Space between the beneficial and harmful rows",
            min = -24, max = 64, subcategory = "Layout")
    public float rowGap = 8.0F;

    @Number(title = "Amplifier Scale", min = 0.1F, max = 4, subcategory = "Layout")
    public float amplifierScale = 1.0F;

    @Number(title = "Amplifier X Offset", min = -32, max = 32, subcategory = "Layout")
    public float amplifierXOffset = 0.0F;

    @Number(title = "Amplifier Y Offset", description = "Positive moves up", min = -32, max = 32, subcategory = "Layout")
    public float amplifierYOffset = 0.0F;

    @Number(title = "Duration Scale", min = 0.1F, max = 4, subcategory = "Layout")
    public float durationScale = 1.0F;

    @Number(title = "Duration X Offset", min = -32, max = 32, subcategory = "Layout")
    public float durationXOffset = 0.0F;

    @Number(title = "Duration Y Offset", description = "Positive moves up", min = -32, max = 32, subcategory = "Layout")
    public float durationYOffset = 0.0F;

    public StatusEffectsHud() {
        super("overlaymod_status_effects.yml", "Status Effects", Hud.Category.Companion.getPLAYER());
    }

    public static boolean replacesVanilla() {
        for (Hud hud : HudManager.INSTANCE.getActiveInstances()) {
            if (hud instanceof StatusEffectsHud effects && effects.hideVanilla && !effects.getHidden()) {
                return true;
            }
        }
        return false;
    }

    private record Rows(List<MobEffectInstance> top, List<MobEffectInstance> bottom) {
        boolean isEmpty() {
            return top.isEmpty() && bottom.isEmpty();
        }
    }

    private Rows rows() {
        List<MobEffectInstance> effects = activeEffects();
        if (effects.isEmpty() && HudManager.INSTANCE.isEditing()) {
            effects = previewEffects();
        }
        if (!separateNegative) {
            return new Rows(effects, List.of());
        }

        List<MobEffectInstance> beneficial = new ArrayList<>();
        List<MobEffectInstance> harmful = new ArrayList<>();
        for (MobEffectInstance effect : effects) {
            (effect.getEffect().value().isBeneficial() ? beneficial : harmful).add(effect);
        }
        return beneficial.isEmpty() ? new Rows(harmful, List.of()) : new Rows(beneficial, harmful);
    }

    private static List<MobEffectInstance> activeEffects() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return List.of();
        }
        List<MobEffectInstance> effects = new ArrayList<>();
        for (MobEffectInstance effect : mc.player.getActiveEffects()) {
            if (effect.showIcon()) {
                effects.add(effect);
            }
        }
        Collections.sort(effects);
        return effects;
    }

    private static List<MobEffectInstance> previewEffects() {
        List<MobEffectInstance> effects = new ArrayList<>(List.of(
                new MobEffectInstance(MobEffects.SPEED, 20 * 185, 1),
                new MobEffectInstance(MobEffects.STRENGTH, 20 * 45),
                new MobEffectInstance(MobEffects.REGENERATION, 20 * 8, 0, true, true),
                new MobEffectInstance(MobEffects.POISON, 20 * 12, 2)));
        Collections.sort(effects);
        return effects;
    }

    private int stride() {
        return ICON_SIZE + effectGap;
    }

    private float rowWidth(int count) {
        return count <= 0 ? 0 : (count - 1) * stride() + ICON_SIZE;
    }

    private float bottomRowY() {
        return ICON_SIZE + rowGap;
    }

    private float durationY() {
        float lineHeight = Minecraft.getInstance().font.lineHeight;
        return 23 + (lineHeight - durationScale * lineHeight) / 2f + durationScale * (1.0f - durationYOffset);
    }

    private float iconRowsWidth(Rows rows) {
        return Math.max(ICON_SIZE, Math.max(rowWidth(rows.top().size()), rowWidth(rows.bottom().size())));
    }

    private float[] bounds(Rows rows) {
        Font font = Minecraft.getInstance().font;
        float width = iconRowsWidth(rows);
        float[] bounds = {Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        includeRow(bounds, font, rows.top(), rowX(width, rows.top().size()), 0);
        includeRow(bounds, font, rows.bottom(), rowX(width, rows.bottom().size()), bottomRowY());
        if (bounds[0] > bounds[2]) {
            return new float[] {0, 0, ICON_SIZE, ICON_SIZE};
        }
        return bounds;
    }

    private void includeRow(float[] bounds, Font font, List<MobEffectInstance> effects, float x, float y) {
        int inset = renderBackground ? 0 : 3;
        for (int i = 0; i < effects.size(); i++) {
            MobEffectInstance effect = effects.get(i);
            float effectX = x + i * stride();
            include(bounds, new float[] {effectX + inset, y + inset, effectX + ICON_SIZE - inset, y + ICON_SIZE - inset});
            if (renderAmplifier && effect.getAmplifier() > 0) {
                String text = amplifierString(effect);
                include(bounds, TextRenderUtil.textBounds(font, font.width(text), effectX + amplifierX(font, text),
                        y + amplifierY(font), amplifierScale, amplifierTextShadow));
            }
            if (renderDuration) {
                String text = durationLabel(effect);
                include(bounds, TextRenderUtil.textBounds(font, font.width(text), effectX + durationX(font, text),
                        y + durationY(), durationScale, durationTextShadow));
            }
        }
    }

    private static void include(float[] bounds, float[] box) {
        bounds[0] = Math.min(bounds[0], box[0]);
        bounds[1] = Math.min(bounds[1], box[1]);
        bounds[2] = Math.max(bounds[2], box[2]);
        bounds[3] = Math.max(bounds[3], box[3]);
    }

    @Override
    public float getWidth() {
        float[] bounds = bounds(rows());
        return bounds[2] - bounds[0];
    }

    @Override
    public float getHeight() {
        float[] bounds = bounds(rows());
        return bounds[3] - bounds[1];
    }

    @Override
    public boolean shouldShow() {
        return !rows().isEmpty();
    }

    @Override
    public boolean update() {
        return false;
    }

    @Override
    public boolean multipleInstancesAllowed() {
        return false;
    }

    @Override
    public Pair<Float, Float> defaultPosition() {
        return new Pair<>((HudManager.guiScreenWidth - getScaledWidth()) / 2f, 4.0F);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics) {
        Rows rows = rows();
        if (rows.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        float width = iconRowsWidth(rows);
        float[] bounds = bounds(rows);
        float bossBarOffset = belowBossBars && !HudManager.INSTANCE.isEditing() ? BossBarUtil.pushDown(this) : 0f;

        graphics.pose().pushMatrix();
        graphics.pose().translate(-bounds[0], -bounds[1] + bossBarOffset);

        float topX = rowX(width, rows.top().size());
        float bottomX = rowX(width, rows.bottom().size());
        drawBackgrounds(graphics, rows.top(), topX, 0);
        drawBackgrounds(graphics, rows.bottom(), bottomX, bottomRowY());
        drawIcons(graphics, rows.top(), topX, 0);
        drawIcons(graphics, rows.bottom(), bottomX, bottomRowY());
        drawText(graphics, mc.font, rows.top(), topX, 0);
        drawText(graphics, mc.font, rows.bottom(), bottomX, bottomRowY());

        graphics.pose().popMatrix();
    }

    private float rowX(float width, int count) {
        float slack = width - rowWidth(count);
        return switch (rowAlignment) {
            case LEFT -> 0;
            case MIDDLE -> slack / 2f;
            case RIGHT -> slack;
        };
    }

    private void drawBackgrounds(GuiGraphicsExtractor graphics, List<MobEffectInstance> effects, float x, float y) {
        if (!renderBackground) {
            return;
        }
        for (int i = 0; i < effects.size(); i++) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(x + i * stride(), y);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                    effects.get(i).isAmbient() ? EFFECT_BACKGROUND_AMBIENT : EFFECT_BACKGROUND, 0, 0, ICON_SIZE, ICON_SIZE);
            graphics.pose().popMatrix();
        }
    }

    private void drawIcons(GuiGraphicsExtractor graphics, List<MobEffectInstance> effects, float x, float y) {
        for (int i = 0; i < effects.size(); i++) {
            MobEffectInstance effect = effects.get(i);
            Identifier sprite = net.minecraft.client.gui.Hud.getMobEffectSprite(effect.getEffect());

            graphics.pose().pushMatrix();
            graphics.pose().translate(x + i * stride() + 3, y + 3);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, 0, 0, 18, 18, ARGB.white(iconAlpha(effect)));
            graphics.pose().popMatrix();
        }
    }

    private float iconAlpha(MobEffectInstance effect) {
        if (!effect.endsWithin((expirationDuration + 1) * 20)) {
            return 1.0F;
        }
        int duration = effect.getDuration();
        int n = 10 - duration / 20;
        return Mth.clamp(duration / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
                + Mth.cos(duration * (float) Math.PI / 5.0F) * Mth.clamp(n / 10.0F * 0.25F, 0.0F, 0.25F);
    }

    private void drawText(GuiGraphicsExtractor graphics, Font font, List<MobEffectInstance> effects, float x, float y) {
        for (int i = 0; i < effects.size(); i++) {
            MobEffectInstance effect = effects.get(i);
            float effectX = x + i * stride();
            if (renderAmplifier && effect.getAmplifier() > 0) {
                drawAmplifier(graphics, font, effect, effectX, y);
            }
            if (renderDuration) {
                drawDuration(graphics, font, effect, effectX, y);
            }
        }
    }

    private String amplifierString(MobEffectInstance effect) {
        String digits = String.valueOf(effect.getAmplifier() + 1);
        return FormatCodes.replaceAnd(effect.isAmbient() ? ambientAmplifierText : amplifierText)
                + (subscriptAmplifier ? FormatCodes.toSubscript(digits) : digits);
    }

    private float amplifierX(Font font, String text) {
        return (ICON_SIZE - amplifierScale * font.width(text)) / 2f + amplifierScale * (0.5f + amplifierXOffset);
    }

    private float amplifierY(Font font) {
        float styleYOffset = subscriptAmplifier ? -1.5f : 0f;
        return 8 + (font.lineHeight - amplifierScale * font.lineHeight) / 2f
                + amplifierScale * (styleYOffset - amplifierYOffset);
    }

    private String durationLabel(MobEffectInstance effect) {
        return FormatCodes.replaceAnd(durationText + durationString(effect));
    }

    private float durationX(Font font, String text) {
        return (ICON_SIZE - durationScale * font.width(text)) / 2f + durationScale * (0.5f + durationXOffset);
    }

    private void drawAmplifier(GuiGraphicsExtractor graphics, Font font, MobEffectInstance effect, float x, float y) {
        String text = amplifierString(effect);
        float scale = amplifierScale;

        graphics.pose().pushMatrix();
        graphics.pose().translate(x + amplifierX(font, text), y + amplifierY(font));
        graphics.pose().scale(scale, scale);
        int color = argb(effect.isAmbient() ? ambientAmplifierColor : amplifierColor);
        TextRenderUtil.drawText(graphics, font, text, 0, 0, color, amplifierTextShadow);
        graphics.pose().popMatrix();
    }

    private void drawDuration(GuiGraphicsExtractor graphics, Font font, MobEffectInstance effect, float x, float y) {
        String text = durationLabel(effect);
        float scale = durationScale;

        graphics.pose().pushMatrix();
        graphics.pose().translate(x + durationX(font, text), y + durationY());
        graphics.pose().scale(scale, scale);
        int color = argb(isExpiringSoon(effect) ? expirationColor
                : effect.isAmbient() ? ambientDurationColor : durationColor);
        TextRenderUtil.drawText(graphics, font, text, 0, 0, color, durationTextShadow);
        graphics.pose().popMatrix();
    }

    private static int argb(PolyColor color) {
        return color == null ? 0xFFFFFFFF : color.getArgb();
    }

    private boolean isExpiringSoon(MobEffectInstance effect) {
        return !effect.isInfiniteDuration() && effect.getDuration() / 20 < expirationDuration + 1;
    }

    private String durationString(MobEffectInstance effect) {
        String ambientPrefix = effect.isAmbient() ? ambientDurationText : "";
        if (effect.isInfiniteDuration()) {
            return ambientPrefix + "∞";
        }

        long totalSeconds = effect.getDuration() / 20;
        if (totalSeconds >= 86400L * 99) {
            return "";
        } else if (totalSeconds >= 86400) {
            return ambientPrefix + DurationFormatUtil.format(totalSeconds, dayFormat);
        } else if (totalSeconds >= 3600) {
            return ambientPrefix + DurationFormatUtil.format(totalSeconds, hourFormat);
        } else if (totalSeconds >= 600) {
            return ambientPrefix + DurationFormatUtil.format(totalSeconds, minutesFormatLong);
        } else if (totalSeconds >= 60) {
            return ambientPrefix + DurationFormatUtil.format(totalSeconds, minutesFormat);
        }

        String seconds = DurationFormatUtil.format(totalSeconds, secondsFormat);
        return isExpiringSoon(effect) ? ambientPrefix + expirationText + seconds : ambientPrefix + seconds;
    }
}
