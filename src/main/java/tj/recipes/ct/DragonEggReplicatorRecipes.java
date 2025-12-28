package tj.recipes.ct;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import static tj.TJRecipeMaps.DRAGON_REPLICATOR_RECIPES;
import static gregicadditions.GAMaterials.DepletedGrowthMedium;
import static gregicadditions.GAMaterials.SterileGrowthMedium;

public class DragonEggReplicatorRecipes {

    public static void init() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("Energy", 4000000);
        ItemStack stack = new ItemStack(Item.getByNameOrId("draconicevolution:wyvern_sword"), 1);
        stack.setTagCompound(compound);
        DRAGON_REPLICATOR_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Items.EGG, 16), stack)
                .outputs(new ItemStack(Item.getByNameOrId("draconicevolution:dragon_heart"), 2), new ItemStack(Item.getByNameOrId("draconicevolution:wyvern_sword")),
                        new ItemStack(Blocks.DRAGON_EGG))
                .fluidInputs(SterileGrowthMedium.getFluid(2000))
                .fluidOutputs(DepletedGrowthMedium.getFluid(2000))
                .EUt(131072)
                .duration(500)
                .buildAndRegister();
    }
}
