package triangled.overlaymod.client.hud;

import java.util.Locale;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.config.v1.annotations.Text;
import org.polyfrost.oneconfig.api.hud.v1.Font;
import org.polyfrost.oneconfig.api.hud.v1.Hud;
import org.polyfrost.oneconfig.api.hud.v1.TextHud;
import triangled.overlaymod.client.utils.Facing;

public class CoordinatesHud extends TextHud {
    private static final int SIGN_NONE = 0;
    private static final int SIGN_POS = 1;
    private static final int SIGN_NEG = 2;

    private static final int[] X_SIGN = {SIGN_NONE, SIGN_POS, SIGN_POS, SIGN_POS, SIGN_NONE, SIGN_NEG, SIGN_NEG, SIGN_NEG};
    private static final int[] Z_SIGN = {SIGN_NEG, SIGN_NEG, SIGN_NONE, SIGN_POS, SIGN_POS, SIGN_POS, SIGN_NONE, SIGN_NEG};

    @Slider(title = "Precision", description = "Decimal places shown for each coordinate",
            min = 0, max = 10, step = 1, subcategory = "Coordinates")
    public int precision = 0;

    @Switch(title = "Show Labels", description = "Show the X/Y/Z labels before each coordinate",
            subcategory = "Coordinates")
    public boolean showLabels = true;

    @Text(title = "X Label", description = "Text shown before the X coordinate",
            subcategory = "Coordinates")
    public String labelX = "§cX: §f";

    @Text(title = "Y Label", description = "Text shown before the Y coordinate",
            subcategory = "Coordinates")
    public String labelY = "§aY: §f";

    @Text(title = "Z Label", description = "Text shown before the Z coordinate",
            subcategory = "Coordinates")
    public String labelZ = "§bZ: §f";

    @Text(title = "Separator", description = "Text placed between each coordinate",
            subcategory = "Coordinates")
    public String separator = ", ";

    @Switch(title = "Axis Signs", description = "Show the axis sign after X and Z to indicate which way you are facing",
            subcategory = "Axis Signs")
    public boolean axisSigns = true;

    @Text(title = "Positive Sign", description = "Text shown when the axis value is increasing",
            subcategory = "Axis Signs")
    public String signPos = "(+)";

    @Text(title = "Negative Sign", description = "Text shown when the axis value is decreasing",
            subcategory = "Axis Signs")
    public String signNeg = "(-)";

    @Text(title = "Neutral Sign", description = "Text shown when the axis is not changing",
            subcategory = "Axis Signs")
    public String signNone = "";

    public CoordinatesHud() {
        super("overlaymod_coordinates.yml", "Coordinates", Hud.Category.Companion.getINFO(), "", "");
        setFont(Font.Minecraft);
    }

    @Override
    protected String getText() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc == null ? null : mc.player;

        if (player == null) {
            return "";
        }

        String x = coordinate(player.getX());
        String y = coordinate(player.getY());
        String z = coordinate(player.getZ());

        int facing = Facing.index(player.getYRot(1.0F), 8);

        StringBuilder text = new StringBuilder(48);
        if (showLabels) text.append(labelX);
        text.append(x);
        if (axisSigns) text.append(X_SIGN[facing] == SIGN_POS ? signPos : X_SIGN[facing] == SIGN_NEG ? signNeg : signNone);
        text.append(separator);
        if (showLabels) text.append(labelY);
        text.append(y);
        text.append(separator);
        if (showLabels) text.append(labelZ);
        text.append(z);
        if (axisSigns) text.append(Z_SIGN[facing] == SIGN_POS ? signPos : Z_SIGN[facing] == SIGN_NEG ? signNeg : signNone);
        return text.toString();
    }

    private String coordinate(double value) {
        if (precision <= 0) {
            return Long.toString((long) Math.floor(value));
        }
        return String.format(Locale.ROOT, "%." + precision + "f", value);
    }

    @Override
    public Pair<Float, Float> defaultPosition() {
        return new Pair<>(4.0F, 4.0F);
    }
}