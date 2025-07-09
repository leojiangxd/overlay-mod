package triangled.overlaymod;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.util.Identifier;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import triangled.overlaymod.config.OverlayModConfig;

import static triangled.overlaymod.config.OverlayModConfig.replaceAnd;

public class OverlayModClient implements ClientModInitializer {
    public static OverlayModConfig config;

    private OverlayModConfig.CoordinatesCategory coordsConfig;
    private OverlayModConfig.SprintingCategory sprintingConfig;
    private OverlayModConfig.ClockCategory clockConfig;

    private static final Identifier COORDS_LAYER = Identifier.of("overlay-mod", "coordinates");
    private static final Identifier TIME_SPRINT_LAYER = Identifier.of("overlay-mod", "time-sprint");

    @Override
    public void onInitializeClient() {
        AutoConfig.register(OverlayModConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(OverlayModConfig.class).getConfig();

        coordsConfig = config.coordinates;
        sprintingConfig = config.sprinting;
        clockConfig = config.clock;

        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerBefore(IdentifiedLayer.CHAT, COORDS_LAYER, this::onRenderCoordsHud));
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerBefore(IdentifiedLayer.CHAT, TIME_SPRINT_LAYER, this::onRenderTimeSprintHud));
    }

    private void onRenderCoordsHud(DrawContext context, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!coordsConfig.showCoordinates || client.options.hudHidden) {
            return;
        }
        if (client.player != null && client.world != null) {
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

    private void onRenderTimeSprintHud(DrawContext context, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(sprintingConfig.showSprinting || clockConfig.showClock) || client.options.hudHidden) {
            return;
        }

        String formattedTime = "";
        String sprinting = "";

        if (clockConfig.showClock) {
            try {
                LocalTime time = LocalTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(clockConfig.clockFormat);
                formattedTime = replaceAnd(clockConfig.clockText + time.format(formatter));
            } catch (Exception ignored) {
            }
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