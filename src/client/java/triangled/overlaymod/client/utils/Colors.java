package triangled.overlaymod.client.utils;

import org.polyfrost.compose.render.PolyColor;

// Shared color helpers.
public final class Colors {
    public static final int WHITE = 0xFFFFFFFF;

    private Colors() {
    }

    // Returns plain white, used for images that shouldn't be tinted.
    public static PolyColor white() {
        return new PolyColor(WHITE, false, 1.0F);
    }

    // Returns a color as an ARGB int, or white if it's null.
    public static int argb(PolyColor color) {
        return color == null ? WHITE : color.getArgb();
    }
}
