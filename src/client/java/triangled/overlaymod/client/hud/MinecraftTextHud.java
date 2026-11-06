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

    private static Font font() {
        return Minecraft.getInstance().font;
    }

    @Override
    public float getWidth() {
        TextRenderUtil.Extent extent = TextRenderUtil.shadowExtent(minecraftTextShadow);
        return font().width(displayText()) + extent.left() + extent.right();
    }

    @Override
    public float getHeight() {
        TextRenderUtil.Extent extent = TextRenderUtil.shadowExtent(minecraftTextShadow);
        return font().lineHeight - 1 + extent.top() + extent.bottom();
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
        TextRenderUtil.Extent extent = TextRenderUtil.shadowExtent(minecraftTextShadow);
        int color = minecraftTextColor == null ? 0xFFFFFFFF : minecraftTextColor.getArgb();
        TextRenderUtil.drawText(graphics, font(), text, extent.left(), extent.top(), color, minecraftTextShadow);
    }
}
