package com.triangled.overlaymod.mixin;

import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.UUID;

@Mixin(BossBarHud.class)
public class BossBarHudMixin {

    @Shadow
    private Map<UUID, ClientBossBar> bossBars;

    public int getNumberOfBossBars() {
        if (bossBars == null) {
            return 0;
        }
        return bossBars.size();
    }
}
