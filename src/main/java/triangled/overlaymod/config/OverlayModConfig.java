package triangled.overlaymod.config;

import triangled.overlaymod.OverlayMod;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.EnumHandler.EnumDisplayOption;

@Config(name = OverlayMod.MOD_ID)
public class OverlayModConfig implements ConfigData {
    public enum TextShadow {
        NONE, SHADOW, FOUR_DIRECTION, EIGHT_DIRECTION
    }

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("coordinates")
    public CoordinatesCategory coordinates = new CoordinatesCategory();

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("clock")
    public ClockCategory clock = new ClockCategory();

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("sprinting")
    public SprintingCategory sprinting = new SprintingCategory();

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("statusEffects")
    public StatusEffectsCategory statusEffects = new StatusEffectsCategory();

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("equipment")
    public EquipmentCategory equipment = new EquipmentCategory();

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("durability")
    public DurabilityCategory durability = new DurabilityCategory();

    @ConfigEntry.Gui.TransitiveObject
    @ConfigEntry.Category("bossbar")
    public BossBarCategory bossbar = new BossBarCategory();

    @Config(name = "coordinates")
    public static class CoordinatesCategory {
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Visibility visibility = new Visibility();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Positioning positioning = new Positioning();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Style style = new Style();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Text text = new Text();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Directions directions = new Directions();

        public String getDirectionText(int index) {
            return switch (index) {
                case 0 -> directions.north;
                case 1 -> directions.northEast;
                case 2 -> directions.east;
                case 3 -> directions.southEast;
                case 4 -> directions.south;
                case 5 -> directions.southWest;
                case 6 -> directions.west;
                default -> directions.northWest;
            };
        }

        public static class Visibility {
            public boolean showCoordinates = true;
            public boolean showDirection = true;
        }

        public static class Text {
            public String xText = "&r";
            public String yText = "&r";
            public String zText = "&r";
            public String dirFacingPos = "&e⁽⁺⁾";
            public String dirFacingNeg = "&e⁽⁻⁾";
            public String dirText = "&r";
            public String deliminator = "&e, ";
        }

        public static class Directions {
            public String north = "ɴ";
            public String northEast = "ɴᴇ";
            public String east = "ᴇ";
            public String southEast = "sᴇ";
            public String south = "s";
            public String southWest = "sᴡ";
            public String west = "ᴡ";
            public String northWest = "ɴᴡ";
        }

        public static class Style {
            @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
            public TextShadow textShadow = TextShadow.SHADOW;
            @ConfigEntry.ColorPicker(allowAlpha = true)
            public int xColor = 0xFFFFFFFF;
            @ConfigEntry.ColorPicker(allowAlpha = true)
            public int yColor = 0xFFFFFFFF;
            @ConfigEntry.ColorPicker(allowAlpha = true)
            public int zColor = 0xFFFFFFFF;
            @ConfigEntry.ColorPicker(allowAlpha = true)
            public int dirFacingPosColor = 0xFFFFFFFF;
            @ConfigEntry.ColorPicker(allowAlpha = true)
            public int dirFacingNegColor = 0xFFFFFFFF;
            @ConfigEntry.ColorPicker(allowAlpha = true)
            public int dirColor = 0xFFFFFFFF;
            @ConfigEntry.ColorPicker(allowAlpha = true)
            public int deliminatorColor = 0xFFFFFFFF;
        }

        public static class Positioning {
            public float xOffset = 0f;
            public float yOffset = 0f;
            public float dirXOffset = 0f;
            public float dirYOffset = 0f;
        }
    }

    @Config(name = "statusEffects")
    public static class StatusEffectsCategory {
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Visibility visibility = new Visibility();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Positioning positioning = new Positioning();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Style style = new Style();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Text text = new Text();

        public static class Visibility {
            public boolean showStatusEffects = true;
            public boolean renderBackground = false;
            public boolean separateNegativeEffects = true;
            public boolean subscriptAmplifiers = true;
            public boolean renderAmplifier = true;
            public boolean renderDuration = true;
        }

        public static class Text {
            public String secondsFormat = "0:ss";
            public String minutesFormat = "m:ss";
            public String minutesFormatLong = "m\\m";
            public String hourFormat = "h\\h";
            public String dayFormat = "d\\d";
            public String amplifierText = "&f";
            public String durationText = "&f";
            public String ambientAmplifierText = "&e";
            public String ambientDurationText = "&e";
            public String expirationText = "&c";
            public int expirationDuration = 10;
        }

        public static class Style {
            @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
            public TextShadow amplifierTextShadow = TextShadow.FOUR_DIRECTION;
            @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
            public TextShadow durationTextShadow = TextShadow.SHADOW;
            @ConfigEntry.ColorPicker(allowAlpha = true)
            public int amplifierColor = 0xFFFFFFFF;
            @ConfigEntry.ColorPicker(allowAlpha = true)
            public int ambientAmplifierColor = 0xFFFFFFFF;
            @ConfigEntry.ColorPicker(allowAlpha = true)
            public int durationColor = 0xFFFFFFFF;
            @ConfigEntry.ColorPicker(allowAlpha = true)
            public int ambientDurationColor = 0xFFFFFFFF;
            @ConfigEntry.ColorPicker(allowAlpha = true)
            public int expirationColor = 0xFFFFFFFF;
        }

        public static class Positioning {
            public int effectGap = 1;
            public float negativeEffectYOffset = 8f;
            public float bossBarInitialYOffset = 0f;
            public float statusEffectXOffset = 0f;
            public float statusEffectYOffset = 0f;
            public float amplifierScale = 1.0f;
            public float amplifierXOffset = 0f;
            public float amplifierYOffset = 0f;
            public float durationScale = 1.0f;
            public float durationXOffset = 0f;
            public float durationYOffset = 0f;
        }
    }

    @Config(name = "equipment")
    public static class EquipmentCategory {
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Visibility visibility = new Visibility();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Positioning positioning = new Positioning();

        public static class Visibility {
            public boolean showEquipment = true;
            public boolean showArmor = true;
            public boolean showEmptyArmor = false;
            public boolean reverseArmorOrder = false;
            public boolean renderBackground = true;
        }

        public static class Positioning {
            @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
            public ArmorPosition armorPosition = ArmorPosition.HOTBAR_RIGHT;
            public float equipmentXOffset = 0f;
            public float equipmentYOffset = 0f;
        }

        public enum ArmorPosition {
            HOTBAR_LEFT, HOTBAR_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
        }
    }

    @Config(name = "sprinting")
    public static class SprintingCategory {
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Visibility visibility = new Visibility();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Positioning positioning = new Positioning();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Style style = new Style();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Text text = new Text();

        public static class Visibility {
            public boolean showSprinting = true;
        }

        public static class Text {
            public String sprintingText = "ꜱᴘʀɪɴᴛɪɴɢ";
        }

        public static class Style {
            @ConfigEntry.ColorPicker(allowAlpha = true)
            public int color = 0xFFFFFFFF;
        }

        public static class Positioning {
            public float xOffset = 0f;
            public float yOffset = 0f;
        }
    }

    @Config(name = "clock")
    public static class ClockCategory {
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Visibility visibility = new Visibility();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Positioning positioning = new Positioning();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Style style = new Style();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Text text = new Text();

        public static class Visibility {
            public boolean showClock = true;
        }

        public static class Text {
            public String clockText = "&r";
            public String clockFormat = "h:mm";
            public String deliminator = "&e | ";
        }

        public static class Style {
            @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
            public TextShadow textShadow = TextShadow.SHADOW;
            @ConfigEntry.ColorPicker(allowAlpha = true)
            public int color = 0xFFFFFFFF;
            @ConfigEntry.ColorPicker(allowAlpha = true)
            public int deliminatorColor = 0xFFFFFFFF;
        }

        public static class Positioning {
            public float xOffset = 0f;
            public float yOffset = 0f;
        }
    }

    @Config(name = "bossbar")
    public static class BossBarCategory {
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Visibility visibility = new Visibility();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Positioning positioning = new Positioning();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Style style = new Style();

        public static class Visibility {
            public boolean shouldScaleBossBars = true;
        }

        public static class Style {
            @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
            public TextShadow nameTextShadow = TextShadow.SHADOW;
            @ConfigEntry.ColorPicker(allowAlpha = true)
            public int nameColor = 0xFFFFFFFF;
        }

        public static class Positioning {
            public float scale = 0.5F;
            public float xOffset = 0f;
            public float yOffset = 3.0F;
            public int maxHeight = 6;
        }
    }

    @Config(name = "durability")
    public static class DurabilityCategory {
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Visibility visibility = new Visibility();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Format format = new Format();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Positioning positioning = new Positioning();
        @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
        public Style style = new Style();

        public static class Visibility {
            public boolean showText = true;
            public boolean showBar = true;
            public boolean showAtFullDurability = false;
        }

        public static class Format {
            public boolean showAsPercentage = false;
            public boolean useSubscript = true;
            public int normalDigitLimit = 4;
            public int subscriptDigitLimit = 5;
        }

        public static class Style {
            @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
            public TextShadow textShadow = TextShadow.SHADOW;
            public boolean useCustomColor = false;
            @ConfigEntry.ColorPicker(allowAlpha = true)
            public int color = 0xFFFFFFFF;
        }

        public static class Positioning {
            @ConfigEntry.Gui.EnumHandler(option = EnumDisplayOption.BUTTON)
            public TextAlignment alignment = TextAlignment.RIGHT;
            public float xOffset = 0f;
            public float yOffset = 0f;
            public float subscriptYOffset = 0f;
            public float exponentYOffset = 0f;
        }

        public enum TextAlignment {
            LEFT, MIDDLE, RIGHT
        }
    }

    public static String replaceAnd(String input) {
        input = input.replaceAll("(?<!\\\\)&", "§");
        input = input.replaceAll("\\\\&", "&");
        return input;
    }
}
