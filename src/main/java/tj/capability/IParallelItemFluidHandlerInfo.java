package tj.capability;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public interface IParallelItemFluidHandlerInfo {

    default Int2ObjectMap<List<ItemStack>> getAllItemInputs() {
        return null;
    }

    default Int2ObjectMap<List<ItemStack>> getAllItemOutputs() {
        return null;
    }

    default Int2ObjectMap<List<FluidStack>> getAllFluidInputs() {
        return null;
    }

    default Int2ObjectMap<List<FluidStack>> getAllFluidOutputs() {
        return null;
    }
}
