package triangled.overlaymod.client.hud;

import java.time.DateTimeException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import kotlin.Pair;
import org.polyfrost.oneconfig.api.config.v1.annotations.Text;
import org.polyfrost.oneconfig.api.hud.v1.Font;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.HudManager;
import org.polyfrost.oneconfig.api.hud.v1.TextHud;

public class ClockHud extends TextHud {
    private String cachedPattern;
    private DateTimeFormatter cachedFormatter;

    @Text(title = "Format", description = "Date/time pattern, for example h:mm a. Wrap literal text in single quotes")
    public String format = "h:mm a";

    public ClockHud() {
        super("overlaymod_clock.yml", "Clock", Hud.Category.Companion.getINFO(), "", "");
        setFont(Font.Minecraft);
    }

    @Override
    protected String getText() {
        DateTimeFormatter formatter = formatter();
        if (formatter == null) {
            return "";
        }

        try {
            return ZonedDateTime.now().format(formatter);
        } catch (DateTimeException e) {
            cachedFormatter = null;
            return "";
        }
    }

    private DateTimeFormatter formatter() {
        if (!Objects.equals(format, cachedPattern)) {
            cachedPattern = format;
            try {
                cachedFormatter = DateTimeFormatter.ofPattern(format);
            } catch (IllegalArgumentException e) {
                cachedFormatter = null;
            }
        }
        return cachedFormatter;
    }

    @Override
    public Pair<Float, Float> defaultPosition() {
        return new Pair<>(HudManager.guiScreenWidth - getScaledWidth() - 4.0F, 4.0F);
    }
}
