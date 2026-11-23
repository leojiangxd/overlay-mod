package triangled.overlaymod.client.hud;

import net.minecraft.client.Minecraft;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import triangled.overlaymod.client.mixin.BossHealthOverlayAccessor;
import triangled.overlaymod.client.mixin.HudAccessor;
import triangled.overlaymod.client.utils.Bounds;

// Finds where boss bars sit on screen: VanillaHUD's boss bar HUD when that mod
// is installed, otherwise vanilla's own layout.
public final class BossBars {
    // Matched by name so VanillaHUD stays optional.
    private static final String VANILLAHUD_BOSS_BAR = "org.polyfrost.vanillahud.hud.BossBarHud";
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int NAME_HEIGHT = 9;
    private static final int BAR_SPACING = 19;
    private static final int FIRST_BAR_Y = 12;
    private static final float GAP = 1.0F;

    private BossBars() {
    }

    public static float pushBelow(Hud hud, float extraGap) {
        Bounds bars = screenBounds();
        Bounds hudBounds = Bounds.of(hud);
        if (bars == null || !hudBounds.overlaps(bars)) {
            return 0F;
        }
        float pushDown = Math.max(0F, bars.bottom() + GAP + extraGap - hudBounds.top());
        return pushDown / Math.max(0.01F, hud.getEffectiveScale());
    }

    private static Bounds screenBounds() {
        for (Hud hud : HudManager.INSTANCE.getActiveInstances()) {
            if (hud.getClass().getName().equals(VANILLAHUD_BOSS_BAR)) {
                return !hud.getHidden() && hud.shouldShow() ? Bounds.of(hud) : null;
            }
        }
        return vanillaBounds();
    }

    private static Bounds vanillaBounds() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui == null) {
            return null;
        }
        BossHealthOverlayAccessor overlay = (BossHealthOverlayAccessor) ((HudAccessor) minecraft.gui.hud)
                .getBossOverlayField();
        int barCount = overlay.getEvents().size();
        if (barCount == 0) {
            return null;
        }
        // Vanilla stops adding bars once they reach a third of the screen height.
        float nextBarY = FIRST_BAR_Y;
        for (int i = 0; i < barCount && nextBarY < HudManager.guiScreenHeight / 3F; i++) {
            nextBarY += BAR_SPACING;
        }
        float screenCenterX = HudManager.guiScreenWidth / 2F;
        return new Bounds(screenCenterX - BAR_WIDTH / 2F, FIRST_BAR_Y - NAME_HEIGHT,
                screenCenterX + BAR_WIDTH / 2F, nextBarY - BAR_SPACING + BAR_HEIGHT);
    }
}
