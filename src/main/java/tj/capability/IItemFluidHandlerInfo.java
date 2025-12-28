package tj.capability;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public interface IItemFluidHandlerInfo {

    default List<ItemStack> getItemInputs() {
        return null;
    }

    default List<FluidStack> getFluidInputs() {
        return null;
    }

    default List<ItemStack> getItemOutputs() {
        return null;
    }

    default List<FluidStack> getFluidOutputs() {
        return null;
    }
}
