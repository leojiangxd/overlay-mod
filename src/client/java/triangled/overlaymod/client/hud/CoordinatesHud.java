package triangled.overlaymod.client.hud;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import kotlin.Pair;
import net.minecraft.client.player.LocalPlayer;
import org.polyfrost.compose.render.RenderContext;
import org.polyfrost.oneconfig.api.config.v1.annotations.Number;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.config.v1.annotations.Text;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import triangled.overlaymod.client.utils.CompassDirection;

public class CoordinatesHud extends MinecraftTextHud {
    // Sign Gap 0 means -1, so the sign starts 1 pixel into the coordinate.
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

    private record Row(String coordinate, String sign) {
    }

    private record VerticalLayout(List<Row> rows, float coordinateWidth, float signWidth) {
        static final VerticalLayout EMPTY = new VerticalLayout(List.of(), 0, 0);
    }

    private transient VerticalLayout verticalLayout = VerticalLayout.EMPTY;

    public CoordinatesHud() {
        super("overlaymod_coordinates.yml", "Coordinates", Hud.Category.Companion.getINFO());
    }

    @Override
    protected String getText() {
        return joined(currentRows());
    }

    @Override
    protected String previewText() {
        return joined(rows(0, 64, 0, null));
    }

    private String joined(List<Row> rows) {
        return rows.stream().map(row -> row.coordinate() + row.sign()).collect(Collectors.joining(separator));
    }

    private String signLabel(int sign) {
        String text = sign > 0 ? signPos : sign < 0 ? signNeg : signNone;
        return text.isEmpty() ? "" : signPrefix + text + signSuffix;
    }

    private String coordinate(double value) {
        if (precision <= 0) {
            return Long.toString(Math.round(value));
        }
        return String.format(Locale.ROOT, "%." + precision + "f", value);
    }

    private List<Row> rows(double x, double y, double z, CompassDirection facing) {
        return List.of(
                new Row(prefixX + coordinate(x) + suffixX, facing == null ? "" : signLabel(facing.dx)),
                new Row(prefixY + coordinate(y) + suffixY, ""),
                new Row(prefixZ + coordinate(z) + suffixZ, facing == null ? "" : signLabel(facing.dz)));
    }

    private List<Row> currentRows() {
        LocalPlayer player = player();
        if (player != null) {
            return rows(player.getX(), player.getY(), player.getZ(), axisSigns ? CompassDirection.fromYaw(player.getYRot(1.0F), true) : null);
        }
        return isEditing() ? rows(0, 64, 0, null) : List.of();
    }

    private float rowStride() {
        return text().height() + rowGap;
    }

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
        float width = coordinateWidth + (signWidth > 0 ? BASE_SIGN_GAP + signGap + signWidth : 0);
        float height = rows.isEmpty() ? 0 : (rows.size() - 1) * rowStride() + text().height();
        setSize(width, height);
    }

    @Override
    public boolean shouldShow() {
        return vertical ? !verticalLayout.rows().isEmpty() : super.shouldShow();
    }

    @Override
    protected void paint(RenderContext renderContext, float x, float y) {
        if (!vertical) {
            super.paint(renderContext, x, y);
            return;
        }
        VerticalLayout layout = verticalLayout;
        float signColumnRight = x + layout.coordinateWidth() + BASE_SIGN_GAP + signGap + layout.signWidth();
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

    @Override
    public Pair<Float, Float> defaultPosition() {
        return new Pair<>(2.0F, 2.0F);
    }
}
