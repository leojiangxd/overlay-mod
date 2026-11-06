package triangled.overlaymod.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public final class ChatPeek {
    private static boolean keyHeld = false;
    private static boolean toggledOn = false;
    private static boolean peeking = false;

    private ChatPeek() {
    }

    public static boolean isPeeking() {
        return peeking;
    }

    static void setKeyHeld(boolean held) {
        if (held && !keyHeld && OverlayModConfig.chatPeekToggle) {
            toggledOn = !toggledOn;
        }
        keyHeld = held;
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(ChatPeek::tick);
    }

    private static void tick(Minecraft client) {
        boolean active = OverlayModConfig.enabled && OverlayModConfig.chatPeekEnabled;
        if (!active || !OverlayModConfig.chatPeekToggle) {
            toggledOn = false;
        }
        boolean nowPeeking = active && (OverlayModConfig.chatPeekToggle ? toggledOn : keyHeld);
        if (peeking && !nowPeeking && client.gui != null && client.gui.hud != null) {
            client.gui.hud.getChat().resetChatScroll();
        }
        peeking = nowPeeking;
    }
}
