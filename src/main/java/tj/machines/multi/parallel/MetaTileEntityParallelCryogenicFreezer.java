package tj.machines.multi.parallel;

import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.GATileEntities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJConfig;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.capability.impl.workable.ParallelGAMultiblockRecipeLogic;
import tj.util.TJFluidUtils;
import tj.util.TooltipHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Queue;
import java.util.function.UnaryOperator;

import static gregicadditions.GAMaterials.Cryotheum;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static tj.machines.multi.electric.MetaTileEntityVoidMOreMiner.CRYOTHEUM;
import static tj.multiblockpart.TJMultiblockAbility.REDSTONE_CONTROLLER;


public class MetaTileEntityParallelCryogenicFreezer extends ParallelRecipeMapMultiblockController implements IProgressBar {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, IMPORT_FLUIDS, EXPORT_FLUIDS, INPUT_ENERGY, MAINTENANCE_HATCH, REDSTONE_CONTROLLER};
    private FluidStack cryotheum;

    public MetaTileEntityParallelCryogenicFreezer(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GATileEntities.VACUUM_FREEZER.recipeMap);
        this.recipeMapWorkable = new ParallelGAMultiblockRecipeLogic(this, this::getEUPercentage, this::getDurationPercentage, this::getChancePercentage, this::getStack) {

            @Override
            protected boolean drawEnergy(long recipeEUt) {
                FluidStack drained = this.getInputTank().drain(cryotheum, true);
                if (drained == null || drained.amount != cryotheum.amount)
                    return false;
                return super.drawEnergy(recipeEUt);
            }
        };
        this.recipeMapWorkable.setMaxVoltage(this::getMaxVoltage);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityParallelCryogenicFreezer(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.parallel_cryogenic_freezer.description"));
        tooltip.add(I18n.format("tj.multiblock.parallel.description"));
        TooltipHelper.shiftText(tooltip, tip -> {
            tip.add(I18n.format("gregtech.multiblock.vol_cryo.description"));
            super.addInformation(stack, player, tip, advanced);
        });
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.fluidInputLine(this.importFluidTank, this.cryotheum);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, FRONT, DOWN);
        for (int layer = 0; layer < this.parallelLayer; layer++) {
            String entityP = layer == 0 ? "XXXXX" : "XXPXX";
            if (layer % 4 == 0) {
                String entityS = layer >= this.parallelLayer - 4 ? "~XSX~" : "~XXX~";
                factoryPattern.aisle("~XXX~", "XXXXX", entityP, "XXXXX", "~XXX~");
                factoryPattern.aisle(entityS, "X#P#X", "XPPPX", "X#P#X", "~XXX~");
            }
        }
        return factoryPattern.aisle("~XXX~", "XXXXX", "XXXXX", "XXXXX", "~XXX~")
                .setAmountAtLeast('L', 16)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('P', statePredicate(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE)))
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.INCOLOY_MA956);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.maxVoltage = this.getAbilities(INPUT_ENERGY).stream()
                .mapToLong(IEnergyContainer::getInputVoltage)
                .filter(voltage -> voltage <= GAValues.V[7])
                .max()
                .orElse(GAValues.V[7]);
        this.cryotheum = Cryotheum.getFluid((int) Math.pow(2, GAUtility.getTierByVoltage(this.maxVoltage)));
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.INCOLOY_MA956_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return ClientHandler.FREEZER_OVERLAY;
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        bars.add(bar -> bar.setProgress(this::getCryotheumAmount).setMaxProgress(this::getCryotheumCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{CRYOTHEUM.getLocalizedName()})
                .setFluidStackSupplier(() -> CRYOTHEUM));
    }

    private long getCryotheumAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(CRYOTHEUM, this.getImportFluidTank());
    }

    private long getCryotheumCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(CRYOTHEUM, this.getImportFluidTank());
    }

    @Override
    public int getEUPercentage() {
        return TJConfig.parallelCryogenicFreezer.eutPercentage;
    }

    @Override
    public int getDurationPercentage() {
        return TJConfig.parallelCryogenicFreezer.durationPercentage;
    }

    @Override
    public int getChancePercentage() {
        return TJConfig.parallelCryogenicFreezer.chancePercentage;
    }

    @Override
    public int getStack() {
        return TJConfig.parallelCryogenicFreezer.stack;
    }

    @Override
    public int getMaxParallel() {
        return TJConfig.parallelCryogenicFreezer.maximumParallel;
    }
}
