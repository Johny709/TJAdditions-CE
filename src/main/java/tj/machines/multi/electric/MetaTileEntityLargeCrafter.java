package tj.machines.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAUtility;
import gregicadditions.client.ClientHandler;
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GAMultiblockCasing;
import gregicadditions.item.components.ConveyorCasing;
import gregicadditions.item.components.RobotArmCasing;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.render.ICubeRenderer;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Triple;
import tj.TJConfig;
import tj.builder.WidgetTabBuilder;
import tj.builder.handlers.CrafterRecipeLogic;
import tj.builder.handlers.IRecipeMapProvider;
import tj.builder.multicontrollers.TJMultiblockDisplayBase;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.gui.TJGuiTextures;
import tj.textures.TJTextures;
import tj.util.Color;
import tj.util.EnumFacingHelper;
import tj.util.TooltipHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static gregicadditions.capabilities.GregicAdditionsCapabilities.MAINTENANCE_HATCH;
import static gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController.conveyorPredicate;
import static gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController.robotArmPredicate;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.metatileentity.multiblock.MultiblockAbility.*;
import static gregtech.api.multiblock.BlockPattern.RelativeDirection.*;
import static gregtech.api.unification.material.Materials.Osmiridium;
import static tj.machines.multi.electric.MetaTileEntityLargeGreenhouse.glassPredicate;
import static tj.multiblockpart.TJMultiblockAbility.CRAFTER;

public class MetaTileEntityLargeCrafter extends TJMultiblockDisplayBase implements IRecipeMapProvider {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {IMPORT_ITEMS, EXPORT_ITEMS, INPUT_ENERGY, MAINTENANCE_HATCH, CRAFTER};
    private final CrafterRecipeLogic recipeLogic = new CrafterRecipeLogic(this)
            .setImportItemsSupplier(this::getImportItemInventory)
            .setExportItemsSupplier(this::getExportItemInventory)
            .setImportEnergySupplier(this::getEnergyContainer)
            .setMaxVoltageSupplier(this::getMaxVoltage)
            .setParallelSupplier(this::getParallel)
            .setInputBus(this::getInputBus);
    private IItemHandlerModifiable importItemInventory;
    private IItemHandlerModifiable exportItemInventory;
    private IEnergyContainer energyContainer;
    private long maxVoltage;
    private int parallel;

    public MetaTileEntityLargeCrafter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLargeCrafter(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("tj.multiblock.large_crafter.description"));
        tooltip.add(TooltipHelper.blinkingText(Color.YELLOW,  20, "tj.multiblock.large_crafter.warning"));
        TooltipHelper.shiftText(tooltip, tip -> tip.add(I18n.format("gtadditions.multiblock.universal.tooltip.4", TJConfig.largeCrafter.stack)));
    }


    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return abilities.containsKey(CRAFTER) && abilities.containsKey(IMPORT_ITEMS) && abilities.containsKey(EXPORT_ITEMS) && abilities.containsKey(INPUT_ENERGY) && super.checkStructureComponents(parts, abilities);
    }

    @Override
    protected void addDisplayText(UIDisplayBuilder builder) {
        super.addDisplayText(builder);
        if (this.isStructureFormed())
            builder.voltageInLine(this.energyContainer)
                    .voltageTierLine(GAUtility.getTierByVoltage(this.maxVoltage))
                    .energyInputLine(this.energyContainer, this.recipeLogic.getEnergyPerTick())
                    .addTranslationLine("tj.multiblock.industrial_fusion_reactor.message", this.parallel)
                    .customLine(text -> text.addTextComponent(new TextComponentTranslation("gtadditions.multiblock.universal.distinct")
                            .appendText(" ")
                            .appendSibling(this.recipeLogic.isDistinct()
                                    ? withButton(new TextComponentTranslation("gtadditions.multiblock.universal.distinct.yes"), "distinctEnabled")
                                    : withButton(new TextComponentTranslation("gtadditions.multiblock.universal.distinct.no"), "distinctDisabled"))))
                    .isWorkingLine(this.recipeLogic.isWorkingEnabled(), this.recipeLogic.isActive(), this.recipeLogic.getProgress(), this.recipeLogic.getMaxProgress())
                    .addRecipeInputLine(this.recipeLogic)
                    .addRecipeOutputLine(this.recipeLogic);
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        this.recipeLogic.setDistinct(!componentData.equals("distinctEnabled"));
    }

    @Override
    protected void mainDisplayTab(List<Widget> widgetGroup) {
        super.mainDisplayTab(widgetGroup);
        widgetGroup.add(new ToggleButtonWidget(172, 151, 18, 18, TJGuiTextures.ITEM_VOID_BUTTON, this.recipeLogic::isVoidOutputs, this.recipeLogic::setVoidOutputs)
                .setTooltipText("machine.universal.toggle.item_voiding"));
    }

    @Override
    protected void addTabs(WidgetTabBuilder tabBuilder, EntityPlayer player) {
        super.addTabs(tabBuilder, player);
        tabBuilder.addTab("tj.multiblock.tab.debug", MetaItems.WRENCH.getStackForm(), debugTab -> {
            debugTab.add(new AdvancedTextWidget(10, -2, this::addDebugDisplayText, 0xFFFFFF)
                    .setMaxWidthLimit(180));
        });
    }

    private void addDebugDisplayText(List<ITextComponent> textList) {
        textList.add(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("tj.multiblock.parallel.debug.cache.capacity", this.recipeLogic.getPreviousRecipe().getCapacity())));
        textList.add(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("tj.multiblock.parallel.debug.cache.hit", this.recipeLogic.getPreviousRecipe().getCacheHit()))
                .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("tj.multiblock.parallel.debug.cache.hit.info")))));
        textList.add(new TextComponentString(net.minecraft.util.text.translation.I18n.translateToLocalFormatted("tj.multiblock.parallel.debug.cache.miss", this.recipeLogic.getPreviousRecipe().getCacheMiss()))
                .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("tj.multiblock.parallel.debug.cache.miss.info")))));
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.importItemInventory = new ItemHandlerList(this.getAbilities(IMPORT_ITEMS));
        this.exportItemInventory = new ItemHandlerList(this.getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.energyContainer = new EnergyContainerList(this.getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.recipeLogic.initialize(this.getAbilities(IMPORT_ITEMS).size());
        int conveyor = context.getOrDefault("Conveyor", ConveyorCasing.CasingType.CONVEYOR_LV).getTier();
        int robotArm = context.getOrDefault("RobotArm", RobotArmCasing.CasingType.ROBOT_ARM_LV).getTier();
        int min = Math.min(conveyor, robotArm);
        this.maxVoltage = (long) (Math.pow(4, min) * 8);
        this.parallel = TJConfig.largeCrafter.stack * min;
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return false;
    }

    @Override
    protected void updateFormedValid() {
        if (((this.getProblems() >> 5) & 1) != 0)
            this.recipeLogic.update();
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, UP, BACK)
                .aisle("XXXXX", "FXXXF", "FXXXF", "FXXXF", "~XXX~")
                .aisle("XXXXX", "G#C#G", "GR#RG", "F#C#F", "~XXX~")
                .aisle("XXXXX", "G#C#G", "GR#RG", "F#C#F", "~XXX~")
                .aisle("XXXXX", "G#C#G", "GR#RG", "F#C#F", "~XXX~")
                .aisle("XXXXX", "FXXXF", "FXSXF", "FXXXF", "~XXX~")
                .setAmountAtLeast('L', 25)
                .where('S', this.selfPredicate())
                .where('L', statePredicate(this.getCasingState()))
                .where('X', statePredicate(this.getCasingState()).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('F', statePredicate(MetaBlocks.FRAMES.get(Osmiridium).getDefaultState()))
                .where('G', glassPredicate())
                .where('C', conveyorPredicate())
                .where('R', robotArmPredicate())
                .where('#', isAirPredicate())
                .where('~', tile -> true)
                .build();
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.MUTLIBLOCK_CASING.getState(GAMultiblockCasing.CasingType.LARGE_ASSEMBLER);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return ClientHandler.LARGE_ASSEMBLER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TJTextures.TJ_MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, this.getFrontFacing(), this.recipeLogic.isActive(), this.recipeLogic.hasProblem(), this.recipeLogic.isWorkingEnabled());
        TJTextures.CRAFTER.renderSided(EnumFacingHelper.getTopFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
    }

    @Override
    public Int2ObjectMap<Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> getRecipeMap() {
        return null;
    }

    @Override
    public void clearRecipeCache() {
        this.recipeLogic.clearCache();
    }

    private IItemHandlerModifiable getImportItemInventory() {
        return this.importItemInventory;
    }

    private IItemHandlerModifiable getExportItemInventory() {
        return this.exportItemInventory;
    }

    private IEnergyContainer getEnergyContainer() {
        return this.energyContainer;
    }

    private IItemHandlerModifiable getInputBus(int index) {
        return this.getAbilities(IMPORT_ITEMS).get(index);
    }

    private long getMaxVoltage() {
        return this.maxVoltage;
    }

    private int getParallel() {
        return this.parallel;
    }
}
