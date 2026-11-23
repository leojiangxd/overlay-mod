package triangled.overlaymod.client.hud;

import org.polyfrost.compose.render.RenderContext;
import org.polyfrost.oneconfig.api.hud.v1.Hud;

public abstract class MinecraftTextHud extends OverlayHud {
    private transient String displayedText = "";

    protected MinecraftTextHud(String id, String title, Hud.Category category) {
        super(id, title, category);
    }

    // Returns the text to show, or blank to hide the HUD.
    protected abstract String getText();

    // Returns sample text for the HUD editor when getText is blank.
    protected abstract String previewText();

    @Override
    protected void refresh() {
        String currentText = getText();
        displayedText = currentText.isEmpty() && isEditing() ? previewText() : currentText;
        setSize(text().width(displayedText), text().height());
    }

    @Override
    public boolean shouldShow() {
        return !displayedText.isEmpty();
    }

    @Override
    protected void paint(RenderContext renderContext, float x, float y) {
        text().draw(renderContext, displayedText, x, y, text().textColor());
    }
}
