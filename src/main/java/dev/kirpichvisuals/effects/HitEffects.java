package dev.kirpichvisuals.effects;

import dev.kirpichvisuals.config.FlyConfig;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/** Дополнительные частицы при ударе. Чисто визуально, на урон и логику не влияет. */
public final class HitEffects {
    private HitEffects() {
    }

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // событие общее для клиента и встроенного сервера, нужна только клиентская сторона
            MinecraftClient mc = MinecraftClient.getInstance();
            if (world.isClient() && player == mc.player && entity instanceof LivingEntity) {
                spawn(world, entity, mc);
            }
            return ActionResult.PASS;
        });
    }

    private static void spawn(World world, Entity target, MinecraftClient mc) {
        FlyConfig cfg = FlyConfig.get();
        if (!cfg.hitParticles || mc.player == null) {
            return;
        }
        // слабые удары без заряда не украшаем, иначе спам кликами засыпает экран
        if (mc.player.getAttackCooldownProgress(0.5f) < 0.5f) {
            return;
        }

        int count = cfg.particleCount;
        if (cfg.optimizeParticles) {
            int level = ParticleOptimizer.thinningLevel(cfg, mc.getCurrentFps());
            count = level == 0 ? count : (level == 1 ? count / 2 : count / 4);
        }
        count = Math.max(1, count);

        ParticleEffect effect = cfg.particleStyle.effect();
        Box box = target.getBoundingBox();
        double cx = (box.minX + box.maxX) / 2.0;
        double cy = box.minY + box.getLengthY() * 0.6;
        double cz = (box.minZ + box.maxZ) / 2.0;
        Random random = world.getRandom();

        ParticleOptimizer.bypass = true;
        try {
            for (int i = 0; i < count; i++) {
                double ox = (random.nextDouble() - 0.5) * box.getLengthX();
                double oy = (random.nextDouble() - 0.5) * box.getLengthY() * 0.6;
                double oz = (random.nextDouble() - 0.5) * box.getLengthZ();
                double vx = (random.nextDouble() - 0.5) * 0.5;
                double vy = random.nextDouble() * 0.4;
                double vz = (random.nextDouble() - 0.5) * 0.5;
                world.addParticle(effect, cx + ox, cy + oy, cz + oz, vx, vy, vz);
            }
        } finally {
            ParticleOptimizer.bypass = false;
        }
    }
}
