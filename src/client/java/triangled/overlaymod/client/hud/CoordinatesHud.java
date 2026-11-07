package triangled.overlaymod.client.hud;

import java.util.List;
import java.util.Locale;
import kotlin.Pair;
import net.minecraft.client.player.LocalPlayer;
import org.polyfrost.compose.render.RenderContext;
import org.polyfrost.oneconfig.api.config.v1.annotations.Number;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.config.v1.annotations.Text;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import triangled.overlaymod.client.utils.AxisSign;
import triangled.overlaymod.client.utils.CompassDirection;

public class CoordinatesHud extends MinecraftTextHud {
    private static final float BASE_SIGN_GAP = -1.0F;

    // --------------- CONFIGS ---------------
    @Slider(title = "Precision", description = "Decimal places shown for each coordinate", min = 0, max = 10, step = 1, subcategory = "Coordinates")
    public int precision = 0;

    @Text(title = "Separator", description = "Text placed between each coordinate (horizontal layout only)", subcategory = "Coordinates")
    public String separator = ", ";

    @Switch(title = "Vertical", description = "Stack X, Y and Z on their own lines with the axis signs in a right-aligned column", subcategory = "Layout")
    public boolean vertical = false;

    @Number(title = "Sign Gap", description = "Space between the longest coordinate and the axis signs in the vertical layout", min = -16, max = 64, subcategory = "Layout")
    public float signGap = 0.0F;

    @Number(title = "Row Gap", description = "Extra space between rows in the vertical layout", min = -8, max = 32, subcategory = "Layout")
    public float rowGap = 0.0F;

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

    // One line of the vertical layout: the coordinate and its axis sign.
    private record Row(String coordinate, String sign) {
    }

    // The vertical rows plus the widths of the coordinate and sign columns.
    private record VerticalLayout(List<Row> rows, float coordinateWidth, float signWidth) {
        static final VerticalLayout EMPTY = new VerticalLayout(List.of(), 0, 0);
    }

    private transient VerticalLayout verticalLayout = VerticalLayout.EMPTY;

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
        return coordinates(player.getX(), player.getY(), player.getZ(), facing(player));
    }

    // Shows sample coordinates in the HUD editor when there's no player.
    @Override
    protected String previewText() {
        return coordinates(0, 64, 0, null);
    }

    // Returns the direction the player is facing, or null if signs are off.
    private CompassDirection facing(LocalPlayer player) {
        return axisSigns ? CompassDirection.fromYaw(player.getYRot(1.0F), true) : null;
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
        text.append(prefix).append(coordinate(value)).append(signLabel(sign)).append(suffix);
    }

    // Returns one coordinate with its prefix and suffix.
    private String axisText(String prefix, double value, String suffix) {
        return prefix + coordinate(value) + suffix;
    }

    // Returns the sign with its prefix and suffix, or blank if there's none.
    private String signLabel(AxisSign sign) {
        if (sign == null) {
            return "";
        }
        String signText = signText(sign);
        return signText.isEmpty() ? "" : signPrefix + signText + signSuffix;
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

    // Returns the X, Y and Z rows for the vertical layout.
    private List<Row> rows(double x, double y, double z, CompassDirection facing) {
        return List.of(
                new Row(axisText(prefixX, x, suffixX), facing == null ? "" : signLabel(facing.xSign())),
                new Row(axisText(prefixY, y, suffixY), ""),
                new Row(axisText(prefixZ, z, suffixZ), facing == null ? "" : signLabel(facing.zSign())));
    }

    // Returns the player's rows, preview rows while editing, or none.
    private List<Row> currentRows() {
        LocalPlayer player = player();
        if (player != null) {
            return rows(player.getX(), player.getY(), player.getZ(), facing(player));
        }
        return isEditing() ? rows(0, 64, 0, null) : List.of();
    }

    // Returns the space between the coordinates and signs. A Sign Gap of 0
    // means a built-in -1, so the sign starts 1 pixel into the coordinate.
    private float signSpacing() {
        return BASE_SIGN_GAP + signGap;
    }

    // Returns the distance from the top of one row to the top of the next.
    private float rowStride() {
        return text().height() + rowGap;
    }

    // OneConfig calls this every update to grab the coordinates and resize.
    @Override
    protected void refresh() {
        if (!vertical) {
            verticalLayout = VerticalLayout.EMPTY;
            super.refresh();
            return;
        }
        List<Row> rows = currentRows();
        float coordinateWidth = 0;
        float signWidth = 0;
        for (Row row : rows) {
            coordinateWidth = Math.max(coordinateWidth, text().width(row.coordinate()));
            if (!row.sign().isEmpty()) {
                signWidth = Math.max(signWidth, text().width(row.sign()));
            }
        }
        verticalLayout = new VerticalLayout(rows, coordinateWidth, signWidth);
        float width = coordinateWidth + (signWidth > 0 ? signSpacing() + signWidth : 0);
        float height = rows.isEmpty() ? 0 : (rows.size() - 1) * rowStride() + text().height();
        setSize(width, height);
    }

    // Hides the HUD when there are no coordinates to show.
    @Override
    public boolean shouldShow() {
        return vertical ? !verticalLayout.rows().isEmpty() : super.shouldShow();
    }

    // Draws one line, or the left coordinate and right sign columns.
    @Override
    protected void paint(RenderContext renderContext, float x, float y) {
        if (!vertical) {
            super.paint(renderContext, x, y);
            return;
        }
        VerticalLayout layout = verticalLayout;
        float signColumnRight = x + layout.coordinateWidth() + signSpacing() + layout.signWidth();
        for (int i = 0; i < layout.rows().size(); i++) {
            Row row = layout.rows().get(i);
            float rowY = y + i * rowStride();
            text().draw(renderContext, row.coordinate(), x, rowY, text().textColor());
            if (!row.sign().isEmpty()) {
                float signX = signColumnRight - text().width(row.sign());
                text().draw(renderContext, row.sign(), signX, rowY, text().textColor());
            }
        }
    }

    // Default position of HUD in OneConfig's HUD editor
    @Override
    public Pair<Float, Float> defaultPosition() {
        return new Pair<>(3.0F, 3.0F);
    }

    // Gives each copy of the HUD its own rows so copies don't share them.
    @Override
    public Hud clone() {
        CoordinatesHud copy = (CoordinatesHud) super.clone();
        copy.verticalLayout = VerticalLayout.EMPTY;
        return copy;
    }
}
