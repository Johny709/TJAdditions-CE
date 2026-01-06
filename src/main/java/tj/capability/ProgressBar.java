package tj.capability;

import gregtech.api.gui.resources.TextureArea;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public final class ProgressBar {

    private final DoubleSupplier progress;
    private final DoubleSupplier maxProgress;
    private final Supplier<Object[]> params;
    private final Supplier<FluidStack> fluidStackSupplier;
    private final TextureArea barTexture;
    private final String locale;
    private final boolean isFluid;

    public ProgressBar(DoubleSupplier progress, DoubleSupplier maxProgress, TextureArea barTexture, String locale, Supplier<Object[]> params, Supplier<FluidStack> fluidStackSupplier, boolean isFluid) {
        this.progress = progress;
        this.maxProgress = maxProgress;
        this.barTexture = barTexture;
        this.locale = locale;
        this.params = params;
        this.fluidStackSupplier = fluidStackSupplier;
        this.isFluid = isFluid;
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

    public Supplier<FluidStack> getFluidStackSupplier() {
        return this.fluidStackSupplier;
    }

    public boolean isFluid() {
        return this.isFluid;
    }

    public static class ProgressBarBuilder {
        private DoubleSupplier progress;
        private DoubleSupplier maxProgress;
        private Supplier<Object[]> params;
        private Supplier<FluidStack> fluidStackSupplier;
        private TextureArea barTexture;
        private String locale;
        private boolean isFluid;

        public ProgressBarBuilder setProgress(DoubleSupplier progress) {
            this.progress = progress;
            return this;
        }

        public ProgressBarBuilder setMaxProgress(DoubleSupplier maxProgress) {
            this.maxProgress = maxProgress;
            return this;
        }

        public ProgressBarBuilder setBarTexture(TextureArea barTexture) {
            this.barTexture = barTexture;
            this.isFluid = false;
            return this;
        }

        public ProgressBarBuilder setLocale(String locale) {
            this.locale = locale;
            return this;
        }

        public ProgressBarBuilder setParams(Supplier<Object[]> params) {
            this.params = params;
            return this;
        }

        public ProgressBarBuilder setFluidStackSupplier(Supplier<FluidStack> fluidStackSupplier) {
            this.fluidStackSupplier = fluidStackSupplier;
            this.isFluid = true;
            return this;
        }

        public ProgressBar build() {
            return new ProgressBar(this.progress, this.maxProgress, this.barTexture, this.locale, this.params, this.fluidStackSupplier, this.isFluid);
        }
    }
}
