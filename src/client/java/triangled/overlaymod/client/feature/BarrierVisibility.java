package triangled.overlaymod.client.feature;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import triangled.overlaymod.client.OverlayModConfig;

public final class BarrierVisibility {
    private static boolean visible;

    private BarrierVisibility() {
    }

    public static boolean isVisible() {
        return visible;
    }

    public static void toggle() {
        if (!OverlayModConfig.enabled) {
            return;
        }
        visible = !visible;
        Minecraft minecraft = Minecraft.getInstance();
        reloadWorld(minecraft);
        if (OverlayModConfig.barrierShowMessage && minecraft.player != null) {
            minecraft.player.sendOverlayMessage(Component.literal("[Barrier Visibility: " + (visible ? "Enabled" : "Disabled") + "]")
                    .withStyle(visible ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(BarrierVisibility::tick);
    }

    private static void tick(Minecraft minecraft) {
        if (visible && !OverlayModConfig.enabled) {
            visible = false;
            reloadWorld(minecraft);
        }
    }

    private static void reloadWorld(Minecraft minecraft) {
        if (minecraft.levelExtractor != null) {
            minecraft.levelExtractor.allChanged();
        }
    }
}
