package tj.capability;

import gregtech.api.gui.resources.TextureArea;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class ProgressBar {

    private final DoubleSupplier progress;
    private final DoubleSupplier maxProgress;
    private final TextureArea barTexture;
    private final String locale;
    private final Supplier<Object[]> params;

    public ProgressBar(DoubleSupplier progress, DoubleSupplier maxProgress, TextureArea barTexture, String locale) {
        this(progress, maxProgress, barTexture, locale, null);
    }

    public ProgressBar(DoubleSupplier progress, DoubleSupplier maxProgress, TextureArea barTexture, String locale, Supplier<Object[]> params) {
        this.progress = progress;
        this.maxProgress = maxProgress;
        this.barTexture = barTexture;
        this.locale = locale;
        this.params = params;
    }

    public DoubleSupplier getProgress() {
        return this.progress;
    }

    public DoubleSupplier getMaxProgress() {
        return this.maxProgress;
    }

    public TextureArea getBarTexture() {
        return this.barTexture;
    }

    public String getLocale() {
        return this.locale;
    }

    public Supplier<Object[]> getParams() {
        return this.params;
    }
}
