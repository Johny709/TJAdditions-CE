package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAValues;
import gregicadditions.Gregicality;
import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.item.components.MotorCasing;
import gregicadditions.item.components.PumpCasing;
import gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.blocks.BlockSolidCasings;
import tj.blocks.TJMetaBlocks;
import tj.capability.impl.workable.InfiniteFluidDrillWorkableHandler;
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.gui.TJGuiTextures;
import tj.textures.TJTextures;
import tj.util.TJFluidUtils;
import tj.util.TJUtility;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.UnaryOperator;

import static gregicadditions.GAMaterials.*;
import static net.minecraft.util.text.TextFormatting.RED;
import static tj.machines.multi.electric.MetaTileEntityVoidMOreMiner.DRILLING_MUD;


public class MetaTileEntityInfiniteFluidDrill extends TJMultiblockControllerBase implements IProgressBar {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {MultiblockAbility.IMPORT_FLUIDS, MultiblockAbility.EXPORT_FLUIDS,
            MultiblockAbility.INPUT_ENERGY, GregicAdditionsCapabilities.MAINTENANCE_HATCH};
    private final InfiniteFluidDrillWorkableHandler workableHandler = new InfiniteFluidDrillWorkableHandler(this);
    private long maxVoltage;
    private int tier;

    public MetaTileEntityInfiniteFluidDrill(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityInfiniteFluidDrill(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        final int drillingMud = (int) Math.pow(4, (9 - GAValues.EV)) * 10;
        final int outputFluid = (int) Math.pow(4, (9 - GAValues.EV)) * 4000;
        tooltip.add(I18n.format("gtadditions.multiblock.drilling_rig.tooltip.1"));
        tooltip.add(I18n.format("tj.multiblock.drilling_rig.voltage", GAValues.VN[9], GAValues.VN[14]));
        tooltip.add(I18n.format("gtadditions.multiblock.drilling_rig.tooltip.void.2"));
        tooltip.add(I18n.format("gtadditions.multiblock.drilling_rig.tooltip.4", outputFluid, GAValues.VN[9]));
        tooltip.add(I18n.format("tj.multiblock.drilling_rig.tooltip.drilling_mud", drillingMud, drillingMud));
        tooltip.add(I18n.format("gtadditions.multiblock.large_chemical_reactor.tooltip.1"));
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        final int fluidInputsCount = abilities.getOrDefault(MultiblockAbility.IMPORT_FLUIDS, Collections.emptyList()).size();
        final int fluidOutputsCount = abilities.getOrDefault(MultiblockAbility.EXPORT_FLUIDS, Collections.emptyList()).size();

        return fluidInputsCount >= 1 && fluidOutputsCount >= 1 && abilities.containsKey(MultiblockAbility.INPUT_ENERGY) && super.checkStructureComponents(parts, abilities);
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return false;
    }

    @Override
    protected void updateFormedValid() {
        if (this.tier > GAValues.UV && ((this.getProblems() >> 5) & 1) != 0 && this.workableHandler.getVeinFluid() != null)
            this.workableHandler.update();
    }

    @Override
    protected void mainDisplayTab(List<Widget> widgetGroup) {
        super.mainDisplayTab(widgetGroup);
        widgetGroup.add(new ToggleButtonWidget(175, 151, 18, 18, TJGuiTextures.FLUID_VOID_BUTTON, this.workableHandler::isVoidingFluids, this.workableHandler::setVoidingFluids)
                .setTooltipText("machine.universal.toggle.fluid_voiding"));
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;

        if (this.workableHandler.getVeinFluid() == null) {
            builder.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.drilling_rig.no_fluid").setStyle(new Style().setColor(RED)));
            return;
        }
        builder.addVoltageInLine(this.inputEnergyContainer)
                .addVoltageTierLine(this.tier)
                .addEnergyInputLine(this.inputEnergyContainer, this.maxVoltage)
                .addFluidInputLine(this.importFluidTank, DRILLING_MUD, this.workableHandler.getDrillingMudAmount())
                .addTranslationLine("gtadditions.multiblock.drilling_rig.fluid", this.workableHandler.getVeinFluid().getName())
                .addIsWorkingLine(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive(), this.workableHandler.getProgress(), this.workableHandler.getMaxProgress(), this.workableHandler.hasProblem())
                .addRecipeInputLine(this.workableHandler)
                .addRecipeOutputLine(this.workableHandler);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CF~FC", "CF~FC", "CCCCC", "~XXX~", "~~C~~", "~~C~~", "~~C~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("F~~~F", "F~~~F", "CCMCC", "X###X", "~C#C~", "~C#C~", "~C#C~", "~FCF~", "~FFF~", "~FFF~", "~~F~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("~~~~~", "~~~~~", "CMPMC", "X#T#X", "C#T#C", "C#T#C", "C#T#C", "~CCC~", "~FCF~", "~FFF~", "~FFF~", "~~F~~", "~~F~~", "~~F~~")
                .aisle("F~~~F", "F~~~F", "CCMCC", "X###X", "~C#C~", "~C#C~" ,"~C#C~", "~FCF~", "~FFF~", "~FFF~", "~~F~~", "~~~~~", "~~~~~", "~~~~~")
                .aisle("CF~FC", "CF~FC", "CCCCC", "~XSX~", "~~C~~", "~~C~~", "~~C~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~", "~~~~~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('F', statePredicate(MetaBlocks.FRAMES.get(Seaborgium).getDefaultState()))
                .where('T', statePredicate(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TUNGSTENSTEEL_PIPE)))
                .where('M', LargeSimpleRecipeMapMultiblockController.motorPredicate())
                .where('P', LargeSimpleRecipeMapMultiblockController.pumpPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return TJMetaBlocks.SOLID_CASING.getState(BlockSolidCasings.SolidCasingType.SEABORGIUM_CASING);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        final int motorTier = context.getOrDefault("Motor", MotorCasing.CasingType.MOTOR_LV).getTier();
        final int pumpTier = context.getOrDefault("Pump", PumpCasing.CasingType.PUMP_LV).getTier();
        final int tier = Math.min(motorTier, pumpTier);
        if (tier >= GAValues.MAX) {
            this.maxVoltage = this.inputEnergyContainer.getInputVoltage();
            this.maxVoltage += this.maxVoltage / Integer.MAX_VALUE;
        } else this.maxVoltage = 8L << tier * 2;
        this.tier = TJUtility.getTierByVoltage(this.maxVoltage);
        this.workableHandler.initialize(this.tier);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return TJTextures.SEABORGIUM;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TJTextures.TJ_MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, this.frontFacing, this.workableHandler.isActive(), this.workableHandler.hasProblem(), this.workableHandler.isWorkingEnabled());
    }

    @Override
    public String getRecipeUid() {
        return Gregicality.MODID + ":drilling_rig";
    }

    @Override
    public int[][] getBarMatrix() {
        return new int[1][1];
    }

    @Override
    public void getProgressBars(Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars) {
        bars.add(bar -> bar.setProgress(this::getDrillingMudAmount).setMaxProgress(this::getDrillingMudCapacity)
                .setLocale("tj.multiblock.bars.fluid").setParams(() -> new Object[]{DRILLING_MUD.getLocalizedName()})
                .setFluidStackSupplier(() -> DRILLING_MUD));
    }

    private long getDrillingMudAmount() {
        return TJFluidUtils.getFluidAmountFromTanks(DRILLING_MUD, this.getImportFluidTank());
    }

    private long getDrillingMudCapacity() {
        return TJFluidUtils.getFluidCapacityFromTanks(DRILLING_MUD, this.getImportFluidTank());
    }

    @Override
    public long getMaxVoltage() {
        return this.maxVoltage;
    }
}
