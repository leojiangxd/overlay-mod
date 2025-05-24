package com.triangled.overlaymod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.triangled.overlaymod.config.OverlayModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.render.RenderTickCounter;
import java.util.List;
import net.minecraft.component.type.UnbreakableComponent;
import java.util.stream.Collectors;
import net.minecraft.util.Identifier;
import me.shedaniel.autoconfig.AutoConfig;

@Mixin(InGameHud.class)
public class EquipmentOverlayMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed) {}

    @Unique
    OverlayModConfig.EquipmentCategory equipmentConfig = AutoConfig.getConfigHolder(OverlayModConfig.class).getConfig().equipment;


    @Inject(method = "renderHotbar", at = @At("TAIL"))
    public void renderArmorHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        // Disable all additional rendering
        if (!(this.client.getCameraEntity() instanceof PlayerEntity player) || !equipmentConfig.showEquipment) {
            return;
        }

        // Variables
        int arm = player.getMainArm() == Arm.RIGHT ? 1 : -1;
        int offsetXLeft =  context.getScaledWindowWidth() / 2 - 120;
        int offsetXRight = context.getScaledWindowWidth() / 2 + 109;
        int offsetY = context.getScaledWindowHeight() - 23 - equipmentConfig.armorYOffset;
        int l = 0;

        // GET ITEMS
        List<ItemStack> armor = player.getInventory().armor.stream()
                .filter(s -> s.getItem() != Items.AIR).collect(Collectors.toList());
        if (equipmentConfig.reverseArmorOrder)
            armor = armor.reversed();
        ItemStack offHand;
        try {
            offHand = player.getInventory().offHand.getLast();
        } catch (Exception e) {
            offHand = ItemStack.EMPTY;
        }
        ItemStack mainHand = player.getInventory().getMainHandStack();


        // Draw durability
        if (equipmentConfig.showEmptyOffHand) {
            String path = arm == 1 ? "hud/hotbar_offhand_left" : "hud/hotbar_offhand_right";
            if (equipmentConfig.renderBackground) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                int x = arm == 1 ? offsetXLeft : offsetXRight - 18;
                context.drawGuiTexture(Identifier.ofVanilla(path), 29, 24, 0, 0, x, offsetY, 29, 24);
                RenderSystem.disableBlend();
            }
        }

        if (equipmentConfig.showDurability && player.getInventory().offHand != null) {
            String durability = getDurability(offHand);
            int durabilityLength = client.textRenderer.getWidth(durability);
            int x = (arm == 1 ? offsetXLeft + 11 : offsetXRight) - (durabilityLength / 2);
            int y = (int) (offsetY - 9 - equipmentConfig.durabilityYOffset);
            context.drawText(client.textRenderer, durability, x, y, offHand.getItemBarColor(), true);
        }

        if (equipmentConfig.showMainHand && (equipmentConfig.showEmptyMainHand || mainHand.getItem() != Items.AIR)) {
            String path = arm == 1 ? "hud/hotbar_offhand_right" : "hud/hotbar_offhand_left";
            if (equipmentConfig.renderMainHandBackground) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                int x = arm == 1 ? offsetXRight - 18 : offsetXLeft;
                context.drawGuiTexture(Identifier.ofVanilla(path), 29, 24, 0, 0, x, offsetY, 29, 24);
                RenderSystem.disableBlend();
            }

            if (mainHand.getItem() != Items.AIR) {
                String durability = getDurability(mainHand);
                int durabilityLength = client.textRenderer.getWidth(durability);
                int itemX = arm == 1 ? offsetXRight - 8 : offsetXLeft + 3;
                this.renderHotbarItem(context, itemX, offsetY + 4, tickCounter, player, mainHand, ++l);

                if (equipmentConfig.showDurability) {
                    int textX = (arm == 1 ? offsetXRight : offsetXLeft + 11) - (durabilityLength / 2);
                    int textY = offsetY - 9 - (int) equipmentConfig.durabilityYOffset;
                    context.drawText(client.textRenderer, durability, textX, textY, mainHand.getItemBarColor(), true);
                }
            }
        }

        offsetY += 1;
        if (equipmentConfig.showArmor && !armor.isEmpty()) {
            int equipmentWidth = armor.size() * 10 + 1;
            int offsetX = 0;
            switch (equipmentConfig.armorPosition) {
                case BOTTOM_LEFT:
                    offsetX -= equipmentConfig.armorXOffset;
                    break;
                case BOTTOM_RIGHT:
                    offsetX = context.getScaledWindowWidth() - equipmentWidth * 2 + equipmentConfig.armorXOffset;
                    break;
                case HOTBAR_LEFT:
                    offsetX += offsetXLeft + 22 - equipmentWidth * 2 - equipmentConfig.armorXOffset;
                    if ((arm == 1 && offHand.getItem() != Items.AIR)
                            || (arm == -1 && mainHand.getItem() != Items.AIR)) {
                        offsetX -= 29;
                    }
                    break;
                case HOTBAR_RIGHT:
                default:
                    offsetX += offsetXRight - 11 + equipmentConfig.armorXOffset;;
                    if ((arm == -1 && offHand.getItem() != Items.AIR)
                            || (arm == 1 && mainHand.getItem() != Items.AIR)) {
                        offsetX += 29;
                    }
                    break;
            }
            if (equipmentConfig.renderBackground) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                context.drawGuiTexture(Identifier.ofVanilla("hud/hotbar"), 182, 22, 0, 0,
                        offsetX, offsetY, equipmentWidth, 22);
                context.drawGuiTexture(Identifier.ofVanilla("hud/hotbar"), 182, 22, 182 - equipmentWidth, 0,
                        offsetX + equipmentWidth, offsetY, equipmentWidth, 22);
                RenderSystem.disableBlend();
            }
            for (int i = 0; i < armor.size(); i++) {
                ItemStack item = armor.get(i);
                this.renderHotbarItem(context, offsetX + 3 + i * 20, offsetY + 3, tickCounter, player, item, ++l);
                int durabilityLength = client.textRenderer.getWidth(getDurability(item));
                if (equipmentConfig.showDurability) {
                    context.drawText(client.textRenderer, getDurability(item),
                            (offsetX + 11 - durabilityLength / 2 + i * 20 + (int) equipmentConfig.durabilityXOffset),
                            (offsetY - 10 - (int)equipmentConfig.durabilityYOffset), item.getItemBarColor(), true);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static final ComponentType<UnbreakableComponent> UNBREAKABLE =
            (ComponentType<UnbreakableComponent>) Registries.DATA_COMPONENT_TYPE
                    .get(Identifier.of("minecraft", "unbreakable"));

    @Unique
    private String getDurability(ItemStack item) {
        int currentDamage = item.getMaxDamage() - item.getDamage();
        if (item.getMaxDamage() == 0 || item.getComponents().contains(UNBREAKABLE)) {
            return "";
        }

        if (equipmentConfig.durabilityAsPercentage) {
            int durabilityPercentage = (int) (currentDamage / (double) item.getMaxDamage() * 100);
            if (durabilityPercentage >= 100) {
                return "";
            } else {
                return String.format("%d%%", durabilityPercentage);
            }
        }

        if (currentDamage < 1000) {
            return Integer.toString(currentDamage);
        } else if (currentDamage < 10000) {
            return String.format("%.1fk", Math.floor(currentDamage / 1000.0 * 10) / 10);
        } else if (currentDamage >= 100000) {
            return String.format("%.0e", (double) currentDamage).replaceAll("e\\+0", "ᴇ");
        }
        return "";
    }
}

