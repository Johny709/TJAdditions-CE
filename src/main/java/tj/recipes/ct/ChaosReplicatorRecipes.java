package tj.recipes.ct;

import gregicadditions.item.GAMetaItems;
import gregtech.common.items.MetaItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static tj.TJRecipeMaps.CHAOS_REPLICATOR_RECIPES;
import static gregtech.api.unification.material.Materials.UUMatter;

public class ChaosReplicatorRecipes {

    public static void init() {
        CHAOS_REPLICATOR_RECIPES.recipeBuilder()
                .inputs(GAMetaItems.UNSTABLE_STAR.getStackForm(16), MetaItems.QUANTUM_STAR.getStackForm(16),
                        new ItemStack(Item.getByNameOrId("contenttweaker:refinedchaoscrystal")), MetaItems.GRAVI_STAR.getStackForm(16))
                .outputs(new ItemStack(Item.getByNameOrId("contenttweaker:refinedchaosshard"), 64),
                        new ItemStack(Item.getByNameOrId("contenttweaker:refinedchaosshard"), 32))
                .fluidInputs(UUMatter.getFluid(32000))
                .EUt(524288)
                .duration(500)
                .buildAndRegister();
    }
}
