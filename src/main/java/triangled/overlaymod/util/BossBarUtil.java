package triangled.overlaymod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.BossHealthOverlay;
import triangled.overlaymod.OverlayMod;
import triangled.overlaymod.config.OverlayModConfig;
import triangled.overlaymod.mixin.BossHealthOverlayAccessor;
import triangled.overlaymod.mixin.HudAccessor;

public class BossBarUtil {

    public static float getBossBarOffset(GuiGraphicsExtractor context, Minecraft client) {
        Gui gui = client.gui;
        if (gui == null) {
            return 0.0F;
        }

        Hud hud = gui.hud;
        BossHealthOverlay bossOverlay = ((HudAccessor) hud).getBossOverlayField();
        int numberOfBossBars = ((BossHealthOverlayAccessor) bossOverlay).getEvents().size();

        if (numberOfBossBars <= 0) {
            return 0.0F;
        }

        OverlayModConfig.BossBarCategory config = OverlayMod.config.bossbar;

        boolean scaled = config.visibility.shouldScaleBossBars;
        float initialOffset = scaled ? 9.0F : 12.0F;
        float bossBarOffset = initialOffset;
        float scale = scaled ? config.positioning.scale : 1.0F;
        int maxHeight = scaled ? config.positioning.maxHeight : 3;
        float yOffset = scaled ? config.positioning.yOffset : 0.0F;

        for (int i = 0; i < numberOfBossBars; i++) {
            bossBarOffset += 19.0F;
            if (bossBarOffset >= (context.guiHeight() / scale) / maxHeight) {
                break;
            }
        }

        float contentBottom = bossBarOffset - 14.0F;

        return contentBottom * scale - yOffset;
    }
}