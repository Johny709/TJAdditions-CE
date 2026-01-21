package tj.machines;

import gregicadditions.GAValues;
import gregicadditions.machines.multi.multiblockpart.GAMetaTileEntityEnergyHatch;
import gregicadditions.machines.multi.nuclear.MetaTileEntityHotCoolantTurbine;
import gregicadditions.recipes.GARecipeMaps;
import gregtech.api.GregTechAPI;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.MetaTileEntityLargeBoiler;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeTurbine;
import net.minecraft.util.ResourceLocation;
import tj.TJ;
import tj.TJConfig;
import tj.machines.multi.electric.*;
import tj.machines.multi.parallel.*;
import tj.machines.multi.steam.*;
import tj.machines.singleblock.*;
import tj.multiblockpart.ender.MetaTileEntityEnderEnergyHatch;
import tj.multiblockpart.rotorholder.MetaTileEntityRotorHolderForNuclearCoolantUHVPlus;
import tj.multiblockpart.rotorholder.MetaTileEntityRotorHolderUHVPlus;
import tj.multiblockpart.utility.*;

import java.util.Arrays;

import static gregicadditions.machines.GATileEntities.*;
import static tj.machines.multi.electric.MetaTileEntityLargeWirelessEnergyEmitter.TransferType.INPUT;
import static tj.machines.multi.electric.MetaTileEntityLargeWirelessEnergyEmitter.TransferType.OUTPUT;
import static tj.machines.singleblock.BoilerType.*;

public class TJMetaTileEntities {

    public static MetaTileEntityPrimitiveAlloy PRIMITIVE_ALLOY;
    public static MetaTileEntityCokeOven COKE_OVEN;
    public static MetaTileEntityMegaCokeOven MEGA_COKE_OVEN;
    public static MetaTileEntityHeatExchanger HEAT_EXCHANGER;
    public static MetaTileEntityArmorInfuser ARMOR_INFUSER;
    public static MetaTileEntityDragonReplicator DRAGON_REPLICATOR;
    public static MetaTileEntityChaosReplicator CHAOS_REPLICATOR;
    public static MetaTileEntityLargePoweredSpawner LARGE_POWERED_SPAWNER;
    public static MetaTileEntityLargeVialProcessor LARGE_VIAL_PROCESSOR;
    public static MetaTileEntityMegaBoiler[] MEGA_BOILER = new MetaTileEntityMegaBoiler[4];
    public static MetaTileEntityXLTurbine XL_STEAM_TURBINE;
    public static MetaTileEntityXLTurbine XL_GAS_TURBINE;
    public static MetaTileEntityXLTurbine XL_PLASMA_TURBINE;
    public static MetaTileEntityXLHotCoolantTurbine XL_COOLANT_TURBINE;
    public static MetaTileEntityRotorHolderUHVPlus ROTOR_HOLDER_UMV;
    public static MetaTileEntityRotorHolderForNuclearCoolantUHVPlus COOLANT_ROTOR_HOLDER_UMV;
    public static MetaTileEntityLargeDecayChamber LARGE_DECAY_CHAMBER;
    public static MetaTileEntityLargeAlloySmelter LARGE_ALLOY_SMELTER;
    public static MetaTileEntityIndustrialFusionReactor INDUSTRIAL_FUSION_REACTOR_LUV;
    public static MetaTileEntityIndustrialFusionReactor INDUSTRIAL_FUSION_REACTOR_ZPM;
    public static MetaTileEntityIndustrialFusionReactor INDUSTRIAL_FUSION_REACTOR_UV;
    public static MetaTileEntityIndustrialFusionReactor INDUSTRIAL_FUSION_REACTOR_UHV;
    public static MetaTileEntityIndustrialFusionReactor INDUSTRIAL_FUSION_REACTOR_UEV;
    public static MetaTileEntityParallelLargeChemicalReactor PARALLEL_CHEMICAL_REACTOR;
    public static MetaTileEntityTJMultiFluidHatch QUADRUPLE_QUADRUPLE_INPUT_HATCH;
    public static MetaTileEntityTJMultiFluidHatch QUADRUPLE_QUADRUPLE_OUTPUT_HATCH;
    public static MetaTileEntityLargeGreenhouse LARGE_GREENHOUSE;
    public static MetaTileEntityLargeArchitectWorkbench LARGE_ARCHITECT_WORKBENCH;
    public static MetaTileEntityMachineController MACHINE_CONTROLLER;
    public static MetaTileEntityEliteLargeMiner ELITE_LARGE_MINER;
    public static MetaTileEntityUltimateLargeMiner ULTIMATE_LARGE_MINER;
    public static MetaTileEntityWorldDestroyer WORLD_DESTROYER;
    public static MetaTileEntityLargeWorldAccelerator LARGE_WORLD_ACCELERATOR;
    public static MetaTileEntityLargeRockBreaker LARGE_ROCK_BREAKER;
    public static MetaTileEntityInfiniteFluidDrill INFINITE_FLUID_DRILL;
    public static MetaTileEntityIndustrialSteamEngine INDUSTRIAL_STEAM_ENGINE;
    public static MetaTileEntityParallelAdvancedLargeChemicalReactor ADVANCED_PARALLEL_CHEMICAL_REACTOR;
    public static MetaTileEntityWaterReservoirHatch WATER_RESERVOIR_HATCH;
    public static MetaTileEntityParallelLargeMacerator PARALLEL_LARGE_MACERATOR;
    public static MetaTileEntityParallelLargeWashingMachine PARALLEL_LARGE_WASHING_MACHINE;
    public static MetaTileEntityParallelLargeCentrifuge PARALLEL_LARGE_CENTRIFUGE;
    public static MetaTileEntityParallelLargeElectrolyzer PARALLEL_LARGE_ELECTROLYZER;
    public static MetaTileEntityParallelLargeSifter PARALLEL_LARGE_SIFTER;
    public static MetaTileEntityParallelLargeBrewery PARALLEL_LARGE_BREWERY;
    public static MetaTileEntityParallelLargeArcFurnace PARALLEL_LARGE_ARC_FURNACE;
    public static MetaTileEntityParallelLargeAssembler PARALLEL_LARGE_ASSEMBLER;
    public static MetaTileEntityParallelLargeBendingAndForming PARALLEL_LARGE_BENDING_AND_FORMING;
    public static MetaTileEntityParallelLargeCanningMachine PARALLEL_LARGE_CANNING_MACHINE;
    public static MetaTileEntityParallelLargeCuttingMachine PARALLEL_LARGE_CUTTING_MACHINE;
    public static MetaTileEntityParallelLargeElectromagnet PARALLEL_LARGE_ELECTROMAGNET;
    public static MetaTileEntityParallelLargeExtractor PARALLEL_LARGE_EXTRACTOR;
    public static MetaTileEntityParallelLargeExtruder PARALLEL_LARGE_EXTRUDER;
    public static MetaTileEntityParallelLargeMixer PARALLEL_LARGE_MIXER;
    public static MetaTileEntityParallelLargeForgeHammer PARALLEL_LARGE_FORGE_HAMMER;
    public static MetaTileEntityParallelLargeLaserEngraver PARALLEL_LARGE_LASER_ENGRAVER;
    public static MetaTileEntityParallelLargePackager PARALLEL_LARGE_PACKAGER;
    public static MetaTileEntityParallelLargeWiremill PARALLEL_LARGE_WIREMILL;
    public static MetaTileEntityParallelPlasmaCondenser PARALLEL_PLASMA_CONDENSER;
    public static MetaTileEntityParallelAlloyBlastSmelter PARALLEL_ALLOY_BLAST_FURNACE;
    public static MetaTileEntityParallelElectricBlastFurnace PARALLEL_ELECTRIC_BLAST_FURNACE;
    public static MetaTileEntityParallelVacuumFreezer PARALLEL_VACUUM_FREEZER;
    public static MetaTileEntityParallelCryogenicFreezer PARALLEL_CRYOGENIC_FREEZER;
    public static MetaTileEntityParallelVolcanus PARALLEL_VOLCANUS;
    public static MetaTileEntityAcceleratorAnchorPoint ACCELERATOR_ANCHOR_POINT;
    public static MetaTileEntityLargeWirelessEnergyEmitter LARGE_WIRELESS_ENERGY_EMITTER;
    public static MetaTileEntityLargeWirelessEnergyReceiver LARGE_WIRELESS_ENERGY_RECEIVER;
    public static MetaTileEntityLargeBatteryCharger LARGE_BATTERY_CHARGER;
    public static MetaTileEntityVoidMOreMiner VOID_MORE_MINER;
    public static MetaTileEntityTeleporter TELEPORTER;
    public static MetaTileEntityLargeChiselWorkbench LARGE_CHISEL_WORKBENCH;
    public static MetaTileEntityLargeEnchanter LARGE_ENCHANTER;
    public static MetaTileEntityLargeCrafter LARGE_CRAFTER;
    public static MetaTileEntityMegaFusion MEGA_FUSION;
    public static MetaTileEntityEnderBatteryTower ENDER_BATTERY_TOWER;
    public static MetaTileEntityLargeSolarBoiler LARGE_SOLAR_BOILER;
    public static MetaTileEntityLargeSolarBoiler MEGA_SOLAR_BOILER;
    public static MetaTileEntityLargeImplosionCompressor LARGE_IMPLOSION_COMPRESSOR;
    public static MetaTileEntityLargeElectricImplosionCompressor LARGE_ELECTRIC_IMPLOSION_COMPRESSOR;
    public static MetaTileEntityCompressedChest COMPRESSED_CHEST;
    public static MetaTileEntityCompressedCrate COMPRESSED_CRATE;
    public static MetaTileEntityCompressedChest INFINITY_CHEST;
    public static MetaTileEntityCompressedCrate INFINITY_CRATE;
    public static MetaTileEntityFilingCabinet FILING_CABINET;
    public static final GAMetaTileEntityEnergyHatch[] ENERGY_INPUT_HATCH_256A = new GAMetaTileEntityEnergyHatch[14];
    public static final GAMetaTileEntityEnergyHatch[] ENERGY_OUTPUT_HATCH_256A = new GAMetaTileEntityEnergyHatch[14];
    public static final MetaTileEntityLargeAtmosphereCollector[] LARGE_ATMOSPHERE_COLLECTOR = new MetaTileEntityLargeAtmosphereCollector[3];
    public static final MetaTileEntityCoalBoiler[] COAL_BOILER = new MetaTileEntityCoalBoiler[3];
    public static final MetaTileEntitySolarBoiler[] SOLAR_BOILER = new MetaTileEntitySolarBoiler[3];
    public static final MetaTileEntityFluidBoiler[] FLUID_BOILER = new MetaTileEntityFluidBoiler[3];
    public static final MetaTileEntityArchitectWorkbench[] ARCHITECT_WORKBENCH = new MetaTileEntityArchitectWorkbench[14];
    public static final MetaTileEntityChiselWorkbench[] CHISEL_WORKBENCH = new MetaTileEntityChiselWorkbench[14];
    public static final MetaTileEntityEnchanter[] ENCHANTER = new MetaTileEntityEnchanter[14];
    public static final MetaTileEntityCrafter[] CRAFTER = new MetaTileEntityCrafter[14];
    public static final MetaTileEntityFarmingStation[] FARMING_STATION = new MetaTileEntityFarmingStation[14];
    public static final MetaTileEntitySuperItemBus[] SUPER_ITEM_INPUT_BUS = new MetaTileEntitySuperItemBus[5];
    public static final MetaTileEntitySuperItemBus[] SUPER_ITEM_OUTPUT_BUS = new MetaTileEntitySuperItemBus[5];
    public static final MetaTileEntitySuperFluidHatch[] SUPER_FLUID_INPUT_HATCH = new MetaTileEntitySuperFluidHatch[5];
    public static final MetaTileEntitySuperFluidHatch[] SUPER_FLUID_OUTPUT_HATCH = new MetaTileEntitySuperFluidHatch[5];
    public static final MetaTileEntityTJSteamHatch[] STEAM_INPUT_FLUID_HATCH = new MetaTileEntityTJSteamHatch[3];
    public static final MetaTileEntityTJSteamHatch[] STEAM_OUTPUT_FLUID_HATCH = new MetaTileEntityTJSteamHatch[3];
    public static final MetaTileEntityCrafterHatch[] CRAFTER_HATCHES = new MetaTileEntityCrafterHatch[14];
    public static final MetaTileEntityEnderEnergyHatch[] ENDER_ENERGY_INPUT_HATCHES = new MetaTileEntityEnderEnergyHatch[14];
    public static final MetaTileEntityEnderEnergyHatch[] ENDER_ENERGY_OUTPUT_HATCHES = new MetaTileEntityEnderEnergyHatch[14];
    public static final MetaTileEntityFilteredBus[] FILTERED_INPUT_BUSES = new MetaTileEntityFilteredBus[15];
    public static final MetaTileEntityFilteredBus[] FILTERED_OUTPUT_BUSES = new MetaTileEntityFilteredBus[15];

    public static void init() {

        if (TJConfig.machines.replaceCTMultis) {
            COKE_OVEN = GregTechAPI.registerMetaTileEntity(1000, new MetaTileEntityCokeOven(multiblockTweakerId("coke_oven_2")));
            PRIMITIVE_ALLOY = GregTechAPI.registerMetaTileEntity(1002, new MetaTileEntityPrimitiveAlloy(multiblockTweakerId("primitive_alloy")));
            HEAT_EXCHANGER = GregTechAPI.registerMetaTileEntity(1003, new MetaTileEntityHeatExchanger(multiblockTweakerId("heat_exchanger")));
            ARMOR_INFUSER = GregTechAPI.registerMetaTileEntity(1004, new MetaTileEntityArmorInfuser(multiblockTweakerId("armor_infuser")));
            CHAOS_REPLICATOR = GregTechAPI.registerMetaTileEntity(1005, new MetaTileEntityChaosReplicator(multiblockTweakerId("chaos_replicator")));
            DRAGON_REPLICATOR = GregTechAPI.registerMetaTileEntity(1006, new MetaTileEntityDragonReplicator(multiblockTweakerId("dragon_egg_replicator")));
            LARGE_POWERED_SPAWNER = GregTechAPI.registerMetaTileEntity(4201, new MetaTileEntityLargePoweredSpawner(multiblockTweakerId("large_powered_spawner")));
            LARGE_VIAL_PROCESSOR = GregTechAPI.registerMetaTileEntity(4202, new MetaTileEntityLargeVialProcessor(multiblockTweakerId("large_vial_processor")));
        }
        MEGA_COKE_OVEN = GregTechAPI.registerMetaTileEntity(4205, new MetaTileEntityMegaCokeOven(TJId("mega_coke_oven")));

        XL_STEAM_TURBINE = GregTechAPI.registerMetaTileEntity(4206, new MetaTileEntityXLTurbine(TJId("xl_turbine.steam"), MetaTileEntityLargeTurbine.TurbineType.STEAM));
        XL_GAS_TURBINE = GregTechAPI.registerMetaTileEntity(4207, new MetaTileEntityXLTurbine(TJId("xl_turbine.gas"), MetaTileEntityLargeTurbine.TurbineType.GAS));
        XL_PLASMA_TURBINE = GregTechAPI.registerMetaTileEntity(4208, new MetaTileEntityXLTurbine(TJId("xl_turbine.plasma"), MetaTileEntityLargeTurbine.TurbineType.PLASMA));
        XL_COOLANT_TURBINE = GregTechAPI.registerMetaTileEntity(4209, new MetaTileEntityXLHotCoolantTurbine(TJId("xl_turbine.coolant"), MetaTileEntityHotCoolantTurbine.TurbineType.HOT_COOLANT));

        LARGE_GREENHOUSE = GregTechAPI.registerMetaTileEntity(5000, new MetaTileEntityLargeGreenhouse(TJId("large_greenhouse"), GARecipeMaps.GREEN_HOUSE_RECIPES));
        LARGE_DECAY_CHAMBER = GregTechAPI.registerMetaTileEntity(5001, new MetaTileEntityLargeDecayChamber(TJId("large_decay_chamber")));
        LARGE_ALLOY_SMELTER = GregTechAPI.registerMetaTileEntity(5002, new MetaTileEntityLargeAlloySmelter(TJId("large_alloy_smelter")));

        INDUSTRIAL_FUSION_REACTOR_LUV = GregTechAPI.registerMetaTileEntity(5003, new MetaTileEntityIndustrialFusionReactor(TJId("industrial_fusion_reactor.luv"), 6));
        INDUSTRIAL_FUSION_REACTOR_ZPM = GregTechAPI.registerMetaTileEntity(5004, new MetaTileEntityIndustrialFusionReactor(TJId("industrial_fusion_reactor.zpm"), 7));
        INDUSTRIAL_FUSION_REACTOR_UV = GregTechAPI.registerMetaTileEntity(5005, new MetaTileEntityIndustrialFusionReactor(TJId("industrial_fusion_reactor.uv"), 8));
        INDUSTRIAL_FUSION_REACTOR_UHV = GregTechAPI.registerMetaTileEntity(5006, new MetaTileEntityIndustrialFusionReactor(TJId("industrial_fusion_reactor.uhv"), 9));
        INDUSTRIAL_FUSION_REACTOR_UEV = GregTechAPI.registerMetaTileEntity(5007, new MetaTileEntityIndustrialFusionReactor(TJId("industrial_fusion_reactor.uev"), 10));

        QUADRUPLE_QUADRUPLE_INPUT_HATCH = GregTechAPI.registerMetaTileEntity(5008, new MetaTileEntityTJMultiFluidHatch(TJId("fluid_input_quad_quad_luv"), 4, false, 64000));
        QUADRUPLE_QUADRUPLE_OUTPUT_HATCH = GregTechAPI.registerMetaTileEntity(5009, new MetaTileEntityTJMultiFluidHatch(TJId("fluid_output_quad_quad_luv"), 4, true, 64000));
        MACHINE_CONTROLLER = GregTechAPI.registerMetaTileEntity(5010, new MetaTileEntityMachineController(TJId("machine_controller")));
        ELITE_LARGE_MINER = GregTechAPI.registerMetaTileEntity(5011, new MetaTileEntityEliteLargeMiner(TJId("elite_large_miner"), TJMiner.Type.ELITE));
        ULTIMATE_LARGE_MINER = GregTechAPI.registerMetaTileEntity(5012, new MetaTileEntityUltimateLargeMiner(TJId("ultimate_large_miner"), TJMiner.Type.ULTIMATE));
        WORLD_DESTROYER = GregTechAPI.registerMetaTileEntity(5013, new MetaTileEntityWorldDestroyer(TJId("world_destroyer"), TJMiner.Type.DESTROYER));

        PARALLEL_CHEMICAL_REACTOR = GregTechAPI.registerMetaTileEntity(5014, new MetaTileEntityParallelLargeChemicalReactor(TJId("parallel_chemical_reactor")));
        LARGE_ARCHITECT_WORKBENCH = GregTechAPI.registerMetaTileEntity(5015, new MetaTileEntityLargeArchitectWorkbench(TJId("large_architect_workbench")));
        LARGE_WORLD_ACCELERATOR = GregTechAPI.registerMetaTileEntity(5052, new MetaTileEntityLargeWorldAccelerator(TJId("large_world_accelerator")));
        LARGE_ROCK_BREAKER = GregTechAPI.registerMetaTileEntity(5053, new MetaTileEntityLargeRockBreaker(TJId("large_rock_breaker")));

        MEGA_BOILER[0] = GregTechAPI.registerMetaTileEntity(5054, new MetaTileEntityMegaBoiler(TJId("mega_bronze_boiler"), MetaTileEntityLargeBoiler.BoilerType.BRONZE, 512));
        MEGA_BOILER[1] = GregTechAPI.registerMetaTileEntity(5055, new MetaTileEntityMegaBoiler(TJId("mega_steel_boiler"), MetaTileEntityLargeBoiler.BoilerType.STEEL, 512));
        MEGA_BOILER[2] = GregTechAPI.registerMetaTileEntity(5056, new MetaTileEntityMegaBoiler(TJId("mega_titanium_boiler"), MetaTileEntityLargeBoiler.BoilerType.TITANIUM, 512));
        MEGA_BOILER[3] = GregTechAPI.registerMetaTileEntity(5057, new MetaTileEntityMegaBoiler(TJId("mega_tungstensteel_boiler"), MetaTileEntityLargeBoiler.BoilerType.TUNGSTENSTEEL, 512));

        LARGE_ATMOSPHERE_COLLECTOR[0] = GregTechAPI.registerMetaTileEntity(5078, new MetaTileEntityLargeAtmosphereCollector(TJId("steam_air_collector_turbine"), MetaTileEntityLargeTurbine.TurbineType.STEAM));
        LARGE_ATMOSPHERE_COLLECTOR[1] = GregTechAPI.registerMetaTileEntity(5079, new MetaTileEntityLargeAtmosphereCollector(TJId("gas_air_collector_turbine"), MetaTileEntityLargeTurbine.TurbineType.GAS));
        LARGE_ATMOSPHERE_COLLECTOR[2] = GregTechAPI.registerMetaTileEntity(5080, new MetaTileEntityLargeAtmosphereCollector(TJId("plasma_air_collector_turbine"), MetaTileEntityLargeTurbine.TurbineType.PLASMA));

        INFINITE_FLUID_DRILL = GregTechAPI.registerMetaTileEntity(5081, new MetaTileEntityInfiniteFluidDrill(TJId("infinite_fluid_drill")));
        INDUSTRIAL_STEAM_ENGINE = GregTechAPI.registerMetaTileEntity(5082, new MetaTileEntityIndustrialSteamEngine(TJId("industrial_steam_engine")));
        ADVANCED_PARALLEL_CHEMICAL_REACTOR = GregTechAPI.registerMetaTileEntity(5083, new MetaTileEntityParallelAdvancedLargeChemicalReactor(TJId("advanced_parallel_chemical_reactor")));
        WATER_RESERVOIR_HATCH = GregTechAPI.registerMetaTileEntity(5084, new MetaTileEntityWaterReservoirHatch(TJId("water_reservoir"), 9));

        // range 5085 - 5126 -> parallel
        PARALLEL_LARGE_MACERATOR = GregTechAPI.registerMetaTileEntity(5085, new MetaTileEntityParallelLargeMacerator(TJId("parallel_large_macerator")));
        PARALLEL_LARGE_WASHING_MACHINE = GregTechAPI.registerMetaTileEntity(5086, new MetaTileEntityParallelLargeWashingMachine(TJId("parallel_large_washing_machine")));
        PARALLEL_LARGE_CENTRIFUGE = GregTechAPI.registerMetaTileEntity(5087, new MetaTileEntityParallelLargeCentrifuge(TJId("parallel_large_centrifuge")));
        PARALLEL_LARGE_ELECTROLYZER = GregTechAPI.registerMetaTileEntity(5088, new MetaTileEntityParallelLargeElectrolyzer(TJId("parallel_large_electrolyzer")));
        PARALLEL_LARGE_SIFTER = GregTechAPI.registerMetaTileEntity(5089, new MetaTileEntityParallelLargeSifter(TJId("parallel_large_sifter")));
        PARALLEL_LARGE_BREWERY = GregTechAPI.registerMetaTileEntity(5090, new MetaTileEntityParallelLargeBrewery(TJId("parallel_large_brewery")));
        PARALLEL_LARGE_ARC_FURNACE = GregTechAPI.registerMetaTileEntity(5091, new MetaTileEntityParallelLargeArcFurnace(TJId("parallel_large_arc_furnace")));
        PARALLEL_LARGE_ASSEMBLER = GregTechAPI.registerMetaTileEntity(5092, new MetaTileEntityParallelLargeAssembler(TJId("parallel_large_assembler")));
        PARALLEL_LARGE_BENDING_AND_FORMING = GregTechAPI.registerMetaTileEntity(5093, new MetaTileEntityParallelLargeBendingAndForming(TJId("parallel_large_bending_and_forming")));
        PARALLEL_LARGE_CANNING_MACHINE = GregTechAPI.registerMetaTileEntity(5094, new MetaTileEntityParallelLargeCanningMachine(TJId("parallel_large_canning_machine")));
        PARALLEL_LARGE_CUTTING_MACHINE = GregTechAPI.registerMetaTileEntity(5095, new MetaTileEntityParallelLargeCuttingMachine(TJId("parallel_large_cutting_machine")));
        PARALLEL_LARGE_ELECTROMAGNET = GregTechAPI.registerMetaTileEntity(5096, new MetaTileEntityParallelLargeElectromagnet(TJId("parallel_large_electromagnet")));
        PARALLEL_LARGE_EXTRACTOR = GregTechAPI.registerMetaTileEntity(5097, new MetaTileEntityParallelLargeExtractor(TJId("parallel_large_extractor")));
        PARALLEL_LARGE_EXTRUDER = GregTechAPI.registerMetaTileEntity(5098, new MetaTileEntityParallelLargeExtruder(TJId("parallel_large_extruder")));
        PARALLEL_LARGE_FORGE_HAMMER = GregTechAPI.registerMetaTileEntity(5099, new MetaTileEntityParallelLargeForgeHammer(TJId("parallel_large_forge_hammer")));
        PARALLEL_LARGE_LASER_ENGRAVER = GregTechAPI.registerMetaTileEntity(5100, new MetaTileEntityParallelLargeLaserEngraver(TJId("parallel_large_laser_engraver")));
        PARALLEL_LARGE_MIXER = GregTechAPI.registerMetaTileEntity(5101, new MetaTileEntityParallelLargeMixer(TJId("parallel_large_mixer")));
        PARALLEL_LARGE_PACKAGER = GregTechAPI.registerMetaTileEntity(5102, new MetaTileEntityParallelLargePackager(TJId("parallel_large_packager")));
        PARALLEL_LARGE_WIREMILL = GregTechAPI.registerMetaTileEntity(5103, new MetaTileEntityParallelLargeWiremill(TJId("parallel_large_wiremill")));
        PARALLEL_PLASMA_CONDENSER = GregTechAPI.registerMetaTileEntity(5104, new MetaTileEntityParallelPlasmaCondenser(TJId("parallel_plasma_condenser")));
        PARALLEL_ALLOY_BLAST_FURNACE = GregTechAPI.registerMetaTileEntity(5105, new MetaTileEntityParallelAlloyBlastSmelter(TJId("parallel_alloy_blast_smelter")));
        PARALLEL_ELECTRIC_BLAST_FURNACE = GregTechAPI.registerMetaTileEntity(5106, new MetaTileEntityParallelElectricBlastFurnace(TJId("parallel_electric_blast_furnace")));
        PARALLEL_VACUUM_FREEZER = GregTechAPI.registerMetaTileEntity(5107, new MetaTileEntityParallelVacuumFreezer(TJId("parallel_vacuum_freezer")));
        PARALLEL_CRYOGENIC_FREEZER = GregTechAPI.registerMetaTileEntity(5108, new MetaTileEntityParallelCryogenicFreezer(TJId("parallel_cryogenic_freezer")));
        PARALLEL_VOLCANUS = GregTechAPI.registerMetaTileEntity(5109, new MetaTileEntityParallelVolcanus(TJId("parallel_volcanus")));

        VOID_MORE_MINER = GregTechAPI.registerMetaTileEntity(5127, new MetaTileEntityVoidMOreMiner(TJId("void_more_miner")));
        TELEPORTER = GregTechAPI.registerMetaTileEntity(5128, new MetaTileEntityTeleporter(TJId("teleporter")));
        LARGE_CHISEL_WORKBENCH = GregTechAPI.registerMetaTileEntity(5129, new MetaTileEntityLargeChiselWorkbench(TJId("large_chisel_workbench")));
        LARGE_ENCHANTER = GregTechAPI.registerMetaTileEntity(5130, new MetaTileEntityLargeEnchanter(TJId("large_enchanter")));

        // range 5300+ -> singleblocks
        COAL_BOILER[0] = GregTechAPI.registerMetaTileEntity(5300, new MetaTileEntityCoalBoiler(TJId("coal_boiler_bronze"), BRONZE));
        COAL_BOILER[1] = GregTechAPI.registerMetaTileEntity(5301, new MetaTileEntityCoalBoiler(TJId("coal_boiler_steel"), STEEL));
        COAL_BOILER[2] = GregTechAPI.registerMetaTileEntity(5302, new MetaTileEntityCoalBoiler(TJId("coal_boiler_lv"), LV));
        SOLAR_BOILER[0] = GregTechAPI.registerMetaTileEntity(5303, new MetaTileEntitySolarBoiler(TJId("solar_boiler_bronze"), BRONZE));
        SOLAR_BOILER[1] = GregTechAPI.registerMetaTileEntity(5304, new MetaTileEntitySolarBoiler(TJId("solar_boiler_steel"), STEEL));
        SOLAR_BOILER[2] = GregTechAPI.registerMetaTileEntity(5305, new MetaTileEntitySolarBoiler(TJId("solar_boiler_lv"), LV));
        FLUID_BOILER[0] = GregTechAPI.registerMetaTileEntity(5306, new MetaTileEntityFluidBoiler(TJId("fluid_boiler_bronze"), BRONZE));
        FLUID_BOILER[1] = GregTechAPI.registerMetaTileEntity(5307, new MetaTileEntityFluidBoiler(TJId("fluid_boiler_steel"), STEEL));
        FLUID_BOILER[2] = GregTechAPI.registerMetaTileEntity(5308, new MetaTileEntityFluidBoiler(TJId("fluid_boiler_lv"), LV));
        for (int i = 0, tier = 1; i < ARCHITECT_WORKBENCH.length; i++, tier++)  // occupies ID range 5309 - 5322
            ARCHITECT_WORKBENCH[i] = GregTechAPI.registerMetaTileEntity(5309 + i, new MetaTileEntityArchitectWorkbench(TJId("architect_workbench_" + GAValues.VN[tier]), tier));
        for (int i = 0, tier = 1; i < CHISEL_WORKBENCH.length; i++, tier++) // occupies ID range 5323 - 5336
            CHISEL_WORKBENCH[i] = GregTechAPI.registerMetaTileEntity(5323 + i, new MetaTileEntityChiselWorkbench(TJId("chisel_workbench_" + GAValues.VN[tier]), tier));
        for (int i = 0, tier = 1; i < ENCHANTER.length; i++, tier++) // occupies ID range 5337 - 5350
            ENCHANTER[i] = GregTechAPI.registerMetaTileEntity(5337 + i, new MetaTileEntityEnchanter(TJId("enchanter_" + GAValues.VN[tier]), tier));
        for (int i = 0, tier = 1; i < CRAFTER.length; i++, tier++) // occupies ID range 5351 - 5364
            CRAFTER[i] = GregTechAPI.registerMetaTileEntity(5351 + i, new MetaTileEntityCrafter(TJId("crafter_" + GAValues.VN[tier]), tier));
        for (int i = 0, tier = 1; i < FARMING_STATION.length; i++, tier++) // occupies ID range 5365 - 5378
            FARMING_STATION[i] = GregTechAPI.registerMetaTileEntity(5365 + i, new MetaTileEntityFarmingStation(TJId("farming_station_" + GAValues.VN[tier]), tier));
        COMPRESSED_CHEST = GregTechAPI.registerMetaTileEntity(5406, new MetaTileEntityCompressedChest(TJId("compressed_chest"), false));
        COMPRESSED_CRATE = GregTechAPI.registerMetaTileEntity(5407, new MetaTileEntityCompressedCrate(TJId("compressed_crate"), false));
        INFINITY_CHEST = GregTechAPI.registerMetaTileEntity(5408, new MetaTileEntityCompressedChest(TJId("infinity_chest"), true));
        INFINITY_CRATE = GregTechAPI.registerMetaTileEntity(5409, new MetaTileEntityCompressedCrate(TJId("infinity_crate"), true));
        FILING_CABINET = GregTechAPI.registerMetaTileEntity(5410, new MetaTileEntityFilingCabinet(TJId("filing_cabinet")));

        // range 5500+ -> misc
        ACCELERATOR_ANCHOR_POINT = GregTechAPI.registerMetaTileEntity(5500, new MetaTileEntityAcceleratorAnchorPoint(TJId("accelerator_anchor_point")));
        ROTOR_HOLDER_UMV = GregTechAPI.registerMetaTileEntity(5501, new MetaTileEntityRotorHolderUHVPlus(TJId("rotor_holder.umv"), GAValues.UMV, 2.5f));
        COOLANT_ROTOR_HOLDER_UMV = GregTechAPI.registerMetaTileEntity(5502, new MetaTileEntityRotorHolderForNuclearCoolantUHVPlus(TJId("coolant_rotor_holder.umv"), GAValues.UMV, 2.5f));
        LARGE_WIRELESS_ENERGY_EMITTER = GregTechAPI.registerMetaTileEntity(5503, new MetaTileEntityLargeWirelessEnergyEmitter(TJId("large_wireless_energy_emitter"), INPUT));
        LARGE_WIRELESS_ENERGY_RECEIVER = GregTechAPI.registerMetaTileEntity(5504, new MetaTileEntityLargeWirelessEnergyReceiver(TJId("large_wireless_energy_receiver"), OUTPUT));
        LARGE_BATTERY_CHARGER = GregTechAPI.registerMetaTileEntity(5505, new MetaTileEntityLargeBatteryCharger(TJId("large_battery_charger")));
        LARGE_CRAFTER = GregTechAPI.registerMetaTileEntity(5506, new MetaTileEntityLargeCrafter(TJId("large_crafter")));
        MEGA_FUSION = GregTechAPI.registerMetaTileEntity(5507, new MetaTileEntityMegaFusion(TJId("mega_fusion")));
        ENDER_BATTERY_TOWER = GregTechAPI.registerMetaTileEntity(5508, new MetaTileEntityEnderBatteryTower(TJId("ender_battery_tower")));
        LARGE_SOLAR_BOILER = GregTechAPI.registerMetaTileEntity(5509, new MetaTileEntityLargeSolarBoiler(TJId("large_solar_boiler"), false));
        MEGA_SOLAR_BOILER = GregTechAPI.registerMetaTileEntity(5510, new MetaTileEntityLargeSolarBoiler(TJId("mega_solar_boiler"), true));
        LARGE_IMPLOSION_COMPRESSOR = GregTechAPI.registerMetaTileEntity(5511, new MetaTileEntityLargeImplosionCompressor(TJId("large_implosion_compressor")));
        LARGE_ELECTRIC_IMPLOSION_COMPRESSOR = GregTechAPI.registerMetaTileEntity(5512, new MetaTileEntityLargeElectricImplosionCompressor(TJId("large_electric_implosion_compressor")));

        // append tiers to existing hatches
        MetaTileEntities.ITEM_IMPORT_BUS = Arrays.copyOf(MetaTileEntities.ITEM_IMPORT_BUS, 15);
        MetaTileEntities.ITEM_EXPORT_BUS = Arrays.copyOf(MetaTileEntities.ITEM_EXPORT_BUS, 15);

        // range 6000+ -> hatches
        int superBusID = 6000; // occupies ID range 6000 - 6019
        for (int i = 0; i < SUPER_ITEM_INPUT_BUS.length; i++) {
            int tier = Math.min(GAValues.MAX, 3 + (3 * i));
            SUPER_ITEM_INPUT_BUS[i] = GregTechAPI.registerMetaTileEntity(superBusID++, new MetaTileEntitySuperItemBus(TJId("super_input_bus." + GAValues.VN[tier]), tier, false));
            SUPER_ITEM_OUTPUT_BUS[i] = GregTechAPI.registerMetaTileEntity(superBusID++, new MetaTileEntitySuperItemBus(TJId("super_output_bus." + GAValues.VN[tier]), tier, true));
            SUPER_FLUID_INPUT_HATCH[i] = GregTechAPI.registerMetaTileEntity(superBusID++, new MetaTileEntitySuperFluidHatch(TJId("super_input_hatch." + GAValues.VN[tier]), tier, false));
            SUPER_FLUID_OUTPUT_HATCH[i] = GregTechAPI.registerMetaTileEntity(superBusID++, new MetaTileEntitySuperFluidHatch(TJId("super_output_hatch." + GAValues.VN[tier]), tier, true));
        }

        int steamHatchID = 6020; // occupies ID range 6020 - 6025
        for (int i = 0; i < STEAM_INPUT_FLUID_HATCH.length; i++) {
            int tier = 3 + (3 * i);
            STEAM_INPUT_FLUID_HATCH[i] = GregTechAPI.registerMetaTileEntity(steamHatchID++, new MetaTileEntityTJSteamHatch(TJId("steam_input_hatch." + GAValues.VN[tier]), tier, false));
            STEAM_OUTPUT_FLUID_HATCH[i] = GregTechAPI.registerMetaTileEntity(steamHatchID++, new MetaTileEntityTJSteamHatch(TJId("steam_output_hatch." + GAValues.VN[tier]), tier, true));
        }

        for (int i = 0, tier = 1; i < CRAFTER.length; i++, tier++) // occupies range 6026 - 6039
            CRAFTER_HATCHES[i] = GregTechAPI.registerMetaTileEntity(6026 + i, new MetaTileEntityCrafterHatch(TJId("crafter_hatch." + GAValues.VN[tier]), tier));
        for (int i = 0, tier = 1; i < ENDER_ENERGY_INPUT_HATCHES.length; i++, tier++) // occupies ID range 6040 - 6053
            ENDER_ENERGY_INPUT_HATCHES[i] = GregTechAPI.registerMetaTileEntity(6040 + i, new MetaTileEntityEnderEnergyHatch(TJId("ender_energy_input_hatch." + GAValues.VN[tier]), tier, false));
        for (int i = 0, tier = 1; i < ENDER_ENERGY_OUTPUT_HATCHES.length; i++, tier++) // occupies ID range 6054 - 6067
            ENDER_ENERGY_OUTPUT_HATCHES[i] = GregTechAPI.registerMetaTileEntity(6054 + i, new MetaTileEntityEnderEnergyHatch(TJId("ender_energy_output_hatch." + GAValues.VN[tier]), tier, true));
        for (int i = 0; i < FILTERED_INPUT_BUSES.length; i++) // occupies ID range 6068 - 6082
            FILTERED_INPUT_BUSES[i] = GregTechAPI.registerMetaTileEntity(6068 + i, new MetaTileEntityFilteredBus(TJId("filtered_input_bus." + GAValues.VN[i]), i, false));
        for (int i = 0; i < FILTERED_OUTPUT_BUSES.length; i++) // occupies ID range 6083 - 6097
            FILTERED_OUTPUT_BUSES[i] = GregTechAPI.registerMetaTileEntity(6083 + i, new MetaTileEntityFilteredBus(TJId("filtered_output_bus." + GAValues.VN[i]), i, true));
        for (int i = 9; i < MetaTileEntities.ITEM_IMPORT_BUS.length; i++) // occupies ID range 6098 - 6112
            MetaTileEntities.ITEM_IMPORT_BUS[i] = GregTechAPI.registerMetaTileEntity(6089 + i, new MetaTileEntityGAItemBus(TJId("item_input_bus." + GAValues.VN[i]), i, false));
        for (int i = 9; i < MetaTileEntities.ITEM_EXPORT_BUS.length; i++) // occupies ID range 6113 - 6127
            MetaTileEntities.ITEM_EXPORT_BUS[i] = GregTechAPI.registerMetaTileEntity(6095 + i, new MetaTileEntityGAItemBus(TJId("item_output_bus." + GAValues.VN[i]), i, true));

        int energyHatchID = 5016; // occupies ID range 5016 - 5043
        for (int i = 0, tier = 1; tier < GAValues.VN.length; i++, tier++) {
            ENERGY_INPUT_HATCH_256A[i] = GregTechAPI.registerMetaTileEntity(energyHatchID++, new GAMetaTileEntityEnergyHatch(TJId("energy_input_256_" + GAValues.VN[tier]), tier, 256, false));
            ENERGY_OUTPUT_HATCH_256A[i] = GregTechAPI.registerMetaTileEntity(energyHatchID++, new GAMetaTileEntityEnergyHatch(TJId("energy_output_256_" + GAValues.VN[tier]), tier, 256, true));
        }

        ENERGY_INPUT_HATCH_4_AMPS.add(GregTechAPI.registerMetaTileEntity(5044, new GAMetaTileEntityEnergyHatch(location("energy_hatch.input.max.4"), 14, 4, false)));
        ENERGY_INPUT_HATCH_16_AMPS.add(GregTechAPI.registerMetaTileEntity(5045, new GAMetaTileEntityEnergyHatch(location("energy_hatch.input.max.16"), 14, 16, false)));
        ENERGY_OUTPUT_HATCH_16_AMPS.add(GregTechAPI.registerMetaTileEntity(5046, new GAMetaTileEntityEnergyHatch(location("energy_hatch.output.max.16"), 14, 16, true)));
        ENERGY_OUTPUT_HATCH_32_AMPS.add(GregTechAPI.registerMetaTileEntity(5047, new GAMetaTileEntityEnergyHatch(location("energy_hatch.output.max.32"), 14, 32, true)));
        ENERGY_INPUT_HATCH_64_AMPS.add(GregTechAPI.registerMetaTileEntity(5048, new GAMetaTileEntityEnergyHatch(location("energy_hatch.input.max.64"), 14, 64, false)));
        ENERGY_OUTPUT_HATCH_64_AMPS.add(GregTechAPI.registerMetaTileEntity(5049, new GAMetaTileEntityEnergyHatch(location("energy_hatch.output.max.64"), 14, 64, true)));
        ENERGY_INPUT_HATCH_128_AMPS.add(GregTechAPI.registerMetaTileEntity(5050, new GAMetaTileEntityEnergyHatch(location("energy_hatch.input.max.128"), 14, 128, false)));
        ENERGY_OUTPUT_HATCH_128_AMPS.add(GregTechAPI.registerMetaTileEntity(5051, new GAMetaTileEntityEnergyHatch(location("energy_hatch.output.max.128"), 14, 128, true)));
    }

    private static ResourceLocation multiblockTweakerId(String name) {
        return new ResourceLocation("multiblocktweaker", name);
    }

    private static ResourceLocation TJId(String name) {
        return new ResourceLocation(TJ.MODID, name);
    }
}
