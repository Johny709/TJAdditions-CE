package tj.recipes;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;
import java.util.List;

import static tj.TJRecipeMaps.ARCHITECT_RECIPES;

public class ArchitectureRecipes {

    private static int inputQuantity;
    private static int outputQuantity;

    @Deprecated
    public static void init() {
        List<Integer> excludeShapeNumber = Arrays.asList(29, 39, 46, 47, 48, 49, 57, 58, 59, 69);

        for (int i = 0; i < 94; i++) {
            if (excludeShapeNumber.contains(i))
                continue;

            getQuantity(i);
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            nbtTagCompound.setInteger("Shape", i);
            nbtTagCompound.setString("BaseName", Blocks.PLANKS.getRegistryName().toString());
            nbtTagCompound.setInteger("BaseData", new ItemStack(Blocks.PLANKS).getMetadata());
            ItemStack oakCatalyst = new ItemStack(Item.getByNameOrId("architecturecraft:shape"));
            oakCatalyst.setTagCompound(nbtTagCompound);

            for (Block block : Block.REGISTRY) {
                String blockName = block.getRegistryName().toString();

                for (IBlockState state : block.getBlockState().getValidStates()) {

                    NBTTagCompound tagCompound = new NBTTagCompound();
                    tagCompound.setInteger("Shape", i);
                    tagCompound.setString("BaseName", blockName);
                    tagCompound.setInteger("BaseData", state.getBlock().getMetaFromState(state));

                    ItemStack architectStack = new ItemStack(Item.getByNameOrId("architecturecraft:shape"), outputQuantity);
                    architectStack.setTagCompound(tagCompound);
                    ItemStack item = new ItemStack(block, inputQuantity, state.getBlock().getMetaFromState(state));
                    if (item.isEmpty())
                        continue;

                    ARCHITECT_RECIPES.recipeBuilder()
                            .notConsumable(oakCatalyst)
                            .inputs(item)
                            .outputs(architectStack)
                            .EUt(30)
                            .duration(20)
                            .hidden()
                            .buildAndRegister();
                }
            }
        }
    }

    public static void getQuantity(int shape) {
        switch (shape) {
            case 0: inputQuantity = 1; outputQuantity = 2; return;
            case 1: inputQuantity = 1; outputQuantity = 3; return;
            case 2: inputQuantity = 2; outputQuantity = 3; return;
            case 3: inputQuantity = 1; outputQuantity = 4; return;
            case 4: inputQuantity = 1; outputQuantity = 2; return;
            case 5: inputQuantity = 1; outputQuantity = 2; return;
            case 6: inputQuantity = 1; outputQuantity = 1; return;
            case 7: inputQuantity = 1; outputQuantity = 2; return;
            case 8: inputQuantity = 1; outputQuantity = 3; return;
            case 9: inputQuantity = 2; outputQuantity = 3; return;
            case 10: inputQuantity = 1; outputQuantity = 1; return;
            case 11: inputQuantity = 1; outputQuantity = 1; return;
            case 12: inputQuantity = 1; outputQuantity = 1; return;
            case 13: inputQuantity = 1; outputQuantity = 1; return;
            case 14: inputQuantity = 1; outputQuantity = 2; return;
            case 15: inputQuantity = 1; outputQuantity = 1; return;
            case 16: inputQuantity = 1; outputQuantity = 4; return;
            case 17: inputQuantity = 1; outputQuantity = 16; return;
            case 18: inputQuantity = 1; outputQuantity = 3; return;
            case 19: inputQuantity = 1; outputQuantity = 1; return;
            case 20: inputQuantity = 1; outputQuantity = 1; return;
            case 21: inputQuantity = 1; outputQuantity = 1; return;
            case 22: inputQuantity = 1; outputQuantity = 1; return;
            case 23: inputQuantity = 1; outputQuantity = 1; return;
            case 24: inputQuantity = 1; outputQuantity = 1; return;
            case 25: inputQuantity = 1; outputQuantity = 1; return;
            case 26: inputQuantity = 1; outputQuantity = 1; return;
            case 27: inputQuantity = 1; outputQuantity = 1; return;
            case 28: inputQuantity = 1; outputQuantity = 1; return;
            case 30: inputQuantity = 1; outputQuantity = 4; return;
            case 31: inputQuantity = 1; outputQuantity = 2; return;
            case 32: inputQuantity = 1; outputQuantity = 2; return;
            case 33: inputQuantity = 1; outputQuantity = 1; return;
            case 34: inputQuantity = 1; outputQuantity = 2; return;
            case 35: inputQuantity = 1; outputQuantity = 4; return;
            case 36: inputQuantity = 1; outputQuantity = 8; return;
            case 37: inputQuantity = 1; outputQuantity = 1; return;
            case 38: inputQuantity = 1; outputQuantity = 1; return;
            case 40: inputQuantity = 1; outputQuantity = 4; return;
            case 41: inputQuantity = 1; outputQuantity = 4; return;
            case 42: inputQuantity = 1; outputQuantity = 4; return;
            case 43: inputQuantity = 1; outputQuantity = 4; return;
            case 44: inputQuantity = 1; outputQuantity = 4; return;
            case 45: inputQuantity = 1; outputQuantity = 4; return;
            case 50: inputQuantity = 1; outputQuantity = 4; return;
            case 51: inputQuantity = 1; outputQuantity = 4; return;
            case 52: inputQuantity = 1; outputQuantity = 4; return;
            case 53: inputQuantity = 1; outputQuantity = 4; return;
            case 54: inputQuantity = 1; outputQuantity = 4; return;
            case 55: inputQuantity = 1; outputQuantity = 4; return;
            case 56: inputQuantity = 1; outputQuantity = 4; return;
            case 60: inputQuantity = 1; outputQuantity = 16; return;
            case 61: inputQuantity = 1; outputQuantity = 1; return;
            case 62: inputQuantity = 1; outputQuantity = 2; return;
            case 63: inputQuantity = 1; outputQuantity = 2; return;
            case 64: inputQuantity = 1; outputQuantity = 1; return;
            case 65: inputQuantity = 1; outputQuantity = 1; return;
            case 66: inputQuantity = 1; outputQuantity = 2; return;
            case 67: inputQuantity = 1; outputQuantity = 1; return;
            case 68: inputQuantity = 1; outputQuantity = 2; return;
            case 70: inputQuantity = 1; outputQuantity = 10; return;
            case 71: inputQuantity = 1; outputQuantity = 10; return;
            case 72: inputQuantity = 1; outputQuantity = 10; return;
            case 73: inputQuantity = 1; outputQuantity = 5; return;
            case 74: inputQuantity = 1; outputQuantity = 2; return;
            case 75: inputQuantity = 1; outputQuantity = 3; return;
            case 76: inputQuantity = 1; outputQuantity = 4; return;
            case 77: inputQuantity = 1; outputQuantity = 10; return;
            case 78: inputQuantity = 1; outputQuantity = 4; return;
            case 79: inputQuantity = 1; outputQuantity = 6; return;
            case 80: inputQuantity = 1; outputQuantity = 8; return;
            case 81: inputQuantity = 1; outputQuantity = 2; return;
            case 82: inputQuantity = 1; outputQuantity = 8; return;
            case 83: inputQuantity = 1; outputQuantity = 8; return;
            case 84: inputQuantity = 1; outputQuantity = 5; return;
            case 85: inputQuantity = 1; outputQuantity = 5; return;
            case 86: inputQuantity = 1; outputQuantity = 5; return;
            case 87: inputQuantity = 1; outputQuantity = 2; return;
            case 88: inputQuantity = 1; outputQuantity = 6; return;
            case 89: inputQuantity = 1; outputQuantity = 6; return;
            case 90: inputQuantity = 1; outputQuantity = 6; return;
            case 91: inputQuantity = 1; outputQuantity = 6; return;
            case 92: inputQuantity = 1; outputQuantity = 6; return;
            case 93: inputQuantity = 1; outputQuantity = 6;
        }
    }
}
