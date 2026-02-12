package tj.machines.multi.parallel;

import tj.TJConfig;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.capability.impl.workable.ParallelGAMultiblockRecipeLogic;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.GATransparentCasing;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
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
import static gregicadditions.GAMaterials.Grisium;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;


public class MetaTileEntityParallelLargeBrewery extends ParallelRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, INPUT_ENERGY, IMPORT_FLUIDS, EXPORT_FLUIDS, MAINTENANCE_HATCH};

    public MetaTileEntityParallelLargeBrewery(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GATileEntities.LARGE_BREWERY.getRecipeMaps());
        this.recipeMapWorkable = new ParallelGAMultiblockRecipeLogic(this, this::getEUPercentage, this::getDurationPercentage, this::getChancePercentage, this::getStack);
        this.recipeMapWorkable.setMaxVoltage(this::getMaxVoltage);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityParallelLargeBrewery(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.parallel_large_brewery.description"));
        tooltip.add(I18n.format("tj.multiblock.parallel.description"));
        TooltipHelper.shiftText(tooltip, tip -> super.addInformation(stack, player, tip, advanced));
    }

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, UP, BACK);
        factoryPattern.aisle("CCCCC", "F#C#F", "CXXXC", "CXmXC", "CXXXC", "~CCC~");
        for (int count = 0; count < this.parallelLayer; count++) {
            factoryPattern.aisle("~CCC~", "~~C~~", "~G#G~", "C#P#C", "~G#G~", "~~C~~");
            factoryPattern.aisle("~MMM~", "~~M~~", "~G#G~", "p#P#p", "~G#G~", "~~C~~");
            factoryPattern.aisle("~CCC~", "~~C~~", "~G#G~", "C#P#C", "~G#G~", "~~C~~");
        }
         return factoryPattern.aisle("CCCCC", "F~C~F", "CXSXC", "CXmXC", "CXXXC", "~CCC~")
                 .where('S', this.selfPredicate())
                 .where('C', statePredicate(this.getCasingState()))
                 .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                 .where('M', statePredicate(this.getCasingState()).or(abilityPartPredicate(REDSTONE_CONTROLLER)))
                 .where('G', statePredicate(GAMetaBlocks.TRANSPARENT_CASING.getState(GATransparentCasing.CasingType.OSMIRIDIUM_GLASS)))
                 .where('P', statePredicate(GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.PTFE_PIPE)))
                 .where('F', statePredicate(MetaBlocks.FRAMES.get(Grisium).getDefaultState()))
                 .where('m', LargeSimpleRecipeMapMultiblockController.motorPredicate())
                 .where('p', LargeSimpleRecipeMapMultiblockController.pumpPredicate())
                 .where('#', isAirPredicate())
                 .where('~', tile -> true)
                 .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.GRISIUM);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.GRISIUM_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        switch (this.getRecipeMapIndex()) {
            case 1: return Textures.FERMENTER_OVERLAY;
            case 2: return Textures.SIFTER_OVERLAY;
            case 3: return Textures.CRACKING_UNIT_OVERLAY;
            default: return Textures.BREWERY_OVERLAY;
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int motor = context.getOrDefault("Motor", MotorCasing.CasingType.MOTOR_LV).getTier();
        int pump = context.getOrDefault("Pump", PumpCasing.CasingType.PUMP_LV).getTier();
        int min = Math.min(motor, pump);
        this.maxVoltage = (long) (Math.pow(4, min) * 8);
    }

    @Override
    public int getEUPercentage() {
        return TJConfig.parallelLargeBrewery.eutPercentage;
    }

    @Override
    public int getDurationPercentage() {
        return TJConfig.parallelLargeBrewery.durationPercentage;
    }

    @Override
    public int getChancePercentage() {
        return TJConfig.parallelLargeBrewery.chancePercentage;
    }

    @Override
    public int getStack() {
        return TJConfig.parallelLargeBrewery.stack;
    }

    @Override
    public int getMaxParallel() {
        return TJConfig.parallelLargeBrewery.maximumParallel;
    }
}
