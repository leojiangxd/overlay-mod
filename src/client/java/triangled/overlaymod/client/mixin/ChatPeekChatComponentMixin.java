package triangled.overlaymod.client.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import triangled.overlaymod.client.feature.ChatPeek;

// Makes chat act as if it's open while peeking.
@Mixin(ChatComponent.class)
public class ChatPeekChatComponentMixin {
    // Reports chat as focused while peeking so older messages stay visible.
    @WrapMethod(method = "isChatFocused")
    private boolean overlayMod$peekFocusesChat(Operation<Boolean> original) {
        return ChatPeek.isPeeking() || original.call();
    }
}
