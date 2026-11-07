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
    // --------------- CONFIGS ---------------
    @Slider(title = "Precision", description = "Decimal places shown for each coordinate", min = 0, max = 10, step = 1, subcategory = "Coordinates")
    public int precision = 0;

    @Text(title = "Separator", description = "Text placed between each coordinate", subcategory = "Coordinates")
    public String separator = ", ";

    @Text(title = "X Prefix", description = "Text shown before the X coordinate", subcategory = "X")
    public String prefixX = "";

    @Text(title = "X Suffix", description = "Text shown after the X coordinate", subcategory = "X")
    public String suffixX = "";

    @Text(title = "Y Prefix", description = "Text shown before the Y coordinate", subcategory = "Y")
    public String prefixY = "";

    @Text(title = "Y Suffix", description = "Text shown after the Y coordinate", subcategory = "Y")
    public String suffixY = "";

    @Text(title = "Z Prefix", description = "Text shown before the Z coordinate", subcategory = "Z")
    public String prefixZ = "";

    @Text(title = "Z Suffix", description = "Text shown after the Z coordinate", subcategory = "Z")
    public String suffixZ = "";

    @Switch(title = "Axis Signs", description = "Show the axis sign after X and Z to indicate which way you are facing", subcategory = "Axis Signs")
    public boolean axisSigns = true;

    @Text(title = "Sign Prefix", description = "Text shown before each axis sign", subcategory = "Axis Signs")
    public String signPrefix = "";

    @Text(title = "Sign Suffix", description = "Text shown after each axis sign", subcategory = "Axis Signs")
    public String signSuffix = "";

    @Text(title = "Positive Sign", description = "Text shown when the axis value is increasing", subcategory = "Axis Signs")
    public String signPos = "⁽⁺⁾";

    @Text(title = "Negative Sign", description = "Text shown when the axis value is decreasing", subcategory = "Axis Signs")
    public String signNeg = "⁽⁻⁾";

    @Text(title = "Neutral Sign", description = "Text shown when the axis is not changing. Leave empty to hide the sign", subcategory = "Axis Signs")
    public String signNone = "";
    // ---------------------------------------

    public CoordinatesHud() {
        super("overlaymod_coordinates.yml", "Coordinates", Hud.Category.Companion.getINFO());
    }

    // Returns the player's coordinates, with facing signs if enabled.
    @Override
    protected String getText() {
        LocalPlayer player = player();
        if (player == null) {
            return "";
        }
        CompassDirection facing = axisSigns ? CompassDirection.fromYaw(player.getYRot(1.0F), true) : null;
        return coordinates(player.getX(), player.getY(), player.getZ(), facing);
    }

    // Shows sample coordinates in the HUD editor when there's no player.
    @Override
    protected String previewText() {
        return coordinates(0, 64, 0, null);
    }

    // Builds the full X, Y, Z text with separators between each axis.
    private String coordinates(double x, double y, double z, CompassDirection facing) {
        StringBuilder text = new StringBuilder(32);
        appendAxis(text, prefixX, x, facing == null ? null : facing.xSign(), suffixX);
        text.append(separator);
        appendAxis(text, prefixY, y, null, suffixY);
        text.append(separator);
        appendAxis(text, prefixZ, z, facing == null ? null : facing.zSign(), suffixZ);
        return text.toString();
    }

    // Adds one axis to the text with its prefix, sign and suffix.
    private void appendAxis(StringBuilder text, String prefix, double value, AxisSign sign, String suffix) {
        text.append(prefix).append(coordinate(value));
        if (sign != null) {
            String signText = signText(sign);
            if (!signText.isEmpty()) {
                text.append(signPrefix).append(signText).append(signSuffix);
            }
        }
        text.append(suffix);
    }

    // Returns the text set for the given axis sign.
    private String signText(AxisSign sign) {
        return switch (sign) {
            case POSITIVE -> signPos;
            case NEGATIVE -> signNeg;
            case NONE -> signNone;
        };
    }

    // Formats coordinates to the chosen number of decimal places.
    private String coordinate(double value) {
        if (precision <= 0) {
            return Long.toString((long) Math.floor(value));
        }
        return String.format(Locale.ROOT, "%." + precision + "f", value);
    }

    // Default position of HUD in OneConfig's HUD editor
    @Override
    public Pair<Float, Float> defaultPosition() {
        return new Pair<>(3.0F, 3.0F);
    }
}
