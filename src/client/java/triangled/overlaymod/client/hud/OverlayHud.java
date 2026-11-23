package triangled.overlaymod.client.hud;

import androidx.compose.runtime.Composer;
import androidx.compose.runtime.MutableState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import org.polyfrost.compose.composables.PolyModifier;
import org.polyfrost.compose.render.RenderContext;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudAnchor;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import triangled.overlaymod.client.utils.Compose;
import triangled.overlaymod.client.utils.HudTextStyle;

public abstract class OverlayHud extends Hud {
    protected record Size(float width, float height) {
        static final Size EMPTY = new Size(1, 1);
    }

    protected record Layout(float originX, float originY, float boxWidth, float boxHeight) {
    }

    private transient MutableState<Size> contentSize = Compose.state(Size.EMPTY);
    private transient MutableState<Float> pushX = Compose.state(0f);
    private transient MutableState<Float> pushY = Compose.state(0f);
    private transient HudTextStyle textStyle;

    protected OverlayHud(String id, String title, Hud.Category category) {
        super(id, title, category);
        setShowBackground(false);
        setShowShadow(true);
    }

    protected static boolean isEditing() {
        return HudManager.INSTANCE.isEditing();
    }

    protected static Font font() {
        return Minecraft.getInstance().font;
    }

    protected static LocalPlayer player() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft == null ? null : minecraft.player;
    }

    protected HudTextStyle text() {
        if (textStyle == null) {
            textStyle = new HudTextStyle(this);
        }
        return textStyle;
    }

    protected abstract void refresh();

    // Draws the HUD. Each HUD overrides this or content() to draw itself.
    protected void paint(RenderContext renderContext, float x, float y) {
    }

    protected void setSize(float width, float height) {
        contentSize.setValue(new Size(Math.max(1, width), Math.max(1, height)));
    }

    // Moves the whole HUD, background included, without changing its saved
    // position.
    protected void setPush(float x, float y) {
        pushX.setValue(x);
        pushY.setValue(y);
    }

    @Override
    public boolean update() {
        textStyle = new HudTextStyle(this);
        refresh();
        return true;
    }

    @Override
    public boolean getAlwaysRedraw() {
        return true;
    }

    @Override
    public kotlin.Pair<Float, Float> minimumSize() {
        return new kotlin.Pair<>(getPadLeft() + getPadRight() + 1f, getPadTop() + getPadBottom() + 1f);
    }

    private static float fractionX(HudAnchor anchor) {
        return switch (anchor) {
            case TopLeft, Left, BottomLeft, Auto -> 0f;
            case Top, Center, Bottom -> 0.5f;
            case TopRight, Right, BottomRight -> 1f;
        };
    }

    private static float fractionY(HudAnchor anchor) {
        return switch (anchor) {
            case TopLeft, Top, TopRight, Auto -> 0f;
            case Left, Center, Right -> 0.5f;
            case BottomLeft, Bottom, BottomRight -> 1f;
        };
    }

    private Layout layout(Size measuredSize) {
        float paddedWidth = measuredSize.width() + getPadLeft() + getPadRight();
        float paddedHeight = measuredSize.height() + getPadTop() + getPadBottom();
        boolean hasFixedSize = getStaticWidth() && getStaticW() > 0f && getStaticH() > 0f;
        if (!hasFixedSize) {
            return new Layout(getPadLeft(), getPadTop(), paddedWidth, paddedHeight);
        }
        HudAnchor alignmentAnchor = HudAnchor.of(getAlignment());
        float boxWidth = getStaticW();
        float boxHeight = getStaticH();
        return new Layout(
                getPadLeft() + (boxWidth - paddedWidth) * fractionX(alignmentAnchor),
                getPadTop() + (boxHeight - paddedHeight) * fractionY(alignmentAnchor),
                boxWidth,
                boxHeight);
    }

    @Override
    public void Content(Composer composer, int changedFlags) {
        Layout layout = layout(contentSize.getValue());
        PolyModifier size = Compose.sized(layout.boxWidth(), layout.boxHeight());
        // Outer box is not absolute because polycompose 3 ignores absolute children when sizing the HUD root.
        Compose.box(composer, size, outer -> Compose.box(outer, hudBackground(Compose.at(size, pushX.getValue(), pushY.getValue())),
                childComposer -> content(childComposer, layout)));
    }

    protected void content(Composer composer, Layout layout) {
        Compose.canvas(composer, Compose.at(Compose.sized(layout.boxWidth(), layout.boxHeight()), 0, 0),
                (renderContext, x, y) -> paint(renderContext, x + layout.originX(), y + layout.originY()));
    }

    @Override
    public Hud clone() {
        OverlayHud copy = (OverlayHud) super.clone();
        copy.contentSize = Compose.state(contentSize.getValue());
        copy.pushX = Compose.state(0f);
        copy.pushY = Compose.state(0f);
        copy.textStyle = null;
        return copy;
    }
}
