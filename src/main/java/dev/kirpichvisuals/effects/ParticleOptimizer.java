package dev.kirpichvisuals.effects;

import dev.kirpichvisuals.config.FlyConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Решает, нужно ли пропустить ванильную частицу.
 * Отсекает далёкие частицы и прореживает остальные, когда FPS просел.
 */
public final class ParticleOptimizer {
    /** true, пока мы сами создаём частицы удара, их трогать не нужно. */
    public static boolean bypass;

    private static int counter;

    private ParticleOptimizer() {
    }

    public static boolean shouldDrop(double x, double y, double z) {
        FlyConfig cfg = FlyConfig.get();
        if (!cfg.optimizeParticles || bypass) {
            return false;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            return false;
        }

        double max = cfg.cullDistance;
        if (player.squaredDistanceTo(x, y, z) > max * max) {
            return true;
        }

        int level = thinningLevel(cfg, mc.getCurrentFps());
        if (level == 0) {
            return false;
        }
        counter++;
        // уровень 1: пропускаем каждую вторую, уровень 2: три из четырёх
        return level == 1 ? (counter & 1) == 0 : (counter & 3) != 0;
    }

    /** 0 - без прореживания, 1 - мягкое, 2 - сильное. */
    public static int thinningLevel(FlyConfig cfg, int fps) {
        if (cfg.lowFpsThreshold <= 0 || fps <= 0) {
            return 0;
        }
        if (fps < cfg.lowFpsThreshold / 2) {
            return 2;
        }
        return fps < cfg.lowFpsThreshold ? 1 : 0;
    }
}
