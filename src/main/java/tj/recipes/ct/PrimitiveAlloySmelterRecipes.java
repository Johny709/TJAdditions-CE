package tj.recipes.ct;

import gregicadditions.item.GAMetaItems;
import gregtech.common.items.MetaItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static gregicadditions.GAMaterials.UraniumRadioactive;
import static tj.TJRecipeMaps.PRIMITIVE_ALLOY_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class PrimitiveAlloySmelterRecipes {

    public static void init() {
        PRIMITIVE_ALLOY_RECIPES.recipeBuilder()
                .input(ingot, Copper, 3)
                .input(ingot, Tin)
                .output(ingot, Bronze, 4)
                .duration(600)
                .buildAndRegister();

        PRIMITIVE_ALLOY_RECIPES.recipeBuilder()
                .input(ingot, Tin, 9)
                .input(ingot, Antimony)
                .output(ingot, SolderingAlloy, 10)
                .duration(600)
                .buildAndRegister();

        PRIMITIVE_ALLOY_RECIPES.recipeBuilder()
                .input(dust, Redstone, 4)
                .input(ingot, Copper)
                .output(ingot, RedAlloy)
                .duration(600)
                .buildAndRegister();

        PRIMITIVE_ALLOY_RECIPES.recipeBuilder()
                .inputs(GAMetaItems.HOT_IRON_INGOT.getStackForm(4))
                .output(ingot, WroughtIron, 4)
                .duration(150)
                .buildAndRegister();

        PRIMITIVE_ALLOY_RECIPES.recipeBuilder()
                .input(dust, Glass, 4)
                .notConsumable(MetaItems.SHAPE_MOLD_BALL)
                .outputs(MetaItems.GLASS_TUBE.getStackForm(4))
                .duration(150)
                .buildAndRegister();

        PRIMITIVE_ALLOY_RECIPES.recipeBuilder()
                .input(ingot, Iron)
                .input(dust, UraniumRadioactive.getMaterial())
                .outputs(new ItemStack(Item.getByNameOrId("enderio:item_alloy_ingot"), 1, 5))
                .duration(160)
                .buildAndRegister();

        PRIMITIVE_ALLOY_RECIPES.recipeBuilder()
                .input(gem, NetherQuartz, 4)
                .outputs(new ItemStack(Item.getByNameOrId("enderio:block_fused_quartz")))
                .duration(160)
                .buildAndRegister();

        PRIMITIVE_ALLOY_RECIPES.recipeBuilder()
                .input(ingot, Iron)
                .input(ingot, UraniumRadioactive.getMaterial())
                .outputs(new ItemStack(Item.getByNameOrId("enderio:item_alloy_ingot"), 1, 5))
                .duration(160)
                .buildAndRegister();

        PRIMITIVE_ALLOY_RECIPES.recipeBuilder()
                .input(dust, Iron)
                .input(dust, UraniumRadioactive.getMaterial())
                .outputs(new ItemStack(Item.getByNameOrId("enderio:item_alloy_ingot"), 1, 5))
                .duration(160)
                .buildAndRegister();

        PRIMITIVE_ALLOY_RECIPES.recipeBuilder()
                .input(ingot, Iron)
                .input(dust, UraniumRadioactive.getMaterial())
                .outputs(new ItemStack(Item.getByNameOrId("enderio:item_alloy_ingot"), 1, 5))
                .duration(160)
                .buildAndRegister();

        PRIMITIVE_ALLOY_RECIPES.recipeBuilder()
                .input(ingot, Iron)
                .input(dust, UraniumRadioactive.getMaterial())
                .outputs(new ItemStack(Item.getByNameOrId("enderio:item_alloy_ingot"), 1, 5))
                .duration(160)
                .buildAndRegister();

        PRIMITIVE_ALLOY_RECIPES.recipeBuilder()
                .input(ingot, Iron, 2)
                .input(ingot, Nickel)
                .output(ingot, Invar, 3)
                .duration(160)
                .buildAndRegister();

        PRIMITIVE_ALLOY_RECIPES.recipeBuilder()
                .input(dust, Iron, 2)
                .input(ingot, Nickel)
                .output(ingot, Invar, 3)
                .duration(160)
                .buildAndRegister();

        PRIMITIVE_ALLOY_RECIPES.recipeBuilder()
                .input(ingot, Iron, 2)
                .input(dust, Nickel)
                .output(ingot, Invar, 3)
                .duration(160)
                .buildAndRegister();

        PRIMITIVE_ALLOY_RECIPES.recipeBuilder()
                .input(dust, Iron, 2)
                .input(dust, Nickel)
                .output(ingot, Invar, 3)
                .duration(160)
                .buildAndRegister();
    }
}
