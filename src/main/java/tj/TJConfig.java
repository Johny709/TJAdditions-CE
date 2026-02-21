package tj;

import net.minecraftforge.common.config.Config;

@Config(modid = TJ.MODID)
public class TJConfig {

    @Config.Comment("Configure Machines")
    public static Machines machines = new Machines();

    public static class Machines {

        @Config.Name("Replace CT Multis")
        @Config.Comment("replaces Multis registered by MultiblockTweaker")
        @Config.RequiresMcRestart
        public boolean replaceCTMultis = false;

        @Config.Name("Parallel Recipe Cache Capacity")
        @Config.Comment("Recipe LRU Caching for Parallel Multiblock Recipe Logic. Recommended not to make this too large.")
        @Config.RequiresMcRestart
        public int recipeCacheCapacity = 10;

        @Config.Name("Recipe has problems cooldown")
        @Config.Comment("Set tick duration of cooldown timer")
        @Config.RequiresMcRestart
        public int recipeCooldown = 200;

        @Config.Name("Override TheOneProbe Info Providers")
        @Config.Comment("Set to replace TheOneProbe display of machines with TJ Additions' TheOneProbe edits and implementation")
        @Config.RequiresMcRestart
        public boolean theOneProbeInfoProviderOverrides = true;

        @Config.Name("Override Logic of GT Generators")
        @Config.Comment("Set to replace generator logic with TJ Additions' edits and implementation")
        @Config.RequiresMcRestart
        public boolean generatorWorkableHandlerOverrides = true;

        @Config.Name("Override Multiblock display UIs")
        @Config.Comment("Set to replace Multiblock UIs with TJ Additions' edits and implementation")
        @Config.RequiresMcRestart
        public boolean multiblockUIOverrides = true;

        @Config.Name("Max Extendable Layers Shown In JEI")
        @Config.Comment("Set the maximum amount of layers to be shown in JEI preview of extendable multiblocks")
        @Config.RequiresMcRestart
        public int maxLayersInJEI = 16;

    }

    @Config.Comment("Industrial Fusion Reactor")
    public static IndustrialFusionReactor industrialFusionReactor = new IndustrialFusionReactor();

    public static class IndustrialFusionReactor {

        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Slice Limit")
        @Config.Comment("Adjust the maximum number of slices the fusion reactor can have")
        @Config.RequiresMcRestart
        public int maximumSlices = 64;
    }

    @Config.Comment("Parallel Large Chemical Reactor")
    public static ParallelChemicalReactor parallelChemicalReactor = new ParallelChemicalReactor();

    public static class ParallelChemicalReactor {

        @Config.Name("Parallel Limit")
        @Config.Comment("Adjust the maximum number of parallel recipes the chemical reactor can do")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Decay Chamber")
    public static DecayChamber decayChamber = new DecayChamber();

    public static class DecayChamber {

        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;
    }

    @Config.Comment("Large Alloy Smelter")
    public static LargeAlloySmelter largeAlloySmelter = new LargeAlloySmelter();

    public static class LargeAlloySmelter {

        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;
    }

    @Config.Comment("Large Greenhouse")
    public static LargeGreenhouse largeGreenhouse = new LargeGreenhouse();

    public static class LargeGreenhouse {

        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("EU/t Percentage [Tree Mode]")
        @Config.RequiresMcRestart
        public int eutPercentageTree = 90;

        @Config.Name("Duration Percentage [Tree Mode]")
        @Config.RequiresMcRestart
        public int durationPercentageTree = 50;

        @Config.Name("Chance Percentage [Tree Mode]")
        @Config.RequiresMcRestart
        public int chancePercentageTree = 150;

        @Config.Name("Stack Size [Tree Mode]")
        @Config.RequiresMcRestart
        public int stackTree = 4;
    }

    @Config.Comment("Large Architect's Workbench")
    public static LargeArchitectWorkbench largeArchitectWorkbench = new LargeArchitectWorkbench();

    public static class LargeArchitectWorkbench {

        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.Comment("Adjust the maximum number of slices the Large Architect Workbench can have")
        @Config.RequiresMcRestart
        public int maximumSlices = 64;
    }

    @Config.Comment("Large Chisel Workbench")
    public static LargeChiselWorkbench largeChiselWorkbench = new LargeChiselWorkbench();

    public static class LargeChiselWorkbench {

        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.Comment("Adjust the maximum number of slices the Large Chisel Workbench can have")
        @Config.RequiresMcRestart
        public int maximumSlices = 64;
    }

    @Config.Comment("Large Chisel Workbench")
    public static LargeEnchanter largeEnchanter = new LargeEnchanter();

    public static class LargeEnchanter {

        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;
    }

    @Config.Comment("Large Crafter")
    public static LargeCrafter largeCrafter = new LargeCrafter();

    public static class LargeCrafter {

        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;
    }

    @Config.Comment("Large Naming Machine")
    public static LargeNamingMachine largeNamingMachine = new LargeNamingMachine();

    public static class LargeNamingMachine {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;
    }

    @Config.Comment("Elite Large Miner")
    public static EliteLargeMiner eliteLargeMiner = new EliteLargeMiner();

    public static class EliteLargeMiner {

        @Config.Name("Elite Large Miner Chunk Diamater")
        @Config.RequiresMcRestart
        @Config.Comment("The length in chunks of the side of the square centered on the Miner that will be mined.")
        public int eliteMinerChunkDiamater = 9;

        @Config.Name("Elite Large Miner Fortune Level")
        @Config.RequiresMcRestart
        @Config.Comment("The level of fortune which will be applied to blocks that the Miner mines.")
        public int eliteMinerFortune = 18;

        @Config.Name("Elite Large Miner Drilling Fluid Consumption")
        @Config.RequiresMcRestart
        @Config.Comment("The amount of drilling fluid consumed per tick")
        public int eliteMinerDrillingFluid = 64;
    }

    @Config.Comment("Elite Large Miner")
    public static UltimateLargeMiner ultimateLargeMiner = new UltimateLargeMiner();

    public static class UltimateLargeMiner {

        @Config.Name("Elite Large Miner Chunk Diamater")
        @Config.RequiresMcRestart
        @Config.Comment("The length in chunks of the side of the square centered on the Miner that will be mined.")
        public int ultimateMinerChunkDiamater = 12;

        @Config.Name("Elite Large Miner Fortune Level")
        @Config.RequiresMcRestart
        @Config.Comment("The level of fortune which will be applied to blocks that the Miner mines.")
        public int ultimateMinerFortune = 24;

        @Config.Name("Elite Large Miner Drilling Fluid Consumption")
        @Config.RequiresMcRestart
        @Config.Comment("The amount of drilling fluid consumed per tick")
        public int ultimateMinerDrillingFluid = 128;
    }

    @Config.Comment("World Destroyer")
    public static WorldDestroyerMiner worldDestroyerMiner = new WorldDestroyerMiner();

    public static class WorldDestroyerMiner {

        @Config.Name("World Destroyer Fortune Level")
        @Config.RequiresMcRestart
        @Config.Comment("The level of fortune which will be applied to blocks that the Miner mines.")
        public int worldDestroyerFortune = 3;

        @Config.Name("World Destroyer Drilling Fluid Consumption")
        @Config.RequiresMcRestart
        @Config.Comment("The amount of drilling fluid consumed per tick")
        public int worldDestroyerDrillingFluid = 100;

        @Config.Name("World Destroyer Chunk Multiplier")
        @Config.RequiresMcRestart
        @Config.Comment("The amount chunk area multiplied per motor tier")
        public int worldDestroyerChunkMultiplier = 2;
    }

    @Config.Comment("Large Rock Breaker")
    public static LargeRockBreaker largeRockBreaker = new LargeRockBreaker();

    public static class LargeRockBreaker {

        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 1;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumSlices = 64;
    }

    @Config.Comment("APLCR")
    public static AdvancedParallelChemicalReactor advancedParallelChemicalReactor = new AdvancedParallelChemicalReactor();

    public static class AdvancedParallelChemicalReactor {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 20;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Macerator")
    public static ParallelLargeMacerator parallelLargeMacerator = new ParallelLargeMacerator();

    public static class ParallelLargeMacerator {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 200;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Washing Machine")
    public static ParallelLargeWashingMachine parallelLargeWashingMachine = new ParallelLargeWashingMachine();

    public static class ParallelLargeWashingMachine {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Centrifuge")
    public static ParallelLargeCentrifuge parallelLargeCentrifuge = new ParallelLargeCentrifuge();

    public static class ParallelLargeCentrifuge {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 150;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Sifter")
    public static ParallelLargeSifter parallelLargeSifter = new ParallelLargeSifter();

    public static class ParallelLargeSifter {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 188;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Electrolyzer")
    public static ParallelLargeElectrolyzer parallelLargeElectrolyzer = new ParallelLargeElectrolyzer();

    public static class ParallelLargeElectrolyzer {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Brewery")
    public static ParallelLargeBrewery parallelLargeBrewery = new ParallelLargeBrewery();

    public static class ParallelLargeBrewery {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 200;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Arc Furnace")
    public static ParallelLargeArcFurnace parallelLargeArcFurnace = new ParallelLargeArcFurnace();

    public static class ParallelLargeArcFurnace {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Assembler")
    public static ParallelLargeAssembler parallelLargeAssembler = new ParallelLargeAssembler();

    public static class ParallelLargeAssembler {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Canning Machine")
    public static ParallelLargeCanningMachine parallelLargeCanningMachine = new ParallelLargeCanningMachine();

    public static class ParallelLargeCanningMachine {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Cutting Machine")
    public static ParallelLargeCuttingMachine parallelLargeCuttingMachine = new ParallelLargeCuttingMachine();

    public static class ParallelLargeCuttingMachine {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Electromagnet")
    public static ParallelLargeElectromagnet parallelLargeElectromagnet = new ParallelLargeElectromagnet();

    public static class ParallelLargeElectromagnet {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Extractor")
    public static ParallelLargeExtractor parallelLargeExtractor = new ParallelLargeExtractor();

    public static class ParallelLargeExtractor {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Extruder")
    public static ParallelLargeExtruder parallelLargeExtruder = new ParallelLargeExtruder();

    public static class ParallelLargeExtruder {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Forge Hammer")
    public static ParallelLargeForgeHammer parallelLargeForgeHammer = new ParallelLargeForgeHammer();

    public static class ParallelLargeForgeHammer {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Laser Engraver")
    public static ParallelLargeLaserEngraver parallelLargeLaserEngraver = new ParallelLargeLaserEngraver();

    public static class ParallelLargeLaserEngraver {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Mixer")
    public static ParallelLargeMixer parallelLargeMixer = new ParallelLargeMixer();

    public static class ParallelLargeMixer {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Packager")
    public static ParallelLargePackager parallelLargePackager = new ParallelLargePackager();

    public static class ParallelLargePackager {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Wiremill")
    public static ParallelLargeWiremill parallelLargeWiremill = new ParallelLargeWiremill();

    public static class ParallelLargeWiremill {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Plasma Condenser")
    public static ParallelPlasmaCondenser parallelPlasmaCondenser = new ParallelPlasmaCondenser();

    public static class ParallelPlasmaCondenser {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Large Bending and Forming")
    public static ParallelLargeBendingAndForming parallelLargeBendingAndForming = new ParallelLargeBendingAndForming();

    public static class ParallelLargeBendingAndForming {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;

        @Config.Name("Slice Limit")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Vacuum Freezer")
    public static ParallelAlloyBlastSmelter parallelAlloyBlastSmelter = new ParallelAlloyBlastSmelter();

    public static class ParallelAlloyBlastSmelter {

        @Config.Name("Parallel Limit")
        @Config.Comment("Adjust the maximum number of parallel recipes the vacuum freezer can do")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Electric Blast Furnace")
    public static ParallelElectricBlastFurnace parallelElectricBlastFurnace = new ParallelElectricBlastFurnace();

    public static class ParallelElectricBlastFurnace {

        @Config.Name("Parallel Limit")
        @Config.Comment("Adjust the maximum number of parallel recipes the electric blast furnace can do")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Vacuum Freezer")
    public static ParallelVacuumFreezer parallelVacuumFreezer = new ParallelVacuumFreezer();

    public static class ParallelVacuumFreezer {

        @Config.Name("Parallel Limit")
        @Config.Comment("Adjust the maximum number of parallel recipes the vacuum freezer can do")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Cryogenic Freezer")
    public static ParallelCryogenicFreezer parallelCryogenicFreezer = new ParallelCryogenicFreezer();

    public static class ParallelCryogenicFreezer {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 20;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 60;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 4;

        @Config.Name("Parallel Limit")
        @Config.Comment("Adjust the maximum number of parallel recipes the vacuum freezer can do")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Parallel Volcanus")
    public static ParallelVolcanus parallelVolcanus = new ParallelVolcanus();

    public static class ParallelVolcanus {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 100;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 100;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 4;

        @Config.Name("Parallel Limit")
        @Config.Comment("Adjust the maximum number of parallel recipes the vacuum freezer can do")
        @Config.RequiresMcRestart
        public int maximumParallel = 64;
    }

    @Config.Comment("Large World Accelerator")
    public static LargeWorldAccelerator largeWorldAccelerator = new LargeWorldAccelerator();

    public static class LargeWorldAccelerator {
        @Config.Name("Block Range Base Radius")
        @Config.RequiresMcRestart
        public int baseRange = 48;

        @Config.Name("Block Range Additional Radius")
        @Config.RequiresMcRestart
        public int additionalRange = 16;
    }

    @Config.Comment("Large Implosion Compressor")
    public static LargeImplosionCompressor largeImplosionCompressor = new LargeImplosionCompressor();

    public static class LargeImplosionCompressor {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;
    }

    @Config.Comment("Large Electric Implosion Compressor")
    public static LargeElectricImplosionCompressor largeElectricImplosionCompressor = new LargeElectricImplosionCompressor();

    public static class LargeElectricImplosionCompressor {
        @Config.Name("EU/t Percentage")
        @Config.RequiresMcRestart
        public int eutPercentage = 90;

        @Config.Name("Duration Percentage")
        @Config.RequiresMcRestart
        public int durationPercentage = 80;

        @Config.Name("Chance Percentage")
        @Config.RequiresMcRestart
        public int chancePercentage = 100;

        @Config.Name("Stack Size")
        @Config.RequiresMcRestart
        public int stack = 16;
    }
}
