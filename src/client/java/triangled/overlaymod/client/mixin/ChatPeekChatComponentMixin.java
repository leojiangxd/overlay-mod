package triangled.overlaymod.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import triangled.overlaymod.client.ChatPeek;

@Mixin(ChatComponent.class)
public class ChatPeekChatComponentMixin {
    @WrapMethod(method = "isChatFocused")
    private boolean overlayMod$peekFocusesChat(Operation<Boolean> original) {
        return ChatPeek.isPeeking() || original.call();
    }
}
