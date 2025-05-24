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

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    @ConfigEntry.Category("equipment")
    public EquipmentCategory equipment = new EquipmentCategory();

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    @ConfigEntry.Category("bossbar")
    public BossBarCategory bossbar = new BossBarCategory();

    @Config(name = "coordinates")
    public static class CoordinatesCategory {
        public boolean showCoordinates = true;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public DirectionStyle directionStyle = DirectionStyle.SMALLCAPS;

        public String xText = "&r";
        public String yText = "&r";
        public String zText = "&r";
        public String dirFacingPos = "&e⁽⁺⁾";
        public String dirFacingNeg = "&e⁽⁻⁾";
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
        public boolean renderAmplifier = true;
        public boolean renderDuration = true;
        public String dayText = "d";
        public String hourText = "h";
        public String amplifierText = "&f";
        public String durationText = "&f";
        public String ambientAmplifierText = "&e";
        public String ambientDurationText = "&e";
        public int expirationDuration = 10;
        public String expirationText = "&c";
        public int effectWidth = 29;
        public float amplifierScale = 1.0f;
        public float amplifierXOffset = 1.0f;
        public float amplifierYOffset = 1.5f;
        public float durationScale = 1.0f;
        public float durationXOffset = 0;
        public float durationYOffset = 0;
        public boolean separateNegativeEffects = true;
        public int negativeEffectYOffset = 32;
        public int statusEffectYOffset = 0;
        public boolean superScriptAmplifiers = true;
        public int bossBarInitialYOffset = 0;
        @ConfigEntry.Gui.Tooltip(count = 1)
        public String filteredEffects = "";
        
    }

    @Config(name = "equipment")
    public static class EquipmentCategory {
        public boolean showEquipment = true;
        public boolean showEmptyMainHand = false;
        public boolean showEmptyOffHand = false;
        public boolean showEmptyArmor = true;
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public ArmorPosition armorPosition = ArmorPosition.HOTBAR_RIGHT;
        public boolean showMainHand = true;
        public boolean showArmor = true;
        public boolean showDurability = true;
        public boolean durabilityAsPercentage = false;
        public boolean reverseArmorOrder = true;
        public boolean renderMainHandBackground = true;
        public boolean renderBackground = true;
        public float durabilityScale = 1.0f;
        public int durabilityXOffset = 0;
        public int durabilityYOffset = 0;
        public int armorXOffset = 0;
        public int armorYOffset = 0;
        public enum ArmorPosition {
            HOTBAR_LEFT, HOTBAR_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
        }
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

    @Config(name = "bossbar")
    public static class BossBarCategory {
        public boolean shouldScaleBossBars = true;
        public float scale = 0.5F;
        public float yOffset = 3.0F;
        public int maxHeight = 6;
    }

    public static String replaceAnd(String input) {
        input = input.replaceAll("(?<!\\\\)&", "§");
        input = input.replaceAll("\\\\&", "&");
        return input;
    }
}

