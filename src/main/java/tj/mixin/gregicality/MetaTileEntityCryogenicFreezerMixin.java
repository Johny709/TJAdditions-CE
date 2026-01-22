package tj.mixin.gregicality;

import gregicadditions.machines.multi.advance.MetaTileEntityCryogenicFreezer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.util.TJFluidUtils;

import java.util.Queue;
import java.util.function.UnaryOperator;

import static tj.machines.multi.electric.MetaTileEntityVoidMOreMiner.CRYOTHEUM;

@Mixin(value = MetaTileEntityCryogenicFreezer.class, remap = false)
public abstract class MetaTileEntityCryogenicFreezerMixin extends MetaTileEntityVacuumFreezerMixin implements IProgressBar {
    public MetaTileEntityCryogenicFreezerMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        bars.add(bar -> bar.setProgress(this::getCryotheumAmount).setMaxProgress(this::getCryotheumCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{CRYOTHEUM.getLocalizedName()})
                .setFluidStackSupplier(() -> CRYOTHEUM));
    }

    @Unique
    private long getCryotheumAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(CRYOTHEUM, this.getInputFluidInventory());
    }

    @Unique
    private long getCryotheumCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(CRYOTHEUM, this.getInputFluidInventory());
    }
}
