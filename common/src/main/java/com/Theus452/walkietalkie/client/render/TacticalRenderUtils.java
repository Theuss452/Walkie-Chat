package com.Theus452.walkietalkie.client.render;

import com.Theus452.walkietalkie.signal.SignalStrength;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;

public final class TacticalRenderUtils {

    private TacticalRenderUtils() {}

    public static final int BEZEL          = 0xFF14181A;
    public static final int BEZEL_EDGE_HI  = 0x28FFFFFF;
    public static final int BEZEL_EDGE_LO  = 0x88000000;
    public static final int SCREEN_BG      = 0xFF0B0E10;
    public static final int BORDER         = 0xFF1E262A;
    public static final int ACTIVE         = 0xFF5EEB8A;
    public static final int LED_GREEN      = 0xFF4ADE80;
    public static final int MUTED          = 0xFF5A6A6E;
    public static final int AMBER          = 0xFFE8B84A;
    public static final int DANGER         = 0xFFFF5C5C;
    public static final int LOCKED         = 0xFF5B9FE8;
    public static final int SURFACE        = 0xFF12171A;
    public static final int SURFACE_HI     = 0xFF1A2226;
    public static final int TEXT_PRIMARY   = 0xFFE8EEF0;
    public static final int TEXT_SECONDARY = 0xFF8A9A9E;

    public static void beginScissor(GuiGraphics g, int x1, int y1, int x2, int y2) {
        g.enableScissor(x1, y1, x2, y2);
    }

    public static void endScissor(GuiGraphics g) {
        g.disableScissor();
    }

    public static void drawChassis(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x + 3, y + 4, x + w + 3, y + h + 4, 0x66000000);
        g.fill(x + 1, y + 2, x + w + 1, y + h + 2, 0x33000000);
        g.fill(x, y, x + w, y + h, withAlpha(ACTIVE, 70));
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF0A0C0E);
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, BEZEL);
        g.fill(x + 2, y + 2, x + w - 2, y + 3, BEZEL_EDGE_HI);
        g.fill(x + 2, y + 2, x + 3, y + h - 2, BEZEL_EDGE_HI);
        g.fill(x + 2, y + h - 3, x + w - 2, y + h - 2, BEZEL_EDGE_LO);
        g.fill(x + w - 3, y + 2, x + w - 2, y + h - 2, BEZEL_EDGE_LO);
        g.fill(x + 3, y + 10, x + 4, y + h - 10, withAlpha(ACTIVE, 160));
        g.fill(x + 12, y + 4, x + w - 12, y + 5, withAlpha(ACTIVE, 70));
    }

    public static void drawScreen(GuiGraphics g, int x, int y, int w, int h, boolean critical) {
        int border = critical ? 0xFF3A2424 : BORDER;
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, border);
        g.fill(x, y, x + w, y + h, SCREEN_BG);
        if (critical) {
            g.fill(x, y, x + w, y + 1, withAlpha(DANGER, 160));
            g.fill(x, y + 1, x + w, y + 2, withAlpha(DANGER, 40));
        }
    }

    public static void drawScanlines(GuiGraphics g, int x, int y, int w, int h) {}

    public static void drawCornerMarks(GuiGraphics g, int x, int y, int w, int h, int color) {
        int c = withAlpha(color, 90);
        g.fill(x, y, x + 4, y + 1, c);
        g.fill(x, y, x + 1, y + 4, c);
        g.fill(x + w - 4, y, x + w, y + 1, c);
        g.fill(x + w - 1, y, x + w, y + 4, c);
        g.fill(x, y + h - 1, x + 4, y + h, c);
        g.fill(x, y + h - 4, x + 1, y + h, c);
        g.fill(x + w - 4, y + h - 1, x + w, y + h, c);
        g.fill(x + w - 1, y + h - 4, x + w, y + h, c);
    }

    public static final long CONNECTED_FLASH_MS = 1800L;

    public static void drawConnectedFeedback(GuiGraphics g, int x1, int y1, int x2, int y2, long flashStartMs) {
        long elapsed = System.currentTimeMillis() - flashStartMs;
        if (elapsed < 0L || elapsed > CONNECTED_FLASH_MS) return;

        float fade = 1.0f - (elapsed / (float) CONNECTED_FLASH_MS);
        int pulsedAlpha = (int) (fade * pulse01(220L, 90, 200));

        g.fill(x1, y1, x2, y2, withAlpha(AMBER, (int) (fade * 35)));
        g.fill(x1, y1, x2, y1 + 1, withAlpha(AMBER, pulsedAlpha));
        g.fill(x1, y2 - 1, x2, y2, withAlpha(AMBER, pulsedAlpha));
        g.fill(x1, y1, x1 + 1, y2, withAlpha(AMBER, pulsedAlpha));
        g.fill(x2 - 1, y1, x2, y2, withAlpha(AMBER, pulsedAlpha));
    }

    public static void drawConnectedLabel(GuiGraphics g, Font font, int cx, int y, long flashStartMs) {
        long elapsed = System.currentTimeMillis() - flashStartMs;
        if (elapsed < 0L || elapsed > CONNECTED_FLASH_MS) return;
        float fade = 1.0f - (elapsed / (float) CONNECTED_FLASH_MS);
        int alpha = (int) (fade * pulse01(220L, 140, 255));
        String text = Component.translatable("gui.walkietalkie.connected").getString();
        g.drawCenteredString(font, text, cx, y, withAlpha(AMBER, alpha));
    }

    public static void drawGlowingText(GuiGraphics g, Font font, String text, int x, int y, int color) {
        g.drawString(font, text, x, y, color, false);
    }

    public static void drawGlowingText(GuiGraphics g, Font font, Component text, int x, int y, int color) {
        drawGlowingText(g, font, text.getString(), x, y, color);
    }

    public static void drawHeaderBlock(GuiGraphics g, Font font, String label, String data,
            int x, int y, int dataColor, boolean alignRight) {
        int labelW = font.width(label);
        int dataW  = font.width(data);
        int lx = alignRight ? x - labelW : x;
        int dx = alignRight ? x - dataW  : x;
        g.drawString(font, label, lx, y, TEXT_SECONDARY, false);
        g.drawString(font, data, dx, y + 9, dataColor, false);
    }

    public static void drawLED(GuiGraphics g, int cx, int cy, int color, boolean lit) {
        g.fill(cx - 3, cy - 3, cx + 3, cy + 3, 0xFF0A0C0E);
        if (lit) {
            g.fill(cx - 2, cy - 2, cx + 2, cy + 2, color);
            g.fill(cx - 1, cy - 2, cx + 1, cy - 1, 0x66FFFFFF);
        } else {
            g.fill(cx - 2, cy - 2, cx + 2, cy + 2, 0xFF1A2022);
        }
    }

    public static void drawLabeledLED(GuiGraphics g, Font font, int cx, int topY, String label,
            int color, boolean lit) {
        drawLED(g, cx, topY, color, lit);
        int w = font.width(label);
        g.drawString(font, label, cx - w / 2, topY + 5, lit ? TEXT_SECONDARY : 0xFF3A4448, false);
    }

    public static void drawSignalBars(GuiGraphics g, int x, int y, int barsLit, int tierColor) {
        for (int i = 0; i < 4; i++) {
            int bh = 3 + i * 2;
            boolean isLit = i < barsLit;
            int color = isLit ? tierColor : 0xFF1E262A;
            g.fill(x + i * 5, y + 9 - bh, x + i * 5 + 3, y + 9, color);
        }
    }

    public static void drawSignalBars(GuiGraphics g, int x, int y, SignalStrength signal) {
        int bars  = signal != null ? signal.barCount() : 0;
        int color = signal != null ? signal.color : MUTED;
        drawSignalBars(g, x, y, bars, color);
    }

    public static int drawTerminalPrompt(GuiGraphics g, Font font, int x, int y, int w, int h,
            String promptLabel, int promptColor) {
        g.fill(x, y, x + w, y + h, SURFACE);
        g.fill(x, y, x + w, y + 1, BORDER);
        g.fill(x, y + h - 1, x + w, y + h, BORDER);
        g.drawString(font, promptLabel, x + 6, y + (h - 8) / 2, promptColor, false);
        return x + 6 + font.width(promptLabel) + 4;
    }

    public static void drawCursor(GuiGraphics g, int x, int y, int color) {
        if (blink(530L)) {
            g.fill(x, y, x + 5, y + 9, color);
        }
    }

    public enum ButtonState { NORMAL, HOVER, ACTIVE, DANGER, LOCKED }

    public static void drawButton(GuiGraphics g, Font font, int x, int y, int w, int h,
            String label, ButtonState state) {
        int accent = switch (state) {
            case ACTIVE  -> ACTIVE;
            case DANGER  -> DANGER;
            case LOCKED  -> LOCKED;
            case HOVER   -> ACTIVE;
            default      -> BORDER;
        };
        int fill = switch (state) {
            case ACTIVE, HOVER -> SURFACE_HI;
            case DANGER -> 0xFF1A1012;
            case LOCKED -> 0xFF10161C;
            default -> SURFACE;
        };
        g.fill(x, y, x + w, y + h, accent);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
        int tint = switch (state) {
            case ACTIVE, HOVER -> withAlpha(ACTIVE, 18);
            case DANGER -> withAlpha(DANGER, 16);
            case LOCKED -> withAlpha(LOCKED, 16);
            default -> 0;
        };
        if (tint != 0) {
            g.fill(x + 1, y + 1, x + w - 1, y + h - 1, tint);
        }
        int textColor = switch (state) {
            case ACTIVE, HOVER -> ACTIVE;
            case DANGER -> DANGER;
            case LOCKED -> LOCKED;
            default -> TEXT_PRIMARY;
        };
        String fitted = fit(font, label == null ? "" : label, Math.max(4, w - 8));
        int textX = x + Math.max(0, (w - font.width(fitted)) / 2);
        int textY = y + Math.max(0, (h - 8) / 2);
        g.drawString(font, fitted, textX, textY, textColor, false);
    }

    public static void drawInfoCard(GuiGraphics g, Font font, int x, int y, int w, int h,
            String title, String value, int valueColor) {
        g.fill(x, y, x + w, y + h, BORDER);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, SURFACE);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, withAlpha(valueColor, 90));
        int maxTextW = w - 12;
        g.drawString(font, fit(font, title, maxTextW), x + 6, y + 5, TEXT_SECONDARY, false);
        g.drawString(font, fit(font, value, maxTextW), x + 6, y + h - 13, valueColor, false);
    }

    public static void drawCorruptionShimmer(GuiGraphics g, int x, int y, int w, int h, float intensity) {
        if (intensity <= 0f) return;
        int alpha = (int) (pulse01(420L, 10, 46) * Mth.clamp(intensity, 0f, 1f));
        g.fill(x, y, x + w, y + h, withAlpha(AMBER, alpha));
    }

    public static void drawStaticBurst(GuiGraphics g, Font font, String burstText, int x, int y, int color) {
        int flicker = blink(140L) ? color : withAlpha(color, 120);
        g.drawString(font, burstText, x, y, flicker, false);
    }

    public static float drawSweepBar(GuiGraphics g, int x, int y, int w, int h, int color) {
        long now  = System.currentTimeMillis();
        float t   = (now % 2200L) / 2200f;
        float tri = t < 0.5f ? t * 2f : 2f - t * 2f;
        int sweepX = x + Math.round(tri * (w - 2));

        g.fill(x, y, x + w, y + h, 0x33000000);
        g.fillGradient(sweepX - 6, y, sweepX, y + h, withAlpha(color, 0), withAlpha(color, 60));
        g.fill(sweepX, y, sweepX + 2, y + h, color);
        g.fillGradient(sweepX + 2, y, sweepX + 8, y + h, withAlpha(color, 60), withAlpha(color, 0));
        return tri;
    }

    public static boolean blink(long periodMs) {
        return (System.currentTimeMillis() / periodMs) % 2 == 0;
    }

    public static int pulse01(long periodMs, int min, int max) {
        double phase = (System.currentTimeMillis() % periodMs) / (double) periodMs;
        double tri   = phase < 0.5 ? phase * 2.0 : 2.0 - phase * 2.0;
        return min + (int) Math.round((max - min) * tri);
    }

    public static int withAlpha(int rgb, int alpha) {
        return (Mth.clamp(alpha, 0, 255) << 24) | (rgb & 0x00FFFFFF);
    }

    public static String fit(Font font, String text, int maxWidth) {
        if (font == null || text == null || text.isEmpty()) return "";
        if (font.width(text) <= maxWidth) return text;
        String ellipsis = "…";
        int targetWidth = Math.max(0, maxWidth - font.width(ellipsis));
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() > 0 && font.width(sb.toString()) > targetWidth) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb + ellipsis;
    }

    public static int wrapHeight(Font font, FormattedText text, int width, int lineHeight) {
        if (font == null || text == null || width <= 0) return lineHeight;
        return Math.max(lineHeight, font.wordWrapHeight(text.getString(), width));
    }

    public static int drawSignalChip(GuiGraphics g, Font font, int x, int y, String label, int color) {
        if (font == null || label == null || label.isEmpty()) return 0;
        int lw    = font.width(label);
        int chipW = lw + 10;
        int chipH = 10;
        g.fill(x, y, x + chipW, y + chipH, withAlpha(color, 30));
        g.fill(x, y, x + chipW, y + 1,     withAlpha(color, 160));
        g.drawString(font, label, x + 5, y + 1, withAlpha(color, 220), false);
        return chipW;
    }

    public static void drawDivider(GuiGraphics g, Font font, int x1, int x2, int y,
            String label, int color) {
        g.fill(x1, y, x2, y + 1, color);
        if (font != null && label != null && !label.isEmpty()) {
            int lw = font.width(label);
            int lx = (x1 + x2) / 2 - lw / 2;
            g.fill(lx - 3, y, lx + lw + 3, y + 1, SCREEN_BG);
            g.drawString(font, label, lx, y - 3, withAlpha(color, 160), false);
        }
    }

    public static int lerpColor(int from, int to, float t) {
        float f  = Mth.clamp(t, 0f, 1f);
        int fa = (from >> 24) & 0xFF, ta = (to >> 24) & 0xFF;
        int fr = (from >> 16) & 0xFF, tr = (to >> 16) & 0xFF;
        int fg = (from >>  8) & 0xFF, tg = (to >>  8) & 0xFF;
        int fb =  from        & 0xFF, tb =  to        & 0xFF;
        int ra = fa + (int)((ta - fa) * f);
        int rr = fr + (int)((tr - fr) * f);
        int rg = fg + (int)((tg - fg) * f);
        int rb = fb + (int)((tb - fb) * f);
        return (Mth.clamp(ra, 0, 255) << 24) | (Mth.clamp(rr, 0, 255) << 16)
             | (Mth.clamp(rg, 0, 255) <<  8) |  Mth.clamp(rb, 0, 255);
    }

    public static float signalQuality01(SignalStrength signal) {
        if (signal == null) return 0.0f;
        return switch (signal) {
            case EXCELLENT -> 1.00f;
            case GOOD      -> 0.78f;
            case MODERATE  -> 0.55f;
            case POOR      -> 0.32f;
            case CRITICAL  -> 0.15f;
            case NO_SIGNAL -> 0.00f;
        };
    }

    public static int signalColor(SignalStrength signal) {
        if (signal == null) return MUTED;
        return switch (signal) {
            case EXCELLENT, GOOD -> ACTIVE;
            case MODERATE        -> AMBER;
            case POOR, CRITICAL  -> 0xFFFF7A32;
            case NO_SIGNAL       -> DANGER;
        };
    }

    public static void drawRadioGlassMood(GuiGraphics g, int x, int y, int w, int h,
            SignalStrength signal, long openSeedMs) {}

    public static void drawSpeakerSlots(GuiGraphics g, int x, int y, int w, int rows, int color) {}

    public static void drawIdleWaveform(GuiGraphics g, int x, int y, int w, int h, int color, long seedMs) {
        if (w <= 4 || h <= 2) return;
        int mid = y + h / 2;
        g.fill(x, mid, x + w, mid + 1, withAlpha(color, 40));
        g.fill(x + w / 3, mid - 2, x + w / 3 + 2, mid + 3, withAlpha(color, 55));
        g.fill(x + 2 * w / 3, mid - 1, x + 2 * w / 3 + 2, mid + 2, withAlpha(color, 45));
    }

    public static void drawPhosphorGrid(GuiGraphics g, int x, int y, int w, int h, int color, float intensity) {}

    public static void drawBootSweep(GuiGraphics g, int x, int y, int w, int h, long openTimeMs, int color) {}

    public static void drawStatusRail(GuiGraphics g, int x, int y, int w, int segments, int activeSegments, int color) {
        int safeSegments = Mth.clamp(segments, 1, 18);
        int gap = 2;
        int segW = Math.max(2, (w - (safeSegments - 1) * gap) / safeSegments);
        for (int i = 0; i < safeSegments; i++) {
            int sx = x + i * (segW + gap);
            boolean lit = i < activeSegments;
            g.fill(sx, y, sx + segW, y + 2, lit ? withAlpha(color, 160) : 0x331E262A);
        }
    }

    public static void drawKnurledKnob(GuiGraphics g, int cx, int cy, int r, int color, boolean active) {}

    public static void drawAntennaMast(GuiGraphics g, int x, int y, int h, int color, boolean live) {}

    public static void drawInsetBay(GuiGraphics g, int x, int y, int w, int h, int color) {
        if (w <= 2 || h <= 2) return;
        g.fill(x, y, x + w, y + h, SURFACE);
        g.fill(x, y, x + 2, y + h, withAlpha(color, 140));
    }

    public static void drawGlassReflections(GuiGraphics g, int x, int y, int w, int h, int color, float intensity) {}

    public static void drawMechanicalFasteners(GuiGraphics g, int x, int y, int w, int h, int color) {}

    public static void drawFrequencyRuler(GuiGraphics g, int x, int y, int w, float normalized, int color) {
        if (w <= 24) return;
        g.fill(x, y + 6, x + w, y + 7, BORDER);
        int needle = x + Math.round((w - 2) * Mth.clamp(normalized, 0.0f, 1.0f));
        g.fill(needle, y + 2, needle + 2, y + 11, withAlpha(color, 180));
    }

    public static void drawDataReadoutMatrix(GuiGraphics g, int x, int y, int w, int h, float quality, int color) {}

    public static void drawTactileLatches(GuiGraphics g, int x, int y, int w, int h, int color, boolean armed) {}

}
