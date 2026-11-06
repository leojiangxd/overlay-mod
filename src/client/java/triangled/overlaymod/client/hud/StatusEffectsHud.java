package triangled.overlaymod.client.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import kotlin.Pair;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
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
import triangled.overlaymod.client.utils.BossBarUtil;
import triangled.overlaymod.client.utils.Bounds;
import triangled.overlaymod.client.utils.Colors;
import triangled.overlaymod.client.utils.DurationFormatUtil;
import triangled.overlaymod.client.utils.FormatCodes;
import triangled.overlaymod.client.utils.TextAlignment;
import triangled.overlaymod.client.utils.TextRenderUtil;
import triangled.overlaymod.client.utils.TextShadow;

public class StatusEffectsHud extends OverlayHud {
    private static final int ICON_SIZE = 24;
    private static final int ICON_INSET = 3;
    private static final int SPRITE_SIZE = 18;
    private static final int TICKS_PER_SECOND = 20;
    private static final float AMPLIFIER_BASELINE = 8;
    private static final float DURATION_BASELINE = 23;
    private static final Identifier EFFECT_BACKGROUND = Identifier.withDefaultNamespace("hud/effect_background");
    private static final Identifier EFFECT_BACKGROUND_AMBIENT = Identifier
            .withDefaultNamespace("hud/effect_background_ambient");

    // ---------------------------------------------------------------- Visibility

    @Switch(title = "Hide Vanilla Effects", description = "Hide the vanilla effect icons in the top-right corner while this HUD is shown", subcategory = "Visibility")
    public boolean hideVanilla = true;

    @Switch(title = "Move Below Boss Bars", description = "Move the HUD down while boss bars above it would overlap it", subcategory = "Visibility")
    public boolean belowBossBars = true;

    @Number(title = "Boss Bar Y Offset", description = "Extra space below the boss bars when the HUD is moved down", min = -64, max = 64, subcategory = "Visibility")
    public float bossBarYOffset = 0.0F;

    @Switch(title = "Render Background", description = "Draw the vanilla effect frame behind each icon", subcategory = "Visibility")
    public boolean renderBackground = false;

    @Switch(title = "Separate Negative Effects", description = "Put harmful effects on their own row below beneficial ones", subcategory = "Visibility")
    public boolean separateNegative = true;

    @Switch(title = "Show Amplifier", description = "Show the effect level on the icon", subcategory = "Visibility")
    public boolean renderAmplifier = true;

    @Switch(title = "Subscript Amplifier", description = "Draw the effect level using subscript digits", subcategory = "Visibility")
    public boolean subscriptAmplifier = true;

    @Switch(title = "Show Duration", description = "Show the time remaining under each icon", subcategory = "Visibility")
    public boolean renderDuration = true;

    // ---------------------------------------------------------------- Text

    @Text(title = "Seconds Format", description = "Under a minute", subcategory = "Text")
    public String secondsFormat = "0:ss";

    @Text(title = "Minutes Format", description = "Under ten minutes", subcategory = "Text")
    public String minutesFormat = "m:ss";

    @Text(title = "Long Minutes Format", description = "Ten minutes up to an hour", subcategory = "Text")
    public String minutesFormatLong = "m\\m";

    @Text(title = "Hours Format", description = "An hour up to a day", subcategory = "Text")
    public String hourFormat = "h\\h";

    @Text(title = "Days Format", description = "A day or longer", subcategory = "Text")
    public String dayFormat = "d\\d";

    @Text(title = "Amplifier Prefix", description = "Text before the level. & starts a formatting code, \\& is a literal &", subcategory = "Text")
    public String amplifierText = "&f";

    @Text(title = "Ambient Amplifier Prefix", description = "Text before the level of a beacon/conduit effect", subcategory = "Text")
    public String ambientAmplifierText = "&e";

    @Text(title = "Duration Prefix", description = "Text before the duration", subcategory = "Text")
    public String durationText = "&f";

    @Text(title = "Ambient Duration Prefix", description = "Text before the duration of a beacon/conduit effect", subcategory = "Text")
    public String ambientDurationText = "&e";

    @Text(title = "Expiring Prefix", description = "Text before the duration once the effect is about to run out", subcategory = "Text")
    public String expirationText = "&c";

    @Slider(title = "Expiring Threshold", description = "Seconds left at which an effect counts as expiring and starts blinking", min = 0, max = 59, step = 1, subcategory = "Text")
    public int expirationDuration = 10;

    // ---------------------------------------------------------------- Style

    @Dropdown(title = "Amplifier Shadow", description = "How the level text is shadowed or outlined", subcategory = "Style")
    public TextShadow amplifierTextShadow = TextShadow.FOUR_DIRECTION;

    @Dropdown(title = "Duration Shadow", description = "How the duration text is shadowed or outlined", subcategory = "Style")
    public TextShadow durationTextShadow = TextShadow.SHADOW;

    @Color(title = "Amplifier Color", subcategory = "Style")
    public PolyColor amplifierColor = Colors.white();

    @Color(title = "Ambient Amplifier Color", subcategory = "Style")
    public PolyColor ambientAmplifierColor = Colors.white();

    @Color(title = "Duration Color", subcategory = "Style")
    public PolyColor durationColor = Colors.white();

    @Color(title = "Ambient Duration Color", subcategory = "Style")
    public PolyColor ambientDurationColor = Colors.white();

    @Color(title = "Expiring Color", subcategory = "Style")
    public PolyColor expirationColor = Colors.white();

    // ---------------------------------------------------------------- Layout

    @Dropdown(title = "Row Alignment", description = "How a shorter row lines up against the longer one", subcategory = "Layout")
    public TextAlignment rowAlignment = TextAlignment.MIDDLE;

    @Slider(title = "Effect Gap", description = "Space between icons", min = -8, max = 16, step = 1, subcategory = "Layout")
    public int effectGap = 1;

    @Number(title = "Row Gap", description = "Space between the beneficial and harmful rows", min = -24, max = 64, subcategory = "Layout")
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

    private record Rows(List<MobEffectInstance> top, List<MobEffectInstance> bottom) {
        boolean isEmpty() {
            return top.isEmpty() && bottom.isEmpty();
        }
    }

    private record Slot(MobEffectInstance effect, float x, float y) {
    }

    private record LabelStyle(float scale, float xOffset, float yOffset, TextShadow shadow) {
        float x(Font font, String text) {
            return (ICON_SIZE - scale * font.width(text)) / 2f + scale * (0.5f + xOffset);
        }

        float y(Font font, float baseline, float nudge) {
            return baseline + (font.lineHeight - scale * font.lineHeight) / 2f + scale * (nudge - yOffset);
        }
    }

    private record Label(String text, float x, float y, LabelStyle style, int color) {
        Bounds bounds(Font font) {
            return TextRenderUtil.textBounds(font, font.width(text), x, y, style.scale(), style.shadow());
        }

        void draw(GuiGraphicsExtractor graphics, Font font) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(x, y);
            graphics.pose().scale(style.scale(), style.scale());
            TextRenderUtil.drawText(graphics, font, text, 0, 0, color, style.shadow());
            graphics.pose().popMatrix();
        }
    }

    private enum DurationTier {
        DAYS(86400), HOURS(3600), LONG_MINUTES(600), MINUTES(60), SECONDS(0);

        private final long minSeconds;

        DurationTier(long minSeconds) {
            this.minSeconds = minSeconds;
        }

        static DurationTier of(long seconds) {
            for (DurationTier tier : values()) {
                if (seconds >= tier.minSeconds) {
                    return tier;
                }
            }
            return SECONDS;
        }
    }

    public StatusEffectsHud() {
        super("overlaymod_status_effects.yml", "Status Effects", Hud.Category.Companion.getPLAYER());
    }

    public static boolean replacesVanilla() {
        return activeHuds(StatusEffectsHud.class).stream().anyMatch(hud -> hud.hideVanilla && !hud.getHidden());
    }

    private Rows rows() {
        List<MobEffectInstance> effects = activeEffects();
        if (effects.isEmpty() && isEditing()) {
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
        LocalPlayer player = player();
        if (player == null) {
            return List.of();
        }
        List<MobEffectInstance> effects = new ArrayList<>();
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (effect.showIcon()) {
                effects.add(effect);
            }
        }
        Collections.sort(effects);
        return effects;
    }

    private static List<MobEffectInstance> previewEffects() {
        List<MobEffectInstance> effects = new ArrayList<>(List.of(
                new MobEffectInstance(MobEffects.SPEED, TICKS_PER_SECOND * 185, 1),
                new MobEffectInstance(MobEffects.STRENGTH, TICKS_PER_SECOND * 45),
                new MobEffectInstance(MobEffects.REGENERATION, TICKS_PER_SECOND * 8, 0, true, true),
                new MobEffectInstance(MobEffects.POISON, TICKS_PER_SECOND * 12, 2)));
        Collections.sort(effects);
        return effects;
    }

    private int stride() {
        return ICON_SIZE + effectGap;
    }

    private float rowWidth(int count) {
        return count <= 0 ? 0 : (count - 1) * stride() + ICON_SIZE;
    }

    private List<Slot> slots(Rows rows) {
        float width = Math.max(ICON_SIZE, Math.max(rowWidth(rows.top().size()), rowWidth(rows.bottom().size())));
        List<Slot> slots = new ArrayList<>();
        addRow(slots, rows.top(), width, 0);
        addRow(slots, rows.bottom(), width, ICON_SIZE + rowGap);
        return slots;
    }

    private void addRow(List<Slot> slots, List<MobEffectInstance> effects, float width, float y) {
        float x = rowAlignment.align(width - rowWidth(effects.size()));
        for (int i = 0; i < effects.size(); i++) {
            slots.add(new Slot(effects.get(i), x + i * stride(), y));
        }
    }

    private Bounds bounds(List<Slot> slots) {
        Font font = font();
        int inset = renderBackground ? 0 : ICON_INSET;
        List<Bounds> boxes = new ArrayList<>();
        for (Slot slot : slots) {
            boxes.add(Bounds.ofSize(slot.x() + inset, slot.y() + inset, ICON_SIZE - 2 * inset, ICON_SIZE - 2 * inset));
            for (Label label : labels(slot, font)) {
                boxes.add(label.bounds(font));
            }
        }
        return Bounds.enclosing(boxes).orElse(Bounds.ofSize(0, 0, ICON_SIZE, ICON_SIZE));
    }

    @Override
    public float getWidth() {
        return bounds(slots(rows())).width();
    }

    @Override
    public float getHeight() {
        return bounds(slots(rows())).height();
    }

    @Override
    public boolean shouldShow() {
        applyGrowthAnchor();
        return !rows().isEmpty();
    }

    @Override
    public Pair<Float, Float> defaultPosition() {
        return new Pair<>((HudManager.guiScreenWidth - getScaledWidth()) / 2f, 4.0F);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics) {
        applyGrowthAnchor();
        Rows rows = rows();
        if (rows.isEmpty()) {
            return;
        }

        Font font = font();
        List<Slot> slots = slots(rows);
        Bounds bounds = bounds(slots);
        float bossBarOffset = belowBossBars && !isEditing() ? BossBarUtil.pushDown(this, bossBarYOffset) : 0f;

        graphics.pose().pushMatrix();
        graphics.pose().translate(-bounds.left(), -bounds.top() + bossBarOffset);

        if (renderBackground) {
            slots.forEach(slot -> drawBackground(graphics, slot));
        }
        slots.forEach(slot -> drawIcon(graphics, slot));
        for (Slot slot : slots) {
            labels(slot, font).forEach(label -> label.draw(graphics, font));
        }

        graphics.pose().popMatrix();
    }

    private static void drawBackground(GuiGraphicsExtractor graphics, Slot slot) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(slot.x(), slot.y());
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                slot.effect().isAmbient() ? EFFECT_BACKGROUND_AMBIENT : EFFECT_BACKGROUND, 0, 0, ICON_SIZE, ICON_SIZE);
        graphics.pose().popMatrix();
    }

    private void drawIcon(GuiGraphicsExtractor graphics, Slot slot) {
        Identifier sprite = net.minecraft.client.gui.Hud.getMobEffectSprite(slot.effect().getEffect());
        graphics.pose().pushMatrix();
        graphics.pose().translate(slot.x() + ICON_INSET, slot.y() + ICON_INSET);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, 0, 0, SPRITE_SIZE, SPRITE_SIZE,
                ARGB.white(iconAlpha(slot.effect())));
        graphics.pose().popMatrix();
    }

    private float iconAlpha(MobEffectInstance effect) {
        if (!effect.endsWithin((expirationDuration + 1) * TICKS_PER_SECOND)) {
            return 1.0F;
        }
        int duration = effect.getDuration();
        int n = 10 - duration / TICKS_PER_SECOND;
        return Mth.clamp(duration / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
                + Mth.cos(duration * (float) Math.PI / 5.0F) * Mth.clamp(n / 10.0F * 0.25F, 0.0F, 0.25F);
    }

    private LabelStyle amplifierStyle() {
        return new LabelStyle(amplifierScale, amplifierXOffset, amplifierYOffset, amplifierTextShadow);
    }

    private LabelStyle durationStyle() {
        return new LabelStyle(durationScale, durationXOffset, durationYOffset, durationTextShadow);
    }

    private List<Label> labels(Slot slot, Font font) {
        MobEffectInstance effect = slot.effect();
        List<Label> labels = new ArrayList<>(2);
        if (renderAmplifier && effect.getAmplifier() > 0) {
            String text = amplifierString(effect);
            LabelStyle style = amplifierStyle();
            float nudge = subscriptAmplifier ? -1.5f : 0f;
            labels.add(
                    new Label(text, slot.x() + style.x(font, text), slot.y() + style.y(font, AMPLIFIER_BASELINE, nudge),
                            style, amplifierColorFor(effect)));
        }
        if (renderDuration) {
            String text = durationLabel(effect);
            LabelStyle style = durationStyle();
            labels.add(new Label(text, slot.x() + style.x(font, text), slot.y() + style.y(font, DURATION_BASELINE, 1f),
                    style, durationColorFor(effect)));
        }
        return labels;
    }

    private int amplifierColorFor(MobEffectInstance effect) {
        return Colors.argb(effect.isAmbient() ? ambientAmplifierColor : amplifierColor);
    }

    private int durationColorFor(MobEffectInstance effect) {
        if (isExpiringSoon(effect)) {
            return Colors.argb(expirationColor);
        }
        return Colors.argb(effect.isAmbient() ? ambientDurationColor : durationColor);
    }

    private String amplifierString(MobEffectInstance effect) {
        String digits = String.valueOf(effect.getAmplifier() + 1);
        return FormatCodes.replaceAnd(effect.isAmbient() ? ambientAmplifierText : amplifierText)
                + (subscriptAmplifier ? FormatCodes.toSubscript(digits) : digits);
    }

    private String durationLabel(MobEffectInstance effect) {
        return FormatCodes.replaceAnd(durationText + durationString(effect));
    }

    private static long secondsLeft(MobEffectInstance effect) {
        return effect.getDuration() / TICKS_PER_SECOND;
    }

    private boolean isExpiringSoon(MobEffectInstance effect) {
        return !effect.isInfiniteDuration() && secondsLeft(effect) < expirationDuration + 1;
    }

    private String durationString(MobEffectInstance effect) {
        String ambientPrefix = effect.isAmbient() ? ambientDurationText : "";
        if (effect.isInfiniteDuration()) {
            return ambientPrefix + "∞";
        }

        long seconds = secondsLeft(effect);
        if (seconds >= 86400L * 99) {
            return "";
        }

        DurationTier tier = DurationTier.of(seconds);
        String text = DurationFormatUtil.format(seconds, pattern(tier));
        if (tier == DurationTier.SECONDS && isExpiringSoon(effect)) {
            text = expirationText + text;
        }
        return ambientPrefix + text;
    }

    private String pattern(DurationTier tier) {
        return switch (tier) {
            case DAYS -> dayFormat;
            case HOURS -> hourFormat;
            case LONG_MINUTES -> minutesFormatLong;
            case MINUTES -> minutesFormat;
            case SECONDS -> secondsFormat;
        };
    }
}
