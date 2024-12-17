package com.triangled.overlaymod;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.triangled.overlaymod.config.OverlayModConfig;

import static com.triangled.overlaymod.config.OverlayModConfig.replaceAnd;

@Environment(EnvType.CLIENT)
public class OverlayModClient implements ClientModInitializer {
    OverlayModConfig.CoordinatesCategory coordsConfig = AutoConfig.getConfigHolder(OverlayModConfig.class).getConfig().coordinates;
    OverlayModConfig.SprintingCategory sprintingConfig = AutoConfig.getConfigHolder(OverlayModConfig.class).getConfig().sprinting;
    OverlayModConfig.ClockCategory clockConfig = AutoConfig.getConfigHolder(OverlayModConfig.class).getConfig().clock;
    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(this::onRenderHud);
    }

    private void onRenderHud(DrawContext context, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(coordsConfig.showCoordinates || sprintingConfig.showSprinting || clockConfig.showClock) || client.options.hudHidden) {
            return;
        }
        if (coordsConfig.showCoordinates) {
            if (client.player != null && client.world != null) {
                // Coordinates Overlay
                int x = (int) client.player.getX();
                int y = (int) client.player.getY();
                int z = (int) client.player.getZ();

                String direction;
                String dirX;
                String dirZ;

                float yaw = ((client.player.getYaw(1.0F) + 180) % 360 + 360) % 360 - 180;

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
                context.drawText(client.textRenderer, Text.literal(replaceAnd(coordinates)), 3, 3, 0xFFFFFF, true);
            }
        }
        String formattedTime = "";
        String sprinting = "";
        if (clockConfig.showClock) {
            try {
                LocalTime time = LocalTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(clockConfig.clockFormat);
                formattedTime = replaceAnd(clockConfig.clockText + time.format(formatter));
            } catch (Exception ignored) {}
        }
        if (sprintingConfig.showSprinting) {
            sprinting = replaceAnd(client.options.sprintKey.isPressed() ? sprintingConfig.sprintingText : "");
        }
        if (clockConfig.showClock || sprintingConfig.showSprinting) {
            context.drawText(client.textRenderer, Text.literal(sprinting + formattedTime), client.getWindow().getScaledWidth()
                    - client.textRenderer.getWidth(sprinting + formattedTime) - 3, 3, 0xFFFFFF, true);
        }
    }
}

