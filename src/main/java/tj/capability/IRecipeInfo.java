package tj.capability;

import gregtech.api.capability.IWorkable;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public interface IRecipeInfo extends IWorkable {

    boolean isHasProblems();

    long getEnergyPerTick();

    String getHasProblemReason();

    @Nonnull
    default List<ItemStack> getItemInputs() {
        return Collections.emptyList();
    }

    @Nonnull
    default List<FluidStack> getFluidInputs() {
        return Collections.emptyList();
    }

    @Nonnull
    default List<ItemStack> getItemOutputs() {
        return Collections.emptyList();
    }

    @Nonnull
    default List<FluidStack> getFluidOutputs() {
        return Collections.emptyList();
    }
}
