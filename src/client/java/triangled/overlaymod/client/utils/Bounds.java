package triangled.overlaymod.client.utils;

import java.util.Collection;
import java.util.Optional;

public record Bounds(float left, float top, float right, float bottom) {
    public static Bounds ofSize(float x, float y, float width, float height) {
        return new Bounds(x, y, x + width, y + height);
    }

    public static Optional<Bounds> enclosing(Collection<Bounds> boxes) {
        return boxes.stream().reduce(Bounds::union);
    }

    public float width() {
        return right - left;
    }

    public float height() {
        return bottom - top;
    }

    public Bounds union(Bounds other) {
        return new Bounds(
                Math.min(left, other.left),
                Math.min(top, other.top),
                Math.max(right, other.right),
                Math.max(bottom, other.bottom));
    }

    public boolean overlaps(Bounds other) {
        return left < other.right && other.left < right && top < other.bottom && other.top < bottom;
    }
}
