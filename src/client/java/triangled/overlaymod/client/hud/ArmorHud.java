package triangled.overlaymod.client.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud;

public class ArmorHud extends LegacyHud {
    private static final Identifier HOTBAR_SPRITE = Identifier.withDefaultNamespace("hud/hotbar");
    private static final int SLOT_SIZE = 20;
    private static final int HOTBAR_HEIGHT = 22;

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    @Switch(title = "Show Empty Slots", description = "Keep a slot for armor pieces you aren't wearing")
    public boolean showEmptyArmor = false;

    @Switch(title = "Reverse Order", description = "List boots first instead of helmet first")
    public boolean reverseArmorOrder = false;

    @Switch(title = "Vertical", description = "Stack the armor top to bottom instead of left to right")
    public boolean vertical = false;

    @Switch(title = "Render Background", description = "Draw the hotbar texture behind the armor")
    public boolean renderBackground = true;

    public ArmorHud() {
        super("overlaymod_armor.yml", "Armor", Hud.Category.Companion.getPLAYER());
    }

    private List<ItemStack> equipment() {
        Minecraft mc = Minecraft.getInstance();
        List<ItemStack> equipment = new ArrayList<>(ARMOR_SLOTS.length);
        if (mc != null && mc.getCameraEntity() instanceof Player player) {
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                ItemStack stack = player.getItemBySlot(slot);
                if (showEmptyArmor || !stack.isEmpty()) {
                    equipment.add(stack);
                }
            }
        }
        if (equipment.stream().allMatch(ItemStack::isEmpty) && HudManager.INSTANCE.isEditing()) {
            equipment = new ArrayList<>(List.of(
                    new ItemStack(Items.DIAMOND_HELMET),
                    new ItemStack(Items.DIAMOND_CHESTPLATE),
                    new ItemStack(Items.DIAMOND_LEGGINGS),
                    new ItemStack(Items.DIAMOND_BOOTS)));
        }
        if (reverseArmorOrder) {
            Collections.reverse(equipment);
        }
        return equipment;
    }

    private float length(int count) {
        return count * SLOT_SIZE + 2;
    }

    @Override
    public float getWidth() {
        return vertical ? HOTBAR_HEIGHT : length(Math.max(1, equipment().size()));
    }

    @Override
    public float getHeight() {
        return vertical ? length(Math.max(1, equipment().size())) : HOTBAR_HEIGHT;
    }

    @Override
    public boolean shouldShow() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.player.isSpectator()) {
            return false;
        }
        return !equipment().isEmpty();
    }

    @Override
    public boolean update() {
        return false;
    }

    @Override
    public boolean multipleInstancesAllowed() {
        return false;
    }

    @Override
    public Pair<Float, Float> defaultPosition() {
        return new Pair<>(HudManager.guiScreenWidth / 2f + 91 + 29, HudManager.guiScreenHeight - HOTBAR_HEIGHT);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics) {
        List<ItemStack> equipment = equipment();
        if (equipment.isEmpty()) {
            return;
        }

        if (renderBackground) {
            drawBackground(graphics, equipment.size());
        }

        Minecraft mc = Minecraft.getInstance();
        for (int i = 0; i < equipment.size(); i++) {
            ItemStack stack = equipment.get(i);
            int x = vertical ? 3 : 3 + i * SLOT_SIZE;
            int y = vertical ? 3 + i * SLOT_SIZE : 3;
            graphics.item(stack, x, y, i);
            graphics.itemDecorations(mc.font, stack, x, y);
        }
    }

    private void drawBackground(GuiGraphicsExtractor graphics, int count) {
        int half = count * SLOT_SIZE / 2 + 1;
        if (!vertical) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, 182, HOTBAR_HEIGHT, 0, 0,
                    0, 0, half, HOTBAR_HEIGHT);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, 182, HOTBAR_HEIGHT, 182 - half, 0,
                    half, 0, half, HOTBAR_HEIGHT);
            return;
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(HOTBAR_HEIGHT, 0);
        graphics.pose().rotate((float) (Math.PI / 2));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, 182, HOTBAR_HEIGHT, 0, 0,
                0, 0, half, HOTBAR_HEIGHT);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, 182, HOTBAR_HEIGHT, 182 - half, 0,
                half, 0, half, HOTBAR_HEIGHT);
        graphics.pose().popMatrix();
    }
}
