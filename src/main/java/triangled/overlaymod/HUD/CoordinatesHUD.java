package triangled.overlaymod.HUD;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import triangled.overlaymod.OverlayMod;
import triangled.overlaymod.config.OverlayModConfig;
import triangled.overlaymod.util.TextRenderUtil;

import static triangled.overlaymod.config.OverlayModConfig.replaceAnd;

public class CoordinatesHUD {
    private static final int X_PADDING = 3;
    private static final int Y_PADDING = 3;

    private static final int DIR_NONE = 0;
    private static final int DIR_POS = 1;
    private static final int DIR_NEG = 2;

    private static final int[] X_DIR_KIND = {DIR_NONE, DIR_POS, DIR_POS, DIR_POS, DIR_NONE, DIR_NEG, DIR_NEG, DIR_NEG};
    private static final int[] Z_DIR_KIND = {DIR_NEG, DIR_NEG, DIR_NONE, DIR_POS, DIR_POS, DIR_POS, DIR_NONE, DIR_NEG};

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        OverlayModConfig config = OverlayMod.config;
        if (config == null) return;

        OverlayModConfig.CoordinatesCategory coordsConfig = config.coordinates;
        if (!coordsConfig.visibility.showCoordinates || client.gui.hud.isHidden()) return;

        LocalPlayer player = client.player;
        if (player == null || client.level == null) return;

        int x = (int) player.getX();
        int y = (int) player.getY();
        int z = (int) player.getZ();

        float yaw = ((player.getYRot(1.0F) + 180) % 360 + 360) % 360 - 180;
        yaw = (yaw + 180) % 360;
        if (yaw < 0) yaw += 360;
        int index = (int) ((yaw + 22.5) / 45) % 8;

        int xDirKind = X_DIR_KIND[index];
        int zDirKind = Z_DIR_KIND[index];

        OverlayModConfig.CoordinatesCategory.Style style = coordsConfig.style;
        OverlayModConfig.CoordinatesCategory.Positioning pos = coordsConfig.positioning;

        graphics.pose().pushMatrix();
        graphics.pose().translate(X_PADDING + pos.xOffset, Y_PADDING + pos.yOffset);

        int cursorX = 0;
        cursorX = drawPiece(graphics, client, coordsConfig, coordsConfig.text.xText + x, style.xColor, cursorX);
        cursorX = drawDirPiece(graphics, client, coordsConfig, xDirKind, cursorX);
        cursorX = drawPiece(graphics, client, coordsConfig, coordsConfig.text.deliminator, style.deliminatorColor, cursorX);
        cursorX = drawPiece(graphics, client, coordsConfig, coordsConfig.text.yText + y, style.yColor, cursorX);
        cursorX = drawPiece(graphics, client, coordsConfig, coordsConfig.text.deliminator, style.deliminatorColor, cursorX);
        cursorX = drawPiece(graphics, client, coordsConfig, coordsConfig.text.zText + z, style.zColor, cursorX);
        cursorX = drawDirPiece(graphics, client, coordsConfig, zDirKind, cursorX);

        if (coordsConfig.visibility.showDirection) {
            String direction = coordsConfig.text.dirText + " " + coordsConfig.getDirectionText(index);
            drawDirectionPiece(graphics, client, coordsConfig, direction, cursorX);
        }

        graphics.pose().popMatrix();
    }

    private static int drawDirPiece(GuiGraphicsExtractor graphics, Minecraft client, OverlayModConfig.CoordinatesCategory coordsConfig, int kind, int cursorX) {
        if (kind == DIR_NONE) return cursorX;
        if (kind == DIR_POS) {
            return drawPiece(graphics, client, coordsConfig, coordsConfig.text.dirFacingPos, coordsConfig.style.dirFacingPosColor, cursorX);
        }
        return drawPiece(graphics, client, coordsConfig, coordsConfig.text.dirFacingNeg, coordsConfig.style.dirFacingNegColor, cursorX);
    }

    private static int drawPiece(GuiGraphicsExtractor graphics, Minecraft client, OverlayModConfig.CoordinatesCategory coordsConfig, String rawText, int color, int cursorX) {
        if (rawText == null || rawText.isEmpty()) return cursorX;
        String text = replaceAnd(rawText);
        TextRenderUtil.drawText(graphics, client.font, text, cursorX, 0, color, coordsConfig.style.textShadow);
        return cursorX + client.font.width(text);
    }

    private static void drawDirectionPiece(GuiGraphicsExtractor graphics, Minecraft client, OverlayModConfig.CoordinatesCategory coordsConfig, String rawText, int cursorX) {
        if (rawText == null || rawText.isEmpty()) return;
        String text = replaceAnd(rawText);
        OverlayModConfig.CoordinatesCategory.Positioning pos = coordsConfig.positioning;

        graphics.pose().pushMatrix();
        graphics.pose().translate(cursorX + pos.dirXOffset, pos.dirYOffset);
        TextRenderUtil.drawText(graphics, client.font, text, 0, 0, coordsConfig.style.dirColor, coordsConfig.style.textShadow);
        graphics.pose().popMatrix();
    }
}
