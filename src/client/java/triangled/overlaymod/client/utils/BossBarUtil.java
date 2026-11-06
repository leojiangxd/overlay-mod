package triangled.overlaymod.client.utils;

import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud;
import triangled.overlaymod.client.hud.BossBarHud;
import triangled.overlaymod.client.hud.OverlayHud;

public final class BossBarUtil {
    private static final float GAP = 1.0F;
    private static final int VANILLA_FIRST_BAR_Y = 12;

    private BossBarUtil() {
    }

    public static float pushDown(LegacyHud hud) {
        float scale = Math.max(0.01F, hud.getFrameScale());
        Bounds frame = Bounds.ofSize(hud.getFrameX(), hud.getFrameY(), hud.getWidth() * scale, hud.getHeight() * scale);
        float push = 0F;

        for (BossBarHud bossBars : OverlayHud.activeHuds(BossBarHud.class)) {
            push = Math.max(push, pushBelow(frame, bossBars.liveScreenBounds()));
        }
        if (!BossBarHud.replacesVanilla()) {
            push = Math.max(push, pushBelow(frame, vanillaBounds()));
        }

        return push / scale;
    }

    private static float pushBelow(Bounds frame, Bounds bars) {
        if (bars == null || !frame.overlaps(bars)) {
            return 0F;
        }
        return bars.bottom() + GAP - frame.top();
    }

    private static Bounds vanillaBounds() {
        int count = BossBarHud.liveEvents().size();
        if (count <= 0) {
            return null;
        }
        float nextBarY = VANILLA_FIRST_BAR_Y;
        for (int i = 0; i < count; i++) {
            nextBarY += BossBarHud.STRIDE;
            if (nextBarY >= HudManager.guiScreenHeight / 3F) {
                break;
            }
        }
        float center = HudManager.guiScreenWidth / 2F;
        float halfWidth = BossBarHud.BAR_WIDTH / 2F;
        return new Bounds(
                center - halfWidth,
                VANILLA_FIRST_BAR_Y - BossBarHud.NAME_HEIGHT,
                center + halfWidth,
                nextBarY - BossBarHud.STRIDE + BossBarHud.BAR_HEIGHT);
    }
}
