package tj.integration.jei;

import com.google.common.collect.ImmutableMap;
import gregtech.common.metatileentities.multi.MetaTileEntityLargeBoiler.BoilerType;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeTurbine.TurbineType;
import gregtech.integration.jei.multiblock.MultiblockInfoRecipeWrapper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.client.resources.I18n;
import tj.TJ;
import tj.TJConfig;
import tj.integration.jei.multi.electric.*;
import tj.integration.jei.multi.parallel.*;
import tj.integration.jei.multi.electric.XLHotCoolantTurbineInfo;
import tj.integration.jei.multi.electric.XLTurbineInfo;
import tj.integration.jei.multi.steam.*;
import tj.machines.TJMetaTileEntities;

import javax.annotation.Nonnull;

import static tj.machines.TJMetaTileEntities.LARGE_WIRELESS_ENERGY_EMITTER;
import static tj.machines.TJMetaTileEntities.LARGE_WIRELESS_ENERGY_RECEIVER;
import static tj.machines.multi.electric.MetaTileEntityLargeWirelessEnergyEmitter.TransferType.INPUT;
import static tj.machines.multi.electric.MetaTileEntityLargeWirelessEnergyEmitter.TransferType.OUTPUT;

public class TJMultiblockInfoCategory implements IRecipeCategory<MultiblockInfoRecipeWrapper> {
    private final IDrawable background;
    private final IGuiHelper guiHelper;
    private static ImmutableMap<String, MultiblockInfoRecipeWrapper> multiblockRecipes;

    public TJMultiblockInfoCategory(IJeiHelpers helpers) {
        this.guiHelper = helpers.getGuiHelper();
        this.background = this.guiHelper.createBlankDrawable(176, 166);
    }

    public static ImmutableMap<String, MultiblockInfoRecipeWrapper> getMultiblockRecipes() {
        if (multiblockRecipes == null) {
            ImmutableMap.Builder<String, MultiblockInfoRecipeWrapper> multiblockRecipes = new ImmutableMap.Builder<>();

                    if (TJConfig.machines.replaceCTMultis) {
                    multiblockRecipes.put("primitive_alloy", new MultiblockInfoRecipeWrapper(new PrimitiveAlloyInfo()))
                                .put("coke_oven", new MultiblockInfoRecipeWrapper(new CokeOvenInfo()))
                                .put("heat_exchanger", new MultiblockInfoRecipeWrapper(new HeatExchangerInfo()))
                                .put("armor_infuser", new MultiblockInfoRecipeWrapper(new ArmorInfuserInfo()))
                                .put("dragon_egg_replicator", new MultiblockInfoRecipeWrapper(new DragonReplicatorInfo()))
                                .put("chaos_replicator", new MultiblockInfoRecipeWrapper(new ChaosReplicatorInfo()))
                                .put("large_powered_spawner", new MultiblockInfoRecipeWrapper(new LargePoweredSpawnerInfo()))
                                .put("large_vial_processor", new MultiblockInfoRecipeWrapper(new LargeVialProcessorInfo()));
                    }

                    multiblockRecipes.put("mega_coke_oven", new MultiblockInfoRecipeWrapper(new MegaCokeOvenInfo()))
                            .put("mega_bronze_boiler", new MultiblockInfoRecipeWrapper(new MegaBoilerInfo(BoilerType.BRONZE, TJMetaTileEntities.MEGA_BOILER[0])))
                            .put("mega_steel_boiler", new MultiblockInfoRecipeWrapper(new MegaBoilerInfo(BoilerType.STEEL, TJMetaTileEntities.MEGA_BOILER[1])))
                            .put("mega_titanium_boiler", new MultiblockInfoRecipeWrapper(new MegaBoilerInfo(BoilerType.TITANIUM, TJMetaTileEntities.MEGA_BOILER[2])))
                            .put("mega_tungstensteel_boiler", new MultiblockInfoRecipeWrapper(new MegaBoilerInfo(BoilerType.TUNGSTENSTEEL, TJMetaTileEntities.MEGA_BOILER[3])))
                            .put("xl_turbine.steam", new MultiblockInfoRecipeWrapper(new XLTurbineInfo(TJMetaTileEntities.XL_STEAM_TURBINE)))
                            .put("xl_turbine.gas", new MultiblockInfoRecipeWrapper(new XLTurbineInfo(TJMetaTileEntities.XL_GAS_TURBINE)))
                            .put("xl_turbine.plasma", new MultiblockInfoRecipeWrapper(new XLTurbineInfo(TJMetaTileEntities.XL_PLASMA_TURBINE)))
                            .put("xl_turbine.coolant", new MultiblockInfoRecipeWrapper(new XLHotCoolantTurbineInfo(TJMetaTileEntities.XL_COOLANT_TURBINE)))
                            .put("large_decay_chamber", new MultiblockInfoRecipeWrapper(new LargeDecayChamberInfo()))
                            .put("large_alloy_smelter", new MultiblockInfoRecipeWrapper(new LargeAlloySmelterInfo()))
                            .put("industrial_fusion_reactor.luv", new MultiblockInfoRecipeWrapper(new IndustrialFusionReactorInfo(TJMetaTileEntities.INDUSTRIAL_FUSION_REACTOR_LUV)))
                            .put("industrial_fusion_reactor.zpm", new MultiblockInfoRecipeWrapper(new IndustrialFusionReactorInfo(TJMetaTileEntities.INDUSTRIAL_FUSION_REACTOR_ZPM)))
                            .put("industrial_fusion_reactor.uv", new MultiblockInfoRecipeWrapper(new IndustrialFusionReactorInfo(TJMetaTileEntities.INDUSTRIAL_FUSION_REACTOR_UV)))
                            .put("industrial_fusion_reactor.uhv", new MultiblockInfoRecipeWrapper(new IndustrialFusionReactorInfo(TJMetaTileEntities.INDUSTRIAL_FUSION_REACTOR_UHV)))
                            .put("industrial_fusion_reactor.uev", new MultiblockInfoRecipeWrapper(new IndustrialFusionReactorInfo(TJMetaTileEntities.INDUSTRIAL_FUSION_REACTOR_UEV)))
                            .put("parallel_chemical_reactor", new MultiblockInfoRecipeWrapper(new ParallelLargeChemicalReactorInfo()))
                            .put("large_greenhouse", new MultiblockInfoRecipeWrapper(new LargeGreenhouseInfo()))
                            .put("large_architect_workbench", new MultiblockInfoRecipeWrapper(new LargeArchitectWorkbenchInfo()))
                            .put("elite_large_miner", new MultiblockInfoRecipeWrapper(new LargeMinerInfo(TJMetaTileEntities.ELITE_LARGE_MINER)))
                            .put("ultimate_large_miner", new MultiblockInfoRecipeWrapper(new LargeMinerInfo(TJMetaTileEntities.ULTIMATE_LARGE_MINER)))
                            .put("world_destroyer", new MultiblockInfoRecipeWrapper(new LargeMinerInfo(TJMetaTileEntities.WORLD_DESTROYER)))
                            .put("large_world_accelerator", new MultiblockInfoRecipeWrapper(new LargeWorldAcceleratorInfo()))
                            .put("large_rock_breaker", new MultiblockInfoRecipeWrapper(new LargeRockBreakerInfo()))
                            .put("steam_air_collector_turbine", new MultiblockInfoRecipeWrapper(new LargeAtmosphereCollectorInfo(TurbineType.STEAM, TJMetaTileEntities.LARGE_ATMOSPHERE_COLLECTOR[0])))
                            .put("gas_air_collector_turbine", new MultiblockInfoRecipeWrapper(new LargeAtmosphereCollectorInfo(TurbineType.GAS, TJMetaTileEntities.LARGE_ATMOSPHERE_COLLECTOR[1])))
                            .put("plasma_air_collector_turbine", new MultiblockInfoRecipeWrapper(new LargeAtmosphereCollectorInfo(TurbineType.PLASMA, TJMetaTileEntities.LARGE_ATMOSPHERE_COLLECTOR[2])))
                            .put("infinite_fluid_drill", new MultiblockInfoRecipeWrapper(new InfiniteFluidDrillInfo()))
                            .put("industrial_steam_engine", new MultiblockInfoRecipeWrapper(new IndustrialSteamEngineInfo()))
                            .put("advanced_parallel_chemical_reactor", new MultiblockInfoRecipeWrapper(new ParallelAdvancedChemicalReactorInfo()))
                            .put("parallel_large_macerator", new MultiblockInfoRecipeWrapper(new ParallelLargeMaceratorInfo()))
                            .put("parallel_large_washing_machine", new MultiblockInfoRecipeWrapper(new ParallelLargeWashingMachineInfo()))
                            .put("parallel_large_centrifuge", new MultiblockInfoRecipeWrapper(new ParallelLargeCentrifugeInfo()))
                            .put("parallel_large_electrolyzer", new MultiblockInfoRecipeWrapper(new ParallelLargeElectrolyzerInfo()))
                            .put("parallel_large_sifter", new MultiblockInfoRecipeWrapper(new ParallelLargeSifterInfo()))
                            .put("parallel_large_brewery", new MultiblockInfoRecipeWrapper(new ParallelLargeBreweryInfo()))
                            .put("parallel_large_arc_furnace", new MultiblockInfoRecipeWrapper(new ParallelLargeArcFurnaceInfo()))
                            .put("parallel_large_assembler", new MultiblockInfoRecipeWrapper(new ParallelLargeAssemblerInfo()))
                            .put("parallel_large_bending_and_forming", new MultiblockInfoRecipeWrapper(new ParallelLargeBendingAndFormingInfo()))
                            .put("parallel_large_canning_machine", new MultiblockInfoRecipeWrapper(new ParallelLargeCanningMachineInfo()))
                            .put("parallel_large_cutting_machine", new MultiblockInfoRecipeWrapper(new ParallelLargeCuttingMachineInfo()))
                            .put("parallel_large_electromagnet", new MultiblockInfoRecipeWrapper(new ParallelLargeElectromagnetInfo()))
                            .put("parallel_large_extractor", new MultiblockInfoRecipeWrapper(new ParallelLargeExtractorInfo()))
                            .put("parallel_large_extruder", new MultiblockInfoRecipeWrapper(new ParallelLargeExtruderInfo()))
                            .put("parallel_large_forge_hammer", new MultiblockInfoRecipeWrapper(new ParallelLargeForgeHammerInfo()))
                            .put("parallel_large_laser_engraver", new MultiblockInfoRecipeWrapper(new ParallelLargeLaserEngraverInfo()))
                            .put("parallel_large_mixer", new MultiblockInfoRecipeWrapper(new ParallelLargeMixerInfo()))
                            .put("parallel_large_packager", new MultiblockInfoRecipeWrapper(new ParallelLargePackagerInfo()))
                            .put("parallel_large_wiremill", new MultiblockInfoRecipeWrapper(new ParallelLargeWiremillInfo()))
                            .put("parallel_plasma_condenser", new MultiblockInfoRecipeWrapper(new ParallelPlasmaCondenserInfo()))
                            .put("parallel_alloy_blast_smelter", new MultiblockInfoRecipeWrapper(new ParallelAlloyBlastSmelterInfo()))
                            .put("parallel_electric_blast_furnace", new MultiblockInfoRecipeWrapper(new ParallelElectricBlastFurnaceInfo()))
                            .put("parallel_vacuum_freezer", new MultiblockInfoRecipeWrapper(new ParallelVacuumFreezerInfo()))
                            .put("parallel_cryogenic_freezer", new MultiblockInfoRecipeWrapper(new ParallelCryogenicFreezerInfo()))
                            .put("parallel_volcunus", new MultiblockInfoRecipeWrapper(new ParallelVolcanusInfo()))
                            .put("large_wireless_energy_emitter", new MultiblockInfoRecipeWrapper(new LargeWirelessEnergyEmitterInfo(INPUT, LARGE_WIRELESS_ENERGY_EMITTER)))
                            .put("large_wireless_energy_receiver", new MultiblockInfoRecipeWrapper(new LargeWirelessEnergyEmitterInfo(OUTPUT, LARGE_WIRELESS_ENERGY_RECEIVER)))
                            .put("large_battery_charger", new MultiblockInfoRecipeWrapper(new LargeBatteryChargerInfo()))
                            .put("void_more_miner", new MultiblockInfoRecipeWrapper(new VoidMOreMinerInfo()))
                            .put("teleporter", new MultiblockInfoRecipeWrapper(new TeleporterInfo()))
                            .put("large_chisel_workbench", new MultiblockInfoRecipeWrapper(new LargeChiselWorkbenchInfo()))
                            .put("large_enchanter", new MultiblockInfoRecipeWrapper(new LargeEnchanterInfo()))
                            .put("large_crafter", new MultiblockInfoRecipeWrapper(new LargeCrafterInfo()))
                            .put("mega_fusion", new MultiblockInfoRecipeWrapper(new MegaFusionInfo()))
                            .put("ender_battery_tower", new MultiblockInfoRecipeWrapper(new EnderBatteryTowerInfo()))
                            .put("large_solar_boiler", new MultiblockInfoRecipeWrapper(new LargeSolarBoilerInfo(false)))
                            .put("mega_solar_boiler", new MultiblockInfoRecipeWrapper(new LargeSolarBoilerInfo(true)))
                            .put("large_implosion_compressor", new MultiblockInfoRecipeWrapper(new LargeImplosionCompressorInfo()))
                            .put("large_electric_implosion_compressor", new MultiblockInfoRecipeWrapper(new LargeElectricImplosionCompressor()))
                            .put("charcoal_pit", new MultiblockInfoRecipeWrapper(new CharcoalPitInfo(false)))
                            .put("charcoal_pit_advanced", new MultiblockInfoRecipeWrapper(new CharcoalPitInfo(true)))
                            .put("primitive_water_pump", new MultiblockInfoRecipeWrapper(new PrimitiveWaterPumpInfo()))
                            .put("advanced_large_chunk_miner.0", new MultiblockInfoRecipeWrapper(new AdvancedLargeChunkMinerInfo(0)))
                            .put("advanced_large_chunk_miner.1", new MultiblockInfoRecipeWrapper(new AdvancedLargeChunkMinerInfo(1)))
                            .put("advanced_large_chunk_miner.2", new MultiblockInfoRecipeWrapper(new AdvancedLargeChunkMinerInfo(2)))
                            .put("advanced_large_chunk_miner.3", new MultiblockInfoRecipeWrapper(new AdvancedLargeChunkMinerInfo(3)))
                            .put("advamced_large_chunk_miner.4", new MultiblockInfoRecipeWrapper(new AdvancedLargeChunkMinerInfo(4)))
                            .put("advanced_large_chunk_miner.5", new MultiblockInfoRecipeWrapper(new AdvancedLargeChunkMinerInfo(5)));
                    return TJMultiblockInfoCategory.multiblockRecipes = multiblockRecipes.build();
        }
        return multiblockRecipes;
    }

    public static void registerRecipes(IModRegistry registry) {
        registry.addRecipes(getMultiblockRecipes().values(), "gregtech:multiblock_info");
    }

    @Nonnull
    @Override
    public String getUid() {
        return "tj:multiblock_info";
    }

    @Nonnull
    @Override
    public String getTitle() {
        return I18n.format("gregtech.multiblock.title");
    }

    @Nonnull
    @Override
    public String getModName() {
        return TJ.MODID;
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public void setRecipe(IRecipeLayout iRecipeLayout, MultiblockInfoRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
        recipeWrapper.setRecipeLayout((RecipeLayout) iRecipeLayout, guiHelper);

        IGuiItemStackGroup itemStackGroup = iRecipeLayout.getItemStacks();
        itemStackGroup.addTooltipCallback(recipeWrapper::addBlockTooltips);
    }
}
