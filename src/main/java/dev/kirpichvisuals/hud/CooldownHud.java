package dev.kirpichvisuals.hud;

import dev.kirpichvisuals.config.Anchor;
import dev.kirpichvisuals.config.FlyConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.Arrays;

/** Кулдауны предметов и заряд атаки. Хранит данные в массивах без объектов Entry на кадр. */
public class CooldownHud extends HudModule {
    private static final int W = 128;
    private static final int ROW = 19;
    private static final int HEADER = 17;
    private static final ItemStack[] TRACKED = {
            new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.GOLDEN_APPLE),
            new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), new ItemStack(Items.CHORUS_FRUIT),
            new ItemStack(Items.SHIELD), new ItemStack(Items.WIND_CHARGE),
            new ItemStack(Items.TRIDENT)
    };
    private static final ItemStack ATTACK_ICON = new ItemStack(Items.IRON_SWORD);

    private final ItemStack[] icons = new ItemStack[TRACKED.length + 1];
    private final float[] fills = new float[TRACKED.length + 1];
    private final boolean[] attack = new boolean[TRACKED.length + 1];
    private int count;

    @Override protected boolean enabled(FlyConfig cfg) { return cfg.cooldownHud; }
    @Override protected Anchor anchor(FlyConfig cfg) { return cfg.cooldownAnchor; }
    @Override protected int width() { return W; }
    @Override protected int height() { return count * ROW + HEADER + 4; }

    @Override
    protected boolean prepare() {
        count = 0;
        float charge = mc.player.getAttackCooldownProgress(0f);
        if (charge < 0.999f) add(ATTACK_ICON, charge, true);

        for (ItemStack stack : TRACKED) {
            float remaining = mc.player.getItemCooldownManager().getCooldownProgress(stack, 0f);
            if (remaining > 0f) add(stack, remaining, false);
        }
        return count > 0;
    }

    private void add(ItemStack icon, float fill, boolean isAttack) {
        icons[count] = icon;
        fills[count] = fill;
        attack[count] = isAttack;
        count++;
    }

    @Override
    protected void draw(DrawContext ctx, int x, int y, int accentRgb) {
        HudUtil.panel(ctx, x, y, W, height(), accentRgb);
        HudUtil.header(ctx, x, y, W, Text.translatable("kirpichvisuals.hud.cooldowns").getString(), mc, accentRgb);

        for (int i = 0; i < count; i++) {
            int ry = y + HEADER + i * ROW;
            ctx.drawItem(icons[i], x + 7, ry);
            int color = attack[i] ? HudUtil.opaque(accentRgb)
                    : HudUtil.lerpColor(0xFFFF6464, 0xFFFFC04D, 1f - fills[i]);
            HudUtil.bar(ctx, x + 29, ry + 6, 56, 5, fills[i], color);
            String pct = Math.round(fills[i] * 100f) + "%";
            ctx.drawText(mc.textRenderer, pct, x + 91, ry + 5, HudUtil.TEXT_DIM, false);
        }
    }
}
