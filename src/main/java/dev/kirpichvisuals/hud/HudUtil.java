package dev.kirpichvisuals.hud;

import net.minecraft.client.gui.DrawContext;

/** Общая палитра и примитивы KirpichVisuals. Только дешёвые заливки без blur и шейдеров. */
public final class HudUtil {
    public static final int PANEL_BG = 0xE611141B;
    public static final int PANEL_BORDER = 0xFF303744;
    public static final int BAR_BG = 0xFF252B35;
    public static final int TEXT = 0xFFF2F5FA;
    public static final int TEXT_DIM = 0xFF9AA5B4;
    public static final int TEXT_BAD = 0xFFFF7777;

    private HudUtil() {}

    public static int opaque(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    /** Компактная панель с тонкой рамкой, верхним бликом и акцентной полосой. */
    public static void panel(DrawContext ctx, int x, int y, int w, int h, int accentRgb) {
        ctx.fill(x, y, x + w, y + h, PANEL_BG);
        ctx.fill(x, y, x + w, y + 1, PANEL_BORDER);
        ctx.fill(x, y + h - 1, x + w, y + h, PANEL_BORDER);
        ctx.fill(x, y, x + 1, y + h, PANEL_BORDER);
        ctx.fill(x + w - 1, y, x + w, y + h, PANEL_BORDER);
        ctx.fill(x + 1, y + 1, x + 3, y + h - 1, opaque(accentRgb));
        ctx.fill(x + 4, y + 1, x + w - 1, y + 2, 0x18FFFFFF);
    }

    public static void header(DrawContext ctx, int x, int y, int w, String label,
                              net.minecraft.client.MinecraftClient mc, int accentRgb) {
        ctx.drawText(mc.textRenderer, label, x + 7, y + 4, opaque(accentRgb), false);
        ctx.fill(x + 7, y + 14, x + w - 7, y + 15, 0x553A4352);
    }

    public static void bar(DrawContext ctx, int x, int y, int w, int h, float fraction, int fillColor) {
        ctx.fill(x, y, x + w, y + h, BAR_BG);
        int fw = Math.round(w * Math.max(0f, Math.min(1f, fraction)));
        if (fw > 0) ctx.fill(x, y, x + fw, y + h, fillColor);
    }

    public static int lerpColor(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        int r = Math.round(ar + (br - ar) * t);
        int g = Math.round(ag + (bg - ag) * t);
        int bl = Math.round(ab + (bb - ab) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    public static int healthColor(float ratio) {
        int red = 0xFF5252, yellow = 0xFFFFC04D, green = 0xFF55D99A;
        return ratio < 0.5f ? lerpColor(red, yellow, ratio * 2f)
                : lerpColor(yellow, green, (ratio - 0.5f) * 2f);
    }

    public static String roman(int n) {
        return switch (n) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV";
            case 5 -> "V"; case 6 -> "VI"; case 7 -> "VII"; case 8 -> "VIII";
            case 9 -> "IX"; case 10 -> "X"; default -> String.valueOf(n);
        };
    }

    public static String time(int ticks) {
        int s = Math.max(0, ticks) / 20;
        return String.format(java.util.Locale.ROOT, "%d:%02d", s / 60, s % 60);
    }

    public static String health(float value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }
}
