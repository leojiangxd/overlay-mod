package triangled.overlaymod.client.utils;

import java.util.List;

public enum TextShadow {
    NONE(Insets.NONE, false, List.of()),
    SHADOW(new Insets(0, 0, 1, 1), true, List.of()),
    FOUR_DIRECTION(Insets.uniform(1), false, List.of(
            new Offset(-1, 0), new Offset(1, 0), new Offset(0, -1), new Offset(0, 1))),
    EIGHT_DIRECTION(Insets.uniform(1), false, List.of(
            new Offset(-1, 0), new Offset(1, 0), new Offset(0, -1), new Offset(0, 1),
            new Offset(-1, -1), new Offset(1, -1), new Offset(-1, 1), new Offset(1, 1)));

    public record Offset(int dx, int dy) {
    }

    private final Insets insets;
    private final boolean dropShadow;
    private final List<Offset> outline;

    TextShadow(Insets insets, boolean dropShadow, List<Offset> outline) {
        this.insets = insets;
        this.dropShadow = dropShadow;
        this.outline = outline;
    }

    public static TextShadow orDefault(TextShadow shadow) {
        return shadow == null ? SHADOW : shadow;
    }

    public Insets insets() {
        return insets;
    }

    public boolean dropShadow() {
        return dropShadow;
    }

    public List<Offset> outline() {
        return outline;
    }

    public boolean hasOutline() {
        return !outline.isEmpty();
    }
}
