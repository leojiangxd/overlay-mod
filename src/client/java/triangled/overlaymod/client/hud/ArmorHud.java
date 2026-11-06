package triangled.overlaymod.client.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;

public class ArmorHud extends OverlayHud {
    private static final Identifier HOTBAR_SPRITE = Identifier.withDefaultNamespace("hud/hotbar");
    private static final int HOTBAR_WIDTH = 182;
    private static final int HOTBAR_HEIGHT = 22;
    private static final int SLOT_SIZE = 20;
    private static final int SLOT_PADDING = 3;

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
        List<ItemStack> equipment = wornArmor();
        if (equipment.stream().allMatch(ItemStack::isEmpty) && isEditing()) {
            equipment = previewArmor();
        }
        if (reverseArmorOrder) {
            Collections.reverse(equipment);
        }
        return equipment;
    }

    private List<ItemStack> wornArmor() {
        List<ItemStack> armor = new ArrayList<>(ARMOR_SLOTS.length);
        if (Minecraft.getInstance().getCameraEntity() instanceof Player player) {
            for (EquipmentSlot slot : ARMOR_SLOTS) {
                ItemStack stack = player.getItemBySlot(slot);
                if (showEmptyArmor || !stack.isEmpty()) {
                    armor.add(stack);
                }
            }
        }
        return armor;
    }

    private static List<ItemStack> previewArmor() {
        return new ArrayList<>(List.of(
                new ItemStack(Items.DIAMOND_HELMET),
                new ItemStack(Items.DIAMOND_CHESTPLATE),
                new ItemStack(Items.DIAMOND_LEGGINGS),
                new ItemStack(Items.DIAMOND_BOOTS)));
    }

    private float length() {
        return Math.max(1, equipment().size()) * SLOT_SIZE + 2;
    }

    @Override
    public float getWidth() {
        return vertical ? HOTBAR_HEIGHT : length();
    }

    @Override
    public float getHeight() {
        return vertical ? length() : HOTBAR_HEIGHT;
    }

    @Override
    public boolean shouldShow() {
        applyGrowthAnchor();
        LocalPlayer player = player();
        return player != null && !player.isSpectator() && !equipment().isEmpty();
    }

    @Override
    public Pair<Float, Float> defaultPosition() {
        return new Pair<>(HudManager.guiScreenWidth - getScaledWidth(), HudManager.guiScreenHeight - getScaledHeight());
    }

    @Override
    public void render(GuiGraphicsExtractor graphics) {
        applyGrowthAnchor();
        List<ItemStack> equipment = equipment();
        if (equipment.isEmpty()) {
            return;
        }

        if (renderBackground) {
            drawBackground(graphics, equipment.size());
        }

        for (int i = 0; i < equipment.size(); i++) {
            ItemStack stack = equipment.get(i);
            int along = SLOT_PADDING + i * SLOT_SIZE;
            int x = vertical ? SLOT_PADDING : along;
            int y = vertical ? along : SLOT_PADDING;
            graphics.item(stack, x, y, i);
            graphics.itemDecorations(font(), stack, x, y);
        }
    }

    private void drawBackground(GuiGraphicsExtractor graphics, int count) {
        if (!vertical) {
            drawHotbarEnds(graphics, count);
            return;
        }
        graphics.pose().pushMatrix();
        graphics.pose().translate(HOTBAR_HEIGHT, 0);
        graphics.pose().rotate((float) (Math.PI / 2));
        drawHotbarEnds(graphics, count);
        graphics.pose().popMatrix();
    }

    private static void drawHotbarEnds(GuiGraphicsExtractor graphics, int count) {
        int half = count * SLOT_SIZE / 2 + 1;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, HOTBAR_WIDTH, HOTBAR_HEIGHT, 0, 0,
                0, 0, half, HOTBAR_HEIGHT);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, HOTBAR_WIDTH, HOTBAR_HEIGHT,
                HOTBAR_WIDTH - half, 0,
                half, 0, half, HOTBAR_HEIGHT);
    }
}
