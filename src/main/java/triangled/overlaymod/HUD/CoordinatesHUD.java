package triangled.overlaymod.HUD;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

public class CoordinatesHUD {
    private static final Minecraft CLIENT = Minecraft.getInstance();

    private static final int COLOR = 0xFFFFFFFF;
    private static final boolean TEXT_SHADOW = true;
    private static final int PRECISION = 0;

    private static final String[] COMPASS_DIRECTIONS = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    private static final String[] X_DIRECTIONS = {"", "(+)", "(+)", "(+)", "", "(-)", "(-)", "(-)"};
    private static final String[] Z_DIRECTIONS = {"(-)", "(-)", "", "(+)", "(+)", "(+)", "", "(-)"};


    public static void render(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        LocalPlayer player = CLIENT.player;
        if (CLIENT.player == null) return;

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        float yaw = player.getYRot();
        int index = getMapIndex(yaw);

        String compass = COMPASS_DIRECTIONS[index];
        String xSign = X_DIRECTIONS[index];
        String zSign = Z_DIRECTIONS[index];

        String formatString = String.format("%%.%df%%s, %%.%df, %%.%df%%s %%S", PRECISION, PRECISION, PRECISION);
        String coordinatesText = String.format(formatString, x, xSign, y, z, zSign, compass);

        graphics.text(CLIENT.font, coordinatesText, 3, 3, COLOR, TEXT_SHADOW);
    }

    private static int getMapIndex(double yaw) {
        double normalizedYaw = (yaw + 180) % 360;
        return (int) ((normalizedYaw + 22.5) / 45) % 8;
    }
}
