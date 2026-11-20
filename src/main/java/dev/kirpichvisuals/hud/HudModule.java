package dev.kirpichvisuals.hud;

import dev.kirpichvisuals.config.Anchor;
import dev.kirpichvisuals.config.FlyConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

/** Общая отрисовка HUD: масштаб, привязка и быстрые проверки состояния. */
public abstract class HudModule {
    protected static final int MARGIN = 6;
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    protected abstract boolean enabled(FlyConfig cfg);
    protected abstract Anchor anchor(FlyConfig cfg);
    protected abstract boolean prepare();
    protected abstract int width();
    protected abstract int height();
    protected abstract void draw(DrawContext ctx, int x, int y, int accentRgb);

    public final void render(DrawContext ctx) {
        FlyConfig cfg = FlyConfig.get();
        if (!enabled(cfg) || mc.player == null || mc.world == null || mc.options.hudHidden) return;
        if (!prepare()) return;

        float scale = FlyConfig.SCALES[cfg.scalePreset];
        int sw = (int) (ctx.getScaledWindowWidth() / scale);
        int sh = (int) (ctx.getScaledWindowHeight() / scale);
        Anchor anchor = anchor(cfg);
        int x = anchor.x(sw, width(), MARGIN);
        int y = anchor.y(sh, height(), MARGIN);

        MatrixStack matrices = ctx.getMatrices();
        matrices.push();
        matrices.scale(scale, scale, 1f);
        draw(ctx, x, y, FlyConfig.ACCENTS[cfg.accent]);
        matrices.pop();
    }
}
