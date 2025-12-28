package tj.recipes;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.FluidRegistry;
import tj.blocks.AdvEnergyPortCasings;
import tj.blocks.EnergyPortCasings;
import tj.blocks.BlockFusionCasings;
import tj.blocks.TJMetaBlocks;
import gregicadditions.GAValues;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMetaItems;
import gregicadditions.item.fusion.GAFusionCasing;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.type.IngotMaterial;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.MetaTileEntityLargeBoiler;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.Objects;

import static gregtech.api.unification.material.MarkerMaterials.Tier.*;
import static tj.TJValues.CIRCUIT_TIERS;
import static tj.items.TJMetaItems.*;
import static tj.machines.TJMetaTileEntities.*;
import static gregicadditions.GAMaterials.*;
import static gregicadditions.item.GAMetaItems.*;
import static gregicadditions.machines.GATileEntities.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.common.items.MetaItems.*;
import static gregtech.common.metatileentities.MetaTileEntities.*;

public class AssemblerRecipes {

    public final static Material[][] MATERIAL_TIER = {{Steel, Aluminium, StainlessSteel, Titanium, TungstenSteel, RhodiumPlatedPalladium, IngotMaterial.MATERIAL_REGISTRY.getObject("star_metal_alloy"), Tritanium, Seaborgium, Bohrium, Adamantium, Vibranium, HeavyQuarkDegenerateMatter, Neutronium},
                                                {IngotMaterial.MATERIAL_REGISTRY.getObject("lv_superconductor"), MVSuperconductor, HVSuperconductor, EVSuperconductor, IVSuperconductor, LuVSuperconductor, ZPMSuperconductor, UVSuperconductor, UHVSuperconductor, UEVSuperconductor, UIVSuperconductor, UMVSuperconductor, UXVSuperconductor, MarkerMaterials.Tier.Superconductor}};
    public final static MetaTileEntityLargeBoiler[] BOILER_TYPE = {LARGE_BRONZE_BOILER, LARGE_STEEL_BOILER, LARGE_TITANIUM_BOILER, LARGE_TUNGSTENSTEEL_BOILER};

    public static void init() {
        MetaItem<?>.MetaValueItem[] emitters = {EMITTER_LV, EMITTER_MV, EMITTER_HV, EMITTER_EV, EMITTER_IV, EMITTER_LUV, EMITTER_ZPM, EMITTER_UV, EMITTER_UHV, EMITTER_UEV, EMITTER_UIV, EMITTER_UMV, EMITTER_UXV, EMITTER_MAX};
        MetaItem<?>.MetaValueItem[] sensors = {SENSOR_LV, SENSOR_MV, SENSOR_HV, SENSOR_EV, SENSOR_IV, SENSOR_LUV, SENSOR_ZPM, SENSOR_UV, SENSOR_UHV, SENSOR_UEV, SENSOR_UIV, SENSOR_UMV, SENSOR_UXV, SENSOR_MAX};
        MetaItem<?>.MetaValueItem[] pumps = {ELECTRIC_PUMP_LV, ELECTRIC_PUMP_MV, ELECTRIC_PUMP_HV, ELECTRIC_PUMP_EV, ELECTRIC_PUMP_IV, ELECTRIC_PUMP_LUV, ELECTRIC_PUMP_ZPM, ELECTRIC_PUMP_UV, ELECTRIC_PUMP_UHV, ELECTRIC_PUMP_UEV, ELECTRIC_PUMP_UIV, ELECTRIC_PUMP_UMV, ELECTRIC_PUMP_UXV, ELECTRIC_PUMP_MAX};
        MetaItem<?>.MetaValueItem[] conveyors = {CONVEYOR_MODULE_LV, CONVEYOR_MODULE_MV, CONVEYOR_MODULE_HV, CONVEYOR_MODULE_EV, CONVEYOR_MODULE_IV, CONVEYOR_MODULE_LUV, CONVEYOR_MODULE_ZPM, CONVEYOR_MODULE_UV, CONVEYOR_MODULE_UHV, CONVEYOR_MODULE_UEV, CONVEYOR_MODULE_UIV, CONVEYOR_MODULE_UMV, CONVEYOR_MODULE_UXV, CONVEYOR_MODULE_MAX};
        MetaItem<?>.MetaValueItem[] robotArms = {ROBOT_ARM_LV, ROBOT_ARM_MV, ROBOT_ARM_HV, ROBOT_ARM_EV, ROBOT_ARM_IV, ROBOT_ARM_LUV, ROBOT_ARM_ZPM, ROBOT_ARM_UV, ROBOT_ARM_UHV, ROBOT_ARM_UEV, ROBOT_ARM_UIV, ROBOT_ARM_UMV, ROBOT_ARM_UXV, ROBOT_ARM_MAX};
        MetaItem<?>.MetaValueItem[] regulators = {FLUID_REGULATOR_LV, FLUID_REGULATOR_MV, FLUID_REGULATOR_HV, FLUID_REGULATOR_EV, FLUID_REGULATOR_IV, FLUID_REGULATOR_LUV, FLUID_REGULATOR_ZPM, FLUID_REGULATOR_UV, FLUID_REGULATOR_UHV, null, null, FLUID_REGULATOR_UMV, null, FLUID_REGULATOR_MAX};

        for (int i = 0; i < BOILER_TYPE.length; i++) {
            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(BOILER_TYPE[i].getStackForm(64))
                    .inputs(BOILER_TYPE[i].getStackForm(64))
                    .inputs(BOILER_TYPE[i].getStackForm(64))
                    .inputs(BOILER_TYPE[i].getStackForm(64))
                    .outputs(MEGA_BOILER[i].getStackForm(1))
                    .EUt(GAValues.VA[2 + i])
                    .duration(1200)
                    .buildAndRegister();
        }

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.pipeLarge, Naquadah, 9)
                .inputs(MetaTileEntities.HULL[6].getStackForm())
                .notConsumable(new IntCircuitIngredient(0))
                .outputs(QUADRUPLE_QUADRUPLE_INPUT_HATCH.getStackForm())
                .duration(100)
                .EUt(7680)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.pipeLarge, Naquadah, 9)
                .inputs(MetaTileEntities.HULL[6].getStackForm())
                .notConsumable(new IntCircuitIngredient(1))
                .outputs(QUADRUPLE_QUADRUPLE_OUTPUT_HATCH.getStackForm())
                .duration(100)
                .EUt(7680)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(MetaTileEntities.ENERGY_INPUT_HATCH[6].getStackForm())
                .inputs(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate)
                .input(OrePrefix.plate, TungstenSteel, 8)
                .outputs(TJMetaBlocks.ENERGY_PORT_CASING.getItemVariant(EnergyPortCasings.AbilityType.ENERGY_PORT_LUV))
                .duration(200)
                .EUt(GAValues.VA[6])
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(RecipeInit.getEnergyHatch(9, false), UHPIC.getStackForm(8))
                .input(OrePrefix.circuit, Infinite)
                .input(OrePrefix.plateDense, Seaborgium, 7)
                .outputs(TJMetaBlocks.ADV_ENERGY_PORT_CASING.getItemVariant(AdvEnergyPortCasings.AbilityType.ENERGY_PORT_UHV))
                .duration(200).EUt(GAValues.VA[9])
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(MetaTileEntities.ENERGY_INPUT_HATCH[7].getStackForm())
                .inputs(MetaItems.HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(2))
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Superconductor)
                .input(OrePrefix.plate, Rutherfordium, 8)
                .outputs(TJMetaBlocks.ENERGY_PORT_CASING.getItemVariant(EnergyPortCasings.AbilityType.ENERGY_PORT_ZPM))
                .duration(200)
                .EUt(GAValues.VA[7])
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(RecipeInit.getEnergyHatch(10, false), UHPIC.getStackForm(8))
                .input(OrePrefix.circuit, UEV)
                .input(OrePrefix.plateDense, Bohrium, 7)
                .outputs(TJMetaBlocks.ADV_ENERGY_PORT_CASING.getItemVariant(AdvEnergyPortCasings.AbilityType.ENERGY_PORT_UEV))
                .duration(200).EUt(GAValues.VA[10])
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(MetaTileEntities.ENERGY_INPUT_HATCH[8].getStackForm())
                .inputs(GAMetaItems.UHPIC.getStackForm(2))
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Infinite)
                .input(OrePrefix.plate, Dubnium, 8)
                .outputs(TJMetaBlocks.ENERGY_PORT_CASING.getItemVariant(EnergyPortCasings.AbilityType.ENERGY_PORT_UV))
                .duration(200)
                .EUt(GAValues.VA[8])
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(RecipeInit.getEnergyHatch(11, false), UHPIC.getStackForm(8))
                .input(OrePrefix.circuit, UIV)
                .input(OrePrefix.plateDense, Adamantium, 7)
                .outputs(TJMetaBlocks.ADV_ENERGY_PORT_CASING.getItemVariant(AdvEnergyPortCasings.AbilityType.ENERGY_PORT_UIV))
                .duration(200).EUt(GAValues.VA[11])
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(ENERGY_INPUT[0].getStackForm())
                .inputs(GAMetaItems.UHPIC.getStackForm(4))
                .input(OrePrefix.circuit, UEV)
                .input(OrePrefix.plate, Seaborgium, 8)
                .outputs(TJMetaBlocks.ENERGY_PORT_CASING.getItemVariant(EnergyPortCasings.AbilityType.ENERGY_PORT_UHV))
                .duration(200)
                .EUt(GAValues.VA[9])
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(RecipeInit.getEnergyHatch(12, false), UHPIC.getStackForm(8))
                .input(OrePrefix.circuit, UMV)
                .input(OrePrefix.plateDense, Vibranium, 7)
                .outputs(TJMetaBlocks.ADV_ENERGY_PORT_CASING.getItemVariant(AdvEnergyPortCasings.AbilityType.ENERGY_PORT_UMV))
                .duration(200).EUt(GAValues.VA[12])
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(ENERGY_INPUT[1].getStackForm())
                .inputs(UHPIC.getStackForm(8))
                .input(OrePrefix.circuit, UIV)
                .input(OrePrefix.plate, Bohrium, 8)
                .outputs(TJMetaBlocks.ENERGY_PORT_CASING.getItemVariant(EnergyPortCasings.AbilityType.ENERGY_PORT_UEV))
                .duration(200)
                .EUt(GAValues.VA[10])
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(RecipeInit.getEnergyHatch(13, false), UHPIC.getStackForm(8))
                .input(OrePrefix.circuit, UXV)
                .input(OrePrefix.plateDense, HeavyQuarkDegenerateMatter, 8)
                .outputs(TJMetaBlocks.ADV_ENERGY_PORT_CASING.getItemVariant(AdvEnergyPortCasings.AbilityType.ENERGY_PORT_UVX))
                .duration(200).EUt(GAValues.VA[13])
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(HULL[8].getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Infinite, 4)
                .input(OrePrefix.cableGtSingle, NaquadahAlloy, 16)
                .inputs(MetaItems.FIELD_GENERATOR_UV.getStackForm(2))
                .inputs(MetaItems.SENSOR_UV.getStackForm(2))
                .inputs(MetaItems.EMITTER_UV.getStackForm(2))
                .outputs(ACCELERATOR_ANCHOR_POINT.getStackForm())
                .fluidInputs(SolderingAlloy.getFluid(1152))
                .duration(200)
                .EUt(GAValues.VA[8])
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, HastelloyK243, 6)
                .inputs(GAMetaBlocks.FUSION_CASING.getItemVariant(GAFusionCasing.CasingType.FUSION_3))
                .outputs(TJMetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasings.FusionType.FUSION_CASING_UHV))
                .duration(50)
                .EUt(500000)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .fluidInputs(Periodicium.getFluid(144))
                .input(OrePrefix.plate, SuperheavyLAlloy, 6)
                .input(OrePrefix.plate, SuperheavyHAlloy, 6)
                .inputs(TJMetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasings.FusionType.FUSION_CASING_UHV))
                .outputs(TJMetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasings.FusionType.FUSION_CASING_UEV))
                .duration(50)
                .EUt(8000000)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.circuit, Master, 2)
                .inputs(ELECTRIC_PUMP_LUV.getStackForm())
                .notConsumable(new IntCircuitIngredient(1))
                .outputs(FLUID_REGULATOR_LUV.getStackForm())
                .duration(100)
                .EUt(GAValues.VA[6])
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(LARGE_SOLAR_BOILER.getStackForm(64), LARGE_SOLAR_BOILER.getStackForm(64), LARGE_SOLAR_BOILER.getStackForm(64), LARGE_SOLAR_BOILER.getStackForm(64))
                .outputs(MEGA_SOLAR_BOILER.getStackForm())
                .duration(1200).EUt(GAValues.VA[1])
                .buildAndRegister();

        for (int i = 0; i < 15; i++) {
            if (i == GAValues.UHV || i == GAValues.UMV || i == GAValues.MAX)
                ASSEMBLER_RECIPES.recipeBuilder()
                        .input(OrePrefix.circuit, CIRCUIT_TIERS[Math.min(12, i)], 2)
                        .inputs(pumps[i - 1].getStackForm())
                        .notConsumable(new IntCircuitIngredient(1))
                        .outputs(regulators[i - 1].getStackForm())
                        .duration(100)
                        .EUt(GAValues.VA[i])
                        .buildAndRegister();
        }

        for (int i = 0; i < SUPER_ITEM_INPUT_BUS.length; i++) {
            int tier = Math.min(GAValues.MAX, 3 + (3 * i));
            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(i < 3 ? ITEM_IMPORT_BUS[tier].getStackForm(64) : SUPER_ITEM_INPUT_BUS[i - 1].getStackForm())
                    .input(OrePrefix.gear, MATERIAL_TIER[0][tier - 1], 16)
                    .inputs(robotArms[tier - 1].getStackForm(4))
                    .outputs(SUPER_ITEM_INPUT_BUS[i].getStackForm())
                    .fluidInputs(i < 2 ? Polybenzimidazole.getFluid(9216) : i < 3 ? Polyetheretherketone.getFluid(9216) : i < 4 ? Zylon.getFluid(9216) : FullerenePolymerMatrix.getFluid(9216))
                    .duration(1200)
                    .EUt(GAValues.VA[tier])
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(i < 3 ? ITEM_EXPORT_BUS[tier].getStackForm(64) : SUPER_ITEM_OUTPUT_BUS[i - 1].getStackForm())
                    .input(OrePrefix.gear, MATERIAL_TIER[0][tier - 1], 16)
                    .inputs(conveyors[tier - 1].getStackForm(4))
                    .outputs(SUPER_ITEM_OUTPUT_BUS[i].getStackForm())
                    .fluidInputs(i < 2 ? Polybenzimidazole.getFluid(9216) : i < 3 ? Polyetheretherketone.getFluid(9216) : i < 4 ? Zylon.getFluid(9216) : FullerenePolymerMatrix.getFluid(9216))
                    .duration(1200)
                    .EUt(GAValues.VA[tier])
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(i < 2 ? INPUT_HATCH_MULTI.get(i).getStackForm(64) : i < 3 ? QUADRUPLE_QUADRUPLE_INPUT_HATCH.getStackForm(64) : SUPER_FLUID_INPUT_HATCH[i - 1].getStackForm())
                    .input(OrePrefix.gear, MATERIAL_TIER[0][tier - 1], 16)
                    .inputs(regulators[tier - 1].getStackForm(4))
                    .outputs(SUPER_FLUID_INPUT_HATCH[i].getStackForm())
                    .fluidInputs(i < 2 ? Polybenzimidazole.getFluid(9216) : i < 3 ? Polyetheretherketone.getFluid(9216) : i < 4 ? Zylon.getFluid(9216) : FullerenePolymerMatrix.getFluid(9216))
                    .duration(1200)
                    .EUt(GAValues.VA[tier])
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(i < 2 ? OUTPUT_HATCH_MULTI.get(i).getStackForm(64) : i < 3 ? QUADRUPLE_QUADRUPLE_OUTPUT_HATCH.getStackForm(64) : SUPER_FLUID_OUTPUT_HATCH[i - 1].getStackForm())
                    .input(OrePrefix.gear, MATERIAL_TIER[0][tier - 1], 16)
                    .inputs(pumps[tier - 1].getStackForm(4))
                    .outputs(SUPER_FLUID_OUTPUT_HATCH[i].getStackForm())
                    .fluidInputs(i < 2 ? Polybenzimidazole.getFluid(9216) : i < 3 ? Polyetheretherketone.getFluid(9216) : i < 4 ? Zylon.getFluid(9216) : FullerenePolymerMatrix.getFluid(9216))
                    .duration(1200)
                    .EUt(GAValues.VA[tier])
                    .buildAndRegister();
        }


        for (int i = 0; i < MATERIAL_TIER[0].length; i++) {

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(ENERGY_INPUT_HATCH_128_AMPS.get(i + 1).getStackForm())
                    .input(OrePrefix.wireGtHex, MATERIAL_TIER[1][i], 2)
                    .input(OrePrefix.plate, MATERIAL_TIER[0][i], 6)
                    .outputs(ENERGY_INPUT_HATCH_256A[i].getStackForm())
                    .duration(600)
                    .EUt(GAValues.VA[i + 1])
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(ENERGY_OUTPUT_HATCH_128_AMPS.get(i + 1).getStackForm())
                    .input(OrePrefix.wireGtHex, MATERIAL_TIER[1][i], 2)
                    .input(OrePrefix.plate, MATERIAL_TIER[0][i], 6)
                    .outputs(ENERGY_OUTPUT_HATCH_256A[i].getStackForm())
                    .duration(600)
                    .EUt(GAValues.VA[i + 1])
                    .buildAndRegister();
        }

        for (int i = 0; i < ENDER_FLUID_COVERS.length; i++) {
            ASSEMBLER_RECIPES.recipeBuilder()
                    .fluidInputs(SolderingAlloy.getFluid(576))
                    .input(OrePrefix.plate, EnderPearl, 9)
                    .input(OrePrefix.plateDense, StainlessSteel)
                    .inputs(emitters[i + 2].getStackForm(2))
                    .inputs(sensors[i + 2].getStackForm(2))
                    .inputs(pumps[i + 2].getStackForm(2))
                    .outputs(ENDER_FLUID_COVERS[i].getStackForm())
                    .duration(600)
                    .EUt(GAValues.VA[i + 3])
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .fluidInputs(SolderingAlloy.getFluid(576))
                    .input(OrePrefix.plate, EnderPearl, 9)
                    .input(OrePrefix.plateDense, StainlessSteel)
                    .inputs(emitters[i + 2].getStackForm(2))
                    .inputs(sensors[i + 2].getStackForm(2))
                    .inputs(conveyors[i + 2].getStackForm(2))
                    .outputs(ENDER_ITEM_COVERS[i].getStackForm())
                    .duration(600)
                    .EUt(GAValues.VA[i + 3])
                    .buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .fluidInputs(SolderingAlloy.getFluid(576))
                    .input(OrePrefix.plate, EnderPearl, 9)
                    .input(OrePrefix.plateDense, StainlessSteel)
                    .inputs(emitters[i + 2].getStackForm(2))
                    .inputs(sensors[i + 2].getStackForm(2))
                    .input(OrePrefix.cableGtHex, MATERIAL_TIER[1][i + 2], 2)
                    .outputs(ENDER_ENERGY_COVERS[i].getStackForm())
                    .duration(600)
                    .EUt(GAValues.VA[i + 3])
                    .buildAndRegister();
        }

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.LEVER))
                .input(OrePrefix.plate, Steel)
                .inputs(HULL[1].getStackForm())
                .fluidInputs(SolderingAlloy.getFluid(144))
                .outputs(MACHINE_CONTROLLER.getStackForm())
                .duration(200)
                .EUt(16)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Objects.requireNonNull(Block.getBlockFromName("enderio:block_reservoir")), 64))
                .inputs(GAMetaItems.UV_INFINITE_WATER_SOURCE.getStackForm())
                .inputs(GA_HULLS[0].getStackForm())
                .fluidInputs(Water.getFluid(4096000))
                .outputs(WATER_RESERVOIR_HATCH.getStackForm())
                .duration(1200)
                .EUt(GAValues.VA[9])
                .buildAndRegister();

        for (int i = 0, tier = 1; i < FARMING_STATION.length; i++, tier++) {
            ASSEMBLER_RECIPES.recipeBuilder()
                    .notConsumable(new IntCircuitIngredient(0))
                    .input(OrePrefix.gear, i != 6 ? MATERIAL_TIER[0][i] : Rutherfordium, 4)
                    .input(OrePrefix.circuit, CIRCUIT_TIERS[tier], 2)
                    .input(Items.DIAMOND_AXE)
                    .input(Items.DIAMOND_HOE)
                    .input(Items.SHEARS)
                    .inputs(robotArms[i].getStackForm(tier >= GTValues.ZPM ? 4 : tier >= GTValues.EV ? 2 : 1))
                    .inputs(new ItemStack(Item.getByNameOrId("enderio:item_material"), 1, 42))
                    .inputs(tier == 14 ? HULL[9].getStackForm() : tier < 9 ? HULL[tier].getStackForm() : GA_HULLS[tier - 9].getStackForm())
                    .outputs(FARMING_STATION[i].getStackForm())
                    .duration(400).EUt(GAValues.VA[tier])
                    .buildAndRegister();
        }

        MetaTileEntity[] chest = new MetaTileEntity[]{COMPRESSED_CHEST, COMPRESSED_CRATE, INFINITY_CHEST, INFINITY_CRATE};
        for (int i = 0; i < 2; i++) {
            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(super_chest[3].getStackForm(64))
                    .inputs(new ItemStack(Item.getByNameOrId("nae2:material"), 64, 4))
                    .inputs(chest[i].getStackForm(27))
                    .input(OrePrefix.circuit, UIV, 4)
                    .inputs(CONVEYOR_MODULE_UEV.getStackForm(4))
                    .inputs(ELECTRIC_PISTON_UEV.getStackForm(4))
                    .inputs(ROBOT_ARM_UEV.getStackForm(4))
                    .fluidInputs(FluidRegistry.getFluidStack("degenerate_rhenium_plasma", 64000))
                    .outputs(chest[i + 2].getStackForm())
                    .duration(1200)
                    .EUt(GAValues.VA[10])
                    .buildAndRegister();
        }
    }
}
