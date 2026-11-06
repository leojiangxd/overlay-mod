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
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud;
import triangled.overlaymod.client.mixin.BossHealthOverlayAccessor;
import triangled.overlaymod.client.mixin.HudAccessor;
import triangled.overlaymod.client.utils.TextRenderUtil;
import triangled.overlaymod.client.utils.TextShadow;

public class BossBarHud extends LegacyHud {
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int NAME_HEIGHT = 9;
    private static final int STRIDE = 19;

    private static final LerpingBossEvent PREVIEW = new LerpingBossEvent(UUID.randomUUID(),
            Component.literal("Boss"), 0.75F, BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.NOTCHED_10,
            false, false, false);

    @Switch(title = "Hide Vanilla Boss Bars", description = "Hide the vanilla boss bars while this HUD is shown")
    public boolean hideVanilla = true;

    // ---------------------------------------------------------------- Layout

    @Slider(title = "Max Height", description = "Stop adding bars once they fill 1/n of the screen height",
            min = 1, max = 10, step = 1, subcategory = "Layout")
    public int maxHeight = 6;

    // ---------------------------------------------------------------- Style

    @Dropdown(title = "Name Shadow", description = "How the boss name is shadowed or outlined", subcategory = "Style")
    public TextShadow nameTextShadow = TextShadow.SHADOW;

    @Color(title = "Name Color", description = "Colour of any part of the name not coloured by the server",
            subcategory = "Style")
    public PolyColor nameColor = new PolyColor(0xFFFFFFFF, false, 1.0F);

    public BossBarHud() {
        super("overlaymod_boss_bars.yml", "Boss Bars", Hud.Category.Companion.getINFO());
        setCustomScale(0.5F);
    }

    public static boolean replacesVanilla() {
        for (Hud hud : HudManager.INSTANCE.getActiveInstances()) {
            if (hud instanceof BossBarHud bossBars && bossBars.hideVanilla && !bossBars.getHidden()) {
                return true;
            }
        }
        return false;
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
        return overlay == null ? List.of() : new ArrayList<>(((BossHealthOverlayAccessor) overlay).getEvents().values());
    }

    private List<? extends BossEvent> shownEvents() {
        List<LerpingBossEvent> events = liveEvents();
        if (events.isEmpty() && HudManager.INSTANCE.isEditing()) {
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

    private float contentWidth(List<? extends BossEvent> events) {
        Font font = Minecraft.getInstance().font;
        int width = BAR_WIDTH;
        for (BossEvent event : events) {
            width = Math.max(width, font.width(event.getName()));
        }
        return width;
    }

    private float[] bounds(List<? extends BossEvent> events) {
        Font font = Minecraft.getInstance().font;
        float width = contentWidth(events);
        float[] bounds = {0, 0, width, Math.max(1, contentHeight(events.size()))};
        for (int i = 0; i < events.size(); i++) {
            int nameWidth = font.width(events.get(i).getName());
            float[] box = TextRenderUtil.textBounds(font, nameWidth, nameX(width, nameWidth), i * STRIDE, 1F,
                    nameTextShadow);
            bounds[0] = Math.min(bounds[0], box[0]);
            bounds[1] = Math.min(bounds[1], box[1]);
            bounds[2] = Math.max(bounds[2], box[2]);
            bounds[3] = Math.max(bounds[3], box[3]);
        }
        return bounds;
    }

    private static int nameX(float width, int nameWidth) {
        return Math.round((width - nameWidth) / 2f);
    }

    @Override
    public float getWidth() {
        float[] bounds = bounds(shownEvents());
        return bounds[2] - bounds[0];
    }

    @Override
    public float getHeight() {
        float[] bounds = bounds(shownEvents());
        return bounds[3] - bounds[1];
    }

    @Override
    public boolean shouldShow() {
        return !shownEvents().isEmpty();
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
        return new Pair<>((HudManager.guiScreenWidth - getScaledWidth()) / 2f, 3.0F);
    }

    public float[] liveScreenBounds() {
        List<LerpingBossEvent> events = liveEvents();
        int count = visibleCount(events.size());
        if (count == 0 || getHidden()) {
            return null;
        }
        float scale = getFrameScale();
        float[] bounds = bounds(events.subList(0, count));
        float left = getFrameX();
        float top = getFrameY();
        return new float[] {left, top, left + (bounds[2] - bounds[0]) * scale, top + (bounds[3] - bounds[1]) * scale};
    }

    @Override
    public void render(GuiGraphicsExtractor graphics) {
        List<? extends BossEvent> events = shownEvents();
        BossHealthOverlay overlay = overlay();
        if (events.isEmpty() || overlay == null) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        float width = contentWidth(events);
        float[] bounds = bounds(events);
        int color = nameColor == null ? 0xFFFFFFFF : nameColor.getArgb();

        graphics.pose().pushMatrix();
        graphics.pose().translate(-bounds[0], -bounds[1]);

        for (int i = 0; i < events.size(); i++) {
            BossEvent event = events.get(i);
            int top = i * STRIDE;

            Component name = event.getName();
            TextRenderUtil.drawText(graphics, font, name, nameX(width, font.width(name)), top, color, nameTextShadow);

            int barX = Math.round((width - BAR_WIDTH) / 2f);
            ((BossHealthOverlayAccessor) overlay).invokeExtractBar(graphics, barX, top + NAME_HEIGHT, event);
        }

        graphics.pose().popMatrix();
    }
}
