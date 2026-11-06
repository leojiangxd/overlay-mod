package triangled.overlaymod.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import triangled.overlaymod.client.OverlayModConfig;
import triangled.overlaymod.client.feature.ChatPeek;

@Mixin(MouseHandler.class)
public class ChatPeekMouseHandlerMixin {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void overlayMod$scrollChatWhilePeeking(long handle, double xoffset, double yoffset, CallbackInfo ci) {
        if (!OverlayModConfig.chatPeekScroll || !ChatPeek.isPeeking() || yoffset == 0.0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gui == null || mc.gui.hud == null) {
            return;
        }
        int lines = OverlayModConfig.chatPeekScrollLines;
        mc.gui.hud.getChat().scrollChat(yoffset > 0.0 ? lines : -lines);
        ci.cancel();
    }
}
