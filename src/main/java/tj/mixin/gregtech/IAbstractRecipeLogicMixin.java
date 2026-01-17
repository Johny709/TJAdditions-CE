package tj.mixin.gregtech;

import gregtech.api.capability.impl.AbstractRecipeLogic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = AbstractRecipeLogic.class, remap = false)
public interface IAbstractRecipeLogicMixin {

    @Accessor("itemOutputs")
    NonNullList<ItemStack> getItemOutputs();

    @Accessor("fluidOutputs")
    List<FluidStack> getFluidOutputs();
}
