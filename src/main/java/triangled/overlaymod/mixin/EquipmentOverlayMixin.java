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
import me.shedaniel.autoconfig.AutoConfig;

import static net.minecraft.core.component.DataComponents.UNBREAKABLE;

@Mixin(Hud.class)
public class EquipmentOverlayMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private void extractSlot(GuiGraphicsExtractor graphics, int x, int y, DeltaTracker tickCounter, Player player, ItemStack stack, int seed) {
    }

    @Unique
    private OverlayModConfig.EquipmentCategory equipmentConfig;

    @Inject(method = "extractHotbarAndDecorations", at = @At("TAIL"))
    public void extractArmorHud(GuiGraphicsExtractor graphics, DeltaTracker tickCounter, CallbackInfo ci) {
        if (equipmentConfig == null) {
            equipmentConfig = AutoConfig.getConfigHolder(OverlayModConfig.class).getConfig().equipment;
        }

        if (!(this.minecraft.getCameraEntity() instanceof Player player) || !equipmentConfig.showEquipment || player.isSpectator()) {
            return;
        }

        int arm = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        int offsetXLeft = graphics.guiWidth() / 2 - 120;
        int offsetXRight = graphics.guiWidth() / 2 + 109;
        int offsetY = graphics.guiHeight() - 23 - equipmentConfig.equipmentYOffset;
        int l = 0;

        Stream<ItemStack> equipmentStream = Stream.of(
                player.getItemBySlot(EquipmentSlot.HEAD),
                player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.FEET)
        );
        if (!equipmentConfig.showEmptyArmor) {
            equipmentStream = equipmentStream.filter(s -> s.getItem() != Items.AIR);
        }
        List<ItemStack> equipment = equipmentStream.toList();
        if (equipmentConfig.reverseArmorOrder) {
            equipment = equipment.reversed();
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand;
        try {
            offHand = player.getOffhandItem();
        } catch (Exception e) {
            offHand = ItemStack.EMPTY;
        }

        boolean offHandVisible = offHand != ItemStack.EMPTY || (equipmentConfig.showEmptyOffhand && equipmentConfig.renderBackground);
        boolean mainHandVisible = equipmentConfig.showMainHand && (mainHand.getItem() != Items.AIR || equipmentConfig.showEmptyMainHand);

        if (offHand != ItemStack.EMPTY) {
            if (equipmentConfig.showDurability) {
                int durabilityLength = minecraft.font.width(getDurability(offHand));
                graphics.text(minecraft.font, getDurability(offHand),
                        (arm == 1 ? offsetXLeft + 11 : offsetXRight) - (durabilityLength / 2),
                        (offsetY - 9 - equipmentConfig.durabilityYOffset),
                        0xFF000000 | offHand.getBarColor(), true);
            }
        } else if (offHandVisible) {
            String path = arm == 1 ? "hud/hotbar_offhand_left" : "hud/hotbar_offhand_right";
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace(path), 29, 24, 0, 0,
                    (arm == 1 ? offsetXLeft : offsetXRight - 18), offsetY, 29, 24);
        }

        if (mainHandVisible) {
            String path = arm == 1 ? "hud/hotbar_offhand_right" : "hud/hotbar_offhand_left";
            if (equipmentConfig.renderBackground) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace(path), 29, 24, 0, 0,
                        (arm == 1 ? offsetXRight - 18 : offsetXLeft), offsetY, 29, 24);
            }
            if (mainHand.getItem() != Items.AIR) {
                this.extractSlot(graphics, (arm == 1 ? offsetXRight - 8 : offsetXLeft + 3),
                        offsetY + 4, tickCounter, player, mainHand, ++l);
                if (equipmentConfig.showDurability) {
                    int durabilityLength = minecraft.font.width(getDurability(mainHand));
                    graphics.text(minecraft.font, getDurability(mainHand),
                            (arm == 1 ? offsetXRight : offsetXLeft + 11) - (durabilityLength) / 2,
                            offsetY - 9 - equipmentConfig.durabilityYOffset, 0xFF000000 | mainHand.getBarColor(), true);
                }
            }
        }

        offsetY += 1;
        if (equipmentConfig.showArmor && !equipment.isEmpty()) {
            int equipmentWidth = equipment.size() * 10 + 1;
            int offsetX = 0;
            switch (equipmentConfig.armorPosition) {
                case BOTTOM_LEFT:
                    offsetX -= equipmentConfig.equipmentXOffset;
                    break;
                case BOTTOM_RIGHT:
                    offsetX = graphics.guiWidth() - equipmentWidth * 2 + equipmentConfig.equipmentXOffset;
                    break;
                case HOTBAR_LEFT:
                    offsetX += offsetXLeft + 22 - equipmentWidth * 2 - equipmentConfig.equipmentXOffset;
                    if ((arm == 1 && offHandVisible) || (arm == -1 && mainHandVisible)) {
                        offsetX -= 29;
                    }
                    break;
                case HOTBAR_RIGHT:
                default:
                    offsetX += offsetXRight - 11 + equipmentConfig.equipmentXOffset;
                    if ((arm == -1 && offHandVisible) || (arm == 1 && mainHandVisible)) {
                        offsetX += 29;
                    }
                    break;
            }
            if (equipmentConfig.renderBackground) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("hud/hotbar"), 182, 22, 0, 0,
                        offsetX, offsetY, equipmentWidth, 22);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("hud/hotbar"), 182, 22, 182 - equipmentWidth, 0,
                        offsetX + equipmentWidth, offsetY, equipmentWidth, 22);
            }
            for (int i = 0; i < equipment.size(); i++) {
                ItemStack item = equipment.get(i);
                this.extractSlot(graphics, offsetX + 3 + i * 20, offsetY + 3, tickCounter, player, item, ++l);
                int durabilityLength = minecraft.font.width(getDurability(item));
                if (equipmentConfig.showDurability) {
                    graphics.text(minecraft.font, getDurability(item),
                            (offsetX + 11 - durabilityLength / 2 + i * 20 + equipmentConfig.durabilityXOffset),
                            (offsetY - 10 - equipmentConfig.durabilityYOffset), 0xFF000000 | item.getBarColor(), true);
                }
            }
        }
    }

    @Unique
    private String getDurability(ItemStack item) {
        int currentDamage = item.getMaxDamage() - item.getDamageValue();
        if (item.getMaxDamage() == 0 || item.getComponents().has(UNBREAKABLE)) {
            return "";
        }

        if (equipmentConfig.subscriptDurability) {
            if (currentDamage < 10000) {
                return convertToSubscript(Integer.toString(currentDamage));
            }
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
        } else {
            return String.format("%.0e", (double) currentDamage).replaceAll("e\\+0", "ᴇ");
        }
    }

    @Unique
    private String convertToSubscript(String input) {
        String[] subscriptDigits = {"₀", "₁", "₂", "₃", "₄", "₅", "₆", "₇", "₈", "₉"};
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                int digit = c - '0';
                result.append(subscriptDigits[digit]);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
