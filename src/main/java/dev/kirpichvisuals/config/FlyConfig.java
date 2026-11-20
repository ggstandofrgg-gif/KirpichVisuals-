package dev.kirpichvisuals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.kirpichvisuals.KirpichVisualsClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Настройки мода. Хранятся в config/kirpichvisuals.json. */
public class FlyConfig {
    /** RGB-цвета акцента: лёд, багровый, изумруд, фиолетовый, золото. */
    public static final int[] ACCENTS = {0x4FC3F7, 0xFF5252, 0x69F0AE, 0xB388FF, 0xFFD740};
    public static final float[] SCALES = {0.8f, 1.0f, 1.25f, 1.5f};

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("kirpichvisuals.json");
    private static FlyConfig instance = load();

    // модули
    public boolean targetHud = true;
    public boolean potionHud = true;
    public boolean cooldownHud = true;
    public boolean hitParticles = true;
    public boolean optimizeParticles = true;

    // внешний вид
    public int accent = 0;
    public int scalePreset = 1;
    public Anchor targetAnchor = Anchor.CENTER;
    public Anchor potionAnchor = Anchor.TOP_LEFT;
    public Anchor cooldownAnchor = Anchor.BOTTOM_RIGHT;

    // частицы
    public ParticleStyle particleStyle = ParticleStyle.CRIT;
    public int particleCount = 8;

    // оптимизация
    public int cullDistance = 32;
    public int lowFpsThreshold = 60;

    public static FlyConfig get() {
        return instance;
    }

    private static FlyConfig load() {
        try {
            if (Files.exists(PATH)) {
                try (Reader reader = Files.newBufferedReader(PATH)) {
                    FlyConfig loaded = GSON.fromJson(reader, FlyConfig.class);
                    if (loaded != null) {
                        loaded.sanitize();
                        return loaded;
                    }
                }
            }
        } catch (Exception e) {
            KirpichVisualsClient.LOGGER.warn("Не удалось прочитать конфиг, использую значения по умолчанию", e);
        }
        FlyConfig fresh = new FlyConfig();
        fresh.save();
        return fresh;
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(PATH)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            KirpichVisualsClient.LOGGER.warn("Не удалось сохранить конфиг", e);
        }
    }

    /** Приводит значения из файла к допустимым, чтобы ручная правка JSON ничего не ломала. */
    public void sanitize() {
        accent = Math.floorMod(accent, ACCENTS.length);
        scalePreset = Math.max(0, Math.min(SCALES.length - 1, scalePreset));
        particleCount = Math.max(2, Math.min(32, particleCount));
        cullDistance = Math.max(8, Math.min(128, cullDistance));
        lowFpsThreshold = Math.max(0, Math.min(240, lowFpsThreshold));
        if (targetAnchor == null) targetAnchor = Anchor.CENTER;
        if (potionAnchor == null) potionAnchor = Anchor.TOP_LEFT;
        if (cooldownAnchor == null) cooldownAnchor = Anchor.BOTTOM_RIGHT;
        if (particleStyle == null) particleStyle = ParticleStyle.CRIT;
    }
}
