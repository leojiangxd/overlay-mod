package triangled.overlaymod.client.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.polyfrost.compose.render.PolyColor;
import org.polyfrost.oneconfig.api.config.v1.annotations.Color;
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import triangled.overlaymod.client.utils.Colors;
import triangled.overlaymod.client.utils.FormatCodes;
import triangled.overlaymod.client.utils.Insets;
import triangled.overlaymod.client.utils.TextRenderUtil;
import triangled.overlaymod.client.utils.TextShadow;

public abstract class MinecraftTextHud extends OverlayHud {
    // ---------------------------------------------------------------- Style

    @Dropdown(title = "Text Shadow", description = "How the text is shadowed or outlined", subcategory = "Style")
    public TextShadow minecraftTextShadow = TextShadow.SHADOW;

    @Color(title = "Text Color", description = "Colour of any text not coloured by a formatting code", subcategory = "Style")
    public PolyColor minecraftTextColor = Colors.white();

    protected MinecraftTextHud(String id, String title, Hud.Category category) {
        super(id, title, category);
    }

    protected abstract String getText();

    protected abstract String previewText();

    private String displayText() {
        String text = getText();
        if ((text == null || text.isEmpty()) && isEditing()) {
            text = previewText();
        }
        return FormatCodes.replaceAnd(text);
    }

    private Insets insets() {
        return TextShadow.orDefault(minecraftTextShadow).insets();
    }

    @Override
    public float getWidth() {
        return font().width(displayText()) + insets().horizontal();
    }

    @Override
    public float getHeight() {
        return font().lineHeight - 1 + insets().vertical();
    }

    @Override
    public boolean shouldShow() {
        return !displayText().isEmpty();
    }

    @Override
    public void render(GuiGraphicsExtractor graphics) {
        String text = displayText();
        if (text.isEmpty()) {
            return;
        }
        Insets insets = insets();
        TextRenderUtil.drawText(graphics, font(), text, insets.left(), insets.top(),
                Colors.argb(minecraftTextColor), minecraftTextShadow);
    }
}
