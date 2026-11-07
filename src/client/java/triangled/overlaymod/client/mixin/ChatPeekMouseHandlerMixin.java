package triangled.overlaymod.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import triangled.overlaymod.client.OverlayModConfig;
import triangled.overlaymod.client.feature.ChatPeek;

// Lets the scroll wheel scroll chat while peeking.
@Mixin(MouseHandler.class)
public class ChatPeekMouseHandlerMixin {
    // Scrolls chat instead of the hotbar while peeking when enabled in the config.
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void overlayMod$scrollChatWhilePeeking(long windowHandle, double horizontalScroll, double verticalScroll,
            CallbackInfo callback) {
        if (!OverlayModConfig.chatPeekScroll || !ChatPeek.isPeeking() || verticalScroll == 0.0) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.gui == null) {
            return;
        }
        int scrollLines = OverlayModConfig.chatPeekScrollLines;
        minecraft.gui.hud.getChat().scrollChat(verticalScroll > 0.0 ? scrollLines : -scrollLines);
        callback.cancel();
    }
}
