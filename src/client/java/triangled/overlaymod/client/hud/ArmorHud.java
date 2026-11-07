package triangled.overlaymod.client.hud;

import androidx.compose.runtime.Composer;
import androidx.compose.runtime.MutableState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.polyfrost.compose.composables.PolyModifier;
import org.polyfrost.compose.render.FilterQuality;
import org.polyfrost.compose.render.PolyBitmap;
import org.polyfrost.compose.render.PolyColor;
import org.polyfrost.compose.render.RenderContext;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import triangled.overlaymod.client.feature.DurabilityOverlay;
import triangled.overlaymod.client.utils.Colors;
import triangled.overlaymod.client.utils.Compose;
import triangled.overlaymod.client.utils.SpriteCache;

public class ArmorHud extends OverlayHud {
    private static final String HOTBAR_SPRITE = "hud/hotbar";
    private static final float ITEM_SIZE = 16;
    private static final PolyColor NO_TINT = Colors.white();
    private static final int HOTBAR_WIDTH = 182;
    private static final int HOTBAR_HEIGHT = 22;
    private static final int SLOT_SIZE = 20;
    private static final int SLOT_PADDING = 3;

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    // --------------- CONFIGS ---------------

    @Switch(title = "Show Empty Slots", description = "Keep a slot for armor pieces you aren't wearing")
    public boolean showEmptyArmor = false;

    @Switch(title = "Reverse Order", description = "List boots first instead")
    public boolean reverseArmorOrder = false;

    @Switch(title = "Vertical", description = "Stack the armor top to bottom instead of left to right")
    public boolean vertical = false;

    @Switch(title = "Render Armor Background", description = "Draw the background texture behind the armor")
    public boolean renderBackground = true;

    // ---------------------------------------

    private transient MutableState<List<ItemStack>> displayedArmor = Compose.state(List.of());

    public ArmorHud() {
        super("overlaymod_armor.yml", "Armor", Hud.Category.Companion.getPLAYER());
    }

    // Returns the player's armor, or preview armor while editing with none
    // worn. Reversed if reverseArmorOrder is on.
    private List<ItemStack> equipment() {
        List<ItemStack> armor = wornArmor();
        if (armor.stream().allMatch(ItemStack::isEmpty) && isEditing()) {
            armor = previewArmor();
        }
        if (reverseArmorOrder) {
            Collections.reverse(armor);
        }
        return armor;
    }

    // Returns the player's armor from.
    // Includes empty slots when showEmptyArmor is on.
    private List<ItemStack> wornArmor() {
        List<ItemStack> armor = new ArrayList<>(ARMOR_SLOTS.length);
        if (!(Minecraft.getInstance().getCameraEntity() instanceof Player player))
            return armor;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (showEmptyArmor || !stack.isEmpty()) {
                armor.add(stack);
            }
        }
        return armor;
    }

    // Returns a list of diamond armor to preview the HUD when no armor is worn.
    private static List<ItemStack> previewArmor() {
        return new ArrayList<>(List.of(new ItemStack(Items.DIAMOND_HELMET), new ItemStack(Items.DIAMOND_CHESTPLATE),
                new ItemStack(Items.DIAMOND_LEGGINGS), new ItemStack(Items.DIAMOND_BOOTS)));
    }

    // OneConfig calls this every update so the HUD can grab the latest game info
    // and resize before it's drawn.
    @Override
    protected void refresh() {
        List<ItemStack> currentArmor = equipment();
        if (!ItemStack.listMatches(currentArmor, displayedArmor.getValue())) {
            displayedArmor.setValue(currentArmor.stream().map(ItemStack::copy).toList());
        }
        float hudLength = Math.max(1, currentArmor.size()) * SLOT_SIZE + 2;
        setSize(vertical ? HOTBAR_HEIGHT : hudLength, vertical ? hudLength : HOTBAR_HEIGHT);
    }

    // Hides the HUD outside a world, while spectating, or no armor to show.
    @Override
    public boolean shouldShow() {
        LocalPlayer player = player();
        return player != null && !player.isSpectator() && !displayedArmor.getValue().isEmpty();
    }

    // Default position of HUD in OneConfig's HUD editor
    @Override
    public Pair<Float, Float> defaultPosition() {
        return new Pair<>(HudManager.guiScreenWidth - getScaledWidth(), HudManager.guiScreenHeight - getScaledHeight());
    }

    // Returns the slot's x position, relative to the HUD's top-left corner.
    private int slotX(int index) {
        return vertical ? SLOT_PADDING : SLOT_PADDING + index * SLOT_SIZE;
    }

    // Returns the slot's y position, relative to the HUD's top-left corner.
    private int slotY(int index) {
        return vertical ? SLOT_PADDING + index * SLOT_SIZE : SLOT_PADDING;
    }

    // Draws the HUD in three layers: drawBackground for the hotbar texture,
    // Compose.item for each armor icon, then drawDecorations on top.
    @Override
    protected void content(Composer composer, Layout layout) {
        List<ItemStack> armor = displayedArmor.getValue();
        float originX = layout.originX();
        float originY = layout.originY();
        PolyModifier fullArea = Compose.at(Compose.sized(layout.boxWidth(), layout.boxHeight()), 0, 0);
        if (renderBackground) {
            Compose.canvas(composer, fullArea,
                    (renderContext, x, y) -> drawBackground(renderContext, x + originX, y + originY, armor.size()));
        }
        for (int i = 0; i < armor.size(); i++) {
            Compose.item(composer, armor.get(i), ITEM_SIZE,
                    Compose.at(Compose.sized(ITEM_SIZE, ITEM_SIZE), originX + slotX(i), originY + slotY(i)));
        }
        Compose.canvas(composer, fullArea,
                (renderContext, x, y) -> drawDecorations(renderContext, x + originX, y + originY, armor));
    }

    // Draws the durability bar and text with DurabilityOverlay, plus the stack
    // count, over each non-empty armor slot.
    private void drawDecorations(RenderContext renderContext, float x, float y, List<ItemStack> armor) {
        Font font = font();
        for (int i = 0; i < armor.size(); i++) {
            ItemStack stack = armor.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            float itemX = x + slotX(i);
            float itemY = y + slotY(i);
            DurabilityOverlay.render(renderContext, font, stack, itemX, itemY);
            if (stack.getCount() > 1) {
                String countText = Integer.toString(stack.getCount());
                renderContext.mcText(countText, itemX + 17 - font.width(countText), itemY + 9, Colors.WHITE, true, 1f);
            }
        }
    }

    // Draws the hotbar texture behind the slots by joining its left and right ends,
    // rotated when the HUD is vertical.
    private void drawBackground(RenderContext renderContext, float x, float y, int slotCount) {
        PolyBitmap sprite = vertical ? SpriteCache.guiSpriteRotated(HOTBAR_SPRITE)
                : SpriteCache.guiSprite(HOTBAR_SPRITE);
        if (sprite == null) {
            return;
        }
        float halfLength = slotCount * SLOT_SIZE / 2 + 1;
        float textureFraction = halfLength / HOTBAR_WIDTH;
        if (vertical) {
            renderContext.image(sprite, x, y, HOTBAR_HEIGHT, halfLength, 0, 0, 1, textureFraction, NO_TINT,
                    FilterQuality.None);
            renderContext.image(sprite, x, y + halfLength, HOTBAR_HEIGHT, halfLength, 0, 1 - textureFraction, 1, 1,
                    NO_TINT, FilterQuality.None);
        } else {
            renderContext.image(sprite, x, y, halfLength, HOTBAR_HEIGHT, 0, 0, textureFraction, 1, NO_TINT,
                    FilterQuality.None);
            renderContext.image(sprite, x + halfLength, y, halfLength, HOTBAR_HEIGHT, 1 - textureFraction, 0, 1, 1,
                    NO_TINT, FilterQuality.None);
        }
    }
}
