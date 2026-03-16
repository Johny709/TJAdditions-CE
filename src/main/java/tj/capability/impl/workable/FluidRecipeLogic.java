package tj.capability.impl.workable;

import gregtech.api.metatileentity.MetaTileEntity;
import tj.capability.impl.handler.IFluidSupplyHandler;

public class FluidRecipeLogic extends ParallelRecipeLogic<IFluidSupplyHandler> {
    public FluidRecipeLogic(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected void progressRecipe(int progress, int i) {
        if (this.handler.getFluidStack().isFluidStackIdentical(this.handler.getImportFluidTank().drain(this.handler.getFluidStack(), true))) {
            super.progressRecipe(progress, i);
        } else if (this.progress[i] > 1)
            this.progress[i]--;
    }
}
