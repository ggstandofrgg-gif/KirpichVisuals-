package dev.kirpichvisuals.screen;

import dev.kirpichvisuals.config.FlyConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Supplier;

/** Экран настроек. Открывается клавишей (по умолчанию Right Shift). */
public class SettingsScreen extends Screen {
    private static final int[] COUNTS = {4, 8, 12, 16, 24, 32};
    private static final int[] DISTANCES = {24, 32, 48, 64, 96, 128};
    private static final int[] FPS_LIMITS = {0, 30, 45, 60, 90};

    private final Screen parent;
    private final FlyConfig cfg = FlyConfig.get();

    public SettingsScreen(Screen parent) {
        super(Text.translatable("kirpichvisuals.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // левая колонка: модули и внешний вид
        option(0, 0, () -> toggle("kirpichvisuals.opt.target", cfg.targetHud), () -> cfg.targetHud = !cfg.targetHud);
        option(0, 1, () -> toggle("kirpichvisuals.opt.potions", cfg.potionHud), () -> cfg.potionHud = !cfg.potionHud);
        option(0, 2, () -> toggle("kirpichvisuals.opt.cooldowns", cfg.cooldownHud), () -> cfg.cooldownHud = !cfg.cooldownHud);
        option(0, 3, () -> value("kirpichvisuals.opt.accent", Text.translatable("kirpichvisuals.accent." + cfg.accent)),
                () -> cfg.accent = (cfg.accent + 1) % FlyConfig.ACCENTS.length);
        option(0, 4, () -> value("kirpichvisuals.opt.scale", Text.literal(Math.round(FlyConfig.SCALES[cfg.scalePreset] * 100) + "%")),
                () -> cfg.scalePreset = (cfg.scalePreset + 1) % FlyConfig.SCALES.length);
        option(0, 5, () -> value("kirpichvisuals.opt.anchor_target", Text.translatable("kirpichvisuals.anchor." + cfg.targetAnchor.name())),
                () -> cfg.targetAnchor = cfg.targetAnchor.next());
        option(0, 6, () -> value("kirpichvisuals.opt.anchor_potions", Text.translatable("kirpichvisuals.anchor." + cfg.potionAnchor.name())),
                () -> cfg.potionAnchor = cfg.potionAnchor.next());
        option(0, 7, () -> value("kirpichvisuals.opt.anchor_cooldowns", Text.translatable("kirpichvisuals.anchor." + cfg.cooldownAnchor.name())),
                () -> cfg.cooldownAnchor = cfg.cooldownAnchor.next());

        // правая колонка: частицы и оптимизация
        option(1, 0, () -> toggle("kirpichvisuals.opt.hits", cfg.hitParticles), () -> cfg.hitParticles = !cfg.hitParticles);
        option(1, 1, () -> value("kirpichvisuals.opt.style", Text.translatable("kirpichvisuals.style." + cfg.particleStyle.name())),
                () -> cfg.particleStyle = cfg.particleStyle.next());
        option(1, 2, () -> value("kirpichvisuals.opt.count", Text.literal(String.valueOf(cfg.particleCount))),
                () -> cfg.particleCount = nextOf(COUNTS, cfg.particleCount));
        option(1, 3, () -> toggle("kirpichvisuals.opt.optimize", cfg.optimizeParticles), () -> cfg.optimizeParticles = !cfg.optimizeParticles);
        option(1, 4, () -> value("kirpichvisuals.opt.distance", Text.literal(cfg.cullDistance + "")),
                () -> cfg.cullDistance = nextOf(DISTANCES, cfg.cullDistance));
        option(1, 5, () -> value("kirpichvisuals.opt.lowfps",
                        cfg.lowFpsThreshold == 0 ? Text.translatable("kirpichvisuals.value.off") : Text.literal(cfg.lowFpsThreshold + " FPS")),
                () -> cfg.lowFpsThreshold = nextOf(FPS_LIMITS, cfg.lowFpsThreshold));

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> close())
                .dimensions(width / 2 - 100, height - 28, 200, 20)
                .build());
    }

    private void option(int column, int row, Supplier<Text> label, Runnable onClick) {
        int x = width / 2 - 154 + column * 158;
        int y = 34 + row * 22;
        addDrawableChild(ButtonWidget.builder(label.get(), button -> {
            onClick.run();
            button.setMessage(label.get());
            cfg.save();
        }).dimensions(x, y, 150, 20).build());
    }

    private static MutableText toggle(String key, boolean on) {
        MutableText state = on
                ? Text.translatable("options.on").formatted(Formatting.GREEN)
                : Text.translatable("options.off").formatted(Formatting.RED);
        return Text.translatable(key).append(Text.literal(": ")).append(state);
    }

    private static MutableText value(String key, Text value) {
        return Text.translatable(key).append(Text.literal(": ")).append(value.copy().formatted(Formatting.AQUA));
    }

    /** Следующее значение по кругу; если текущего нет в списке, берётся первое. */
    private static int nextOf(int[] values, int current) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == current) {
                return values[(i + 1) % values.length];
            }
        }
        return values[0];
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // фон рисует сам Screen.render, повторный вызов renderBackground ломает блюр
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 14, 0xFFFFFFFF);
    }

    @Override
    public void close() {
        cfg.save();
        client.setScreen(parent);
    }
}
