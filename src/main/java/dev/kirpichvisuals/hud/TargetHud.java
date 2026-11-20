package dev.kirpichvisuals.hud;

import dev.kirpichvisuals.config.Anchor;
import dev.kirpichvisuals.config.FlyConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;

/** Компактный Target HUD с плавной полосой здоровья и данными брони. */
public class TargetHud extends HudModule {
    private static final int W = 154;
    private static final int H = 48;
    private static final long KEEP_MS = 2200L;

    private LivingEntity target;
    private long lastSeen;
    private long lastFrame;
    private float shownHealth;

    @Override protected boolean enabled(FlyConfig cfg) { return cfg.targetHud; }
    @Override protected Anchor anchor(FlyConfig cfg) { return cfg.targetAnchor; }
    @Override protected int width() { return W; }
    @Override protected int height() { return H; }

    @Override
    protected boolean prepare() {
        long now = System.currentTimeMillis();
        Entity looking = mc.targetedEntity;
        if (looking instanceof LivingEntity living && living.isAlive() && living != mc.player) {
            if (living != target) {
                target = living;
                shownHealth = living.getHealth();
                lastFrame = now;
            }
            lastSeen = now;
        }
        if (target == null || target.isRemoved() || now - lastSeen > KEEP_MS) {
            target = null;
            return false;
        }

        float dt = lastFrame == 0 ? 0f : Math.min(0.25f, (now - lastFrame) / 1000f);
        lastFrame = now;
        float k = 1f - (float) Math.exp(-12f * dt);
        shownHealth += (target.getHealth() - shownHealth) * k;
        return true;
    }

    @Override
    protected void draw(DrawContext ctx, int x, int y, int accentRgb) {
        LivingEntity t = target;
        float max = Math.max(1f, t.getMaxHealth());
        float ratio = Math.max(0f, Math.min(1f, shownHealth / max));
        HudUtil.panel(ctx, x, y, W, H, accentRgb);

        String health = HudUtil.health(t.getHealth()) + " / " + HudUtil.health(max);
        int hw = mc.textRenderer.getWidth(health);
        String name = mc.textRenderer.trimToWidth(t.getName().getString(), Math.max(32, W - hw - 24));
        ctx.drawText(mc.textRenderer, name, x + 8, y + 5, HudUtil.TEXT, false);
        ctx.drawText(mc.textRenderer, health, x + W - 7 - hw, y + 5, HudUtil.TEXT_DIM, false);

        HudUtil.bar(ctx, x + 8, y + 19, W - 16, 6, ratio, HudUtil.healthColor(ratio));

        int armor = t.getArmor();
        float absorption = t.getAbsorptionAmount();
        String footer = Text.translatable("kirpichvisuals.hud.armor").getString() + " " + armor;
        if (absorption > 0f) footer += "  +" + HudUtil.health(absorption);
        ctx.drawText(mc.textRenderer, footer, x + 8, y + 31, HudUtil.TEXT_DIM, false);
        ctx.drawText(mc.textRenderer, "TARGET", x + W - 43, y + 31, HudUtil.opaque(accentRgb), false);
    }
}
