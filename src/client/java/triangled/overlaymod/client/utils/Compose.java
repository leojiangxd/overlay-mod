package triangled.overlaymod.client.utils;

import androidx.compose.runtime.Composer;
import androidx.compose.runtime.MutableState;
import androidx.compose.runtime.SnapshotStateKt;
import java.util.function.Consumer;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function5;
import net.minecraft.world.item.ItemStack;
import org.polyfrost.compose.composables.ComposablesKt;
import org.polyfrost.compose.composables.PolyModifier;
import org.polyfrost.compose.composables.PolyModifierKt;
import org.polyfrost.compose.render.RenderContext;
import org.polyfrost.oneconfig.api.ui.v1.item.PolyItemIconKt;

// Shortcuts for calling Compose and PolyUI
public final class Compose {
    private Compose() {
    }

    // Code that draws inside a canvas.
    @FunctionalInterface
    public interface Painter {
        void paint(RenderContext renderContext, float x, float y);
    }

    // Returns a Compose state that redraws the HUD when its value changes.
    public static <T> MutableState<T> state(T initialValue) {
        return SnapshotStateKt.mutableStateOf(initialValue, SnapshotStateKt.structuralEqualityPolicy());
    }

    // Returns a modifier with the given width and height.
    public static PolyModifier sized(float width, float height) {
        return PolyModifierKt.size(PolyModifier.Companion, width, height);
    }

    // Returns the modifier placed at an exact position.
    public static PolyModifier at(PolyModifier modifier, float x, float y) {
        return PolyModifierKt.absoluteAt(modifier, x, y);
    }

    // Draws a box and the content inside it.
    public static void box(Composer composer, PolyModifier modifier, Consumer<Composer> content) {
        Function2<Composer, Integer, Unit> composableBody = (childComposer, changedFlags) -> {
            content.accept(childComposer);
            return Unit.INSTANCE;
        };
        ComposablesKt.PolyBox(modifier, composableBody, composer, 0, 0);
    }

    // Draws a canvas that runs the painter to draw on it.
    public static void canvas(Composer composer, PolyModifier modifier, Painter painter) {
        Function5<RenderContext, Float, Float, Float, Float, Unit> drawCallback = (renderContext, x, y, width,
                height) -> {
            painter.paint(renderContext, x, y);
            return Unit.INSTANCE;
        };
        ComposablesKt.PolyCanvas(modifier, true, drawCallback, composer, 0, 0);
    }

    // Draws an item icon at the given size.
    public static void item(Composer composer, ItemStack stack, float size, PolyModifier modifier) {
        PolyItemIconKt.PolyItemIcon(stack, size, modifier, false, null, composer, 0, 0);
    }
}
