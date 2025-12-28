package tj.recipes.ct;

import net.minecraft.init.Blocks;
import net.minecraftforge.fluids.FluidRegistry;

import static tj.TJRecipeMaps.HEAT_EXCHANGER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.ELECTROLYZER_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;

public class HeatExchangerRecipes {

    public static void init() {
        HEAT_EXCHANGER_RECIPES.recipeBuilder()
                .fluidInputs(Water.getFluid(2880), Lava.getFluid(1000))
                .fluidOutputs(Steam.getFluid(172800), FluidRegistry.getFluidStack("pahoehoe_lava", 1000))
                .duration(20)
                .buildAndRegister();

        ELECTROLYZER_RECIPES.recipeBuilder()
                .fluidInputs(FluidRegistry.getFluidStack("pahoehoe_lava", 10000))
                .output(Blocks.OBSIDIAN)
                .output(dust, Sulfur)
                .output(dust, Carbon)
                .duration(20)
                .EUt(7000)
                .buildAndRegister();
    }
}
