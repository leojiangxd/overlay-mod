package triangled.overlaymod.client.utils;

import java.util.Locale;
import java.util.function.Consumer;
import kotlin.Unit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.polyfrost.compose.render.PolyColor;
import org.polyfrost.compose.render.PolyFont;
import org.polyfrost.compose.render.RenderContext;
import org.polyfrost.oneconfig.api.hud.v1.Hud;

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

    public HudTextStyle(Hud hud) {
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
        hudFormatCodes = (hud.getTextBold() ? "§l" : "") + (hud.getTextItalic() ? "§o" : "") + (underlined ? "§n" : "");
    }

    public PolyColor textColor() {
        return textColor;
    }

    private static Font mcFont() {
        return Minecraft.getInstance().font;
    }

    private String cased(String text) {
        return switch (caseType) {
            case 1 -> text.toUpperCase(Locale.ROOT);
            case 2 -> text.toLowerCase(Locale.ROOT);
            default -> text;
        };
    }

    private String mcString(String text) {
        String casedText = cased(text);
        return hudFormatCodes.isEmpty() ? casedText : hudFormatCodes + casedText.replace("§r", "§r" + hudFormatCodes);
    }

    private String plain(String text) {
        String stripped = ChatFormatting.stripFormatting(cased(text));
        return stripped == null ? "" : stripped;
    }

    private PolyFont poppinsFont(float extraScale) {
        return new PolyFont(poppinsFontName, 8f * textScale * extraScale);
    }

    private float outlineInset(float scaledPixel) {
        return outline ? scaledPixel : 0;
    }

    // Extra space a shadow or outline adds on the right/bottom; Poppins shadows use their own offset.
    private float shadowInset(float scaledPixel, float poppinsOffset) {
        if (outline) {
            return scaledPixel;
        }
        if (!dropShadow) {
            return 0;
        }
        return usePoppins ? Math.max(0, poppinsOffset) : scaledPixel;
    }

    private float rawWidth(String text, float extraScale) {
        if (usePoppins) {
            return poppinsFont(extraScale).measureTextWidth(plain(text));
        }
        return mcFont().width(mcString(text)) * textScale * extraScale;
    }

    private float rawHeight(float extraScale) {
        if (usePoppins) {
            PolyFont.Metrics metrics = poppinsFont(extraScale).getMetrics();
            return metrics.getDescent() - metrics.getAscent();
        }
        return (mcFont().lineHeight - 1) * textScale * extraScale;
    }

    public float width(String text, float extraScale) {
        float scaledPixel = textScale * extraScale;
        return rawWidth(text, extraScale) + outlineInset(scaledPixel) + shadowInset(scaledPixel, shadowOffsetX);
    }

    public float width(String text) {
        return width(text, 1f);
    }

    public float height(float extraScale) {
        float scaledPixel = textScale * extraScale;
        return rawHeight(extraScale) + outlineInset(scaledPixel) + shadowInset(scaledPixel, shadowOffsetY);
    }

    public float height() {
        return height(1f);
    }

    public Bounds bounds(String text, float x, float y, float extraScale) {
        return Bounds.ofSize(x, y, width(text, extraScale), height(extraScale));
    }

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

    private void drawMc(RenderContext renderContext, String text, float x, float y, PolyColor color,
            float scaledPixel) {
        if (outline) {
            renderContext.mcTextOutline(text, x, y, color.getArgb(), shadowColor.getArgb(), scaledPixel);
        } else {
            renderContext.mcText(text, x, y, color.getArgb(), dropShadow, scaledPixel);
        }
    }

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
}
