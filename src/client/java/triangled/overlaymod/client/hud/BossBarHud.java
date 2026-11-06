package triangled.overlaymod.client.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import org.polyfrost.compose.render.PolyColor;
import org.polyfrost.oneconfig.api.config.v1.annotations.Color;
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import triangled.overlaymod.client.mixin.BossHealthOverlayAccessor;
import triangled.overlaymod.client.mixin.HudAccessor;
import triangled.overlaymod.client.utils.Bounds;
import triangled.overlaymod.client.utils.Colors;
import triangled.overlaymod.client.utils.TextRenderUtil;
import triangled.overlaymod.client.utils.TextShadow;

public class BossBarHud extends OverlayHud {
    public static final int BAR_WIDTH = 182;
    public static final int BAR_HEIGHT = 5;
    public static final int NAME_HEIGHT = 9;
    public static final int STRIDE = 19;

    private static final LerpingBossEvent PREVIEW = new LerpingBossEvent(UUID.randomUUID(),
            Component.literal("Boss"), 0.75F, BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.NOTCHED_10,
            false, false, false);

    @Switch(title = "Hide Vanilla Boss Bars", description = "Hide the vanilla boss bars while this HUD is shown")
    public boolean hideVanilla = true;

    // ---------------------------------------------------------------- Layout

    @Slider(title = "Max Height", description = "Stop adding bars once they fill 1/n of the screen height", min = 1, max = 10, step = 1, subcategory = "Layout")
    public int maxHeight = 6;

    // ---------------------------------------------------------------- Style

    @Dropdown(title = "Name Shadow", description = "How the boss name is shadowed or outlined", subcategory = "Style")
    public TextShadow nameTextShadow = TextShadow.SHADOW;

    @Color(title = "Name Color", description = "Color of any part of the name not colored by the server", subcategory = "Style")
    public PolyColor nameColor = Colors.white();

    public BossBarHud() {
        super("overlaymod_boss_bars.yml", "Boss Bars", Hud.Category.Companion.getINFO());
        setCustomScale(0.5F);
    }

    public static boolean replacesVanilla() {
        return activeHuds(BossBarHud.class).stream().anyMatch(hud -> hud.hideVanilla && !hud.getHidden());
    }

    private static BossHealthOverlay overlay() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gui == null) {
            return null;
        }
        return ((HudAccessor) mc.gui.hud).getBossOverlayField();
    }

    public static List<LerpingBossEvent> liveEvents() {
        BossHealthOverlay overlay = overlay();
        return overlay == null ? List.of()
                : new ArrayList<>(((BossHealthOverlayAccessor) overlay).getEvents().values());
    }

    private List<? extends BossEvent> shownEvents() {
        List<LerpingBossEvent> events = liveEvents();
        if (events.isEmpty() && isEditing()) {
            return List.of(PREVIEW);
        }
        return events.subList(0, visibleCount(events.size()));
    }

    private int visibleCount(int total) {
        if (total <= 0) {
            return 0;
        }
        float limit = HudManager.guiScreenHeight / Math.max(0.01F, getEffectiveScale()) / Math.max(1, maxHeight);
        int count = 1;
        while (count < total && 12 + count * STRIDE < limit) {
            count++;
        }
        return count;
    }

    private static float contentHeight(int count) {
        return count <= 0 ? 0 : (count - 1) * STRIDE + NAME_HEIGHT + BAR_HEIGHT;
    }

    private static float contentWidth(List<? extends BossEvent> events) {
        Font font = font();
        int width = BAR_WIDTH;
        for (BossEvent event : events) {
            width = Math.max(width, font.width(event.getName()));
        }
        return width;
    }

    private Bounds bounds(List<? extends BossEvent> events) {
        Font font = font();
        float width = contentWidth(events);
        Bounds bounds = new Bounds(0, 0, width, Math.max(1, contentHeight(events.size())));
        for (int i = 0; i < events.size(); i++) {
            int nameWidth = font.width(events.get(i).getName());
            bounds = bounds.union(TextRenderUtil.textBounds(font, nameWidth, centered(width, nameWidth), i * STRIDE, 1F,
                    nameTextShadow));
        }
        return bounds;
    }

    private static int centered(float containerWidth, int width) {
        return Math.round((containerWidth - width) / 2f);
    }

    @Override
    public float getWidth() {
        return bounds(shownEvents()).width();
    }

    @Override
    public float getHeight() {
        return bounds(shownEvents()).height();
    }

    @Override
    public boolean shouldShow() {
        applyGrowthAnchor();
        return !shownEvents().isEmpty();
    }

    @Override
    public Pair<Float, Float> defaultPosition() {
        return new Pair<>((HudManager.guiScreenWidth - getScaledWidth()) / 2f, 3.0F);
    }

    public Bounds liveScreenBounds() {
        List<LerpingBossEvent> events = liveEvents();
        int count = visibleCount(events.size());
        if (count == 0 || getHidden()) {
            return null;
        }
        float scale = getFrameScale();
        Bounds bounds = bounds(events.subList(0, count));
        return Bounds.ofSize(getFrameX(), getFrameY(), bounds.width() * scale, bounds.height() * scale);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics) {
        applyGrowthAnchor();
        List<? extends BossEvent> events = shownEvents();
        BossHealthOverlay overlay = overlay();
        if (events.isEmpty() || overlay == null) {
            return;
        }

        Font font = font();
        float width = contentWidth(events);
        Bounds bounds = bounds(events);
        int color = Colors.argb(nameColor);
        int barX = centered(width, BAR_WIDTH);

        graphics.pose().pushMatrix();
        graphics.pose().translate(-bounds.left(), -bounds.top());

        for (int i = 0; i < events.size(); i++) {
            BossEvent event = events.get(i);
            int top = i * STRIDE;

            Component name = event.getName();
            TextRenderUtil.drawText(graphics, font, name, centered(width, font.width(name)), top, color,
                    nameTextShadow);
            ((BossHealthOverlayAccessor) overlay).invokeExtractBar(graphics, barX, top + NAME_HEIGHT, event);
        }

        graphics.pose().popMatrix();
    }
}
