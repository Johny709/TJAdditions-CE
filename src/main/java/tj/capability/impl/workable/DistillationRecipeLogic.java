package tj.capability.impl.workable;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import tj.capability.impl.handler.IDistillationHandler;
import tj.util.ItemStackHelper;

import static tj.util.TJFluidUtils.VOID_TANK;

public class DistillationRecipeLogic extends BasicRecipeLogic<IDistillationHandler> {

    public DistillationRecipeLogic(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected boolean completeRecipe() {
        for (int i = this.itemOutputIndex; i < this.itemOutputs.size(); i++) {
            ItemStack stack = this.itemOutputs.get(i);
            if (this.voidingItems || ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, true).isEmpty()) {
                ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, false);
                this.itemOutputIndex++;
            } else return false;
        }
        for (int i = this.fluidOutputIndex; i < this.fluidOutputs.size(); i++) {
            FluidStack stack = this.fluidOutputs.get(i);
            IMultipleTankHandler fluidTank = this.handler.getOutputHatchAt(i);
            if (fluidTank == null)
                fluidTank = VOID_TANK;
            if (this.voidingFluids || fluidTank.fill(stack, false) == stack.amount) {
                fluidTank.fill(stack, true);
                this.fluidOutputIndex++;
            } else return false;
        }
        this.recipeRecheck = true;
        this.itemOutputIndex = 0;
        this.fluidOutputIndex = 0;
        this.itemInputs.clear();
        this.itemOutputs.clear();
        this.fluidInputs.clear();
        this.fluidOutputs.clear();
        return true;
    }
}
