package tj.mixin.gregicality;

import gregicadditions.machines.multi.advance.MetaTileEntityVolcanus;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.util.TJFluidUtils;

import java.util.Queue;
import java.util.function.UnaryOperator;

import static tj.machines.multi.electric.MetaTileEntityVoidMOreMiner.PYROTHEUM;

@Mixin(value = MetaTileEntityVolcanus.class, remap = false)
public abstract class MetaTileEntityVolcanusMixin extends MetaTileEntityElectricBlastFurnaceMixin implements IProgressBar {
    public MetaTileEntityVolcanusMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        bars.add(bar -> bar.setProgress(this::getPyrotheumAmount).setMaxProgress(this::getPyrotheumCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{PYROTHEUM.getLocalizedName()})
                .setFluidStackSupplier(() -> PYROTHEUM));
    }

    @Unique
    private long getPyrotheumAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(PYROTHEUM, this.getInputFluidInventory());
    }

    @Unique
    private long getPyrotheumCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(PYROTHEUM, this.getInputFluidInventory());
    }
}
