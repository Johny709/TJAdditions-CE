package tj.capability;

import gregtech.api.gui.resources.TextureArea;

import java.util.function.DoubleSupplier;

public class ProgressBar {

    private final DoubleSupplier progress;
    private final DoubleSupplier maxProgress;
    private final TextureArea barTexture;

    public ProgressBar(DoubleSupplier progress, DoubleSupplier maxProgress, TextureArea barTexture) {
        this.progress = progress;
        this.maxProgress = maxProgress;
        this.barTexture = barTexture;
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
}
