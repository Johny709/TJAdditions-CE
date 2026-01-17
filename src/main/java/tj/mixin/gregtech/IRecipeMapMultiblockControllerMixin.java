package tj.mixin.gregtech;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RecipeMapMultiblockController.class, remap = false)
public interface IRecipeMapMultiblockControllerMixin {

    @Accessor("recipeMapWorkable")
    MultiblockRecipeLogic getRecipeLogic();
}
