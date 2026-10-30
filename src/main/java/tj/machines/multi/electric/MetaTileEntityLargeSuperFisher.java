package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAUtility;
import gregicadditions.GAValues;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.RobotArmCasing;
import gregicadditions.item.metal.MetalCasing1;
import gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController;
import gregtech.api.gui.Widget;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.common.sound.GTSoundEvents;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJConfig;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.capability.impl.handler.IPosHandler;
import tj.capability.impl.workable.FisherWorkableHandler;
import tj.mui.TJGuiTextures;
import tj.mui.widgets.impl.TJToggleButtonWidget;
import tj.textures.TJTextures;
import tj.util.EnumFacingHelper;
import tj.util.TJUtility;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;

public class MetaTileEntityLargeSuperFisher extends TJMultiblockControllerBase implements IPosHandler {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, INPUT_ENERGY, MAINTENANCE_HATCH};
    private final FisherWorkableHandler workableHandler = new FisherWorkableHandler(this);
    private BlockPos blockPos = this.getPos();
    private long maxVoltage;
    private int parallel;

    public MetaTileEntityLargeSuperFisher(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeSuperFisher(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.4", TJConfig.largeNamingMachine.stack));
        tooltip.add(I18n.format("gregtech.machine.fisher.requirement", (int) Math.sqrt(25)));
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return abilities.containsKey(IMPORT_ITEMS) && abilities.containsKey(EXPORT_ITEMS) && abilities.containsKey(INPUT_ENERGY) && super.checkStructureComponents(parts, abilities);
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return false;
    }

    @Override
    protected void updateFormedValid() {
        if (((this.getProblems() >> 5) & 1) != 0)
            this.workableHandler.update();
    }

    @Override
    protected void mainDisplayTab(List<Widget> widgetGroup) {
        super.mainDisplayTab(widgetGroup);
        widgetGroup.add(new TJToggleButtonWidget(175, 151, 18, 18, TJGuiTextures.TOGGLE_DISTINCT_BUTTON, this.workableHandler::isDistinct, this.workableHandler::setDistinct)
                .setToggleTitleTooltipHoverText("machine.universal.toggle.distinct.mode.disabled", "machine.universal.toggle.distinct.mode.enabled"));
    }

    @Override
    protected void maintenanceDisplayTab(List<Widget> widgetGroup) {
        super.maintenanceDisplayTab(widgetGroup);
        widgetGroup.add(new TJToggleButtonWidget(175, 133, 18, 18, TJGuiTextures.TOGGLE_ITEM_VOID_BUTTON, this.workableHandler::isVoiding, this.workableHandler::setVoiding)
                .setToggleTitleTooltipHoverText("machine.universal.toggle.item_voiding.disabled", "machine.universal.toggle.item_voiding.enabled"));
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addVoltageInLine(this.getInputEnergyContainer())
                .addVoltageTierLine(GAUtility.getTierByVoltage(this.maxVoltage))
                .addEnergyInputLine(this.inputEnergyContainer, this.workableHandler.getEnergyPerTick())
                .addTranslationLine("tj.multiblock.parallel", this.parallel)
                .customLine(text -> text.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.universal.distinct")
                        .appendText(" ")
                        .appendSibling(this.workableHandler.isDistinct()
                                ? withButton(new TextComponentTranslation("gtadditions.multiblock.universal.distinct.yes"), "distinctEnabled")
                                : withButton(new TextComponentTranslation("gtadditions.multiblock.universal.distinct.no"), "distinctDisabled"))))
                .addIsWorkingLine(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive(), this.workableHandler.getProgress(), this.workableHandler.getMaxProgress(), this.workableHandler.isHasProblems())
                .addRecipeInputLine(this.workableHandler)
                .addRecipeOutputLine(this.workableHandler);
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        this.workableHandler.setDistinct(!componentData.equals("distinctEnabled"));
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("~~CCCCC~~", "~~XXXXX~~", "~~C~~~C~~", "~~C~~~C~~", "~~C~~~C~~", "~~~~~~~~~", "~~~~~~~~~")
                .aisle("~CCCCCCC~", "~X~~~~~X~", "~C~~~~~C~", "~C~~~~~C~", "~C~~~~~C~", "~~C~~~C~~", "~~~~~~~~~")
                .aisle("CC~~~~~CC", "X~~~~~~~X", "C~~~~~~~C", "C~~~~~~~C", "C~~~~~~~C", "~C~~~~~C~", "~~C~~~C~~")
                .aisle("CC~~~~~CC", "X~~###~~X", "~~~###~~~", "~~~###~~~", "~~~###~~~", "~~~###~~~", "~~~CCC~~~")
                .aisle("CC~~~~~CC", "X~~###~~X", "~~~###~~~", "~~~###~~~", "~~~###~~~", "~~~###~~~", "~~~CRC~~~")
                .aisle("CC~~~~~CC", "X~~###~~X", "~~~###~~~", "~~~###~~~", "~~~###~~~", "~~~###~~~", "~~~CCC~~~")
                .aisle("CC~~~~~CC", "X~~~~~~~X", "C~~~~~~~C", "C~~~~~~~C", "C~~~~~~~C", "~C~~~~~C~", "~~C~~~C~~")
                .aisle("~CCCCCCC~", "~X~~~~~X~", "~C~~~~~C~", "~C~~~~~C~", "~C~~~~~C~", "~~C~~~C~~", "~~~~~~~~~")
                .aisle("~~CCCCC~~", "~~XXSXX~~", "~~C~~~C~~", "~~C~~~C~~", "~~C~~~C~~", "~~~~~~~~~", "~~~~~~~~~")
                .where('S', this.selfPredicate())
                .where('C', statePredicate(this.getCasingState()))
                .where('X',statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('R', LargeSimpleRecipeMapMultiblockController.robotArmPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int tier = context.getOrDefault("RobotArm", RobotArmCasing.CasingType.ROBOT_ARM_LV).getTier();
        if (tier >= GAValues.MAX) {
            this.maxVoltage = this.inputEnergyContainer.getInputVoltage();
            this.maxVoltage += this.maxVoltage / Integer.MAX_VALUE;
            tier = TJUtility.getTierByVoltage(this.maxVoltage);
        } else this.maxVoltage = 8L << tier * 2;
        this.parallel = TJConfig.largeSuperFisher.stack * tier;
        this.workableHandler.initialize(this.getAbilities(IMPORT_ITEMS).size());
        this.blockPos = this.getPos().offset(this.getFrontFacing().getOpposite(), 4).offset(EnumFacing.DOWN);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.workableHandler.invalidate();
        this.maxVoltage = 0;
        this.parallel = 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TJTextures.TJ_MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, this.getFrontFacing(), this.workableHandler.isActive(), this.workableHandler.isHasProblems(), this.workableHandler.isWorkingEnabled());
        TJTextures.NAME_TAG.renderSided(EnumFacingHelper.getLeftFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
        TJTextures.NAME_TAG.renderSided(EnumFacingHelper.getRightFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.GRISIUM);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.GRISIUM_CASING;
    }

    @Override
    public long getMaxVoltage() {
        return this.maxVoltage;
    }

    @Override
    public int getParallel() {
        return this.parallel;
    }

    @Override
    public BlockPos getBlockPos() {
        return this.blockPos;
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
    @SideOnly(Side.CLIENT)
    public boolean shouldPlaySound() {
        return this.isStructureFormed() && this.workableHandler.isActive() && this.workableHandler.isWorkingEnabled();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SoundEvent getSound() {
        return GTSoundEvents.PUMP;
    }
}
