package tj.recipes.ct;

import gregtech.common.items.MetaItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static tj.TJRecipeMaps.COKE_OVEN_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class CokeOvenRecipes {

    public static void init() {
        COKE_OVEN_RECIPES.recipeBuilder()
                .input(log, Wood)
                .output(gem, Charcoal)
                .fluidOutputs(Creosote.getFluid(1000))
                .duration(180)
                .buildAndRegister();

        COKE_OVEN_RECIPES.recipeBuilder()
                .input(gem, Charcoal)
                .output(gem, Coke)
                .fluidOutputs(Creosote.getFluid(1000))
                .duration(360)
                .buildAndRegister();

        COKE_OVEN_RECIPES.recipeBuilder()
                .input(gem, Coal)
                .output(gem, Coke)
                .fluidOutputs(Creosote.getFluid(1800))
                .duration(180)
                .buildAndRegister();

        COKE_OVEN_RECIPES.recipeBuilder()
                .input(gem, Lignite)
                .output(gem, Coke)
                .fluidOutputs(Creosote.getFluid(1800))
                .duration(180)
                .buildAndRegister();

        COKE_OVEN_RECIPES.recipeBuilder()
                .input(dust, Coal)
                .output(dust, Coke)
                .fluidOutputs(Creosote.getFluid(1800))
                .duration(180)
                .buildAndRegister();

        COKE_OVEN_RECIPES.recipeBuilder()
                .input(dust, Lignite)
                .output(dust, Coke)
                .fluidOutputs(Creosote.getFluid(1800))
                .duration(180)
                .buildAndRegister();

        COKE_OVEN_RECIPES.recipeBuilder()
                .inputs(MetaItems.RUBBER_DROP.getStackForm(2))
                .outputs(new ItemStack(Item.getByNameOrId("thermalfoundation:material"), 1, 833))
                .fluidOutputs(Creosote.getFluid(750))
                .duration(180)
                .buildAndRegister();
    }
}
