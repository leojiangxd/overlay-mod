package triangled.overlaymod.HUD;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClockHUD {
    private static final Minecraft CLIENT = Minecraft.getInstance();

    private static final int X_PADDING = 3;
    private static final int Y_PADDING = 13;
    private static final int COLOR = 0xFFFFFFFF;
    private static final boolean TEXT_SHADOW = true;

    private static final String CLOCK_FORMAT = "h:mma";

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        LocalPlayer player = CLIENT.player;
        if (player == null) return;

        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(CLOCK_FORMAT);
        String clockText = time.format(formatter);

        graphics.text(CLIENT.font, clockText, X_PADDING, Y_PADDING, COLOR, TEXT_SHADOW);
    }
}
