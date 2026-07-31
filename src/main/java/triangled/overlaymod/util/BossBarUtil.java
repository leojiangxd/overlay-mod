package triangled.overlaymod.util;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.BossHealthOverlay;
import triangled.overlaymod.config.OverlayModConfig;
import triangled.overlaymod.mixin.BossHealthOverlayAccessor;
import triangled.overlaymod.mixin.HudAccessor;

public class BossBarUtil {

    public static int getBossBarOffset(GuiGraphicsExtractor context, Minecraft client) {
        Gui gui = client.gui;
        if (gui == null) {
            return 0;
        }

        Hud hud = gui.hud;
        BossHealthOverlay bossOverlay = ((HudAccessor) hud).getBossOverlayField();
        int numberOfBossBars = ((BossHealthOverlayAccessor) bossOverlay).getEvents().size();

        if (numberOfBossBars <= 0) {
            return 0;
        }

        OverlayModConfig.BossBarCategory config =
                AutoConfig.getConfigHolder(OverlayModConfig.class).getConfig().bossbar;

        int bossBarOffset = 12;
        float scale = config.shouldScaleBossBars ? config.scale : 1.0F;
        int maxHeight = config.shouldScaleBossBars ? config.maxHeight : 3;

        for (int i = 0; i < numberOfBossBars; i++) {
            bossBarOffset += 19;
            if (bossBarOffset >= (context.guiHeight() / scale) / maxHeight) {
                break;
            }
        }

        return (int) ((bossBarOffset - 12) * scale);
    }
}
