package tj.recipes;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.wood.BlockGregLog;
import gregtech.common.items.MetaItems;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import static tj.TJRecipeMaps.GREENHOUSE_TREE_RECIPES;
import static gregicadditions.GAMaterials.OrganicFertilizer;

public class GreenhouseRecipes {

    public static void init() {
        for (int i = 0; i < 2; i++) {
            int multiplier = i == 0 ? 1 : 2;
            ItemStack fertilizer = i == 0 ? new ItemStack(Items.DYE, 1, 15) : OreDictUnifier.get(OrePrefix.dust, OrganicFertilizer);
            GREENHOUSE_TREE_RECIPES.recipeBuilder() //Sugar Cane
                    .notConsumable(new ItemStack(Items.REEDS))
                    .inputs(fertilizer)
                    .outputs(new ItemStack(Items.REEDS, 5 * multiplier))
                    .outputs(new ItemStack(Items.REEDS))
                    .outputs(new ItemStack(Items.SUGAR, 10 * multiplier))
                    .duration(900)
                    .buildAndRegister();

            GREENHOUSE_TREE_RECIPES.recipeBuilder() // Spectre Tree
                    .notConsumable(new ItemStack(Item.getByNameOrId("randomthings:spectresapling")))
                    .inputs(fertilizer)
                    .outputs(new ItemStack(Item.getByNameOrId("randomthings:spectrelog"), 5 * multiplier))
                    .outputs(new ItemStack(Item.getByNameOrId("randomthings:spectresapling")))
                    .outputs(new ItemStack(Item.getByNameOrId("randomthings:ingredient"), 10 * multiplier, 2))
                    .duration(900)
                    .buildAndRegister();

            GREENHOUSE_TREE_RECIPES.recipeBuilder() // Spruce Tree
                    .notConsumable(new ItemStack(Blocks.SAPLING, 1, 1))
                    .inputs(fertilizer)
                    .outputs(new ItemStack(Blocks.LOG, 5 * multiplier, 1))
                    .outputs(new ItemStack(Blocks.SAPLING, 1, 1))
                    .duration(600)
                    .buildAndRegister();

            GREENHOUSE_TREE_RECIPES.recipeBuilder() // Oak Tree
                    .notConsumable(new ItemStack(Blocks.SAPLING, 1, 0))
                    .inputs(fertilizer)
                    .outputs(new ItemStack(Blocks.LOG, 5 * multiplier))
                    .outputs(new ItemStack(Blocks.SAPLING, 1))
                    .outputs(new ItemStack(Items.APPLE, 10 * multiplier))
                    .duration(600)
                    .buildAndRegister();

            GREENHOUSE_TREE_RECIPES.recipeBuilder() // Birch Tree
                    .notConsumable(new ItemStack(Blocks.SAPLING, 1, 2))
                    .inputs(fertilizer)
                    .outputs(new ItemStack(Blocks.LOG, 5 * multiplier, 2))
                    .outputs(new ItemStack(Blocks.SAPLING, 1, 2))
                    .duration(600)
                    .buildAndRegister();

            GREENHOUSE_TREE_RECIPES.recipeBuilder() // Jungle Tree
                    .notConsumable(new ItemStack(Blocks.SAPLING, 1, 3))
                    .inputs(fertilizer)
                    .outputs(new ItemStack(Blocks.LOG, 5 * multiplier, 3))
                    .outputs(new ItemStack(Blocks.SAPLING, 1, 3))
                    .outputs(new ItemStack(Blocks.COCOA, 10 * multiplier))
                    .duration(600)
                    .buildAndRegister();

            GREENHOUSE_TREE_RECIPES.recipeBuilder() // Chorus Fruit
                    .notConsumable(new ItemStack(Items.CHORUS_FRUIT))
                    .inputs(fertilizer)
                    .outputs(new ItemStack(Items.CHORUS_FRUIT, 5 * multiplier))
                    .outputs(new ItemStack(Items.CHORUS_FRUIT_POPPED, 10 * multiplier))
                    .outputs(new ItemStack(Items.ENDER_PEARL))
                    .duration(6000)
                    .buildAndRegister();

            GREENHOUSE_TREE_RECIPES.recipeBuilder() // Lightwood Tree
                    .notConsumable(new ItemStack(Block.getBlockFromName("advancedrocketry:aliensapling")))
                    .inputs(fertilizer)
                    .outputs(new ItemStack(Block.getBlockFromName("advancedrocketry:alienwood"), 5 * multiplier))
                    .outputs(new ItemStack(Items.APPLE, (int) (6 * multiplier)))
                    .outputs(new ItemStack(Block.getBlockFromName("advancedrocketry:charcoallog"), 32 * multiplier))
                    .outputs(new ItemStack(Block.getBlockFromName("advancedrocketry:aliensapling")))
                    .duration(600)
                    .buildAndRegister();

            GREENHOUSE_TREE_RECIPES.recipeBuilder() // Acacia Tree
                    .notConsumable(new ItemStack(Blocks.SAPLING, 1, 4))
                    .inputs(fertilizer)
                    .outputs(new ItemStack(Blocks.LOG2, 5 * multiplier))
                    .outputs(new ItemStack(Blocks.SAPLING, 1, 4))
                    .duration(600)
                    .buildAndRegister();

            GREENHOUSE_TREE_RECIPES.recipeBuilder() // Dark Oak Tree
                    .notConsumable(new ItemStack(Blocks.SAPLING, 1, 5))
                    .inputs(fertilizer)
                    .outputs(new ItemStack(Blocks.LOG2, 5 * multiplier, 1))
                    .outputs(new ItemStack(Blocks.SAPLING, 1, 5))
                    .duration(600)
                    .buildAndRegister();

            GREENHOUSE_TREE_RECIPES.recipeBuilder() // Rubber Tree
                    .notConsumable(new ItemStack(MetaBlocks.SAPLING))
                    .inputs(fertilizer)
                    .outputs(new ItemStack(MetaBlocks.LOG.getItem(BlockGregLog.LogVariant.RUBBER_WOOD).getItem(), 5 * multiplier))
                    .outputs(new ItemStack(Items.APPLE, 6 * multiplier))
                    .outputs(MetaItems.RUBBER_DROP.getStackForm(6 * multiplier))
                    .outputs(new ItemStack(MetaBlocks.SAPLING))
                    .duration(600)
                    .buildAndRegister();

            GREENHOUSE_TREE_RECIPES.recipeBuilder() // Canola
                    .notConsumable(new ItemStack(Item.getByNameOrId("actuallyadditions:item_canola_seed")))
                    .inputs(fertilizer)
                    .outputs(new ItemStack(Item.getByNameOrId("actuallyadditions:item_misc"), 5 * multiplier, 13))
                    .outputs(new ItemStack(Item.getByNameOrId("actuallyadditions:item_canola_seed"), 5 * multiplier))
                    .duration(600)
                    .buildAndRegister();
        }
    }
}
