package triangled.overlaymod.client.utils;

import org.polyfrost.compose.render.PolyColor;

public final class Colors {
    public static final int WHITE = 0xFFFFFFFF;

    private Colors() {
    }

    public static PolyColor white() {
        return new PolyColor(WHITE, false, 1.0F);
    }

    public static int argb(PolyColor color) {
        return color == null ? WHITE : color.getArgb();
    }
}
