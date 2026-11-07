package triangled.overlaymod.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import triangled.overlaymod.client.feature.ChatPeek;

// Draws chat in full while peeking, even though the chat screen is closed.
@Mixin(Hud.class)
public class ChatPeekHudMixin {
        // Draws chat in foreground mode while peeking, like when chat is open.
        @ModifyArg(method = "extractChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V"), index = 5)
        private ChatComponent.DisplayMode overlayMod$peekDisplayMode(ChatComponent.DisplayMode original) {
                return ChatPeek.isPeeking() ? ChatComponent.DisplayMode.FOREGROUND : original;
        }

        // Treats chat as focused here only when the chat screen is really open,
        // so the HUD still draws chat while peeking.
        @ModifyExpressionValue(method = "extractChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;isChatFocused()Z"))
        private boolean overlayMod$onlySkipForChatScreen(boolean original) {
                return Minecraft.getInstance().gui.screen() instanceof ChatScreen;
        }
}
