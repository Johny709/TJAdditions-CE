package tj.recipes.ct;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

import static tj.TJRecipeMaps.ARMOR_INFUSER_RECIPES;
import static gregicadditions.GAMaterials.Cryotheum;
import static gregicadditions.GAMaterials.SupercooledCryotheum;

public class ArmorInfuserRecipes {

    public static void init() {
        Map<String, ItemStack[]> armorInfuserMap = new HashMap<>();
        armorInfuserMap.put("draconicevolution:draconic_pick", new ItemStack[]{new ItemStack(Item.getByNameOrId("draconicevolution:wyvern_pick"))});
        armorInfuserMap.put("draconicevolution:draconic_shovel", new ItemStack[]{new ItemStack(Item.getByNameOrId("draconicevolution:wyvern_shovel"))});
        armorInfuserMap.put("draconicevolution:draconic_axe", new ItemStack[]{new ItemStack(Item.getByNameOrId("draconicevolution:wyvern_axe"))});
        armorInfuserMap.put("draconicevolution:draconic_hoe", new ItemStack[]{new ItemStack(Items.DIAMOND_HOE)});
        armorInfuserMap.put("draconicevolution:draconium_capacitor", new ItemStack[]{new ItemStack(Item.getByNameOrId("draconicevolution:draconium_capacitor"), 1)});
        armorInfuserMap.put("draconicevolution:draconic_staff_of_power", new ItemStack[]{new ItemStack(Item.getByNameOrId("draconicevolution:draconic_shovel")),
                new ItemStack(Item.getByNameOrId("draconicevolution:draconic_sword")), new ItemStack(Item.getByNameOrId("draconicevolution:draconic_pick"))});
        armorInfuserMap.put("draconicadditions:chaotic_staff_of_power", new ItemStack[]{new ItemStack(Item.getByNameOrId("draconicevolution:draconic_staff_of_power"))});
        armorInfuserMap.put("draconicadditions:chaotic_bow", new ItemStack[]{new ItemStack(Item.getByNameOrId("draconicevolution:draconic_bow"))});
        armorInfuserMap.put("draconicevolution:draconic_sword", new ItemStack[]{new ItemStack(Item.getByNameOrId("draconicevolution:wyvern_sword"))});
        armorInfuserMap.put("draconicevolution:draconic_bow", new ItemStack[]{new ItemStack(Item.getByNameOrId("draconicevolution:wyvern_bow"))});
        armorInfuserMap.put("draconicevolution:draconic_helm", new ItemStack[]{new ItemStack(Item.getByNameOrId("draconicevolution:wyvern_helm"))});
        armorInfuserMap.put("draconicevolution:draconic_chest", new ItemStack[]{new ItemStack(Item.getByNameOrId("draconicevolution:wyvern_chest"))});
        armorInfuserMap.put("draconicevolution:draconic_legs", new ItemStack[]{new ItemStack(Item.getByNameOrId("draconicevolution:wyvern_legs"))});
        armorInfuserMap.put("draconicevolution:draconic_boots", new ItemStack[]{new ItemStack(Item.getByNameOrId("draconicevolution:wyvern_boots"))});
        armorInfuserMap.put("draconicadditions:chaotic_helm", new ItemStack[]{new ItemStack(Item.getByNameOrId("draconicevolution:draconic_helm"))});
        armorInfuserMap.put("draconicadditions:chaotic_chest", new ItemStack[]{new ItemStack(Item.getByNameOrId("draconicevolution:draconic_chest"))});
        armorInfuserMap.put("draconicadditions:chaotic_legs", new ItemStack[]{new ItemStack(Item.getByNameOrId("draconicevolution:draconic_legs"))});
        armorInfuserMap.put("draconicadditions:chaotic_boots", new ItemStack[]{new ItemStack(Item.getByNameOrId("draconicevolution:draconic_boots"))});

        armorInfuserMap.forEach((output, input) -> {
            if (output.contains("chaotic")) {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setBoolean("isStable", true);
                ItemStack stack = new ItemStack(Item.getByNameOrId(output));
                stack.setTagCompound(compound);
                ARMOR_INFUSER_RECIPES.recipeBuilder()
                        .inputs(new ItemStack(Item.getByNameOrId("contenttweaker:refinedchaoscrystal"), 4), new ItemStack(Item.getByNameOrId("draconicevolution:chaotic_core"), 2),
                                new ItemStack(Item.getByNameOrId("draconicadditions:chaotic_energy_core"), 2))
                        .inputs(input)
                        .outputs(stack)
                        .fluidInputs(SupercooledCryotheum.getFluid(4000))
                        .fluidOutputs(Cryotheum.getFluid(4000))
                        .EUt(33554432)
                        .duration(150)
                        .buildAndRegister();
            } else {
                ARMOR_INFUSER_RECIPES.recipeBuilder()
                        .inputs(new ItemStack(Item.getByNameOrId("draconicevolution:awakened_core"), output.contains("capacitor") ? 4 : 2),
                                new ItemStack(Item.getByNameOrId("draconicevolution:draconic_energy_core"), output.contains("capacitor") ? 4 : 2))
                        .inputs(input)
                        .outputs(new ItemStack(Item.getByNameOrId(output), 1, output.contains("capacitor") ? 1 : 0))
                        .fluidInputs(SupercooledCryotheum.getFluid(3000))
                        .fluidOutputs(Cryotheum.getFluid(3000))
                        .EUt(8388608)
                        .duration(150)
                        .buildAndRegister();
            }
        });
    }
}
