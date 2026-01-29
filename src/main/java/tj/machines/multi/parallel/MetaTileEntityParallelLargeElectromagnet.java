package tj.machines.multi.parallel;

import tj.TJConfig;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.capability.impl.ParallelGAMultiblockRecipeLogic;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.FieldGenCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
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

import static tj.machines.multi.electric.MetaTileEntityLargeGreenhouse.glassPredicate;
import static tj.multiblockpart.TJMultiblockAbility.REDSTONE_CONTROLLER;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController.fieldGenPredicate;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.render.Textures.ELECTROMAGNETIC_SEPARATOR_OVERLAY;
import static gregtech.api.render.Textures.POLARIZER_OVERLAY;


public class MetaTileEntityParallelLargeElectromagnet extends ParallelRecipeMapMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, MAINTENANCE_HATCH, INPUT_ENERGY, REDSTONE_CONTROLLER};

    public MetaTileEntityParallelLargeElectromagnet(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GATileEntities.LARGE_ELECTROMAGNET.getRecipeMaps());
        this.recipeMapWorkable = new ParallelGAMultiblockRecipeLogic(this, this::getEUPercentage, this::getDurationPercentage, this::getChancePercentage, this::getStack);
        this.recipeMapWorkable.setMaxVoltage(this::getMaxVoltage);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityParallelLargeElectromagnet(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.parallel_large_electromagnet.description"));
        tooltip.add(I18n.format("tj.multiblock.parallel.description"));
        TooltipHelper.shiftText(tooltip, tip -> {
            tip.add(I18n.format("tj.multiblock.parallel.extend.tooltip"));
            super.addInformation(stack, player, tip, advanced);
        });
    }

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, UP, BACK);
        factoryPattern.aisle("~~~~~", "~XXX~", "~XXX~", "~XXX~", "~~~~~");
        for (int layer = 0; layer < this.parallelLayer; layer++) {
            factoryPattern.aisle("~C~C~", "X#X#X", "G###G", "X#X#X", "~C~C~");
            factoryPattern.aisle("~C~C~", "X#X#X", "GF#FG", "X#X#X", "~C~C~");
        }
        return factoryPattern
                .aisle("~C~C~", "X#X#X", "G###G", "X#X#X", "~C~C~")
                .aisle("~~~~~", "~XXX~", "~XSX~", "~XXX~", "~~~~~")
                .setAmountAtLeast('L', 10)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('G', glassPredicate())
                .where('F', fieldGenPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.BABBITT_ALLOY);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int fieldGen = context.getOrDefault("FieldGen", FieldGenCasing.CasingType.FIELD_GENERATOR_LV).getTier();
        this.maxVoltage = (long) (Math.pow(4, fieldGen) * 8);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.BABBITT_ALLOY_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return this.getRecipeMapIndex() == 0 ? POLARIZER_OVERLAY : ELECTROMAGNETIC_SEPARATOR_OVERLAY;
    }

    @Override
    public int getEUPercentage() {
        return TJConfig.parallelLargeElectromagnet.eutPercentage;
    }

    @Override
    public int getDurationPercentage() {
        return TJConfig.parallelLargeElectromagnet.durationPercentage;
    }

    @Override
    public int getChancePercentage() {
        return TJConfig.parallelLargeElectromagnet.chancePercentage;
    }

    @Override
    public int getStack() {
        return TJConfig.parallelLargeElectromagnet.stack;
    }

    @Override
    public int getMaxParallel() {
        return TJConfig.parallelLargeElectromagnet.maximumParallel;
    }
}
