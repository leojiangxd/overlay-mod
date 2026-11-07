package triangled.overlaymod.client.utils;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import kotlin.Unit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;
import org.polyfrost.compose.render.PolyColor;
import org.polyfrost.compose.render.PolyFont;
import org.polyfrost.compose.render.RenderContext;
import org.polyfrost.oneconfig.api.hud.v1.Hud;

// The text settings from a HUD, used to measure and draw its text.
public final class HudTextStyle {
    private final boolean usePoppins;
    private final String poppinsFontName;
    private final float textScale;
    private final String hudFormatCodes;
    private final boolean underlined;
    private final int caseType;
    private final boolean dropShadow;
    private final boolean outline;
    private final PolyColor shadowColor;
    private final float shadowOffsetX;
    private final float shadowOffsetY;
    private final PolyColor textColor;

    // Copies the font, scale, shadow, color and formatting settings from a HUD.
    private HudTextStyle(Hud hud) {
        usePoppins = hud.getFont() == org.polyfrost.oneconfig.api.hud.v1.Font.Poppins;
        poppinsFontName = usePoppins ? hud.getPoppinsFontName() : null;
        textScale = hud.getTextScale();
        underlined = hud.getTextUnderline();
        caseType = hud.getCaseType();
        dropShadow = hud.getShowShadow() && !hud.getFullShadow();
        outline = hud.getShowShadow() && hud.getFullShadow();
        shadowColor = new PolyColor(hud.getShadowColor(), hud.getShadowChroma(), hud.getShadowChromaSpeed());
        shadowOffsetX = hud.getShadowOffsetX();
        shadowOffsetY = hud.getShadowOffsetY();
        textColor = new PolyColor(hud.getTextColor(), hud.getTextChroma(), hud.getTextChromaSpeed());
        StringBuilder formatCodes = new StringBuilder();
        if (hud.getTextBold()) {
            formatCodes.append("§l");
        }
        if (hud.getTextItalic()) {
            formatCodes.append("§o");
        }
        if (underlined) {
            formatCodes.append("§n");
        }
        hudFormatCodes = formatCodes.toString();
    }

    // Returns the text settings for the given HUD.
    public static HudTextStyle of(Hud hud) {
        return new HudTextStyle(hud);
    }

    // Returns the HUD's text color.
    public PolyColor textColor() {
        return textColor;
    }

    // Returns Minecraft's font.
    private static Font mcFont() {
        return Minecraft.getInstance().font;
    }

    // Applies the uppercase or lowercase setting.
    private String cased(String text) {
        return switch (caseType) {
            case 1 -> text.toUpperCase(Locale.ROOT);
            case 2 -> text.toLowerCase(Locale.ROOT);
            default -> text;
        };
    }

    // Returns the text with case and bold/italic/underline codes applied.
    private String mcString(String text) {
        String casedText = cased(text);
        return hudFormatCodes.isEmpty() ? casedText : hudFormatCodes + casedText.replace("§r", "§r" + hudFormatCodes);
    }

    // Returns the text with case applied and formatting codes removed.
    private String plain(String text) {
        String stripped = ChatFormatting.stripFormatting(cased(text));
        return stripped == null ? "" : stripped;
    }

    // Returns the Poppins font at the HUD's text scale.
    private PolyFont poppinsFont(float extraScale) {
        return new PolyFont(poppinsFontName, 8f * textScale * extraScale);
    }

    // Returns the extra space the outline adds on the top and left.
    private float outlineInset(float scaledPixel) {
        return outline ? scaledPixel : 0;
    }

    // Returns the extra space a shadow or outline adds on the right.
    private float insetRight(float scaledPixel) {
        if (outline) {
            return scaledPixel;
        }
        if (!dropShadow) {
            return 0;
        }
        return usePoppins ? Math.max(0, shadowOffsetX) : scaledPixel;
    }

    // Returns the extra space a shadow or outline adds at the bottom.
    private float insetBottom(float scaledPixel) {
        if (outline) {
            return scaledPixel;
        }
        if (!dropShadow) {
            return 0;
        }
        return usePoppins ? Math.max(0, shadowOffsetY) : scaledPixel;
    }

    // Returns the text's width without any shadow or outline space.
    private float rawWidth(String text, float extraScale) {
        if (usePoppins) {
            return poppinsFont(extraScale).measureTextWidth(plain(text));
        }
        return mcFont().width(mcString(text)) * textScale * extraScale;
    }

    // Returns the text's height without any shadow or outline space.
    private float rawHeight(float extraScale) {
        if (usePoppins) {
            PolyFont.Metrics metrics = poppinsFont(extraScale).getMetrics();
            return metrics.getDescent() - metrics.getAscent();
        }
        return (mcFont().lineHeight - 1) * textScale * extraScale;
    }

    // Returns the text's full width, including shadow and outline space.
    public float width(String text, float extraScale) {
        float scaledPixel = textScale * extraScale;
        return rawWidth(text, extraScale) + outlineInset(scaledPixel) + insetRight(scaledPixel);
    }

    // Returns the text's full width at normal scale.
    public float width(String text) {
        return width(text, 1f);
    }

    // Returns the text's full height, including shadow and outline space.
    public float height(float extraScale) {
        float scaledPixel = textScale * extraScale;
        return rawHeight(extraScale) + outlineInset(scaledPixel) + insetBottom(scaledPixel);
    }

    // Returns the text's full height at normal scale.
    public float height() {
        return height(1f);
    }

    // Returns the area the text takes up when drawn at the given spot.
    public Bounds bounds(String text, float x, float y, float extraScale) {
        return Bounds.ofSize(x, y, width(text, extraScale), height(extraScale));
    }

    // Draws the text with the HUD's font, scale, shadow and outline settings.
    public void draw(RenderContext renderContext, String text, float x, float y, PolyColor color, float extraScale) {
        if (text.isEmpty()) {
            return;
        }
        float scaledPixel = textScale * extraScale;
        float left = x + outlineInset(scaledPixel);
        float top = y + outlineInset(scaledPixel);
        if (usePoppins) {
            String preparedText = plain(text);
            drawLive(renderContext, color,
                    liveContext -> drawPoppins(liveContext, preparedText, left, top, color, extraScale));
        } else {
            String preparedText = mcString(text);
            drawLive(renderContext, color,
                    liveContext -> drawMc(liveContext, preparedText, left, top, color, scaledPixel));
        }
    }

    // Draws the text at normal scale.
    public void draw(RenderContext renderContext, String text, float x, float y, PolyColor color) {
        draw(renderContext, text, x, y, color, 1f);
    }

    // Runs the drawing every frame when chroma is on so the colors animate.
    private void drawLive(RenderContext renderContext, PolyColor color, Consumer<RenderContext> drawAction) {
        if (color.getChroma() || ((dropShadow || outline) && shadowColor.getChroma())) {
            renderContext.live(liveContext -> {
                drawAction.accept(liveContext);
                return Unit.INSTANCE;
            });
        } else {
            drawAction.accept(renderContext);
        }
    }

    // Draws text in Minecraft's font, with a shadow or outline if set.
    private void drawMc(RenderContext renderContext, String text, float x, float y, PolyColor color,
            float scaledPixel) {
        if (outline) {
            renderContext.mcTextOutline(text, x, y, color.getArgb(), shadowColor.getArgb(), scaledPixel);
        } else {
            renderContext.mcText(text, x, y, color.getArgb(), dropShadow, scaledPixel);
        }
    }

    // Draws text in Poppins, with its shadow or outline and underline.
    private void drawPoppins(RenderContext renderContext, String text, float x, float y, PolyColor fillColor,
            float extraScale) {
        PolyFont font = poppinsFont(extraScale);
        PolyFont.Metrics metrics = font.getMetrics();
        float baseline = y - metrics.getAscent();
        float textWidth = font.measureTextWidth(text);
        float underlineY = baseline + metrics.getUnderlinePosition();
        float underlineThickness = metrics.getUnderlineThickness();
        if (outline) {
            float outlineWidth = textScale * extraScale;
            renderContext.textOutline(text, x, baseline, shadowColor, font, outlineWidth);
            if (underlined) {
                renderContext.line(x - outlineWidth, underlineY, x + textWidth + outlineWidth, underlineY, shadowColor,
                        underlineThickness + outlineWidth * 2f);
            }
        } else if (dropShadow) {
            renderContext.text(text, x + shadowOffsetX, baseline + shadowOffsetY, shadowColor, font);
            if (underlined) {
                renderContext.line(x + shadowOffsetX, underlineY + shadowOffsetY, x + shadowOffsetX + textWidth,
                        underlineY + shadowOffsetY, shadowColor, underlineThickness);
            }
        }
        renderContext.text(text, x, baseline, fillColor, font);
        if (underlined) {
            renderContext.line(x, underlineY, x + textWidth, underlineY, fillColor, underlineThickness);
        }
    }

    // Returns the width of styled chat text, keeping each part's formatting.
    public float width(Component text) {
        if (usePoppins) {
            return width(text.getString());
        }
        float[] totalWidth = { 0 };
        text.visit((style, segment) -> {
            totalWidth[0] += mcFont().width(mcString(formatPrefix(style) + segment)) * textScale;
            return Optional.empty();
        }, Style.EMPTY);
        return totalWidth[0] + outlineInset(textScale) + insetRight(textScale);
    }

    // Draws styled chat text, keeping each part's color and formatting.
    public void draw(RenderContext renderContext, Component text, float x, float y, PolyColor baseColor) {
        if (usePoppins) {
            draw(renderContext, text.getString(), x, y, baseColor);
            return;
        }
        float[] cursorX = { x + outlineInset(textScale) };
        float top = y + outlineInset(textScale);
        text.visit((style, segment) -> {
            if (!segment.isEmpty()) {
                String formatted = mcString(formatPrefix(style) + segment);
                PolyColor color = color(style, baseColor);
                float left = cursorX[0];
                drawLive(renderContext, color,
                        liveContext -> drawMc(liveContext, formatted, left, top, color, textScale));
                cursorX[0] += mcFont().width(formatted) * textScale;
            }
            return Optional.empty();
        }, Style.EMPTY);
    }

    // Returns the part's own color, or the base color if it has none.
    private static PolyColor color(Style style, PolyColor baseColor) {
        TextColor styleColor = style.getColor();
        return styleColor == null ? baseColor
                : new PolyColor(ARGB.color(baseColor.getAlpha(), styleColor.getValue()), false, 1f);
    }

    // Returns the format codes for a part's bold, italic and other styles.
    private static String formatPrefix(Style style) {
        StringBuilder formatCodes = new StringBuilder();
        if (style.isObfuscated()) {
            formatCodes.append("§k");
        }
        if (style.isBold()) {
            formatCodes.append("§l");
        }
        if (style.isStrikethrough()) {
            formatCodes.append("§m");
        }
        if (style.isUnderlined()) {
            formatCodes.append("§n");
        }
        if (style.isItalic()) {
            formatCodes.append("§o");
        }
        return formatCodes.toString();
    }
}
