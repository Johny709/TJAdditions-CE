package tj.machines.multi.parallel;

import tj.TJConfig;
import tj.builder.ParallelRecipeMap;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.capability.impl.ParallelGAMultiblockRecipeLogic;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.util.TooltipHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static tj.TJRecipeMaps.PARALLEL_EXTRACTOR_RECIPES;
import static tj.TJRecipeMaps.PARALLEL_FLUID_EXTRACTION_RECIPES;
import static tj.multiblockpart.TJMultiblockAbility.REDSTONE_CONTROLLER;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController.pumpPredicate;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.render.Textures.EXTRACTOR_OVERLAY;
import static gregtech.api.render.Textures.FLUID_EXTRACTOR_OVERLAY;


public class MetaTileEntityParallelLargeExtractor extends ParallelRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, EXPORT_FLUIDS, MAINTENANCE_HATCH, INPUT_ENERGY, REDSTONE_CONTROLLER};

    public MetaTileEntityParallelLargeExtractor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, new ParallelRecipeMap[]{PARALLEL_FLUID_EXTRACTION_RECIPES, PARALLEL_EXTRACTOR_RECIPES});
        this.recipeMapWorkable = new ParallelGAMultiblockRecipeLogic(this, this::getEUPercentage, this::getDurationPercentage, this::getChancePercentage, this::getStack);
        this.recipeMapWorkable.setMaxVoltage(this::getMaxVoltage);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityParallelLargeExtractor(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.parallel_large_extractor.description"));
        tooltip.add(I18n.format("tj.multiblock.parallel.description"));
        TooltipHelper.shiftText(tooltip, tip -> {
            tip.add(I18n.format("tj.multiblock.parallel.extend.tooltip"));
            super.addInformation(stack, player, tip, advanced);
        });
    }

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, FRONT, DOWN);
        for (int layer = 1; layer < this.parallelLayer; layer++) {
            factoryPattern.aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX");
            factoryPattern.aisle("~PpP~", "P#P#P", "pPPPp", "P#P#P", "~PpP~");
        }
        return factoryPattern
                .aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                .aisle("~PSP~", "P#P#P", "pPPPp", "P#P#P", "~PpP~")
                .aisle("XXXXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                .setAmountAtLeast('L', 13)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('P', statePredicate(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('p', pumpPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.TALONITE);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int pump = context.getOrDefault("Pump", PumpCasing.CasingType.PUMP_LV).getTier();
        this.maxVoltage = (long) (Math.pow(4, pump) * 8);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.TALONITE_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return this.getRecipeMapIndex() == 0 ? FLUID_EXTRACTOR_OVERLAY : EXTRACTOR_OVERLAY;
    }

    @Override
    public int getEUPercentage() {
        return TJConfig.parallelLargeExtractor.eutPercentage;
    }

    @Override
    public int getDurationPercentage() {
        return TJConfig.parallelLargeExtractor.durationPercentage;
    }

    @Override
    public int getChancePercentage() {
        return TJConfig.parallelLargeExtractor.chancePercentage;
    }

    @Override
    public int getStack() {
        return TJConfig.parallelLargeExtractor.stack;
    }

    @Override
    public int getMaxParallel() {
        return TJConfig.parallelLargeExtractor.maximumParallel;
    }

    @Override
    public RecipeMap<?>[] getRecipeMaps() {
        return GATileEntities.LARGE_EXTRACTOR.getRecipeMaps();
    }
}
