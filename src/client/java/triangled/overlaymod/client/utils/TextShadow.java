package triangled.overlaymod.client.utils;

/**
 * How {@link TextRenderUtil} draws the backing behind a piece of text.
 *
 * <p>{@link #FOUR_DIRECTION} and {@link #EIGHT_DIRECTION} draw a full outline
 * whose colour is derived per colour run, which is why they are rendered here
 * rather than through OneConfig's own single-colour full shadow.
 * </p>
 */
public enum TextShadow {
    NONE, SHADOW, FOUR_DIRECTION, EIGHT_DIRECTION
}
