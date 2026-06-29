package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAValues;
import gregicadditions.item.GAHeatingCoil;
import gregtech.api.gui.Widget;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockFireboxCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.capability.impl.handler.ICoilHandler;
import tj.capability.impl.workable.MultiSmelterWorkableHandler;
import tj.mui.TJGuiTextures;
import tj.mui.widgets.impl.TJToggleButtonWidget;
import tj.textures.TJTextures;
import tj.util.EnumFacingHelper;
import tj.util.TJUtility;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.MUFFLER_HATCH;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;

public class MetaTileEntityTJMultiSmelter extends TJMultiblockControllerBase implements ICoilHandler {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, INPUT_ENERGY, MAINTENANCE_HATCH};
    private final MultiSmelterWorkableHandler workableHandler = new MultiSmelterWorkableHandler(this);
    private final Set<BlockPos> activeStates = new HashSet<>();
    private long maxVoltage;
    private int coilEnergyDiscount;
    private int coilLevel;
    private int tier;

    public MetaTileEntityTJMultiSmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.workableHandler.setActiveConsumer(this::replaceCoilsAsActive);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityTJMultiSmelter(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.temporary"));
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return abilities.containsKey(INPUT_ENERGY) && abilities.containsKey(IMPORT_ITEMS) && abilities.containsKey(EXPORT_ITEMS) && super.checkStructureComponents(parts, abilities);
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
        if (!this.hasDistinct()) return;
        widgetGroup.add(new TJToggleButtonWidget(175, 151, 18, 18, TJGuiTextures.TOGGLE_DISTINCT_BUTTON, this.workableHandler::isDistinct, this.workableHandler::setDistinct)
                .setToggleTitleTooltipHoverText("machine.universal.toggle.distinct.mode.disabled", "machine.universal.toggle.distinct.mode.enabled"));
    }

    @Override
    protected void addDisplayText(GUIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.addVoltageInLine(this.inputEnergyContainer)
                .addVoltageTierLine(this.tier)
                .addEnergyInputLine(this.inputEnergyContainer, this.workableHandler.getEnergyPerTick())
                .addParallelLine(this.workableHandler.getParallelsPerformed(), this.getParallel())
                .addTranslationLine("gregtech.multiblock.multi_furnace.heating_coil_level", this.coilLevel)
                .addTranslationLine("gregtech.multiblock.multi_furnace.heating_coil_discount", this.coilEnergyDiscount)
                .addIsWorkingLine(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive(), this.workableHandler.getProgress(), this.workableHandler.getMaxProgress(), this.workableHandler.hasProblem(), 998)
                .addRecipeInputLine(this.workableHandler, 999)
                .addRecipeOutputLine(this.workableHandler, 1000);
        if (this.hasDistinct())
            builder.addDistinctLine(this.workableHandler.isDistinct(), 997);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "CCC", "XXX")
                .aisle("XXX", "C#C", "XMX")
                .aisle("XSX", "CCC", "XXX")
                .where('S', this.selfPredicate())
                .where('X', statePredicate(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF)).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('C', coilPredicate())
                .where('M', abilityPartPredicate(MUFFLER_HATCH))
                .where('#', isAirPredicate())
                .build();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.activeStates.addAll(context.getOrDefault("coilPos", new HashSet<>()));
        this.workableHandler.initialize(this.getAbilities(IMPORT_ITEMS).size());
        this.coilLevel = context.getOrDefault("coilLevel", 0);
        this.coilEnergyDiscount = context.getOrDefault("coilEnergyDiscount", 0);
        this.maxVoltage = Math.max(this.inputEnergyContainer.getInputVoltage(), this.outputEnergyContainer.getOutputVoltage());
        this.tier = this.maxVoltage >= Integer.MAX_VALUE ? 14 : TJUtility.getTierByVoltage(this.maxVoltage);
        if (this.tier >= GAValues.MAX) {
            this.maxVoltage += this.maxVoltage / Integer.MAX_VALUE;
            this.tier = TJUtility.getTierByVoltage(this.maxVoltage); // correct tier for post MAX
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.activeStates.clear();
        this.coilEnergyDiscount = 0;
        this.coilLevel = 0;
        this.maxVoltage = 0;
        this.tier = 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TJTextures.TJ_MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, this.getFrontFacing(), this.workableHandler.isActive(), this.workableHandler.hasProblem(), this.workableHandler.isWorkingEnabled());
        TJTextures.TJ_LOGO.renderSided(EnumFacingHelper.getLeftFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
        TJTextures.TJ_LOGO.renderSided(EnumFacingHelper.getRightFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        this.writeActiveBlockPacket(buf, this.workableHandler.isActive());
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.readActiveBlockPacket(buf);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 128) {
            this.readActiveBlockPacket(buf);
        }
    }

    private void writeActiveBlockPacket(PacketBuffer buffer, boolean isActive) {
        buffer.writeBoolean(isActive);
        buffer.writeInt(this.activeStates.size());
        for (BlockPos pos : this.activeStates) {
            buffer.writeBlockPos(pos);
        }
    }

    private void readActiveBlockPacket(PacketBuffer buffer) {
        boolean isActive = buffer.readBoolean();
        int size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            BlockPos pos = buffer.readBlockPos();
            IBlockState state = this.getWorld().getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof BlockWireCoil) {
                state = state.withProperty(BlockFireboxCasing.ACTIVE, isActive);
                this.getWorld().setBlockState(pos, state);
            } else if (block instanceof GAHeatingCoil) {
                state = state.withProperty(GAHeatingCoil.ACTIVE, isActive);
                this.getWorld().setBlockState(pos, state);
            }
        }
    }

    private void replaceCoilsAsActive(boolean isActive) {
        this.writeCustomData(128, buffer -> this.writeActiveBlockPacket(buffer, isActive));
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!this.getWorld().isRemote) {
            this.replaceCoilsAsActive(false);
        }
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.HEAT_PROOF_CASING;
    }

    @Override
    public int getParallel() {
        return 32 * this.coilLevel;
    }

    @Override
    public int getCoilEnergyDiscount() {
        return this.coilEnergyDiscount;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    @Override
    public long getMaxVoltage() {
        return this.maxVoltage;
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return RecipeMaps.FURNACE_RECIPES;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workableHandler.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.workableHandler.setWorkingEnabled(isActivationAllowed);
    }
}
