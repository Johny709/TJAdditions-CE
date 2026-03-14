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
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.recipeproperties.BlastTemperatureProperty;
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
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.capability.IProgressBar;
import tj.capability.OverclockManager;
import tj.capability.ProgressBar;
import tj.capability.impl.handler.IFluidSupplyHandler;
import tj.capability.impl.workable.FluidRecipeLogic;
import tj.capability.impl.workable.ParallelRecipeLogic;
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
import static tj.machines.multi.electric.MetaTileEntityLargeAlloySmelter.heatingCoilPredicate;
import static tj.machines.multi.electric.MetaTileEntityLargeAlloySmelter.heatingCoilPredicate2;
import static tj.machines.multi.electric.MetaTileEntityVoidMOreMiner.PYROTHEUM;
import static tj.multiblockpart.TJMultiblockAbility.REDSTONE_CONTROLLER;


public class MetaTileEntityParallelVolcanus extends ParallelRecipeMapMultiblockController implements IProgressBar, IFluidSupplyHandler {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, IMPORT_FLUIDS, EXPORT_FLUIDS, INPUT_ENERGY, MAINTENANCE_HATCH, REDSTONE_CONTROLLER};

    private FluidStack pyro;
    private int blastFurnaceTemperature;
    private int bonusTemperature;

    public MetaTileEntityParallelVolcanus(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GATileEntities.VOLCANUS.recipeMap);
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
        TooltipHelper.shiftTextJEI(tooltip, tip -> {
            tip.add(I18n.format("gregtech.multiblock.vol_cryo.description"));
            tip.add(I18n.format("gtadditions.multiblock.electric_blast_furnace.tooltip.1"));
            tip.add(I18n.format("gtadditions.multiblock.electric_blast_furnace.tooltip.2"));
            tip.add(I18n.format("gtadditions.multiblock.electric_blast_furnace.tooltip.3"));
            super.addInformation(stack, player, tip, advanced);
        });
    }

    @Override
    protected ParallelRecipeLogic<IFluidSupplyHandler> createRecipeLogic() {
        return new FluidRecipeLogic(this);
    }

    @Override
    public void preOverclock(OverclockManager<?> overclockManager, Recipe recipe) {
        long recipeEUt = overclockManager.getEUt() * 4;
        int duration = overclockManager.getDuration();
        int heat = this.blastFurnaceTemperature - recipe.getRecipePropertyStorage().getRecipePropertyValue(BlastTemperatureProperty.getInstance(), 0);
        // Apply EUt discount for every 900K above the base recipe temperature
        recipeEUt /= (long) (1.00 + 0.05 * (heat / 900D));
        while (duration > 1 && recipeEUt <= this.maxVoltage) {
            if (heat < 1800) break;
            heat -= 1800;
            duration /= 4;
            recipeEUt *= 4;
        }
        overclockManager.setEUt(recipeEUt / 4);
        overclockManager.setDuration(duration);
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addTextComponent(new TextComponentTranslation("gregtech.multiblock.blast_furnace.max_temperature", this.blastFurnaceTemperature))
                .addTextComponent(new TextComponentTranslation("gtadditions.multiblock.blast_furnace.additional_temperature", this.bonusTemperature))
                .addFluidInputLine(this.importFluidTank, this.pyro);
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
        this.tier = GAUtility.getTierByVoltage(this.maxVoltage);
        this.bonusTemperature = Math.max(0, 100 * (this.tier - 2));
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
        return TJFluidUtils.getFluidAmountFromTanks(PYROTHEUM, this.getImportFluidTank());
    }

    private long getPyrotheumCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(PYROTHEUM, this.getImportFluidTank());
    }

    @Override
    public int getEUtMultiplier() {
        return TJConfig.parallelVolcanus.eutPercentage;
    }

    @Override
    public int getDurationMultiplier() {
        return TJConfig.parallelVolcanus.durationPercentage;
    }

    @Override
    public int getChanceMultiplier() {
        return TJConfig.parallelVolcanus.chancePercentage;
    }

    @Override
    public int getParallel() {
        return TJConfig.parallelVolcanus.stack;
    }

    @Override
    public int getMaxParallel() {
        return TJConfig.parallelVolcanus.maximumParallel;
    }

    @Override
    public FluidStack getFluidStack() {
        return this.pyro;
    }
}
