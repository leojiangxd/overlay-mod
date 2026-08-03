package triangled.overlaymod.mixin;

import triangled.overlaymod.config.OverlayModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Stream;

import net.minecraft.resources.Identifier;

import triangled.overlaymod.OverlayMod;

@Mixin(Hud.class)
public class EquipmentOverlayMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private void extractSlot(GuiGraphicsExtractor graphics, int x, int y, DeltaTracker tickCounter, Player player,
            ItemStack stack, int seed) {
    }

    @Unique
    private OverlayModConfig.EquipmentCategory equipmentConfig;

    @Inject(method = "extractHotbarAndDecorations", at = @At("TAIL"))
    public void extractArmorHud(GuiGraphicsExtractor graphics, DeltaTracker tickCounter, CallbackInfo ci) {
        if (equipmentConfig == null) {
            equipmentConfig = OverlayMod.config.equipment;
        }

        if (!(this.minecraft.getCameraEntity() instanceof Player player) || !equipmentConfig.visibility.showEquipment
                || this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            return;
        }

        int offsetXLeft = graphics.guiWidth() / 2 - 120;
        int offsetXRight = graphics.guiWidth() / 2 + 109;
        int offsetY = graphics.guiHeight() - 23;
        int l = 1000;

        Stream<ItemStack> equipmentStream = Stream.of(
                player.getItemBySlot(EquipmentSlot.HEAD),
                player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.FEET));
        if (!equipmentConfig.visibility.showEmptyArmor) {
            equipmentStream = equipmentStream.filter(s -> s.getItem() != Items.AIR);
        }
        List<ItemStack> equipment = equipmentStream.toList();
        if (equipmentConfig.visibility.reverseArmorOrder) {
            equipment = equipment.reversed();
        }

        offsetY += 1;
        if (equipmentConfig.visibility.showArmor && !equipment.isEmpty()) {
            int equipmentWidth = equipment.size() * 10 + 1;
            int offsetX = 0;
            float xOffset = equipmentConfig.positioning.equipmentXOffset;
            boolean mainArmRight = player.getMainArm() == HumanoidArm.RIGHT;
            switch (equipmentConfig.positioning.armorPosition) {
                case BOTTOM_LEFT:
                    xOffset = -xOffset;
                    break;
                case BOTTOM_RIGHT:
                    offsetX = graphics.guiWidth() - equipmentWidth * 2;
                    break;
                case HOTBAR_LEFT:
                    offsetX += offsetXLeft + 22 - equipmentWidth * 2;
                    if (mainArmRight) {
                        offsetX -= 29;
                    }
                    xOffset = -xOffset;
                    break;
                case HOTBAR_RIGHT:
                default:
                    offsetX += offsetXRight - 11;
                    if (!mainArmRight) {
                        offsetX += 29;
                    }
                    break;
            }

            graphics.pose().pushMatrix();
            graphics.pose().translate(xOffset, -equipmentConfig.positioning.equipmentYOffset);

            if (equipmentConfig.visibility.renderBackground) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("hud/hotbar"), 182,
                        22, 0, 0,
                        offsetX, offsetY, equipmentWidth, 22);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("hud/hotbar"), 182,
                        22, 182 - equipmentWidth, 0,
                        offsetX + equipmentWidth, offsetY, equipmentWidth, 22);
            }
            for (int i = 0; i < equipment.size(); i++) {
                ItemStack item = equipment.get(i);
                this.extractSlot(graphics, offsetX + 3 + i * 20, offsetY + 3, tickCounter, player, item, ++l);
            }

            graphics.pose().popMatrix();
        }
    }
}
