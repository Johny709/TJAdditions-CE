package tj.recipes.ct;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import static tj.TJRecipeMaps.LARGE_POWERED_SPAWNER_RECIPES;

public class LargePoweredSpawnerRecipes {

    public static void init() {
        String[] entity = {"minecraft:enderman", "minecraft:zombie", "minecraft:skeleton", "minecraft:blaze", "minecraft:ghast", "minecraft:spider", "minecraft:slime", "minecraft:creeper",
                "minecraft:magma_cube", "minecraft:witch", "minecraft:wither_skeleton", "minecraft:snowman", "minecraft:sheep", "minecraft:pig", "minecraft:chicken", "minecraft:cow"};

        for (int i = 0; i < 32; i++) {
            NBTTagCompound compound = new NBTTagCompound();
            if (i < 16) {
                compound.setString("entityId", entity[i]);
                ItemStack stack = new ItemStack(Item.getByNameOrId("enderio:item_soul_vial"), 16, 1);
                stack.setTagCompound(compound);
                LARGE_POWERED_SPAWNER_RECIPES.recipeBuilder()
                        .inputs(new ItemStack(Item.getByNameOrId("enderio:item_soul_vial"), 16))
                        .notConsumable(IntCircuitIngredient.getIntegratedCircuit(i))
                        .outputs(stack)
                        .EUt(30720)
                        .duration(20)
                        .buildAndRegister();
            } else {
                compound.setString("entityId", entity[i - 16]);
                ItemStack stack = new ItemStack(Item.getByNameOrId("enderio:item_soul_vial"), 64, 1);
                stack.setTagCompound(compound);
                LARGE_POWERED_SPAWNER_RECIPES.recipeBuilder()
                        .inputs(new ItemStack(Item.getByNameOrId("enderio:item_soul_vial"), 64))
                        .notConsumable(IntCircuitIngredient.getIntegratedCircuit(i))
                        .outputs(stack)
                        .EUt(122880)
                        .duration(20)
                        .buildAndRegister();
            }
        }
    }
}
