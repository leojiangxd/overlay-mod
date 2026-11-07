package triangled.overlaymod.client.hud;

import org.polyfrost.compose.render.RenderContext;
import org.polyfrost.oneconfig.api.hud.v1.Hud;

// This is a base class for text-based HUDs
public abstract class MinecraftTextHud extends OverlayHud {
    private transient String displayedText = "";

    protected MinecraftTextHud(String id, String title, Hud.Category category) {
        super(id, title, category);
    }

    // Returns the text to show, or blank to hide the HUD.
    protected abstract String getText();

    // Returns sample text for the HUD editor when getText is blank.
    protected abstract String previewText();

    // OneConfig calls this every update to grab the latest text and resize.
    @Override
    protected void refresh() {
        String currentText = getText();
        displayedText = currentText.isEmpty() && isEditing() ? previewText() : currentText;
        setSize(text().width(displayedText), text().height());
    }

    // Hides the HUD when there's no text to show.
    @Override
    public boolean shouldShow() {
        return !displayedText.isEmpty();
    }

    // Draws the text using the HUD's text settings.
    @Override
    protected void paint(RenderContext renderContext, float x, float y) {
        text().draw(renderContext, displayedText, x, y, text().textColor());
    }
}
