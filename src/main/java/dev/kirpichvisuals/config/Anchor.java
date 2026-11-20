package dev.kirpichvisuals.config;

/** Куда на экране прикрепляется HUD-модуль. */
public enum Anchor {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER;

    /** Координаты без создания временного массива на каждом кадре. */
    public int x(int sw, int w, int margin) {
        return switch (this) {
            case TOP_LEFT, BOTTOM_LEFT -> margin;
            case TOP_RIGHT, BOTTOM_RIGHT -> sw - w - margin;
            case CENTER -> (sw - w) / 2;
        };
    }

    public int y(int sh, int h, int margin) {
        return switch (this) {
            case TOP_LEFT, TOP_RIGHT -> margin;
            case BOTTOM_LEFT -> sh - h - 50;
            case BOTTOM_RIGHT -> sh - h - margin;
            case CENTER -> sh / 2 + 28;
        };
    }

    public int[] place(int sw, int sh, int w, int h, int margin) {
        return new int[]{x(sw, w, margin), y(sh, h, margin)};
    }

    public Anchor next() {
        Anchor[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
