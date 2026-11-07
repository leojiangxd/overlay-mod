package triangled.overlaymod.client.utils;

import java.util.Collection;
import java.util.Optional;

// A rectangle stored as its left, top, right and bottom edges.
public record Bounds(float left, float top, float right, float bottom) {
    // Returns a rectangle from its top-left corner and size.
    public static Bounds ofSize(float x, float y, float width, float height) {
        return new Bounds(x, y, x + width, y + height);
    }

    // Returns the smallest rectangle around all the given ones, if any.
    public static Optional<Bounds> enclosing(Collection<Bounds> boxes) {
        return boxes.stream().reduce(Bounds::union);
    }

    // Returns the rectangle's width.
    public float width() {
        return right - left;
    }

    // Returns the rectangle's height.
    public float height() {
        return bottom - top;
    }

    // Returns the smallest rectangle that covers this one and another.
    public Bounds union(Bounds other) {
        return new Bounds(
                Math.min(left, other.left),
                Math.min(top, other.top),
                Math.max(right, other.right),
                Math.max(bottom, other.bottom));
    }

    // Returns true if this rectangle and another overlap.
    public boolean overlaps(Bounds other) {
        return left < other.right && other.left < right && top < other.bottom && other.top < bottom;
    }
}
