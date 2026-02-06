package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAUtility;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.ConveyorCasing;
import gregicadditions.item.components.RobotArmCasing;
import gregicadditions.item.metal.MetalCasing1;
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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.TJConfig;
import tj.capability.impl.workable.ChiselWorkbenchWorkableHandler;
import tj.builder.multicontrollers.ExtendableMultiblockController;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.textures.TJTextures;
import tj.util.EnumFacingHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController.conveyorPredicate;
import static gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController.robotArmPredicate;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;

public class MetaTileEntityLargeChiselWorkbench extends ExtendableMultiblockController {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, INPUT_ENERGY, MAINTENANCE_HATCH};
    private final ChiselWorkbenchWorkableHandler workableHandler = new ChiselWorkbenchWorkableHandler(this);
    private long maxVoltage;
    private int parallel;

    public MetaTileEntityLargeChiselWorkbench(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeChiselWorkbench(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.4", TJConfig.largeChiselWorkbench.stack));
        tooltip.add(I18n.format("tj.multiblock.large_chisel_workbench.description"));
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return abilities.containsKey(IMPORT_ITEMS) && abilities.containsKey(EXPORT_ITEMS) && abilities.containsKey(INPUT_ENERGY) && super.checkStructureComponents(parts, abilities);
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (this.isStructureFormed())
            builder.voltageInLine(this.inputEnergyContainer)
                    .voltageTierLine(GAUtility.getTierByVoltage(this.maxVoltage))
                    .energyInputLine(this.inputEnergyContainer, this.workableHandler.getEnergyPerTick())
                    .addTranslationLine("tj.multiblock.industrial_fusion_reactor.message", this.parallel)
                    .customLine(text -> text.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.universal.distinct")
                            .appendText(" ")
                            .appendSibling(this.workableHandler.isDistinct()
                                    ? withButton(new TextComponentTranslation("gtadditions.multiblock.universal.distinct.yes"), "distinctEnabled")
                                    : withButton(new TextComponentTranslation("gtadditions.multiblock.universal.distinct.no"), "distinctDisabled"))))
                    .isWorkingLine(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive(), this.workableHandler.getProgress(), this.workableHandler.getMaxProgress())
                    .addRecipeInputLine(this.workableHandler)
                    .addRecipeOutputLine(this.workableHandler);
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        this.workableHandler.setDistinct(!componentData.equals("distinctEnabled"));
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
    protected BlockPattern createStructurePattern() {
        FactoryBlockPattern factoryPattern = FactoryBlockPattern.start(RIGHT, UP, BACK);
        for (int layer = 0; layer < this.parallelLayer; layer++) {
            factoryPattern.aisle("XXX", "XXX", "C~C", "C~C");
            factoryPattern.aisle("XXX", "XcX", "###", "CrC");
        }
        return factoryPattern.aisle("XXX", "XSX", "C~C", "C~C")
                .setAmountAtLeast('L', 12)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('c', conveyorPredicate())
                .where('r', robotArmPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_1.getState(MetalCasing1.CasingType.MARAGING_STEEL_250);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        ConveyorCasing.CasingType conveyor = context.getOrDefault("Conveyor", ConveyorCasing.CasingType.CONVEYOR_LV);
        RobotArmCasing.CasingType robotArm = context.getOrDefault("RobotArm", RobotArmCasing.CasingType.ROBOT_ARM_LV);
        int min = Math.min(conveyor.getTier(), robotArm.getTier());
        this.workableHandler.initialize(this.getAbilities(IMPORT_ITEMS).size());
        this.maxVoltage = (long) (Math.pow(4, min) * 8);
        this.parallel = TJConfig.largeChiselWorkbench.stack * min;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.MARAGING_STEEL_250_CASING;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TJTextures.TJ_ASSEMBLER_OVERLAY.render(renderState, translation, pipeline, this.frontFacing, this.workableHandler.isActive(), this.workableHandler.hasProblem(), this.workableHandler.isWorkingEnabled());
        TJTextures.CHISEL.renderSided(EnumFacingHelper.getLeftFacingFrom(this.frontFacing), renderState, translation, pipeline);
        TJTextures.CHISEL.renderSided(EnumFacingHelper.getRightFacingFrom(this.frontFacing), renderState, translation, pipeline);
    }

    @Override
    public void setWorkingEnabled(boolean isWorking) {
        this.workableHandler.setWorkingEnabled(isWorking);
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workableHandler.isWorkingEnabled();
    }

    @Override
    public int getMaxParallel() {
        return TJConfig.largeChiselWorkbench.maximumSlices;
    }

    @Override
    public long getMaxVoltage() {
        return this.maxVoltage;
    }

    @Override
    public int getParallel() {
        return this.parallel;
    }
}
