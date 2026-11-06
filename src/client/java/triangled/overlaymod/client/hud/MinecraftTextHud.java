package triangled.overlaymod.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.compose.render.PolyColor;
import org.polyfrost.oneconfig.api.config.v1.annotations.Color;
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.oneconfig.api.hud.v1.LegacyHud;
import triangled.overlaymod.client.utils.FormatCodes;
import triangled.overlaymod.client.utils.TextRenderUtil;
import triangled.overlaymod.client.utils.TextShadow;

public abstract class MinecraftTextHud extends LegacyHud {
    // ---------------------------------------------------------------- Style

    @Dropdown(title = "Text Shadow", description = "How the text is shadowed or outlined", subcategory = "Style")
    public TextShadow minecraftTextShadow = TextShadow.SHADOW;

    @Color(title = "Text Color", description = "Colour of any text not coloured by a formatting code", subcategory = "Style")
    public PolyColor minecraftTextColor = new PolyColor(0xFFFFFFFF, false, 1.0F);

    protected MinecraftTextHud(String id, String title, Hud.Category category) {
        super(id, title, category);
    }

    protected abstract String getText();

    protected abstract String previewText();

    private String displayText() {
        String text = getText();
        if ((text == null || text.isEmpty()) && HudManager.INSTANCE.isEditing()) {
            text = previewText();
        }
        return FormatCodes.replaceAnd(text);
    }

    private int[] shadowPadding() {
        return switch (minecraftTextShadow == null ? TextShadow.SHADOW : minecraftTextShadow) {
            case FOUR_DIRECTION, EIGHT_DIRECTION -> new int[] {1, 1};
            case SHADOW -> new int[] {0, 1};
            case NONE -> new int[] {0, 0};
        };
    }

    private static Font font() {
        return Minecraft.getInstance().font;
    }

    @Override
    public float getWidth() {
        int[] pad = shadowPadding();
        return font().width(displayText()) + pad[0] + pad[1];
    }

    @Override
    public float getHeight() {
        int[] pad = shadowPadding();
        return font().lineHeight - 1 + pad[0] + pad[1];
    }

    @Override
    public boolean shouldShow() {
        return !displayText().isEmpty();
    }

    @Override
    public boolean update() {
        return false;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics) {
        String text = displayText();
        if (text.isEmpty()) {
            return;
        }
        int pad = shadowPadding()[0];
        int color = minecraftTextColor == null ? 0xFFFFFFFF : minecraftTextColor.getArgb();
        TextRenderUtil.drawText(graphics, font(), text, pad, pad, color, minecraftTextShadow);
    }
}
