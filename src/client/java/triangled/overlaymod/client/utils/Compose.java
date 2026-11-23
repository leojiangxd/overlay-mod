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
import org.polyfrost.compose.minecraft.mc.McItemIconKt;

public final class Compose {
    private Compose() {
    }

    @FunctionalInterface
    public interface Painter {
        void paint(RenderContext renderContext, float x, float y);
    }

    public static <T> MutableState<T> state(T initialValue) {
        return SnapshotStateKt.mutableStateOf(initialValue, SnapshotStateKt.structuralEqualityPolicy());
    }

    public static PolyModifier sized(float width, float height) {
        return PolyModifierKt.size(PolyModifier.Companion, width, height);
    }

    public static PolyModifier at(PolyModifier modifier, float x, float y) {
        return PolyModifierKt.absoluteAt(modifier, x, y);
    }

    public static void box(Composer composer, PolyModifier modifier, Consumer<Composer> content) {
        Function2<Composer, Integer, Unit> composableBody = (childComposer, changedFlags) -> {
            content.accept(childComposer);
            return Unit.INSTANCE;
        };
        ComposablesKt.PolyBox(modifier, composableBody, composer, 0, 0);
    }

    public static void canvas(Composer composer, PolyModifier modifier, Painter painter) {
        Function5<RenderContext, Float, Float, Float, Float, Unit> drawCallback = (renderContext, x, y, width,
                height) -> {
            painter.paint(renderContext, x, y);
            return Unit.INSTANCE;
        };
        ComposablesKt.PolyCanvas(modifier, true, drawCallback, composer, 0, 0);
    }

    public static void item(Composer composer, ItemStack stack, float size, PolyModifier modifier) {
        // 96 = default mask: alpha and maxScale use library defaults
        McItemIconKt.NodeMcItemIcon(stack, size, modifier, false, null, 0f, 0f, composer, 0, 96);
    }
}
