package tj.machines.multi.parallel;

import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.metal.MetalCasing1;
import gregtech.api.capability.IEnergyContainer;
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
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJConfig;
import tj.builder.ParallelRecipeMap;
import tj.builder.handlers.ParallelVolcanusRecipeLogic;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.util.TJFluidUtils;
import tj.util.TooltipHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Queue;
import java.util.function.UnaryOperator;

import static gregicadditions.GAMaterials.Pyrotheum;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MUFFLER_HATCH;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.recipes.RecipeMaps.BLAST_RECIPES;
import static tj.TJRecipeMaps.PARALLEL_BLAST_RECIPES;
import static tj.machines.multi.electric.MetaTileEntityLargeAlloySmelter.heatingCoilPredicate;
import static tj.machines.multi.electric.MetaTileEntityLargeAlloySmelter.heatingCoilPredicate2;
import static tj.machines.multi.electric.MetaTileEntityVoidMOreMiner.PYROTHEUM;
import static tj.multiblockpart.TJMultiblockAbility.REDSTONE_CONTROLLER;


public class MetaTileEntityParallelVolcanus extends ParallelRecipeMapMultiblockController implements IProgressBar {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, IMPORT_FLUIDS, EXPORT_FLUIDS, INPUT_ENERGY, MAINTENANCE_HATCH, REDSTONE_CONTROLLER};

    private FluidStack pyro;
    private int blastFurnaceTemperature;
    private int bonusTemperature;

    public MetaTileEntityParallelVolcanus(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, new ParallelRecipeMap[]{PARALLEL_BLAST_RECIPES});
        this.recipeMapWorkable = new ParallelVolcanusRecipeLogic(this, this::getBlastFurnaceTemperature, this::getPyroConsumeAmount, this::getEUPercentage, this::getDurationPercentage, this::getChancePercentage, this::getStack);
        this.recipeMapWorkable.setMaxVoltage(this::getMaxVoltage);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityParallelVolcanus(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.parallel_volcanus.description"));
        tooltip.add(I18n.format("tj.multiblock.parallel.description"));
        TooltipHelper.shiftText(tooltip, tip -> {
            tip.add(I18n.format("tj.multiblock.parallel.extend.tooltip"));
            tip.add(I18n.format("gregtech.multiblock.vol_cryo.description"));
            tip.add(I18n.format("gtadditions.multiblock.electric_blast_furnace.tooltip.1"));
            tip.add(I18n.format("gtadditions.multiblock.electric_blast_furnace.tooltip.2"));
            tip.add(I18n.format("gtadditions.multiblock.electric_blast_furnace.tooltip.3"));
            super.addInformation(stack, player, tip, advanced);
        });
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.blast_furnace.max_temperature", this.blastFurnaceTemperature))
                .addTextComponent(new TextComponentTranslation("gtadditions.multiblock.blast_furnace.additional_temperature", this.bonusTemperature))
                .fluidInputLine(this.inputFluidInventory, this.pyro);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, FRONT, DOWN);
        for (int layer = 0; layer < this.parallelLayer; layer++) {
            if (layer % 4 == 0) {
                String muffler = layer == 0 ? "XXMXX" : "XXPXX";
                factoryPattern.aisle("XXXXX", "XXXXX", muffler, "XXXXX", "XXXXX");
                factoryPattern.aisle("ccccc", "c#c#c", "ccPcc", "c#c#c", "ccccc");
                factoryPattern.aisle("ccccc", "c#c#c", "ccPcc", "c#c#c", "ccccc");
            }
        }
        return factoryPattern.aisle("XXSXX", "XXXXX", "XXXXX", "XXXXX", "XXXXX")
                .setAmountAtLeast('L', 12)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('c', heatingCoilPredicate().or(heatingCoilPredicate2()))
                .where('P', statePredicate(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE)))
                .where('#', isAirPredicate())
                .where('M', abilityPartPredicate(MUFFLER_HATCH))
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.HASTELLOY_N);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.maxVoltage = this.getAbilities(INPUT_ENERGY).stream()
                .mapToLong(IEnergyContainer::getInputVoltage)
                .filter(voltage -> voltage <= GAValues.V[7])
                .max()
                .orElse(GAValues.V[7]);
        int energyTier = GAUtility.getTierByVoltage(this.maxVoltage);
        this.bonusTemperature = Math.max(0, 100 * (energyTier - 2));
        this.blastFurnaceTemperature = context.getOrDefault("blastFurnaceTemperature", 0);
        this.blastFurnaceTemperature += this.bonusTemperature;
        this.pyro = Pyrotheum.getFluid((int) Math.pow(2, GAUtility.getTierByVoltage(this.maxVoltage)));
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.HASTELLOY_N_CASING;
    }

    @Nonnull
    @Override
    protected OrientedOverlayRenderer getFrontOverlay() {
        return Textures.PRIMITIVE_BLAST_FURNACE_OVERLAY;
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        bars.add(bar -> bar.setProgress(this::getPyrotheumAmount).setMaxProgress(this::getPyrotheumCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{PYROTHEUM.getLocalizedName()})
                .setFluidStackSupplier(() -> PYROTHEUM));
    }

    private long getPyrotheumAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(PYROTHEUM, this.getInputFluidInventory());
    }

    private long getPyrotheumCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(PYROTHEUM, this.getInputFluidInventory());
    }

    public int getBlastFurnaceTemperature() {
        return this.blastFurnaceTemperature;
    }

    public FluidStack getPyroConsumeAmount() {
        return this.pyro;
    }

    @Override
    public int getEUPercentage() {
        return TJConfig.parallelVolcanus.eutPercentage;
    }

    @Override
    public int getDurationPercentage() {
        return TJConfig.parallelVolcanus.durationPercentage;
    }

    @Override
    public int getChancePercentage() {
        return TJConfig.parallelVolcanus.chancePercentage;
    }

    @Override
    public int getStack() {
        return TJConfig.parallelVolcanus.stack;
    }

    @Override
    public int getMaxParallel() {
        return TJConfig.parallelVolcanus.maximumParallel;
    }

    @Override
    public RecipeMap<?>[] getRecipeMaps() {
        return new RecipeMap[]{BLAST_RECIPES};
    }
}
