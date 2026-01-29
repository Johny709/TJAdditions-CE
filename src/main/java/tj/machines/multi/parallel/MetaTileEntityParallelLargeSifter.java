package tj.machines.multi.parallel;

import gregicadditions.machines.GATileEntities;
import tj.TJConfig;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.capability.impl.workable.ParallelGAMultiblockRecipeLogic;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.PistonCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockMultiblockCasing;
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

import static tj.multiblockpart.TJMultiblockAbility.REDSTONE_CONTROLLER;
import static gregicadditions.GAMaterials.EglinSteel;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;


public class MetaTileEntityParallelLargeSifter extends ParallelRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, INPUT_ENERGY, MAINTENANCE_HATCH, REDSTONE_CONTROLLER};

    public MetaTileEntityParallelLargeSifter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GATileEntities.LARGE_SIFTER.recipeMap);
        this.recipeMapWorkable = new ParallelGAMultiblockRecipeLogic(this, this::getEUPercentage, this::getDurationPercentage, this::getChancePercentage, this::getStack);
        this.recipeMapWorkable.setMaxVoltage(this::getMaxVoltage);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityParallelLargeSifter(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.parallel_large_sifter.description"));
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
            factoryPattern.aisle("~XXX~", "X###X", "X###X", "X###X", "~XXX~");
            factoryPattern.aisle("XXXXX", "PGGGP", "XGGGX", "PGGGP", "XXXXX");
            factoryPattern.aisle("~XXX~", "X###X", "X###X", "X###X", "~XXX~");
            factoryPattern.aisle("~FCF~", "F###F", "C###C", "F###F", "~FCF~");
        }
        return factoryPattern
                .aisle("~XXX~", "X###X", "X###X", "X###X", "~XXX~")
                .aisle("XXSXX", "PGGGP", "XGGGX", "PGGGP", "XXXXX")
                .aisle("~XXX~", "X###X", "X###X", "X###X", "~XXX~")
                .aisle("~C~C~", "CCCCC", "~C~C~", "CCCCC", "~C~C~")
                .aisle("~C~C~", "CCCCC", "~C~C~", "CCCCC", "~C~C~")
                .setAmountAtLeast('L', 40)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('F', statePredicate(MetaBlocks.FRAMES.get(EglinSteel).getDefaultState()))
                .where('G', statePredicate(MetaBlocks.MUTLIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING)))
                .where('P', LargeSimpleRecipeMapMultiblockController.pistonPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.EGLIN_STEEL);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.EGLIN_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.SIFTER_OVERLAY;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int min = context.getOrDefault("Piston", PistonCasing.CasingType.PISTON_LV).getTier();
        this.maxVoltage = (long) (Math.pow(4, min) * 8);
    }

    @Override
    public int getEUPercentage() {
        return TJConfig.parallelLargeSifter.eutPercentage;
    }

    @Override
    public int getDurationPercentage() {
        return TJConfig.parallelLargeSifter.durationPercentage;
    }

    @Override
    public int getChancePercentage() {
        return TJConfig.parallelLargeSifter.chancePercentage;
    }

    @Override
    public int getStack() {
        return TJConfig.parallelLargeSifter.stack;
    }

    @Override
    public int getMaxParallel() {
        return TJConfig.parallelLargeSifter.maximumParallel;
    }
}
