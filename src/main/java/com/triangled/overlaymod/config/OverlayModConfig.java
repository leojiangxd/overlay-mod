package com.triangled.overlaymod.config;

import com.triangled.overlaymod.OverlayMod;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = OverlayMod.MOD_ID)
public class OverlayModConfig implements ConfigData {
    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    @ConfigEntry.Category("coordinates")
    public CoordinatesCategory coordinates = new CoordinatesCategory();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    @ConfigEntry.Category("sprinting")
    public SprintingCategory sprinting = new SprintingCategory();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    @ConfigEntry.Category("clock")
    public ClockCategory clock = new ClockCategory();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    @ConfigEntry.Category("statusEffects")
    public StatusEffectsCategory statusEffects = new StatusEffectsCategory();

    @Config(name = "coordinates")
    public static class CoordinatesCategory {
        public boolean showCoordinates = true;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public DirectionStyle directionStyle = DirectionStyle.SMALLCAPS;

        public String xText = "&r";
        public String yText = "&r";
        public String zText = "&r";
        public String dirFacingPos = "&e₍₊₎";
        public String dirFacingNeg = "&e₍₋₎";
        public String dirText = "&r";
        public String deliminator = "&e, ";

        @ConfigEntry.Gui.Excluded
        private static final String[] smallCapsDirections = {"ɴ", "ɴᴇ", "ᴇ", "sᴇ", "s", "sᴡ", "ᴡ", "ɴᴡ"};
        @ConfigEntry.Gui.Excluded
        private static final String[] lowercaseDirections = {"n", "ne", "e", "se", "s", "sw", "w", "nw"};
        @ConfigEntry.Gui.Excluded
        private static final String[] uppercaseDirections = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

        public String[] getCurrentDirectionArray() {
            return switch (directionStyle) {
                case LOWERCASE -> lowercaseDirections;
                case UPPERCASE -> uppercaseDirections;
                case NONE -> new String[0];
                default -> smallCapsDirections;
            };
        }

        public enum DirectionStyle {
            SMALLCAPS, LOWERCASE, UPPERCASE, NONE
        }
    }

    @Config(name = "statusEffects")
    public static class StatusEffectsCategory {
        public boolean showStatusEffects = true;
        public boolean renderBackground = false;
        public String dayText = "ᴅ";
        public String hourText = "ʜ";
        public String amplifierText = "&f";
        public String durationText = "&f";
        public String ambientAmplifierText = "&e";
        public String ambientDurationText = "&e";
        public int expirationDuration = 10;
        public String expirationText = "&c";
    }

    @Config(name = "sprinting")
    public static class SprintingCategory {
        public boolean showSprinting = true;
        public String sprintingText = "ꜱᴘʀɪɴᴛɪɴɢ &e| ";
    }

    @Config(name = "clock")
    public static class ClockCategory {
        public boolean showClock = true;
        public String clockText = "&r";
        public String clockFormat = "h:mm";
    }

    public static String replaceAnd(String input) {
        input = input.replaceAll("(?<!\\\\)&", "§");
        input = input.replaceAll("\\\\&", "&");
        return input;
    }
}

