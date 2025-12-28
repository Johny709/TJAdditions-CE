package tj.recipes;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import static tj.TJRecipeMaps.ROCK_BREAKER_RECIPES;
import static gregtech.api.unification.material.Materials.Lava;
import static gregtech.api.unification.material.Materials.Water;

public class RockBreakerRecipes {

    public static ItemStack[] rocks = {new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.STONE, 1, 0), new ItemStack(Blocks.STONE, 1, 1),
            new ItemStack(Blocks.STONE, 1, 3), new ItemStack(Blocks.STONE, 1, 5)};

    public static void init() {

        for (ItemStack rock : rocks) {
            ROCK_BREAKER_RECIPES.recipeBuilder()
                    .notConsumable(rock)
                    .notConsumable(Lava.getFluid(1000))
                    .notConsumable(Water.getFluid(1000))
                    .outputs(rock)
                    .EUt(30)
                    .duration(20)
                    .buildAndRegister();
        }
    }
}
