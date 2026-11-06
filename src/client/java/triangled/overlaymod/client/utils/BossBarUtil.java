package triangled.overlaymod.client.utils;

import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import triangled.overlaymod.client.hud.BossBarHud;

public class BossBarUtil {
    private static final float GAP = 1.0F;

    private BossBarUtil() {
    }

    public static float pushDown(Hud hud) {
        float[] span = {hud.getFrameX(), hud.getFrameX() + hud.getScaledWidth()};
        float top = hud.getFrameY();
        float push = 0F;

        for (Hud other : HudManager.INSTANCE.getActiveInstances()) {
            if (other instanceof BossBarHud bossBars) {
                float[] bounds = bossBars.liveScreenBounds();
                if (bounds != null && bossBars.getFrameY() <= top && overlaps(span, bounds[0], bounds[1])) {
                    push = Math.max(push, bounds[2] + GAP - top);
                }
            }
        }

        if (!BossBarHud.replacesVanilla()) {
            float vanillaBottom = vanillaBottom();
            float center = HudManager.guiScreenWidth / 2F;
            if (vanillaBottom > 0 && overlaps(span, center - 91, center + 91)) {
                push = Math.max(push, vanillaBottom + GAP - top);
            }
        }

        return Math.max(0F, push) / Math.max(0.01F, hud.getFrameScale());
    }

    private static boolean overlaps(float[] span, float left, float right) {
        return span[0] < right && left < span[1];
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
