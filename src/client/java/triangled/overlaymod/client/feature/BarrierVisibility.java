package triangled.overlaymod.client.feature;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import triangled.overlaymod.client.OverlayModConfig;

// Lets the player show or hide barrier blocks with a key press.
public final class BarrierVisibility {
    private static boolean visible;

    private BarrierVisibility() {
    }

    // Returns true while barrier blocks should be drawn.
    public static boolean isVisible() {
        return visible;
    }

    // Flips barrier visibility and tells the player about it.
    public static void toggle() {
        if (!OverlayModConfig.enabled) {
            return;
        }
        visible = !visible;
        Minecraft minecraft = Minecraft.getInstance();
        reloadWorld(minecraft);
        if (OverlayModConfig.barrierShowMessage && minecraft.player != null) {
            Component message = visible
                    ? Component.literal("[Barrier Visibility: Enabled]")
                            .withStyle(ChatFormatting.GREEN)
                    : Component.literal("[Barrier Visibility: Disabled]")
                            .withStyle(ChatFormatting.RED);
            minecraft.player.sendOverlayMessage(message);
        }
    }

    // Hooks the per-tick check into the game.
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(BarrierVisibility::tick);
    }

    // Hides barriers again if the whole mod gets turned off.
    private static void tick(Minecraft minecraft) {
        if (visible && !OverlayModConfig.enabled) {
            visible = false;
            reloadWorld(minecraft);
        }
    }

    // Rebuilds the visible world so barrier changes show up.
    private static void reloadWorld(Minecraft minecraft) {
        if (minecraft.levelExtractor != null) {
            minecraft.levelExtractor.allChanged();
        }
    }
}
