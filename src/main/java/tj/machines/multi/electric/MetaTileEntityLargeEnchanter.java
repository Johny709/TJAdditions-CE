package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAUtility;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.components.EmitterCasing;
import gregicadditions.item.metal.MetalCasing2;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.Widget;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.BlockWorldState;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.TJConfig;
import tj.builder.handlers.EnchanterWorkableHandler;
import tj.builder.multicontrollers.TJMultiblockDisplayBase;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.textures.TJTextures;
import tj.util.EnumFacingHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController.*;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.unification.material.Materials.BlackSteel;
import static tj.machines.multi.electric.MetaTileEntityLargeGreenhouse.glassPredicate;


public class MetaTileEntityLargeEnchanter extends TJMultiblockDisplayBase {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, IMPORT_FLUIDS, INPUT_ENERGY, MAINTENANCE_HATCH};
    private final EnchanterWorkableHandler workableHandler = new EnchanterWorkableHandler(this);
    private IItemHandlerModifiable itemInputs;
    private IItemHandlerModifiable itemOutputs;
    private IMultipleTankHandler fluidInputs;
    private IEnergyContainer energyInput;
    private long maxVoltage;
    private int parallel;
    private int tier;

    public MetaTileEntityLargeEnchanter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.workableHandler.setImportItemsSupplier(() -> this.itemInputs)
                .setExportItemsSupplier(() -> this.itemOutputs)
                .setImportFluidsSupplier(() -> this.fluidInputs)
                .setImportEnergySupplier(() -> this.energyInput)
                .setInputBus(this::getInputBus)
                .setMaxVoltageSupplier(() -> this.maxVoltage)
                .setTierSupplier(() -> this.tier)
                .setParallelSupplier(() -> parallel);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeEnchanter(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gtadditions.multiblock.universal.tooltip.4", TJConfig.largeEnchanter.stack));
        tooltip.add(I18n.format("tj.multiblock.large_enchanter.description"));
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return abilities.containsKey(IMPORT_ITEMS) && abilities.containsKey(EXPORT_ITEMS) && abilities.containsKey(IMPORT_FLUIDS) && abilities.containsKey(INPUT_ENERGY) && super.checkStructureComponents(parts, abilities);
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (this.isStructureFormed())
            builder.voltageInLine(this.energyInput)
                    .voltageTierLine(GAUtility.getTierByVoltage(this.maxVoltage))
                    .energyInputLine(this.energyInput, this.workableHandler.getEnergyPerTick())
                    .addTranslationLine("tj.multiblock.industrial_fusion_reactor.message", this.parallel)
                    .customLine(text -> text.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.universal.distinct")
                            .appendText(" ")
                            .appendSibling(this.workableHandler.isDistinct()
                                    ? withButton(new TextComponentTranslation("gtadditions.multiblock.universal.distinct.yes"), "distinctEnabled")
                                    : withButton(new TextComponentTranslation("gtadditions.multiblock.universal.distinct.no"), "distinctDisabled"))))
                    .isWorkingLine(this.workableHandler.isWorkingEnabled(), this.workableHandler.isActive(), this.workableHandler.getProgress(), this.workableHandler.getMaxProgress());
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
        return FactoryBlockPattern.start(RIGHT, FRONT, DOWN)
                .aisle("~~~~~~~", "~~~~~~~", "~~~C~~~", "~~CCC~~", "~~~C~~~", "~~~~~~~", "~~~~~~~")
                .aisle("~~~~~~~", "~~~~~~~", "~~CCC~~", "~~CEC~~", "~~CCC~~", "~~~~~~~", "~~~~~~~")
                .aisle("~~~~~~~", "~C~~~C~", "~~CGC~~", "~~G#G~~", "~~CGC~~", "~C~~~C~", "~~~~~~~")
                .aisle("~~~~~~~", "~CFFFC~", "~FCCCF~", "~FCCCF~", "~FCCCF~", "~CFFFC~", "~~~~~~~")
                .aisle("~~~~~~~", "~CXGXC~", "~X###X~", "~G###G~", "~X###X~", "~CXGXC~", "~~~~~~~")
                .aisle("~~~~~~~", "~CGGGC~", "~GBBBG~", "~GB#BG~", "~GBBBG~", "~CGGGC~", "~~~~~~~")
                .aisle("~~~~~~~", "~CGGGC~", "~GBBBG~", "~GB#BG~", "~GBBBG~", "~CGGGC~", "~~~~~~~")
                .aisle("~~~~~~~", "~CGGGC~", "~GBBBG~", "~GB#BG~", "~GBBBG~", "~CGGGC~", "~~~~~~~")
                .aisle("~~~~~~~", "~XXSXX~", "~XOOOX~", "~XOOOX~", "~XOOOX~", "~XXXXX~", "~~~~~~~")
                .aisle("~XXXXX~", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "~XXXXX~")
                .setAmountAtLeast('L', 64)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('C', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('G', glassPredicate())
                .where('E', emitterPredicate())
                .where('O', blockPredicate(Blocks.OBSIDIAN))
                .where('B', this::bookshelfPredicate)
                .where('F', statePredicate(MetaBlocks.FRAMES.get(BlackSteel).getDefaultState()))
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.METAL_CASING_2.getState(MetalCasing2.CasingType.BLACK_STEEL);
    }

    private boolean bookshelfPredicate(BlockWorldState blockWorldState) {
        Block block = blockWorldState.getBlockState().getBlock();
        return block == Block.getBlockFromName("apotheosis:hellshelf");
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.tier = context.getOrDefault("Emitter", EmitterCasing.CasingType.EMITTER_LV).getTier();
        this.itemInputs = new ItemHandlerList(this.getAbilities(IMPORT_ITEMS));
        this.itemOutputs = new ItemHandlerList(this.getAbilities(EXPORT_ITEMS));
        this.fluidInputs = new FluidTankList(true, this.getAbilities(IMPORT_FLUIDS));
        this.energyInput = new EnergyContainerList(this.getAbilities(INPUT_ENERGY));
        this.workableHandler.initialize(this.getAbilities(IMPORT_ITEMS).size());
        this.maxVoltage = (long) (Math.pow(4, this.tier) * 8);
        this.parallel = TJConfig.largeEnchanter.stack * this.tier;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.BLACK_STEEL_CASING;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TJTextures.TJ_MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, this.frontFacing, this.workableHandler.isActive(), this.workableHandler.hasProblem(), this.workableHandler.isWorkingEnabled());
        TJTextures.ENCHANTED_BOOK.renderSided(this.frontFacing.getOpposite(), renderState, translation, pipeline);
        TJTextures.ENCHANTED_BOOK.renderSided(EnumFacingHelper.getLeftFacingFrom(this.frontFacing), renderState, translation, pipeline);
        TJTextures.ENCHANTED_BOOK.renderSided(EnumFacingHelper.getRightFacingFrom(this.frontFacing), renderState, translation, pipeline);
        TJTextures.ENCHANTING_TABLE.renderSided(EnumFacingHelper.getTopFacingFrom(this.frontFacing), renderState, translation, pipeline);
    }

    private IItemHandlerModifiable getInputBus(int index) {
        return this.getAbilities(IMPORT_ITEMS).get(index);
    }
}
