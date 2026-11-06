package triangled.overlaymod.client.hud;

import java.util.Locale;
import kotlin.Pair;
import net.minecraft.client.player.LocalPlayer;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.config.v1.annotations.Text;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import triangled.overlaymod.client.utils.AxisSign;
import triangled.overlaymod.client.utils.CompassDirection;

public class CoordinatesHud extends MinecraftTextHud {
    // ---------------------------------------------------------------- Coordinates

    @Slider(title = "Precision", description = "Decimal places shown for each coordinate", min = 0, max = 10, step = 1, subcategory = "Coordinates")
    public int precision = 0;

    @Switch(title = "Show Labels", description = "Show the X/Y/Z labels before each coordinate", subcategory = "Coordinates")
    public boolean showLabels = true;

    @Text(title = "X Label", description = "Text shown before the X coordinate. & starts a formatting code, \\& is a literal &", subcategory = "Coordinates")
    public String labelX = "&r";

    @Text(title = "Y Label", description = "Text shown before the Y coordinate. & starts a formatting code, \\& is a literal &", subcategory = "Coordinates")
    public String labelY = "&r";

    @Text(title = "Z Label", description = "Text shown before the Z coordinate. & starts a formatting code, \\& is a literal &", subcategory = "Coordinates")
    public String labelZ = "&r";

    @Text(title = "Separator", description = "Text placed between each coordinate", subcategory = "Coordinates")
    public String separator = "&e, ";

    // ---------------------------------------------------------------- Axis Signs

    @Switch(title = "Axis Signs", description = "Show the axis sign after X and Z to indicate which way you are facing", subcategory = "Axis Signs")
    public boolean axisSigns = true;

    @Text(title = "Positive Sign", description = "Text shown when the axis value is increasing", subcategory = "Axis Signs")
    public String signPos = "&e⁽⁺⁾";

    @Text(title = "Negative Sign", description = "Text shown when the axis value is decreasing", subcategory = "Axis Signs")
    public String signNeg = "&e⁽⁻⁾";

    @Text(title = "Neutral Sign", description = "Text shown when the axis is not changing", subcategory = "Axis Signs")
    public String signNone = "";

    public CoordinatesHud() {
        super("overlaymod_coordinates.yml", "Coordinates", Hud.Category.Companion.getINFO());
    }

    @Override
    protected String getText() {
        LocalPlayer player = player();
        if (player == null) {
            return "";
        }
        CompassDirection facing = axisSigns ? CompassDirection.fromYaw(player.getYRot(1.0F), true) : null;
        return coordinates(player.getX(), player.getY(), player.getZ(), facing);
    }

    @Override
    protected String previewText() {
        return coordinates(0, 64, 0, null);
    }

    private String coordinates(double x, double y, double z, CompassDirection facing) {
        StringBuilder text = new StringBuilder(32);
        appendAxis(text, labelX, x, facing == null ? null : facing.xSign());
        text.append(separator);
        appendAxis(text, labelY, y, null);
        text.append(separator);
        appendAxis(text, labelZ, z, facing == null ? null : facing.zSign());
        return text.toString();
    }

    private void appendAxis(StringBuilder text, String label, double value, AxisSign sign) {
        if (showLabels) {
            text.append(label);
        }
        text.append(coordinate(value));
        if (sign != null) {
            text.append(signText(sign));
        }
    }

    private String signText(AxisSign sign) {
        return switch (sign) {
            case POSITIVE -> signPos;
            case NEGATIVE -> signNeg;
            case NONE -> signNone;
        };
    }

    private String coordinate(double value) {
        if (precision <= 0) {
            return Long.toString((long) Math.floor(value));
        }
        return String.format(Locale.ROOT, "%." + precision + "f", value);
    }

    @Override
    public Pair<Float, Float> defaultPosition() {
        return new Pair<>(3.0F, 3.0F);
    }
}
