package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import gregicadditions.GAValues;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MTETrait;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fluids.FluidStack;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.builder.handlers.VoidMOreMinerWorkableHandler;
import tj.builder.multicontrollers.TJMultiblockDisplayBase;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.gui.TJGuiTextures;
import tj.textures.TJTextures;
import tj.util.TJFluidUtils;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.UnaryOperator;

import static tj.textures.TJTextures.HEAVY_QUARK_DEGENERATE_MATTER;
import static gregicadditions.GAMaterials.*;

public class MetaTileEntityVoidMOreMiner extends TJMultiblockDisplayBase implements IProgressBar {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.EXPORT_ITEMS, MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_FLUIDS,
            MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};
    public static final FluidStack DRILLING_MUD = DrillingMud.getFluid(1);
    public static final FluidStack PYROTHEUM = Pyrotheum.getFluid(1);
    public static final FluidStack CRYOTHEUM = Cryotheum.getFluid(1);

    private final VoidMOreMinerWorkableHandler workableHandler = new VoidMOreMinerWorkableHandler(this);
    private IMultipleTankHandler importFluidHandler;
    private IMultipleTankHandler exportFluidHandler;
    private ItemHandlerList outputInventory;
    private IEnergyContainer energyContainer;
    private long maxVoltage;
    private int tier;

    public MetaTileEntityVoidMOreMiner(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.workableHandler.setExportItemsSupplier(this::getOutputInventory)
                .setImportFluidsSupplier(this::getImportFluidHandler)
                .setExportFluidsSupplier(this::getExportFluidHandler)
                .setImportEnergySupplier(this::getEnergyContainer)
                .setMaxVoltageSupplier(this::getMaxVoltage);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityVoidMOreMiner(this.metaTileEntityId);
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        int fluidInputsCount = abilities.getOrDefault(MultiblockAbility.IMPORT_FLUIDS, Collections.emptyList()).size();
        int fluidOutputsCount = abilities.getOrDefault(MultiblockAbility.EXPORT_FLUIDS, Collections.emptyList()).size();

        return fluidInputsCount >= 1 && fluidOutputsCount >= 1 && abilities.containsKey(MultiblockAbility.INPUT_ENERGY) && super.checkStructureComponents(parts, abilities);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gtadditions.multiblock.void_miner.description.1"));
        tooltip.add(I18n.format("gtadditions.multiblock.void_miner.description.2"));
        tooltip.add(I18n.format("gtadditions.multiblock.void_miner.description.3"));
        tooltip.add(I18n.format("gtadditions.multiblock.void_miner.description.4"));
        tooltip.add(I18n.format("gtadditions.multiblock.void_miner.description.5"));
        tooltip.add(I18n.format("gtadditions.multiblock.void_miner.description.6"));
        tooltip.add(I18n.format("tj.multiblock.void_more_miner.description"));
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.voltageInLine(this.energyContainer)
                .voltageTierLine(this.tier)
                .energyInputLine(this.energyContainer, this.workableHandler.getEnergyPerTick())
                .temperatureLine(this.workableHandler.heat(), this.workableHandler.maxHeat())
                .isWorkingLine(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive(), this.workableHandler.getProgress(), this.workableHandler.getMaxProgress())
                .customLine(text -> {
                    if (this.workableHandler.isOverheat())
                        text.addTextComponent(new TextComponentTranslation("gregtech.multiblock.universal.overheat").setStyle(new Style().setColor(TextFormatting.RED)));
                }).addRecipeInputLine(this.workableHandler)
                .addRecipeOutputLine(this.workableHandler);
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return false;
    }

    @Override
    protected void updateFormedValid() {
        if (this.tier > GTValues.ZPM && ((this.getProblems() >> 5) & 1) != 0)
            this.workableHandler.update();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.importFluidHandler = new FluidTankList(true, this.getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.exportFluidHandler = new FluidTankList(true, this.getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        this.outputInventory = new ItemHandlerList(this.getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.energyContainer = new EnergyContainerList(this.getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.tier = context.getOrDefault("Motor", MotorCasing.CasingType.MOTOR_LV).getTier();
        this.maxVoltage = GAValues.VA[this.tier];
        this.workableHandler.initialize(this.tier);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.importFluidHandler = new FluidTankList(true);
        this.exportFluidHandler = new FluidTankList(true);
        this.outputInventory = new ItemHandlerList(Lists.newArrayList());
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCCCCCCC", "CCCCCCCCC", "C#######C", "C#######C", "C#######C", "CCCCCCCCC", "CFFFFFFFC", "CFFFFFFFC", "C#######C", "C#######C")
                .aisle("C#######C", "C#######C", "#########", "#########", "#########", "C###D###C", "F##DDD##F", "F##DDD##F", "###DDD###", "#########")
                .aisle("C#######C", "C#######C", "#########", "####D####", "###DDD###", "C##DDD##C", "F#DD#DD#F", "F#D###D#F", "##D###D##", "#########")
                .aisle("C###D###C", "C###D###C", "###DDD###", "###D#D###", "##DD#DD##", "C#D###D#C", "FDD###DDF", "FD#####DF", "#D#####D#", "#########")
                .aisle("C##DMD##C", "C##DMD##C", "###DMD###", "##D###D##", "##D###D##", "CDD###DDC", "FD#####DF", "FD#####DF", "#D#####D#", "#########")
                .aisle("C###D###C", "C###D###C", "###DDD###", "###D#D###", "##DD#DD##", "C#D###D#C", "FDD###DDF", "FD#####DF", "#D#####D#", "#########")
                .aisle("C#######C", "C#######C", "#########", "####D####", "###DDD###", "C##DDD##C", "F#DD#DD#F", "F#D###D#F", "##D###D##", "#########")
                .aisle("C#######C", "C#######C", "#########", "#########", "#########", "C###D###C", "F##DDD##F", "F##DDD##F", "###DDD###", "#########")
                .aisle("CCCCCCCCC", "CCCCSCCCC", "C#######C", "C#######C", "C#######C", "CCCCCCCCC", "CFFFFFFFC", "CFFFFFFFC", "C#######C", "C#######C")
                .setAmountAtLeast('L', 100)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('C', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('D', statePredicate(TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.PERIODICIUM)))
                .where('F', statePredicate(MetaBlocks.FRAMES.get(QCDMatter).getDefaultState()))
                .where('M', LargeSimpleRecipeMapMultiblockController.motorPredicate())
                .where('#', (tile) -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.HEAVY_QUARK_DEGENERATE_MATTER);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return HEAVY_QUARK_DEGENERATE_MATTER;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TJTextures.TJ_MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, this.frontFacing, this.workableHandler.isActive(), this.workableHandler.hasProblem(), this.workableHandler.isWorkingEnabled());
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.workableHandler.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workableHandler.isWorkingEnabled();
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[][]{{0}, {0, 0, 0}};
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        bars.add(bar -> bar.setProgress(this.workableHandler::heat).setMaxProgress(this.workableHandler::maxHeat)
                .setLocale("tj.multiblock.bars.heat")
                .setBarTexture(TJGuiTextures.BAR_RED));
        bars.add(bar -> bar.setProgress(this::getDrillingMudAmount).setMaxProgress(this::getDrillingMudCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{DRILLING_MUD.getLocalizedName()})
                .setFluidStackSupplier(() -> DRILLING_MUD));
        bars.add(bar -> bar.setProgress(this::getPyrotheumAmount).setMaxProgress(this::getPyrotheumCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{PYROTHEUM.getLocalizedName()})
                .setFluidStackSupplier(() -> PYROTHEUM));
        bars.add(bar -> bar.setProgress(this::getCryotheumAmount).setMaxProgress(this::getCryotheumCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{CRYOTHEUM.getLocalizedName()})
                .setFluidStackSupplier(() -> CRYOTHEUM));
    }

    private long getDrillingMudAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(DRILLING_MUD, this.getImportFluidHandler());
    }

    private long getDrillingMudCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(DRILLING_MUD, this.getImportFluidHandler());
    }

    private long getPyrotheumAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(PYROTHEUM, this.getImportFluidHandler());
    }

    private long getPyrotheumCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(PYROTHEUM, this.getImportFluidHandler());
    }

    private long getCryotheumAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(CRYOTHEUM, this.getImportFluidHandler());
    }

    private long getCryotheumCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(CRYOTHEUM, this.getImportFluidHandler());
    }

    private ItemHandlerList getOutputInventory() {
        return this.outputInventory;
    }

    private IMultipleTankHandler getImportFluidHandler() {
        return this.importFluidHandler;
    }

    private IMultipleTankHandler getExportFluidHandler() {
        return this.exportFluidHandler;
    }

    private IEnergyContainer getEnergyContainer() {
        return this.energyContainer;
    }

    private long getMaxVoltage() {
        return this.maxVoltage;
    }
}
