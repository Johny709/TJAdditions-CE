package tj.mixin.gregtech;

import gregtech.api.capability.impl.FuelRecipeLogic;
import gregtech.common.metatileentities.multi.electric.generator.FueledMultiblockController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = FueledMultiblockController.class, remap = false)
public interface IFueledMultiblockControllerMixin {

    @Accessor("workableHandler")
    FuelRecipeLogic getFuelRecipeLogic();
}
