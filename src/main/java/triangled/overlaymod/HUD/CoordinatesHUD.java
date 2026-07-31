package triangled.overlaymod.HUD;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import triangled.overlaymod.OverlayMod;
import triangled.overlaymod.config.OverlayModConfig;

import static triangled.overlaymod.config.OverlayModConfig.replaceAnd;

public class CoordinatesHUD {
    private static final int X_PADDING = 3;
    private static final int Y_PADDING = 3;
    private static final int COLOR = 0xFFFFFFFF;
    private static final boolean TEXT_SHADOW = true;

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        OverlayModConfig config = OverlayMod.config;
        if (config == null) return;

        OverlayModConfig.CoordinatesCategory coordsConfig = config.coordinates;
        if (!coordsConfig.showCoordinates || client.gui.hud.isHidden()) return;

        LocalPlayer player = client.player;
        if (player == null || client.level == null) return;

        int x = (int) player.getX();
        int y = (int) player.getY();
        int z = (int) player.getZ();

        String direction;
        String dirX;
        String dirZ;

        float yaw = ((player.getYRot(1.0F) + 180) % 360 + 360) % 360 - 180;

        String[] directions = coordsConfig.getCurrentDirectionArray();
        String pos = coordsConfig.dirFacingPos;
        String neg = coordsConfig.dirFacingNeg;
        String[] dirXs = {"", pos, pos, pos, "", neg, neg, neg};
        String[] dirZs = {neg, neg, "", pos, pos, pos, "", neg};

        yaw = (yaw + 180) % 360;
        if (yaw < 0) yaw += 360;
        int index = (int) ((yaw + 22.5) / 45) % 8;

        direction = coordsConfig.dirText + " " + (directions.length > 0 ? directions[index] : "");
        dirX = dirXs[index];
        dirZ = dirZs[index];

        String coordinates = String.format(coordsConfig.xText + x + dirX + coordsConfig.deliminator + coordsConfig.yText + y
                + coordsConfig.deliminator + coordsConfig.zText + z + dirZ + direction);

        graphics.text(client.font, replaceAnd(coordinates), X_PADDING, Y_PADDING, COLOR, TEXT_SHADOW);
    }
}
