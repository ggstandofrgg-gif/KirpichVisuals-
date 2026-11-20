package dev.kirpichvisuals.hud;

import dev.kirpichvisuals.config.Anchor;
import dev.kirpichvisuals.config.FlyConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Таймеры эффектов. Снимок списка обновляется не чаще 10 раз в секунду. */
public class PotionHud extends HudModule {
    private static final int W = 138;
    private static final int ROW = 18;
    private static final int HEADER = 17;
    private static final int MAX_ROWS = 6;
    private static final long REFRESH_NS = 100_000_000L;
    private static final Comparator<StatusEffectInstance> ORDER =
            Comparator.comparingInt(e -> e.isInfinite() ? Integer.MAX_VALUE : e.getDuration());

    private final List<StatusEffectInstance> effects = new ArrayList<>(MAX_ROWS);
    private final Map<RegistryEntry<StatusEffect>, Integer> maxDuration = new HashMap<>();
    private final Set<RegistryEntry<StatusEffect>> present = new HashSet<>();
    private long lastRefresh;
    private boolean cachedVisible;

    @Override protected boolean enabled(FlyConfig cfg) { return cfg.potionHud; }
    @Override protected Anchor anchor(FlyConfig cfg) { return cfg.potionAnchor; }
    @Override protected int width() { return W; }
    @Override protected int height() { return effects.size() * ROW + HEADER + 4; }

    @Override
    protected boolean prepare() {
        long now = System.nanoTime();
        if (now - lastRefresh < REFRESH_NS) return cachedVisible;
        lastRefresh = now;

        effects.clear();
        present.clear();
        for (StatusEffectInstance inst : mc.player.getStatusEffects()) {
            effects.add(inst);
            RegistryEntry<StatusEffect> key = inst.getEffectType();
            present.add(key);
            maxDuration.merge(key, inst.isInfinite() ? 1 : inst.getDuration(), Math::max);
        }

        for (Iterator<RegistryEntry<StatusEffect>> it = maxDuration.keySet().iterator(); it.hasNext();) {
            if (!present.contains(it.next())) it.remove();
        }

        effects.sort(ORDER);
        while (effects.size() > MAX_ROWS) effects.remove(effects.size() - 1);
        cachedVisible = !effects.isEmpty();
        return cachedVisible;
    }

    @Override
    protected void draw(DrawContext ctx, int x, int y, int accentRgb) {
        HudUtil.panel(ctx, x, y, W, height(), accentRgb);
        HudUtil.header(ctx, x, y, W, Text.translatable("kirpichvisuals.hud.effects").getString(), mc, accentRgb);

        int i = 0;
        for (StatusEffectInstance inst : effects) {
            RegistryEntry<StatusEffect> entry = inst.getEffectType();
            StatusEffect effect = entry.value();
            int ry = y + HEADER + i * ROW;
            int color = HudUtil.opaque(effect.getColor());
            ctx.fill(x + 8, ry + 3, x + 14, ry + 9, color);

            String name = effect.getName().getString();
            if (inst.getAmplifier() > 0) name += " " + HudUtil.roman(inst.getAmplifier() + 1);
            name = mc.textRenderer.trimToWidth(name, W - 65);
            ctx.drawText(mc.textRenderer, name, x + 18, ry + 2,
                    effect.isBeneficial() ? HudUtil.TEXT : HudUtil.TEXT_BAD, false);

            String time = inst.isInfinite() ? "∞" : HudUtil.time(inst.getDuration());
            int tw = mc.textRenderer.getWidth(time);
            int timeColor = !inst.isInfinite() && inst.getDuration() <= 100 ? HudUtil.TEXT_BAD : HudUtil.TEXT_DIM;
            ctx.drawText(mc.textRenderer, time, x + W - 8 - tw, ry + 2, timeColor, false);

            float fraction = 1f;
            if (!inst.isInfinite()) {
                int max = Math.max(1, maxDuration.getOrDefault(entry, inst.getDuration()));
                fraction = inst.getDuration() / (float) max;
            }
            HudUtil.bar(ctx, x + 8, ry + 13, W - 16, 2, fraction, color);
            i++;
        }
    }
}
