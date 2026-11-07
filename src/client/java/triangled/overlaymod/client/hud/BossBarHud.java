package triangled.overlaymod.client.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import org.polyfrost.compose.render.FilterQuality;
import org.polyfrost.compose.render.PolyBitmap;
import org.polyfrost.compose.render.PolyColor;
import org.polyfrost.compose.render.RenderContext;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import triangled.overlaymod.client.mixin.BossHealthOverlayAccessor;
import triangled.overlaymod.client.mixin.HudAccessor;
import triangled.overlaymod.client.utils.Bounds;
import triangled.overlaymod.client.utils.Colors;
import triangled.overlaymod.client.utils.SpriteCache;

public class BossBarHud extends OverlayHud {
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int NAME_HEIGHT = 9;
    private static final int BAR_SPACING = 19;
    private static final int VANILLA_FIRST_BAR_Y = 12;
    private static final float BOSS_BAR_GAP = 1.0F;
    private static final PolyColor NO_TINT = Colors.white();
    private static final Bounds EMPTY_BOUNDS = new Bounds(0, 0, 1, 1);

    private static final LerpingBossEvent PREVIEW_BAR = new LerpingBossEvent(UUID.randomUUID(),
            Component.literal("Boss"), 0.75F, BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.NOTCHED_10,
            false, false, false);

    @Switch(title = "Hide Vanilla Boss Bars", description = "Hide the vanilla boss bars while this HUD is shown")
    public boolean hideVanilla = true;

    @Slider(title = "Max Height", description = "Stop adding bars once they fill 1/n of the screen height", min = 1, max = 10, step = 1, subcategory = "Layout")
    public int maxHeight = 6;

    private transient List<? extends BossEvent> displayedBars = List.of();
    private transient Bounds contentBounds = EMPTY_BOUNDS;

    public BossBarHud() {
        super("overlaymod_boss_bars.yml", "Boss Bars", Hud.Category.Companion.getINFO());
        setCustomScale(0.5F);
    }

    // Returns true if any visible boss bar HUD is set to hide the vanilla bars.
    public static boolean replacesVanilla() {
        return activeHuds(BossBarHud.class).stream().anyMatch(hud -> hud.hideVanilla && !hud.getHidden());
    }

    // Returns the boss bars the game is currently showing.
    private static List<LerpingBossEvent> liveEvents() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui == null) {
            return List.of();
        }
        BossHealthOverlayAccessor bossOverlay = (BossHealthOverlayAccessor) ((HudAccessor) minecraft.gui.hud)
                .getBossOverlayField();
        return new ArrayList<>(bossOverlay.getEvents().values());
    }

    // Returns how far to move a HUD down so it doesn't overlap any boss bars.
    public static float pushBelowBars(Hud hud, float extraGap) {
        Bounds hudBounds = Bounds.ofSize(hud.getFrameX(), hud.getFrameY(), hud.getScaledWidth(), hud.getScaledHeight());
        float gap = BOSS_BAR_GAP + extraGap;
        float pushDown = 0F;
        for (BossBarHud bossBarHud : activeHuds(BossBarHud.class)) {
            pushDown = Math.max(pushDown, pushBelow(hudBounds, bossBarHud.liveScreenBounds(), gap));
        }
        if (!replacesVanilla()) {
            pushDown = Math.max(pushDown, pushBelow(hudBounds, vanillaBounds(), gap));
        }
        return pushDown / Math.max(0.01F, hud.getEffectiveScale());
    }

    // Returns how far to push the HUD down to clear one set of boss bars.
    private static float pushBelow(Bounds hudBounds, Bounds barBounds, float gap) {
        return barBounds != null && hudBounds.overlaps(barBounds)
                ? Math.max(0F, barBounds.bottom() + gap - hudBounds.top())
                : 0F;
    }

    // Returns the screen area the vanilla boss bars take up, or null if none.
    private static Bounds vanillaBounds() {
        int barCount = liveEvents().size();
        if (barCount == 0) {
            return null;
        }
        float nextBarY = VANILLA_FIRST_BAR_Y;
        for (int i = 0; i < barCount && nextBarY < HudManager.guiScreenHeight / 3F; i++) {
            nextBarY += BAR_SPACING;
        }
        float screenCenterX = HudManager.guiScreenWidth / 2F;
        return new Bounds(screenCenterX - BAR_WIDTH / 2F, VANILLA_FIRST_BAR_Y - NAME_HEIGHT,
                screenCenterX + BAR_WIDTH / 2F, nextBarY - BAR_SPACING + BAR_HEIGHT);
    }

    // Returns the screen area this HUD's bars take up, or null if none.
    private Bounds liveScreenBounds() {
        List<LerpingBossEvent> liveBars = liveEvents();
        int barCount = visibleCount(liveBars.size());
        if (barCount == 0 || getHidden()) {
            return null;
        }
        Bounds barArea = bounds(liveBars.subList(0, barCount));
        float scale = getEffectiveScale();
        return Bounds.ofSize(getFrameX(), getFrameY(), barArea.width() * scale, barArea.height() * scale);
    }

    // Returns the bars to draw, or a preview bar while editing with none.
    private List<? extends BossEvent> shownEvents() {
        List<LerpingBossEvent> liveBars = liveEvents();
        if (liveBars.isEmpty() && isEditing()) {
            return List.of(PREVIEW_BAR);
        }
        return liveBars.subList(0, visibleCount(liveBars.size()));
    }

    // Returns how many bars fit before reaching the Max Height setting.
    private int visibleCount(int totalBars) {
        if (totalBars == 0) {
            return 0;
        }
        float heightLimit = HudManager.guiScreenHeight / Math.max(0.01F, getEffectiveScale()) / Math.max(1, maxHeight);
        int barCount = 1;
        while (barCount < totalBars && VANILLA_FIRST_BAR_Y + barCount * BAR_SPACING < heightLimit) {
            barCount++;
        }
        return barCount;
    }

    // Returns the width of the widest bar or boss name.
    private float contentWidth(List<? extends BossEvent> bars) {
        float width = BAR_WIDTH;
        for (BossEvent bar : bars) {
            width = Math.max(width, text().width(bar.getName()));
        }
        return width;
    }

    // Returns the area the bars and their names take up.
    private Bounds bounds(List<? extends BossEvent> bars) {
        float width = contentWidth(bars);
        float height = bars.isEmpty() ? 1 : (bars.size() - 1) * BAR_SPACING + NAME_HEIGHT + BAR_HEIGHT;
        Bounds barArea = new Bounds(0, 0, width, height);
        for (int i = 0; i < bars.size(); i++) {
            float nameWidth = text().width(bars.get(i).getName());
            barArea = barArea.union(Bounds.ofSize(centered(width, nameWidth), nameTop(i), nameWidth, text().height()));
        }
        return barArea;
    }

    // Returns the y position of the name above the bar at the given index.
    private float nameTop(int index) {
        return index * BAR_SPACING + NAME_HEIGHT - text().height();
    }

    // Returns the x offset that centers an item inside a container.
    private static int centered(float containerWidth, float itemWidth) {
        return Math.round((containerWidth - itemWidth) / 2f);
    }

    // OneConfig calls this every update to grab the boss bars and resize.
    @Override
    protected void refresh() {
        displayedBars = shownEvents();
        contentBounds = bounds(displayedBars);
        setSize(contentBounds.width(), contentBounds.height());
    }

    // Hides the HUD when there are no boss bars to show.
    @Override
    public boolean shouldShow() {
        return !displayedBars.isEmpty();
    }

    // Places the HUD at the top center of the screen by default.
    @Override
    public Pair<Float, Float> defaultPosition() {
        return new Pair<>((HudManager.guiScreenWidth - getScaledWidth()) / 2f, 3.0F);
    }

    // Draws each boss name centered above its bar.
    @Override
    protected void paint(RenderContext renderContext, float x, float y) {
        List<? extends BossEvent> bars = displayedBars;
        float areaWidth = contentWidth(bars);
        float originX = x - contentBounds.left();
        float originY = y - contentBounds.top();
        float barX = originX + centered(areaWidth, BAR_WIDTH);
        PolyColor textColor = text().textColor();
        for (int i = 0; i < bars.size(); i++) {
            BossEvent bar = bars.get(i);
            Component name = bar.getName();
            text().draw(renderContext, name, originX + centered(areaWidth, text().width(name)), originY + nameTop(i),
                    textColor);
            drawBar(renderContext, barX, originY + i * BAR_SPACING + NAME_HEIGHT, bar);
        }
    }

    // Draws one bar's empty background, then its filled part on top.
    private static void drawBar(RenderContext renderContext, float x, float y, BossEvent bar) {
        drawLayer(renderContext, x, y, BAR_WIDTH, bar, "_background");
        int filledWidth = Mth.lerpDiscrete(bar.getProgress(), 0, BAR_WIDTH);
        if (filledWidth > 0) {
            drawLayer(renderContext, x, y, filledWidth, bar, "_progress");
        }
    }

    // Draws one layer of a bar in its color, plus notches if it has them.
    private static void drawLayer(RenderContext renderContext, float x, float y, int width, BossEvent bar,
            String layerSuffix) {
        drawSprite(renderContext, x, y, width, bar.getColor().getName() + layerSuffix);
        if (bar.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
            drawSprite(renderContext, x, y, width, bar.getOverlay().getName() + layerSuffix);
        }
    }

    // Draws a boss bar sprite, cropped to the given width.
    private static void drawSprite(RenderContext renderContext, float x, float y, int width, String spriteName) {
        PolyBitmap sprite = SpriteCache.guiSprite("boss_bar/" + spriteName);
        if (sprite != null) {
            renderContext.image(sprite, x, y, width, BAR_HEIGHT, 0, 0, width / (float) BAR_WIDTH, 1, NO_TINT,
                    FilterQuality.None);
        }
    }

    // Gives each copy of the HUD its own bars so copies don't share them.
    @Override
    public Hud clone() {
        BossBarHud copy = (BossBarHud) super.clone();
        copy.displayedBars = List.of();
        copy.contentBounds = EMPTY_BOUNDS;
        return copy;
    }
}
