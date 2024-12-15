package com.triangled.overlaymod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.util.Formatting;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Environment(EnvType.CLIENT)
public class OverlayModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(this::onRenderHud);
    }

    private void onRenderHud(DrawContext context, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null && client.world != null) {
            // Coordinates Overlay
            int x = (int) client.player.getX();
            int y = (int) client.player.getY();
            int z = (int) client.player.getZ();

            String direction;
            String dirX;
            String dirZ;

            float yaw = ((client.player.getYaw(1.0F) + 180) % 360 + 360) % 360 - 180;

            String[] directions = {"ɴ", "ɴᴇ", "ᴇ", "sᴇ", "s", "sᴡ", "ᴡ", "ɴᴡ"};
            String[] dirXs = {"", "₍₊₎", "₍₊₎", "₍₊₎", "", "₍₋₎", "₍₋₎", "₍₋₎"};
            String[] dirZs = {"₍₋₎", "₍₋₎", "", "₍₊₎", "₍₊₎", "₍₊₎", "", "₍₋₎"};

            yaw = (yaw + 180) % 360;
            if (yaw < 0) yaw += 360;
            int index = (int)((yaw + 22.5) / 45) % 8;

            direction = directions[index];
            dirX = dirXs[index];
            dirZ = dirZs[index];

            String coordinates = String.format(Formatting.WHITE + Integer.toString(x) + Formatting.YELLOW + dirX + ", "
                    + Formatting.WHITE + y + Formatting.YELLOW + ", " + Formatting.WHITE + z + Formatting.YELLOW + dirZ
                    + " " + Formatting.WHITE + direction);

            // Sprint + Time Overlay
            LocalTime time = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm");
            String formattedTime = time.format(formatter);
            String sprinting = client.options.sprintKey.isPressed() ? "ꜱᴘʀɪɴᴛɪɴɢ" + Formatting.YELLOW + " | " + Formatting.WHITE : "";

            TextRenderer textRenderer = client.textRenderer;
            context.drawText(client.textRenderer, Text.literal(coordinates), 3, 3, 0xFFFFFF, true);
            context.drawText(client.textRenderer, Text.literal(sprinting + formattedTime), client.getWindow().getScaledWidth()
                    - textRenderer.getWidth(sprinting + formattedTime) - 2, 3, 0xFFFFFF, true);
        }
    }

}
