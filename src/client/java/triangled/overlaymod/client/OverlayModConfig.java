package triangled.overlaymod.client;

import java.util.Locale.Category;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;
import org.polyfrost.compose.render.PolyColor;
import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.annotations.Color;
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown;
import org.polyfrost.oneconfig.api.config.v1.annotations.Keybind;
import org.polyfrost.oneconfig.api.config.v1.annotations.MasterToggle;
import org.polyfrost.oneconfig.api.config.v1.annotations.Number;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.oneconfig.api.ui.v1.keybind.KeybindHelper;
import org.polyfrost.oneconfig.api.ui.v1.keybind.OneConfigKeybind;
import triangled.overlaymod.client.utils.TextAlignment;
import triangled.overlaymod.client.utils.TextShadow;

public class OverlayModConfig extends Config {
        public static final OverlayModConfig INSTANCE = new OverlayModConfig();

        @MasterToggle
        public static boolean enabled = true;

        // ---------------------------------------------------------------- Visibility

        @Switch(title = "Durability Text", description = "Show the remaining durability over the item's durability bar", category = "Durability", subcategory = "Visibility")
        public static boolean durabilityShowText = true;

        @Switch(title = "Durability Bar", description = "Show the item's vanilla durability bar", category = "Durability", subcategory = "Visibility")
        public static boolean durabilityShowBar = true;

        @Switch(title = "Show At Full Durability", description = "Keep showing the durability on undamaged items", category = "Durability", subcategory = "Visibility")
        public static boolean durabilityShowAtFull = false;

        // ---------------------------------------------------------------- Format

        @Switch(title = "Show As Percentage", description = "Show remaining durability as a percentage instead of a count", category = "Durability", subcategory = "Format")
        public static boolean durabilityAsPercentage = false;

        @Switch(title = "Use Subscript", description = "Draw the durability using subscript digits", category = "Durability", subcategory = "Format")
        public static boolean durabilityUseSubscript = true;

        @Slider(title = "Normal Digit Limit", description = "Digits shown before switching to scientific notation", min = 1, max = 10, step = 1, category = "Durability", subcategory = "Format")
        public static int durabilityNormalDigitLimit = 4;

        @Slider(title = "Subscript Digit Limit", description = "Digits shown before switching to scientific notation, in subscript", min = 1, max = 10, step = 1, category = "Durability", subcategory = "Format")
        public static int durabilitySubscriptDigitLimit = 5;

        // ---------------------------------------------------------------- Style

        @Dropdown(title = "Text Shadow", description = "How the durability text is shadowed or outlined", category = "Durability", subcategory = "Style")
        public static TextShadow durabilityTextShadow = TextShadow.SHADOW;

        @Switch(title = "Use Custom Color", description = "Use the colour below instead of the bar's own colour", category = "Durability", subcategory = "Style")
        public static boolean durabilityUseCustomColor = false;

        @Color(title = "Color", description = "Colour of the durability text when a custom colour is in use", category = "Durability", subcategory = "Style")
        public static PolyColor durabilityColor = new PolyColor(0xFFFFFFFF, false, 1.0F);

        // ---------------------------------------------------------------- Position

        @Dropdown(title = "Alignment", description = "Where the durability text sits over the bar", category = "Durability", subcategory = "Position")
        public static TextAlignment durabilityAlignment = TextAlignment.RIGHT;

        @Number(title = "X Offset", description = "Horizontal nudge, in scaled pixels", min = -32, max = 32, category = "Durability", subcategory = "Position")
        public static float durabilityXOffset = 0.0F;

        @Number(title = "Y Offset", description = "Vertical nudge, in scaled pixels", min = -32, max = 32, category = "Durability", subcategory = "Position")
        public static float durabilityYOffset = 0.0F;

        @Number(title = "Subscript Y Offset", description = "Extra vertical nudge applied only to subscript text", min = -32, max = 32, category = "Durability", subcategory = "Position")
        public static float durabilitySubscriptYOffset = 0.0F;

        @Number(title = "Exponent Y Offset", description = "Vertical nudge for the exponent marker in scientific notation", min = -32, max = 32, category = "Durability", subcategory = "Position")
        public static float durabilityExponentYOffset = 0.0F;

        // ---------------------------------------------------------------- Chat Peek

        @Switch(title = "Chat Peek", description = "Show chat without opening it", category = "Chat Peek", subcategory = "General")
        public static boolean chatPeekEnabled = true;

        @Switch(title = "Toggle Mode", description = "Press the peek key to toggle peeking instead of holding it", category = "Chat Peek", subcategory = "General")
        public static boolean chatPeekToggle = false;

        @Keybind(title = "Peek Key", description = "Hold (or press, in toggle mode) to peek at chat", category = "Chat Peek", subcategory = "General")
        public static OneConfigKeybind chatPeekKey = KeybindHelper.builder()
                        .key(InputConstants.KEY_Z)
                        .action((Consumer<Boolean>) ChatPeek::setKeyHeld)
                        .register();

        @Switch(title = "Scroll Chat", description = "Use the scroll wheel to scroll chat while peeking", category = "Chat Peek", subcategory = "General")
        public static boolean chatPeekScroll = true;

        @Slider(title = "Scroll Lines", description = "Lines scrolled while peeking", min = 1, max = 20, step = 1, category = "Chat Peek", subcategory = "General")
        public static int chatPeekScrollLines = 7;

        // ----------------------------------------------------------------

        public OverlayModConfig() {
                super(
                                "overlay-mod.json",
                                "/assets/overlay-mod/icon.png",
                                "Overlay Mod",
                                Category.HUD);
        }
}
