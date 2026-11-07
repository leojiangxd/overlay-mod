package triangled.overlaymod.client.feature;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import triangled.overlaymod.client.OverlayModConfig;

public final class ChatPeek {
    private static boolean keyHeld;
    private static boolean toggledOn;
    private static boolean peeking;

    private ChatPeek() {
    }

    public static boolean isPeeking() {
        return peeking;
    }

    public static void setKeyHeld(boolean held) {
        if (held && !keyHeld && OverlayModConfig.chatPeekToggle) {
            toggledOn = !toggledOn;
        }
        keyHeld = held;
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(ChatPeek::tick);
    }

    private static void tick(Minecraft minecraft) {
        boolean featureEnabled = OverlayModConfig.enabled && OverlayModConfig.chatPeekEnabled;
        if (!featureEnabled || !OverlayModConfig.chatPeekToggle) {
            toggledOn = false;
        }
        boolean shouldPeek = featureEnabled && (OverlayModConfig.chatPeekToggle ? toggledOn : keyHeld);
        if (peeking && !shouldPeek && minecraft.gui != null) {
            minecraft.gui.hud.getChat().resetChatScroll();
        }
        peeking = shouldPeek;
    }
}
