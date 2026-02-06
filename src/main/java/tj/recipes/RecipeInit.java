package tj.recipes;

import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.core.Api;
import appeng.core.api.definitions.ApiBlocks;
import gregicadditions.GAEnums;
import gregicadditions.GAValues;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMetaItems;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.fusion.GAFusionCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregicadditions.recipes.helper.GACraftingComponents;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.BlockMachineCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.electric.MetaTileEntityAirCollector;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumTank;
import gregtech.loaders.recipe.CraftingComponent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import tj.TJConfig;
import tj.blocks.*;
import tj.integration.appeng.IApiItems;
import tj.integration.appeng.IApiMaterials;
import tj.machines.TJMetaTileEntities;
import tj.recipes.ct.*;

import java.util.Arrays;

import static gregicadditions.GAMaterials.*;
import static gregicadditions.machines.GATileEntities.AIR_COLLECTOR;
import static gregicadditions.machines.GATileEntities.GA_HULLS;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.material.MarkerMaterials.Tier.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.common.metatileentities.MetaTileEntities.*;
import static tj.TJValues.CIRCUIT_TIERS;
import static tj.items.TJMetaItems.*;
import static tj.machines.TJMetaTileEntities.*;
import static tj.recipes.AssemblerRecipes.MATERIAL_TIER;

public class RecipeInit {

    public static MetaTileEntityAirCollector[] AIR_COLLECTORS = {MetaTileEntities.AIR_COLLECTOR[3], AIR_COLLECTOR[4], AIR_COLLECTOR[5]};

    public static void init() {

        craftingRecipes();
        GreenhouseRecipes.init();
        AssemblerRecipes.init();
        AssemblyLineRecipes.init();
        RockBreakerRecipes.init();
        CokeOvenRecipes.init();

        if (TJConfig.machines.replaceCTMultis) {
            PrimitiveAlloySmelterRecipes.init();
            HeatExchangerRecipes.init();
            LargePoweredSpawnerRecipes.init();
            LargeVialProcessorRecipes.init();
            ArmorInfuserRecipes.init();
            DragonEggReplicatorRecipes.init();
            ChaosReplicatorRecipes.init();
        }
    }
    private static void craftingRecipes() {
        ApiBlocks aeBlocks = Api.INSTANCE.definitions().blocks();
        IApiItems aeItems = ((IApiItems) (IItems) Api.INSTANCE.definitions().items());
        IApiMaterials aeMaterials = ((IApiMaterials) (IMaterials) Api.INSTANCE.definitions().materials());
        ItemStack[] aeCellParts = {aeMaterials.getCell65mPart().maybeStack(1).get(), aeMaterials.getCell262mPart().maybeStack(1).get(), aeMaterials.getCell1048mPart().maybeStack(1).get(), aeMaterials.getCellDigitalSingularityPart().maybeStack(1).get(),
            aeMaterials.getFluidCell65mPart().maybeStack(1).get(), aeMaterials.getFluidCell262mPart().maybeStack(1).get(), aeMaterials.getFluidCell1048mPart().maybeStack(1).get(), aeMaterials.getFluidCellDigitalSingularityPart().maybeStack(1).get()};
        ItemStack[] aeCells = {aeItems.getCell65m().maybeStack(1).get(), aeItems.getCell262m().maybeStack(1).get(), aeItems.getCell1048m().maybeStack(1).get(), aeItems.getCellDigitalSingularity().maybeStack(1).get(),
            aeItems.getFluidCell65m().maybeStack(1).get(), aeItems.getFluidCell262m().maybeStack(1).get(), aeItems.getFluidCell1048m().maybeStack(1).get(), aeItems.getFluidCellDigitalSingularity().maybeStack(1).get()};

        ALLOY_SMELTER_RECIPES.recipeBuilder()
                .input(Items.CLAY_BALL)
                .input(Blocks.SAND)
                .outputs(MetaItems.COKE_OVEN_BRICK.getStackForm(2))
                .EUt(64)
                .duration(600)
                .buildAndRegister();

        ModHandler.addShapedRecipe("large_decay_chamber", LARGE_DECAY_CHAMBER.getStackForm(), "LCL", "FMF", "LCL",
                'L', new UnificationEntry(OrePrefix.plateDense, Lead),
                'C', CraftingComponent.CIRCUIT.getIngredient(6),
                'F', CraftingComponent.FIELD_GENERATOR.getIngredient(6),
                'M', GATileEntities.DECAY_CHAMBER[5].getMetaTileEntity().getStackForm());

        ModHandler.addShapedRecipe("large_alloy_smelter", LARGE_ALLOY_SMELTER.getStackForm(), "PCP", "WSW", "PCP",
                'P', new UnificationEntry(OrePrefix.plate, ZirconiumCarbide),
                'C', CraftingComponent.CIRCUIT.getIngredient(5),
                'W', new UnificationEntry(OrePrefix.cableGtOctal, Naquadah),
                'S', GATileEntities.ALLOY_SMELTER[4].getMetaTileEntity().getStackForm());

        ModHandler.addShapedRecipe("large_greenhouse", LARGE_GREENHOUSE.getStackForm(), "PCP", "RSR", "GCG",
                'P', new UnificationEntry(OrePrefix.plate, RhodiumPlatedPalladium),
                'G', new UnificationEntry(OrePrefix.gear, RhodiumPlatedPalladium),
                'C', CraftingComponent.CIRCUIT.getIngredient(6),
                'R', CraftingComponent.PUMP.getIngredient(6),
                'S', GATileEntities.GREEN_HOUSE[5].getMetaTileEntity().getStackForm());

        ModHandler.addShapedRecipe("large_architect_workbench", LARGE_ARCHITECT_WORKBENCH.getStackForm(), "GCG", "RSB", "GCG",
                'G', new UnificationEntry(OrePrefix.gear, Steel),
                'C', CraftingComponent.CIRCUIT.getIngredient(5),
                'R', CraftingComponent.ROBOT_ARM.getIngredient(5),
                'B', CraftingComponent.CONVEYOR.getIngredient(5),
                'S', ARCHITECT_WORKBENCH[4].getStackForm());

        ModHandler.addShapedRecipe("large_chisel_workbench", LARGE_CHISEL_WORKBENCH.getStackForm(), "GCG", "RSB", "GCG",
                'G', new UnificationEntry(OrePrefix.gear, MaragingSteel250),
                'C', CraftingComponent.CIRCUIT.getIngredient(5),
                'R', CraftingComponent.ROBOT_ARM.getIngredient(5),
                'B', CraftingComponent.CONVEYOR.getIngredient(5),
                'S', CHISEL_WORKBENCH[4].getStackForm());

        for (int i = 0, tier = 1; i < ARCHITECT_WORKBENCH.length; i++, tier++) {
            ModHandler.addShapedRecipe("architect_workbench_" + GAValues.VN[tier].toLowerCase(), ARCHITECT_WORKBENCH[i].getStackForm(), "PAP", "CSC", "MWM",
                    'P', GACraftingComponents.PISTON.getIngredient(tier),
                    'A', new ItemStack(Item.getByNameOrId("architecturecraft:sawbench")),
                    'S', tier == 14 ? HULL[9].getStackForm() : tier < 9 ? HULL[tier].getStackForm() : GA_HULLS[tier - 9].getStackForm(),
                    'C', GACraftingComponents.CIRCUIT.getIngredient(tier),
                    'W', GACraftingComponents.CABLE_SINGLE.getIngredient(tier),
                    'M', GACraftingComponents.MOTOR.getIngredient(tier));
        }

        for (int i = 0, tier = 1; i < ARCHITECT_WORKBENCH.length; i++, tier++) {
            ModHandler.addShapedRecipe("chisel_workbench_" + GAValues.VN[tier].toLowerCase(), CHISEL_WORKBENCH[i].getStackForm(), "CAC", "RSR", "GWG",
                    'R', GACraftingComponents.CONVEYOR.getIngredient(tier),
                    'A', new ItemStack(Item.getByNameOrId("chisel:auto_chisel")),
                    'S', tier == 14 ? HULL[9].getStackForm() : tier < 9 ? HULL[tier].getStackForm() : GA_HULLS[tier - 9].getStackForm(),
                    'C', GACraftingComponents.CIRCUIT.getIngredient(tier),
                    'W', GACraftingComponents.CABLE_SINGLE.getIngredient(tier),
                    'G', GACraftingComponents.GEAR.getIngredient(tier));
        }

        for (int i = 0, tier = 1; i < ARCHITECT_WORKBENCH.length; i++, tier++) {
            ModHandler.addShapedRecipe("enchanter_" + GAValues.VN[tier].toLowerCase(), ENCHANTER[i].getStackForm(), "CEC", "PSP", "DWD",
                    'C', GACraftingComponents.CIRCUIT.getIngredient(tier),
                    'E', new ItemStack(Blocks.ENCHANTING_TABLE),
                    'S', tier == 14 ? HULL[9].getStackForm() : tier < 9 ? HULL[tier].getStackForm() : GA_HULLS[tier - 9].getStackForm(),
                    'P', GACraftingComponents.PUMP.getIngredient(tier),
                    'D', new UnificationEntry(OrePrefix.block, Diamond),
                    'W', GACraftingComponents.CABLE_SINGLE.getIngredient(tier));
        }

        ModHandler.addShapedRecipe("elite_large_miner", ELITE_LARGE_MINER.getStackForm(), "GCG", "THT", "SCS",
                'G', new UnificationEntry(OrePrefix.gear, Duranium),
                'C', CraftingComponent.CIRCUIT.getIngredient(7),
                'T', MetaItems.COMPONENT_GRINDER_TUNGSTEN.getStackForm(),
                'H', CraftingComponent.HULL.getIngredient(7),
                'S', CraftingComponent.SENSOR.getIngredient(7));

        ModHandler.addShapedRecipe("ultimate_large_miner", ULTIMATE_LARGE_MINER.getStackForm(), "GCG", "THT", "SCS",
                'G', new UnificationEntry(OrePrefix.gear, Seaborgium),
                'C', CraftingComponent.CIRCUIT.getIngredient(8),
                'T', MetaItems.COMPONENT_GRINDER_TUNGSTEN.getStackForm(),
                'H', CraftingComponent.HULL.getIngredient(8),
                'S', CraftingComponent.SENSOR.getIngredient(8));

        ModHandler.addShapedRecipe("world_destroyer", WORLD_DESTROYER.getStackForm(), "GCG", "DTD", "SCS",
                'G', new UnificationEntry(OrePrefix.gear, TungstenTitaniumCarbide),
                'C', CraftingComponent.CIRCUIT.getIngredient(5),
                'D', MetaItems.COMPONENT_GRINDER_TUNGSTEN.getStackForm(),
                'T', MetaTileEntities.BLOCK_BREAKER[3].getStackForm(),
                'S', CraftingComponent.SENSOR.getIngredient(5));

        ModHandler.addShapedRecipe("large_rock_breaker", LARGE_ROCK_BREAKER.getStackForm(), "PCP", "USU", "GBG",
                'P', CraftingComponent.PISTON.getIngredient(5),
                'C', CraftingComponent.CIRCUIT.getIngredient(5),
                'U', CraftingComponent.PIPE.getIngredient(5),
                'S', GATileEntities.ROCK_BREAKER[4].getStackForm(),
                'G', GAMetaBlocks.TRANSPARENT_CASING.getItemVariant(GATransparentCasing.CasingType.CHROME_GLASS),
                'B', MetaItems.COMPONENT_GRINDER_TUNGSTEN.getStackForm());

        ModHandler.addShapedRecipe("stainless_pipe_casing", TJMetaBlocks.PIPE_CASING.getItemVariant(BlockPipeCasings.PipeCasingType.STAINLESS_PIPE_CASING, 3), "PTP", "TFT", "PTP",
                'P', new UnificationEntry(OrePrefix.plate, StainlessSteel),
                'T', new UnificationEntry(OrePrefix.pipeMedium, StainlessSteel),
                'F', new UnificationEntry(OrePrefix.frameGt, StainlessSteel));

        ModHandler.addShapelessRecipe("bronze_solar_boiler", SOLAR_BOILER[0].getStackForm(), MetaTileEntities.STEAM_BOILER_SOLAR_BRONZE.getStackForm());
        ModHandler.addShapelessRecipe("bronze_coal_boiler", COAL_BOILER[0].getStackForm(), MetaTileEntities.STEAM_BOILER_COAL_BRONZE.getStackForm());
        ModHandler.addShapelessRecipe("bronze_fluid_boiler", FLUID_BOILER[0].getStackForm(), MetaTileEntities.STEAM_BOILER_LAVA_BRONZE.getStackForm());

        ModHandler.addShapedRecipe("steel_solar_boiler", SOLAR_BOILER[1].getStackForm(), "GGG", "SSS", "PBP",
                'G', Blocks.GLASS,
                'S', new UnificationEntry(GAEnums.GAOrePrefix.plateDouble, Silver),
                'P', new UnificationEntry(OrePrefix.pipeMedium, Steel),
                'B', MetaBlocks.MACHINE_CASING.getItemVariant(BlockMachineCasing.MachineCasingType.STEEL_BRICKS_HULL));
        ModHandler.addShapelessRecipe("steel_coal_boiler", COAL_BOILER[1].getStackForm(), MetaTileEntities.STEAM_BOILER_COAL_STEEL.getStackForm());
        ModHandler.addShapelessRecipe("steel_fluid_boiler", FLUID_BOILER[1].getStackForm(), MetaTileEntities.STEAM_BOILER_LAVA_STEEL.getStackForm());

        ModHandler.addShapedRecipe("lv_solar_boiler", SOLAR_BOILER[2].getStackForm(), "GGG", "SSS", "PBP",
                'G', Blocks.GLASS,
                'S', new UnificationEntry(GAEnums.GAOrePrefix.plateDouble, Silver),
                'P', new UnificationEntry(OrePrefix.pipeLarge, Steel),
                'B', MetaTileEntities.HULL[1].getStackForm());

        ModHandler.addShapedRecipe("lv_coal_boiler", COAL_BOILER[2].getStackForm(), "PPP", "PHP", "BFB",
                'P', new UnificationEntry(OrePrefix.plate, Steel),
                'H', MetaTileEntities.HULL[1].getStackForm(),
                'B', Blocks.BRICK_BLOCK,
                'F', Blocks.FURNACE);

        ModHandler.addShapedRecipe("lv_fluid_boiler", FLUID_BOILER[2].getStackForm(), "PPP", "GGG", "PHP",
                'P', new UnificationEntry(OrePrefix.plate, Steel),
                'G', Blocks.GLASS,
                'H', MetaTileEntities.HULL[1].getStackForm());

        for (int i = 0; i < AIR_COLLECTORS.length; i++) {
            ModHandler.addShapedRecipe("large_atmosphere_collector." + i, LARGE_ATMOSPHERE_COLLECTOR[i].getStackForm(), "CRC", "RSR", "PRP",
                    'C', CraftingComponent.CIRCUIT.getIngredient(5 + i),
                    'R', CraftingComponent.ROTOR.getIngredient(5 + i),
                    'S', AIR_COLLECTORS[i].getStackForm(),
                    'P', CraftingComponent.PIPE.getIngredient(5 + i));
        }

        Material[] materials = {Duranium, Seaborgium, TungstenTitaniumCarbide, HeavyQuarkDegenerateMatter, Periodicium};
        for (Material material : materials) {
            ModHandler.addShapedRecipe(material.toString(), TJMetaBlocks.SOLID_CASING.getItemVariant(Arrays.stream(BlockSolidCasings.SolidCasingType.values())
                            .filter(block -> block.getName().equals(material.toString()))
                            .findFirst()
                            .orElse(null), 3), "PhP", "PFP", "PwP",
                    'P', new UnificationEntry(OrePrefix.plate, material),
                    'F', new UnificationEntry(OrePrefix.frameGt, material));

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.plate, material, 6)
                    .input(OrePrefix.frameGt, material)
                    .notConsumable(new IntCircuitIngredient(0))
                    .outputs(TJMetaBlocks.SOLID_CASING.getItemVariant(Arrays.stream(BlockSolidCasings.SolidCasingType.values())
                            .filter(block -> block.getName().equals(material.toString()))
                            .findFirst()
                            .orElse(null), 3))
                    .duration(50)
                    .EUt(16)
                    .buildAndRegister();
        }

        MetaTileEntityQuantumTank[] superTanks = {QUANTUM_TANK[1], Super_tank[2], Super_tank[4]};
        Material[] pipe = {StainlessSteel, Naquadah, TantalumHafniumSeaborgiumCarbide};
        for (int i = 0; i < STEAM_INPUT_FLUID_HATCH.length; i++) {
            int tier = 3 + (3 * i);
            MetaTileEntityQuantumTank superTank = superTanks[i];
            ModHandler.addShapedRecipe("steam_input_hatch." + GAValues.VN[tier], STEAM_INPUT_FLUID_HATCH[i].getStackForm(), "DPD", "DSD", "DPD",
                    'D', new UnificationEntry(OrePrefix.plate, MATERIAL_TIER[0][tier -1]),
                    'P', new UnificationEntry(OrePrefix.pipeLarge, pipe[i]),
                    'S', superTank.getStackForm());

            ModHandler.addShapedRecipe("steam_output_hatch." + GAValues.VN[tier], STEAM_OUTPUT_FLUID_HATCH[i].getStackForm(), "DDD", "PSP", "DDD",
                    'D', new UnificationEntry(OrePrefix.plate, MATERIAL_TIER[0][tier -1]),
                    'P', new UnificationEntry(OrePrefix.pipeLarge, pipe[i]),
                    'S', superTank.getStackForm());
        }

        ModHandler.addShapedRecipe("linking_device", LINKING_DEVICE.getStackForm(), "SIS", "RLR", "CIC",
                'S', CraftingComponent.SENSOR.getIngredient(5),
                'I', new UnificationEntry(OrePrefix.cableGtSingle, IVSuperconductor),
                'R', new UnificationEntry(OrePrefix.ring, HSSE),
                'L', new UnificationEntry(OrePrefix.stickLong, Osmium),
                'C', CraftingComponent.CIRCUIT.getIngredient(6));

        ModHandler.addShapedRecipe("industrial_stean_engine", INDUSTRIAL_STEAM_ENGINE.getStackForm(), "PCP", "BHB", "GOG",
                'P', new UnificationEntry(OrePrefix.pipeLarge, Bronze),
                'C', CraftingComponent.CIRCUIT.getIngredient(2),
                'B', new UnificationEntry(OrePrefix.plate, Brass),
                'H', GAMetaBlocks.METAL_CASING_1.getItemVariant(MetalCasing1.CasingType.TUMBAGA),
                'G', new UnificationEntry(OrePrefix.gear, Bronze),
                'O', new UnificationEntry(OrePrefix.gear, Steel));

        ModHandler.addShapedRecipe("void_plunger", VOID_PLUNGER.getStackForm(), " OO", " SO", "S  ",
                'O', new ItemStack(Item.getByNameOrId("enderio:block_reinforced_obsidian")),
                'S', new UnificationEntry(OrePrefix.stick, Steel));

        ModHandler.addShapedRecipe("nbt_reader", NBT_READER.getStackForm(), "PPP", "PCP", "PPP",
                'P', new ItemStack(Items.PAPER),
                'C', new UnificationEntry(OrePrefix.circuit, Basic));

        ModHandler.addShapedRecipe("rotor_holder_umv", ROTOR_HOLDER_UMV.getStackForm(), "MQM", "QHQ", "MQM",
                'M', new UnificationEntry(OrePrefix.gearSmall, MetastableHassium),
                'Q', new UnificationEntry(OrePrefix.gear, Quantum),
                'H', GATileEntities.GA_HULLS[3].getStackForm());

        ModHandler.addShapedRecipe("coolant_rotor_holder_umv", COOLANT_ROTOR_HOLDER_UMV.getStackForm(), "MVM", "VHV", "MVM",
                'M', new UnificationEntry(OrePrefix.gearSmall, MetastableOganesson),
                'V', new UnificationEntry(OrePrefix.gear, Vibranium),
                'H', GATileEntities.GA_HULLS[3].getStackForm());

        ModHandler.addShapedRecipe("remote_multiblock_controller", REMOTE_MULTIBLOCK_CONTROLLER.getStackForm(), "SCS", "EDE", "SES",
                'S', MetaItems.SENSOR_ZPM.getStackForm(),
                'E', MetaItems.EMITTER_ZPM.getStackForm(),
                'C', new UnificationEntry(OrePrefix.circuit, Superconductor),
                'D', GATileEntities.CENTRAL_MONITOR.getStackForm());

        ModHandler.addShapedRecipe("compressed_chest", COMPRESSED_CHEST.getStackForm(), "OCO", "PKP", "OCO",
                'O', new UnificationEntry(OrePrefix.block, Obsidian),
                'C', new ItemStack(Item.getByNameOrId("actuallyadditions:block_giant_chest_large")),
                'K', new ItemStack(Item.getByNameOrId("actuallyadditions:item_crate_keeper")),
                'P', CraftingComponent.PISTON.getIngredient(2));

        ModHandler.addShapedRecipe("compressed_crate", COMPRESSED_CRATE.getStackForm(), "OPO", "CKC", "OPO",
                'O', new UnificationEntry(OrePrefix.block, Obsidian),
                'C', new ItemStack(Item.getByNameOrId("actuallyadditions:block_giant_chest_large")),
                'K', new ItemStack(Item.getByNameOrId("actuallyadditions:item_crate_keeper")),
                'P', CraftingComponent.PISTON.getIngredient(2));

        ModHandler.addShapedRecipe("tj_toolbox", TOOLBOX.getStackForm(), "RCR", "PSP", "PPP",
                'R', new UnificationEntry(OrePrefix.stickLong, RedSteel),
                'C', new UnificationEntry(OrePrefix.circuit, Advanced),
                'S', STAINLESS_STEEL_CHEST.getStackForm(),
                'P', new UnificationEntry(OrePrefix.plate, RedSteel));

        ModHandler.addShapedRecipe("large_solar_boiler", LARGE_SOLAR_BOILER.getStackForm(), "WCW", "CSC", "WCW",
                'W', new UnificationEntry(OrePrefix.cableGtSingle, Tin),
                'C', new UnificationEntry(OrePrefix.circuit, Basic),
                'S', SOLAR_BOILER[2].getStackForm());

        ModHandler.addShapedRecipe("solar_collector", TJMetaBlocks.ABILITY_BLOCKS.getItemVariant(AbilityBlocks.AbilityType.SOLAR_COLLECTOR), "GGG", "DDD", "PSP",
                'G', new ItemStack(Blocks.GLASS),
                'D', new UnificationEntry(OrePrefix.plateDense, Silver),
                'P', new UnificationEntry(OrePrefix.pipeLarge, Steel),
                'S', HULL[1].getStackForm());

        for (int i = 0; i < 2; i++) {
            ModHandler.addShapedRecipe("filing_cabinet." + i, FILING_CABINET.getStackForm(), "NCN", "PSP", "NCN",
                    'N', new UnificationEntry(OrePrefix.plateDense, StainlessSteel),
                    'C', CraftingComponent.CIRCUIT.getIngredient(3),
                    'P', CraftingComponent.PISTON.getIngredient(3),
                    'S', i == 0 ? COMPRESSED_CHEST.getStackForm() : COMPRESSED_CRATE.getStackForm());
        }

        for (int i = 0, tier = 1; i < CRAFTER.length; i++, tier++) {
            ItemStack craftingTable = new ItemStack(Blocks.CRAFTING_TABLE);
            ModHandler.addShapedRecipe("crafter_" + GAValues.VN[tier], CRAFTER[i].getStackForm(), "CTC", "RSR", "EWE",
                    'C', GACraftingComponents.CONVEYOR.getIngredient(tier),
                    'T', craftingTable,
                    'R', GACraftingComponents.ROBOT_ARM.getIngredient(tier),
                    'S', tier == 14 ? HULL[9].getStackForm() : tier < 9 ? HULL[tier].getStackForm() : GA_HULLS[tier - 9].getStackForm(),
                    'E', GACraftingComponents.CIRCUIT.getIngredient(tier),
                    'W', GACraftingComponents.CABLE_SINGLE.getIngredient(tier));
            ModHandler.addShapedRecipe("crafter_hatch_" + GAValues.VN[tier], CRAFTER_HATCHES[i].getStackForm(), " T ", " S ", "   ",
                    'T', craftingTable,
                    'S', CRAFTER[i].getStackForm());
        }

        for (int i = 0; i < FILTERED_INPUT_BUSES.length; i++) {
            Object motor = i == 0 ? new ItemStack(Item.getByNameOrId("contenttweaker:steammotor")) : GACraftingComponents.MOTOR.getIngredient(i);
            ModHandler.addShapedRecipe("filtered_input_bus." + GAValues.VN[i], FILTERED_INPUT_BUSES[i].getStackForm(), " F ", "MHM", " F ",
                    'F', MetaItems.ITEM_FILTER.getStackForm(),
                    'H', ITEM_IMPORT_BUS[i].getStackForm(),
                    'M', motor);
            ModHandler.addShapedRecipe("filtered_output_bus." + GAValues.VN[i], FILTERED_OUTPUT_BUSES[i].getStackForm(), " F ", "MHM", " F ",
                    'F', MetaItems.ITEM_FILTER.getStackForm(),
                    'H', ITEM_EXPORT_BUS[i].getStackForm(),
                    'M', motor);
        }

        for (int i = 10; i < ITEM_IMPORT_BUS.length; i++) {
            ModHandler.addShapedRecipe("input_bus." + GAValues.VN[i], ITEM_IMPORT_BUS[i].getStackForm(), " C", " H",
                    'H', TJMetaTileEntities.getHull(i).getStackForm(),
                    'C', new ItemStack(Blocks.CHEST));
            ModHandler.addShapedRecipe("output_bus." + GAValues.VN[i], ITEM_EXPORT_BUS[i].getStackForm(), " H", " C",
                    'H', TJMetaTileEntities.getHull(i).getStackForm(),
                    'C', new ItemStack(Blocks.CHEST));
            ModHandler.addShapedRecipe("input_hatch." + GAValues.VN[i], FLUID_IMPORT_HATCH[i].getStackForm(), " G", " H",
                    'H', TJMetaTileEntities.getHull(i).getStackForm(),
                    'G', new ItemStack(Blocks.GLASS));
            ModHandler.addShapedRecipe("output_hatch." + GAValues.VN[i], FLUID_EXPORT_HATCH[i].getStackForm(), " H", " G",
                    'H', TJMetaTileEntities.getHull(i).getStackForm(),
                    'G', new ItemStack(Blocks.GLASS));
        }

        for (int i = 0; i < UNIVERSAL_CIRCUITS.length; i++) {
            ModHandler.addShapelessRecipe(GAValues.VN[i].toLowerCase() + "_universal_circuit", UNIVERSAL_CIRCUITS[i].getStackForm(), new UnificationEntry(OrePrefix.circuit, CIRCUIT_TIERS[i]));

            PACKER_RECIPES.recipeBuilder()
                    .input(OrePrefix.circuit, CIRCUIT_TIERS[i])
                    .notConsumable(new IntCircuitIngredient(0))
                    .outputs(UNIVERSAL_CIRCUITS[i].getStackForm())
                    .EUt(2)
                    .duration(20)
                    .buildAndRegister();
        }

        for (int i = 0; i < aeCells.length; i++) {
            ModHandler.addShapelessRecipe("ae_cell_simple." + aeCells[i].getTranslationKey(), aeCells[i], aeMaterials.emptyStorageCell().maybeStack(1).get(), aeCellParts[i]);
            ModHandler.addShapedRecipe("ae_cell." + aeCells[i].getTranslationKey(), aeCells[i], "QRQ", "RCR", "III",
                    'Q', aeBlocks.quartzGlass().maybeStack(1).get(),
                    'I', new ItemStack(Items.IRON_INGOT),
                    'R', new ItemStack(Items.REDSTONE),
                    'C', aeCellParts[i]);
        }
        for (int i = 0; i < 2; i++) {
            ASSEMBLER_RECIPES.recipeBuilder()
                    .circuitMeta(1)
                    .input(OrePrefix.circuit, CIRCUIT_TIERS[i + 9])
                    .input(OrePrefix.gemExquisite, Amethyst, 4)
                    .input(OrePrefix.plate, MATERIAL_TIER[0][i + 9], 4)
                    .inputs(GAMetaItems.UNSTABLE_STAR.getStackForm(), GAMetaItems.OPTICAL_PROCESSING_CORE.getStackForm(4), GAMetaItems.OPTICAL_SOC.getStackForm(16))
                    .outputs(i == 0 ? aeCellParts[0] : aeCellParts[4])
                    .duration(300).EUt(GAValues.VA[9])
                    .buildAndRegister();
        }
        for (int i = 0; i < 4; i++) {
            ItemStack aeCellParts2 = i == 0 ? new ItemStack(Item.getByNameOrId("nae2:material"), 3, 4) : aeCellParts[i - 1].copy();
            aeCellParts2.setCount(3);
            ASSEMBLER_RECIPES.recipeBuilder()
                    .circuitMeta(1)
                    .input(OrePrefix.circuit, CIRCUIT_TIERS[i + 9])
                    .input(OrePrefix.gemExquisite, Amethyst, 4)
                    .input(OrePrefix.plate, MATERIAL_TIER[0][i + 9], 4)
                    .inputs(aeCellParts2, GAMetaItems.OPTICAL_PROCESSING_CORE.getStackForm(4))
                    .outputs(aeCellParts[i])
                    .duration(300).EUt(GAValues.VA[Math.min(11, i + 9)])
                    .buildAndRegister();
            aeCellParts2 = i == 0 ? new ItemStack(Item.getByNameOrId("nae2:material"), 3, 8) : aeCellParts[i + 3].copy();
            aeCellParts2.setCount(3);
            ASSEMBLER_RECIPES.recipeBuilder()
                    .circuitMeta(1)
                    .input(OrePrefix.circuit, CIRCUIT_TIERS[i + 9])
                    .input(OrePrefix.gemExquisite, Amethyst, 4)
                    .input(OrePrefix.plate, MATERIAL_TIER[0][i + 9], 4)
                    .inputs(aeCellParts2, GAMetaItems.OPTICAL_PROCESSING_CORE.getStackForm(4))
                    .outputs(aeCellParts[i + 4])
                    .duration(300).EUt(GAValues.VA[Math.min(11, i + 9)])
                    .buildAndRegister();
        }

        BlockFusionGlass.GlassType[] fusionGlass = BlockFusionGlass.GlassType.values();
        ItemStack[] fusionCasing = {MetaBlocks.MUTLIBLOCK_CASING.getItemVariant(BlockMultiblockCasing.MultiblockCasingType.FUSION_CASING), MetaBlocks.MUTLIBLOCK_CASING.getItemVariant(BlockMultiblockCasing.MultiblockCasingType.FUSION_CASING_MK2),
                GAMetaBlocks.FUSION_CASING.getItemVariant(GAFusionCasing.CasingType.FUSION_3), TJMetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasings.FusionType.FUSION_CASING_UHV),
                TJMetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasings.FusionType.FUSION_CASING_UEV)};
        for (int i = 0; i < fusionGlass.length; i++) {
            ModHandler.addShapelessRecipe("fusion_glass" + fusionCasing[i].getTranslationKey(), TJMetaBlocks.FUSION_GLASS.getItemVariant(fusionGlass[i]), fusionCasing[i], new ItemStack(Blocks.GLASS));
            ModHandler.addShapelessRecipe("fusion_casing" + fusionCasing[i].getTranslationKey(), fusionCasing[i], TJMetaBlocks.FUSION_GLASS.getItemVariant(fusionGlass[i]));
        }
    }

    public static ItemStack getEnergyHatch(int tier, boolean isOutput) {
        switch (tier) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8: return isOutput ? MetaTileEntities.ENERGY_OUTPUT_HATCH[tier].getStackForm() : MetaTileEntities.ENERGY_INPUT_HATCH[tier].getStackForm();
            case 9:
            case 10:
            case 11:
            case 12:
            case 13: return isOutput ? GATileEntities.ENERGY_OUTPUT[tier - 9].getStackForm() : GATileEntities.ENERGY_INPUT[tier - 9].getStackForm();
            case 14: return isOutput ? MetaTileEntities.ENERGY_OUTPUT_HATCH[9].getStackForm() : MetaTileEntities.ENERGY_INPUT_HATCH[9].getStackForm();
            default: return isOutput ? MetaTileEntities.ENERGY_OUTPUT_HATCH[0].getStackForm() : MetaTileEntities.ENERGY_INPUT_HATCH[0].getStackForm();
        }
    }
}
