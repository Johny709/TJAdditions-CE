package tj.recipes.ct;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

import static tj.TJRecipeMaps.LARGE_VIAL_PROCESSOR_RECIPES;
import static gregtech.api.unification.material.Materials.Milk;

public class LargeVialProcessorRecipes {

    public static void init() {
        Map<String, ItemStack[]> vialRecipeMap = new HashMap<>();
        vialRecipeMap.put("minecraft:enderman", new ItemStack[]{new ItemStack(Item.getByNameOrId("enderio:block_enderman_skull"), 192), new ItemStack(Items.ENDER_PEARL, 192)});
        vialRecipeMap.put("minecraft:zombie", new ItemStack[]{new ItemStack(Items.SKULL, 184, 2), new ItemStack(Items.ROTTEN_FLESH, 160),
                new ItemStack(Items.POTATO, 4), new ItemStack(Items.CARROT, 4), new ItemStack(Items.MELON, 4)});
        vialRecipeMap.put("minecraft:skeleton", new ItemStack[]{new ItemStack(Items.BONE, 128), new ItemStack(Items.SKULL, 24)});
        vialRecipeMap.put("minecraft:blaze", new ItemStack[]{new ItemStack(Items.BLAZE_ROD, 160), new ItemStack(Items.BLAZE_POWDER, 64)});
        vialRecipeMap.put("minecraft:ghast", new ItemStack[]{new ItemStack(Items.GHAST_TEAR, 112), new ItemStack(Item.getByNameOrId("gregtech:meta_item_1"), 48, 2155)});
        vialRecipeMap.put("minecraft:spider", new ItemStack[]{new ItemStack(Items.STRING, 160), new ItemStack(Blocks.WEB, 12), new ItemStack(Items.SPIDER_EYE, 16)});
        vialRecipeMap.put("minecraft:slime", new ItemStack[]{new ItemStack(Items.SLIME_BALL, 128), new ItemStack(Blocks.SLIME_BLOCK, 4)});
        vialRecipeMap.put("minecraft:creeper", new ItemStack[]{new ItemStack(Items.GUNPOWDER, 96), new ItemStack(Item.getByNameOrId("gregtech:meta_item_1"), 32, 2065),
                new ItemStack(Item.getByNameOrId("gregtech:meta_item_1"), 16, 2156), new ItemStack(Item.getByNameOrId("gregtech:meta_item_1"), 16, 2106),
                new ItemStack(Items.SKULL, 24, 4)});
        vialRecipeMap.put("minecraft:magma_cube", new ItemStack[]{new ItemStack(Items.MAGMA_CREAM, 80), new ItemStack(Items.BLAZE_POWDER, 32)});
        vialRecipeMap.put("minecraft:witch", new ItemStack[]{new ItemStack(Items.REDSTONE, 80), new ItemStack(Items.GLOWSTONE_DUST, 48),
                new ItemStack(Items.GUNPOWDER, 32), new ItemStack(Items.SUGAR, 96), new ItemStack(Items.GLASS_BOTTLE, 12),
                new ItemStack(Items.POTIONITEM, 4), new ItemStack(Items.STICK, 32)});
        vialRecipeMap.put("minecraft:wither_skeleton", new ItemStack[]{new ItemStack(Items.BONE, 48), new ItemStack(Items.SKULL, 12, 1), new ItemStack(Item.getByNameOrId("gregtech:meta_item_1"), 32, 2106)});
        vialRecipeMap.put("minecraft:snowman", new ItemStack[]{new ItemStack(Items.SNOWBALL, 64), new ItemStack(Blocks.SNOW, 4), new ItemStack(Blocks.ICE, 8), new ItemStack(Blocks.PUMPKIN, 4)});
        vialRecipeMap.put("minecraft:sheep", new ItemStack[]{new ItemStack(Blocks.WOOL, 128), new ItemStack(Items.STRING, 32), new ItemStack(Items.MUTTON, 128)});
        vialRecipeMap.put("minecraft:pig", new ItemStack[]{new ItemStack(Items.PORKCHOP, 512), new ItemStack(Items.PORKCHOP, 512)});
        vialRecipeMap.put("minecraft:chicken", new ItemStack[]{new ItemStack(Items.EGG, 960), new ItemStack(Items.FEATHER, 400)});
        vialRecipeMap.put("minecraft:cow", new ItemStack[]{new ItemStack(Items.LEATHER, 128), new ItemStack(Items.BEEF, 512)});

        vialRecipeMap.forEach((mob, drops) -> {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setString("entityId", mob);
            ItemStack stack = new ItemStack(Item.getByNameOrId("enderio:item_soul_vial"), 16, 1);
            stack.setTagCompound(compound);
            if (mob.equals("minecraft:cow")) {
                LARGE_VIAL_PROCESSOR_RECIPES.recipeBuilder()
                        .inputs(stack)
                        .outputs(drops)
                        .outputs(new ItemStack(Item.getByNameOrId("enderio:item_soul_vial"), 16), new ItemStack(Item.getByNameOrId("actuallyadditions:item_solidified_experience"), 128))
                        .fluidOutputs(Milk.getFluid(16000))
                        .EUt(30720)
                        .duration(20)
                        .buildAndRegister();
            } else {
                LARGE_VIAL_PROCESSOR_RECIPES.recipeBuilder()
                        .inputs(stack)
                        .outputs(drops)
                        .outputs(new ItemStack(Item.getByNameOrId("enderio:item_soul_vial"), 16), new ItemStack(Item.getByNameOrId("actuallyadditions:item_solidified_experience"), 128))
                        .EUt(30720)
                        .duration(20)
                        .buildAndRegister();
            }
        });
    }
}
