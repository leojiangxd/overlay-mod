package triangled.overlaymod.client.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import kotlin.Pair;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.polyfrost.compose.render.FilterQuality;
import org.polyfrost.compose.render.PolyBitmap;
import org.polyfrost.compose.render.PolyColor;
import org.polyfrost.compose.render.RenderContext;
import org.polyfrost.oneconfig.api.config.v1.annotations.Color;
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown;
import org.polyfrost.oneconfig.api.config.v1.annotations.Number;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.config.v1.annotations.Text;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import triangled.overlaymod.client.utils.Bounds;
import triangled.overlaymod.client.utils.DurationFormatUtil;
import triangled.overlaymod.client.utils.Subscript;
import triangled.overlaymod.client.utils.HudTextStyle;
import triangled.overlaymod.client.utils.SpriteCache;
import triangled.overlaymod.client.utils.TextAlignment;

public class StatusEffectsHud extends OverlayHud {
    private static final int SLOT_SIZE = 24;
    private static final int ICON_PADDING = 3;
    private static final int ICON_SIZE = 18;
    private static final int TICKS_PER_SECOND = 20;
    private static final float LINE_HEIGHT = 9;
    private static final float AMPLIFIER_BASELINE = 8;
    private static final float DURATION_BASELINE = 23;
    private static final String EFFECT_BACKGROUND = "hud/effect_background";
    private static final String EFFECT_BACKGROUND_AMBIENT = "hud/effect_background_ambient";

    // --------------- CONFIGS ---------------
    // VISIBILITY

    @Switch(title = "Hide Vanilla Effects", description = "Hide the vanilla effect icons in the top-right corner while this HUD is shown", subcategory = "Visibility")
    public boolean hideVanilla = true;

    @Switch(title = "Move Below Boss Bars", description = "Move the HUD down while boss bars above it would overlap it", subcategory = "Visibility")
    public boolean belowBossBars = true;

    @Number(title = "Boss Bar Y Offset", description = "Extra space below the boss bars when the HUD is moved down", min = -64, max = 64, subcategory = "Visibility")
    public float bossBarYOffset = 1.5F;

    @Switch(title = "Render Effect Background", description = "Draw the effect background behind each icon", subcategory = "Visibility")
    public boolean renderBackground = true;

    @Switch(title = "Separate Negative Effects", description = "Put harmful effects on their own row below beneficial ones", subcategory = "Visibility")
    public boolean separateNegative = true;

    @Switch(title = "Show Amplifier", description = "Show the effect level on the icon", subcategory = "Visibility")
    public boolean renderAmplifier = true;

    @Switch(title = "Subscript Amplifier", description = "Draw the effect level using subscript digits", subcategory = "Visibility")
    public boolean subscriptAmplifier = true;

    @Switch(title = "Show Duration", description = "Show the time remaining under each icon", subcategory = "Visibility")
    public boolean renderDuration = true;

    // TEXT

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

    @Text(title = "Amplifier Prefix", description = "Text before the level", subcategory = "Text")
    public String amplifierText = "";

    @Text(title = "Ambient Amplifier Prefix", description = "Text before the level of a beacon/conduit effect", subcategory = "Text")
    public String ambientAmplifierText = "";

    @Text(title = "Duration Prefix", description = "Text before the duration", subcategory = "Text")
    public String durationText = "";

    @Text(title = "Ambient Duration Prefix", description = "Text before the duration of a beacon/conduit effect", subcategory = "Text")
    public String ambientDurationText = "";

    @Text(title = "Expiring Prefix", description = "Text before the duration once the effect is about to run out", subcategory = "Text")
    public String expirationText = "";

    @Slider(title = "Expiring Threshold", description = "Seconds left at which an effect counts as expiring and starts blinking", min = 0, max = 59, step = 1, subcategory = "Text")
    public int expirationDuration = 10;

    // STYLE

    @Color(title = "Ambient Amplifier Color", subcategory = "Style")
    public PolyColor ambientAmplifierColor = new PolyColor(0xFFFFFF55, false, 1.0F);

    @Color(title = "Ambient Duration Color", subcategory = "Style")
    public PolyColor ambientDurationColor = new PolyColor(0xFFFFFF55, false, 1.0F);

    @Color(title = "Expiring Color", subcategory = "Style")
    public PolyColor expirationColor = new PolyColor(0xFFFF5555, false, 1.0F);

    // LAYOUT

    @Dropdown(title = "Row Alignment", description = "How a shorter row lines up against the longer one", subcategory = "Layout")
    public TextAlignment rowAlignment = TextAlignment.MIDDLE;

    @Slider(title = "Effect Gap", description = "Space between icons", min = -8, max = 16, step = 1, subcategory = "Layout")
    public int effectGap = 2;

    @Number(title = "Row Gap", description = "Space between the beneficial and harmful rows", min = -24, max = 64, subcategory = "Layout")
    public float rowGap = 6.5F;

    @Number(title = "Amplifier Scale", min = 0.1F, max = 4, subcategory = "Layout")
    public float amplifierScale = 0.5F;

    @Number(title = "Amplifier X Offset", min = -32, max = 32, subcategory = "Layout")
    public float amplifierXOffset = 0.5F;

    @Number(title = "Amplifier Y Offset", description = "Positive moves up", min = -32, max = 32, subcategory = "Layout")
    public float amplifierYOffset = 0.5F;

    @Number(title = "Duration Scale", min = 0.1F, max = 4, subcategory = "Layout")
    public float durationScale = 1.0F;

    @Number(title = "Duration X Offset", min = -32, max = 32, subcategory = "Layout")
    public float durationXOffset = 0.5F;

    @Number(title = "Duration Y Offset", description = "Positive moves up", min = -32, max = 32, subcategory = "Layout")
    public float durationYOffset = 3.5F;
    // ---------------------------------------

    // The effects split into a top row and a bottom row.
    private record Rows(List<MobEffectInstance> top, List<MobEffectInstance> bottom) {
    }

    // One effect and where its icon goes.
    private record Slot(MobEffectInstance effect, float x, float y) {
    }

    // Scale and offsets for one kind of label, like the level or duration.
    private record LabelStyle(HudTextStyle textStyle, float scale, float xOffset, float yOffset) {
        // Returns a label centered over the slot at the given line.
        Label at(Slot slot, String labelText, float lineTop, float extraYOffset, PolyColor color) {
            float x = (SLOT_SIZE - textStyle.width(labelText, scale)) / 2f + scale * (0.5f + xOffset);
            float y = lineTop + (LINE_HEIGHT - textStyle.height(scale)) / 2f + scale * (extraYOffset - yOffset);
            return new Label(labelText, slot.x() + x, slot.y() + y, this, color);
        }
    }

    // A piece of text, where it goes, and how to draw it.
    private record Label(String text, float x, float y, LabelStyle style, PolyColor color) {
        // Returns the area this label takes up.
        Bounds bounds() {
            return style.textStyle().bounds(text, x, y, style.scale());
        }

        // Draws this label.
        void draw(RenderContext renderContext) {
            style.textStyle().draw(renderContext, text, x, y, color, style.scale());
        }
    }

    // Which duration format to use, based on how much time is left.
    private enum DurationTier {
        DAYS(86400), HOURS(3600), LONG_MINUTES(600), MINUTES(60), SECONDS(0);

        private final long minSeconds;

        DurationTier(long minSeconds) {
            this.minSeconds = minSeconds;
        }

        // Returns the tier for the given number of seconds.
        static DurationTier of(long seconds) {
            for (DurationTier tier : values()) {
                if (seconds >= tier.minSeconds) {
                    return tier;
                }
            }
            return SECONDS;
        }
    }

    // Everything needed to draw the HUD: slots, labels and their area.
    private record Frame(List<Slot> slots, List<Label> labels, Bounds bounds) {
        static final Frame EMPTY = new Frame(List.of(), List.of(), Bounds.ofSize(0, 0, SLOT_SIZE, SLOT_SIZE));
    }

    private transient Frame displayedFrame = Frame.EMPTY;

    public StatusEffectsHud() {
        super("overlaymod_status_effects.yml", "Status Effects", Hud.Category.Companion.getPLAYER());
    }

    // Returns true if any visible effects HUD is set to hide vanilla icons.
    public static boolean replacesVanilla() {
        return activeHuds(StatusEffectsHud.class).stream().anyMatch(hud -> hud.hideVanilla && !hud.getHidden());
    }

    // Returns the effects to show, split into rows if Separate Negative is on.
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

    // Returns the player's effects that show an icon, in vanilla order.
    private static List<MobEffectInstance> activeEffects() {
        LocalPlayer player = player();
        if (player == null) {
            return List.of();
        }
        return player.getActiveEffects().stream().filter(MobEffectInstance::showIcon).sorted().toList();
    }

    // Returns sample effects for previewing the HUD in the editor.
    private static List<MobEffectInstance> previewEffects() {
        return Stream.of(
                new MobEffectInstance(MobEffects.SPEED, TICKS_PER_SECOND * 185, 1),
                new MobEffectInstance(MobEffects.STRENGTH, TICKS_PER_SECOND * 45),
                new MobEffectInstance(MobEffects.REGENERATION, TICKS_PER_SECOND * 8, 0, true, true),
                new MobEffectInstance(MobEffects.POISON, TICKS_PER_SECOND * 12, 2)).sorted().toList();
    }

    // Returns the distance from one icon to the next.
    private int stride() {
        return SLOT_SIZE + effectGap;
    }

    // Returns how wide a row of the given number of icons is.
    private float rowWidth(int count) {
        return count == 0 ? 0 : (count - 1) * stride() + SLOT_SIZE;
    }

    // Places every effect's icon in its row.
    private List<Slot> slots(Rows rows) {
        float rowAreaWidth = Math.max(SLOT_SIZE, Math.max(rowWidth(rows.top().size()), rowWidth(rows.bottom().size())));
        List<Slot> slots = new ArrayList<>();
        addRow(slots, rows.top(), rowAreaWidth, 0);
        addRow(slots, rows.bottom(), rowAreaWidth, SLOT_SIZE + rowGap);
        return slots;
    }

    // Places one row of icons, lined up with Row Alignment.
    private void addRow(List<Slot> slots, List<MobEffectInstance> effects, float areaWidth, float y) {
        float startX = rowAlignment.align(areaWidth - rowWidth(effects.size()));
        for (int i = 0; i < effects.size(); i++) {
            slots.add(new Slot(effects.get(i), startX + i * stride(), y));
        }
    }

    // Builds the level and duration labels for every icon.
    private List<Label> labels(List<Slot> slots) {
        LabelStyle amplifierStyle = new LabelStyle(text(), amplifierScale, amplifierXOffset, amplifierYOffset);
        LabelStyle durationStyle = new LabelStyle(text(), durationScale, durationXOffset, durationYOffset);
        float amplifierYShift = subscriptAmplifier ? -1.5f : 0f;
        List<Label> labels = new ArrayList<>();
        for (Slot slot : slots) {
            MobEffectInstance effect = slot.effect();
            if (renderAmplifier && effect.getAmplifier() > 0) {
                labels.add(amplifierStyle.at(slot, amplifierLabel(effect), AMPLIFIER_BASELINE, amplifierYShift,
                        effect.isAmbient() ? ambientAmplifierColor : text().textColor()));
            }
            if (renderDuration) {
                labels.add(durationStyle.at(slot, durationLabel(effect), DURATION_BASELINE, 1f, durationColor(effect)));
            }
        }
        return labels;
    }

    // Returns the area the icons and labels take up.
    private Bounds bounds(List<Slot> slots, List<Label> labels) {
        int padding = renderBackground ? 0 : ICON_PADDING;
        List<Bounds> areas = new ArrayList<>();
        for (Slot slot : slots) {
            areas.add(Bounds.ofSize(slot.x() + padding, slot.y() + padding, SLOT_SIZE - 2 * padding,
                    SLOT_SIZE - 2 * padding));
        }
        for (Label label : labels) {
            areas.add(label.bounds());
        }
        return Bounds.enclosing(areas).orElse(Frame.EMPTY.bounds());
    }

    // OneConfig calls this every update to lay out the effects, resize, and
    // move below any boss bars.
    @Override
    protected void refresh() {
        List<Slot> slots = slots(rows());
        if (slots.isEmpty()) {
            displayedFrame = Frame.EMPTY;
        } else {
            List<Label> labels = labels(slots);
            displayedFrame = new Frame(slots, labels, bounds(slots, labels));
        }
        setSize(displayedFrame.bounds().width(), displayedFrame.bounds().height());
        setPushDown(belowBossBars && !isEditing() ? BossBarHud.pushBelowBars(this, bossBarYOffset) : 0f);
    }

    // Hides the HUD when there are no effects to show.
    @Override
    public boolean shouldShow() {
        return !displayedFrame.slots().isEmpty();
    }

    // Places the HUD at the top center of the screen by default.
    @Override
    public Pair<Float, Float> defaultPosition() {
        return new Pair<>((HudManager.guiScreenWidth - getScaledWidth()) / 2f, 2.0F);
    }

    // Draws the backgrounds, icons and labels.
    @Override
    protected void paint(RenderContext renderContext, float x, float y) {
        Frame frame = displayedFrame;
        float offsetX = x - frame.bounds().left();
        float offsetY = y - frame.bounds().top();
        renderContext.translate(offsetX, offsetY);
        if (renderBackground) {
            frame.slots().forEach(slot -> drawBackground(renderContext, slot));
        }
        frame.slots().forEach(slot -> drawIcon(renderContext, slot));
        frame.labels().forEach(label -> label.draw(renderContext));
        renderContext.translate(-offsetX, -offsetY);
    }

    // Draws the vanilla effect frame behind a slot.
    private static void drawBackground(RenderContext renderContext, Slot slot) {
        PolyBitmap sprite = SpriteCache
                .guiSprite(slot.effect().isAmbient() ? EFFECT_BACKGROUND_AMBIENT : EFFECT_BACKGROUND);
        if (sprite != null) {
            renderContext.image(sprite, slot.x(), slot.y(), SLOT_SIZE, SLOT_SIZE, FilterQuality.None);
        }
    }

    // Draws an effect's icon, blinking it when the effect is about to end.
    private void drawIcon(RenderContext renderContext, Slot slot) {
        PolyBitmap sprite = SpriteCache
                .sprite(net.minecraft.client.gui.Hud.getMobEffectSprite(slot.effect().getEffect()));
        if (sprite != null) {
            PolyColor tint = new PolyColor(ARGB.white(iconAlpha(slot.effect())), false, 1f);
            renderContext.image(sprite, slot.x() + ICON_PADDING, slot.y() + ICON_PADDING, ICON_SIZE, ICON_SIZE, 0, 0, 1,
                    1, tint,
                    FilterQuality.None);
        }
    }

    // Returns the icon's opacity, using vanilla's blinking when expiring.
    private float iconAlpha(MobEffectInstance effect) {
        if (!effect.endsWithin((expirationDuration + 1) * TICKS_PER_SECOND)) {
            return 1.0F;
        }
        int ticksLeft = effect.getDuration();
        int blinkWeight = 10 - ticksLeft / TICKS_PER_SECOND;
        return Mth.clamp(ticksLeft / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
                + Mth.cos(ticksLeft * (float) Math.PI / 5.0F) * Mth.clamp(blinkWeight / 10.0F * 0.25F, 0.0F, 0.25F);
    }

    // Returns true when the effect is within the Expiring Threshold.
    private boolean isExpiringSoon(MobEffectInstance effect) {
        return !effect.isInfiniteDuration() && effect.getDuration() / TICKS_PER_SECOND <= expirationDuration;
    }

    // Returns the duration color: expiring, ambient, or the normal text color.
    private PolyColor durationColor(MobEffectInstance effect) {
        if (isExpiringSoon(effect)) {
            return expirationColor;
        }
        return effect.isAmbient() ? ambientDurationColor : text().textColor();
    }

    // Returns the level text, like 2 for Speed II, with its prefix.
    private String amplifierLabel(MobEffectInstance effect) {
        String level = String.valueOf(effect.getAmplifier() + 1);
        return (effect.isAmbient() ? ambientAmplifierText : amplifierText)
                + (subscriptAmplifier ? Subscript.of(level) : level);
    }

    // Returns the time left text, using the format for that much time.
    private String durationLabel(MobEffectInstance effect) {
        String prefix = durationText + (effect.isAmbient() ? ambientDurationText : "");
        if (effect.isInfiniteDuration()) {
            return prefix + "∞";
        }
        long seconds = effect.getDuration() / TICKS_PER_SECOND;
        if (seconds >= 86400L * 99) {
            return durationText;
        }
        DurationTier tier = DurationTier.of(seconds);
        String expiringPrefix = tier == DurationTier.SECONDS && isExpiringSoon(effect) ? expirationText : "";
        return prefix + expiringPrefix + DurationFormatUtil.format(seconds, pattern(tier));
    }

    // Returns the format setting for the given duration tier.
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
