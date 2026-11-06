package triangled.overlaymod.client.utils;

import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud;
import triangled.overlaymod.client.hud.BossBarHud;

public class BossBarUtil {
    private static final float GAP = 1.0F;

    private BossBarUtil() {
    }

    public static float pushDown(LegacyHud hud) {
        float scale = Math.max(0.01F, hud.getFrameScale());
        float left = hud.getFrameX();
        float top = hud.getFrameY();
        float right = left + hud.getWidth() * scale;
        float bottom = top + hud.getHeight() * scale;
        float push = 0F;

        for (Hud other : HudManager.INSTANCE.getActiveInstances()) {
            if (!(other instanceof BossBarHud bossBars))
                continue;

            float[] bars = bossBars.liveScreenBounds();
            if (bars == null)
                continue;
            if (!overlaps(left, top, right, bottom, bars[0], bars[1], bars[2], bars[3]))
                continue;
            push = Math.max(push, bars[3] + GAP - top);
        }

        if (!BossBarHud.replacesVanilla()) {
            float barsBottom = vanillaBottom();
            float center = HudManager.guiScreenWidth / 2F;
            if (barsBottom > 0 && overlaps(left, top, right, bottom, center - 91, 3F, center + 91, barsBottom)) {
                push = Math.max(push, barsBottom + GAP - top);
            }
        }

        return push / scale;
    }

    private static boolean overlaps(float l1, float t1, float r1, float b1, float l2, float t2, float r2, float b2) {
        return l1 < r2 && l2 < r1 && t1 < b2 && t2 < b1;
    }

    private static float vanillaBottom() {
        int count = BossBarHud.liveEvents().size();
        if (count <= 0) {
            return 0F;
        }
        float next = 12F;
        for (int i = 0; i < count; i++) {
            next += 19F;
            if (next >= HudManager.guiScreenHeight / 3F) {
                break;
            }
        }
        return next - 14F;
    }
}
