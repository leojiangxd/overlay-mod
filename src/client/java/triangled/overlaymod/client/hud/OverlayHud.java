package triangled.overlaymod.client.hud;

import androidx.compose.runtime.Composer;
import androidx.compose.runtime.MutableState;
import java.util.ArrayList;
import java.util.List;
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

// This is a base class for all HUDs in Overlay Mod. It handles the HUD's size, background, and text settings.
public abstract class OverlayHud extends Hud {
    // The width and height of a HUD's content, without padding.
    protected record Size(float width, float height) {
        static final Size EMPTY = new Size(1, 1);
    }

    // Where the content starts inside the HUD and how big the HUD is.
    protected record Layout(float originX, float originY, float boxWidth, float boxHeight) {
    }

    private transient MutableState<Size> contentSize = Compose.state(Size.EMPTY);
    private transient MutableState<Float> pushDown = Compose.state(0f);
    private transient HudTextStyle textStyle;

    protected OverlayHud(String id, String title, Hud.Category category) {
        super(id, title, category);
        setShowBackground(false);
        setShowShadow(true);
    }

    // Returns every active HUD of the given type.
    public static <T extends Hud> List<T> activeHuds(Class<T> hudType) {
        List<T> matchingHuds = new ArrayList<>();
        for (Hud hud : HudManager.INSTANCE.getActiveInstances()) {
            if (hudType.isInstance(hud)) {
                matchingHuds.add(hudType.cast(hud));
            }
        }
        return matchingHuds;
    }

    // Returns true while the OneConfig HUD editor is open.
    protected static boolean isEditing() {
        return HudManager.INSTANCE.isEditing();
    }

    // Returns Minecraft's font.
    protected static Font font() {
        return Minecraft.getInstance().font;
    }

    // Returns the player, or null when not in a world.
    protected static LocalPlayer player() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft == null ? null : minecraft.player;
    }

    // Returns this HUD's text settings, creating them if needed.
    protected HudTextStyle text() {
        if (textStyle == null) {
            textStyle = HudTextStyle.of(this);
        }
        return textStyle;
    }

    // Called every update so each HUD can grab its latest info and resize.
    protected abstract void refresh();

    // Draws the HUD. Each HUD overrides this or content() to draw itself.
    protected void paint(RenderContext renderContext, float x, float y) {
    }

    // Sets the content size, at least 1x1, so the HUD resizes to fit.
    protected void setSize(float width, float height) {
        contentSize.setValue(new Size(Math.max(1, width), Math.max(1, height)));
    }

    // Moves the whole HUD, background included, down without changing its
    // saved position.
    protected void setPushDown(float distance) {
        pushDown.setValue(distance);
    }

    // OneConfig calls this every update to reload text settings and refresh.
    @Override
    public boolean update() {
        textStyle = HudTextStyle.of(this);
        refresh();
        return true;
    }

    // Tells OneConfig to redraw the HUD every frame.
    @Override
    public boolean getAlwaysRedraw() {
        return true;
    }

    // Lets OneConfig shrink the HUD down to just its padding plus 1 pixel.
    @Override
    public kotlin.Pair<Float, Float> minimumSize() {
        return new kotlin.Pair<>(getPadLeft() + getPadRight() + 1f, getPadTop() + getPadBottom() + 1f);
    }

    // Returns how far across an anchor sits: 0 left, 0.5 center, 1 right.
    private static float fractionX(HudAnchor anchor) {
        return switch (anchor) {
            case TopLeft, Left, BottomLeft, Auto -> 0f;
            case Top, Center, Bottom -> 0.5f;
            case TopRight, Right, BottomRight -> 1f;
        };
    }

    // Returns how far down an anchor sits: 0 top, 0.5 center, 1 bottom.
    private static float fractionY(HudAnchor anchor) {
        return switch (anchor) {
            case TopLeft, Top, TopRight, Auto -> 0f;
            case Left, Center, Right -> 0.5f;
            case BottomLeft, Bottom, BottomRight -> 1f;
        };
    }

    // Works out the HUD's size and where its content starts, aligning it
    // inside a fixed size when one is set.
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

    // OneConfig draws the HUD with this: background first, then content(),
    // both moved down by the push-down distance.
    @Override
    public void Content(Composer composer, int changedFlags) {
        Layout layout = layout(contentSize.getValue());
        PolyModifier box = Compose.at(Compose.sized(layout.boxWidth(), layout.boxHeight()), 0, pushDown.getValue());
        Compose.box(composer, hudBackground(box), childComposer -> content(childComposer, layout));
    }

    // Draws the HUD's content by calling paint(). Some HUDs override this.
    protected void content(Composer composer, Layout layout) {
        Compose.canvas(composer, Compose.at(Compose.sized(layout.boxWidth(), layout.boxHeight()), 0, 0),
                (renderContext, x, y) -> paint(renderContext, x + layout.originX(), y + layout.originY()));
    }

    // Gives each copy of the HUD its own size, push-down and text settings.
    @Override
    public Hud clone() {
        OverlayHud copy = (OverlayHud) super.clone();
        copy.contentSize = Compose.state(contentSize.getValue());
        copy.pushDown = Compose.state(0f);
        copy.textStyle = null;
        return copy;
    }
}
