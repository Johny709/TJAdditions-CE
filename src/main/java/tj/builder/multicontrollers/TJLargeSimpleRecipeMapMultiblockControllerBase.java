package tj.builder.multicontrollers;

import gregicadditions.machines.GATileEntities;
import gregicadditions.machines.multi.simple.LargeSimpleRecipeMapMultiblockController;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.RecipeMap;
import gregtech.common.ConfigHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import tj.builder.WidgetTabBuilder;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.gui.TJGuiTextures;
import tj.gui.TJHorizontoalTabListRenderer;
import tj.gui.widgets.AdvancedDisplayWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.TJProgressBarWidget;
import tj.gui.widgets.impl.GhostCircuitWidget;
import tj.gui.widgets.impl.ScrollableDisplayWidget;
import tj.multiblockpart.TJMultiblockAbility;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.UnaryOperator;

import static gregtech.api.gui.widgets.AdvancedTextWidget.withButton;
import static gregtech.api.gui.widgets.AdvancedTextWidget.withHoverTextTranslate;
import static tj.gui.TJHorizontoalTabListRenderer.HorizontalStartCorner.LEFT;
import static tj.gui.TJHorizontoalTabListRenderer.VerticalLocation.BOTTOM;

public abstract class TJLargeSimpleRecipeMapMultiblockControllerBase extends LargeSimpleRecipeMapMultiblockController implements IMultiblockAbilityPart<IItemHandlerModifiable> {

    protected boolean doStructureCheck = false;

    public TJLargeSimpleRecipeMapMultiblockControllerBase(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int EUtPercentage, int durationPercentage, int chancePercentage, int stack) {
        super(metaTileEntityId, recipeMap, EUtPercentage, durationPercentage, chancePercentage, stack);
    }

    public TJLargeSimpleRecipeMapMultiblockControllerBase(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, int EUtPercentage, int durationPercentage, int chancePercentage, int stack, boolean hasMuffler, boolean hasMaintenance, boolean canDistinct) {
        super(metaTileEntityId, recipeMap, EUtPercentage, durationPercentage, chancePercentage, stack, hasMuffler, hasMaintenance, canDistinct);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return TJMultiblockAbility.CIRCUIT_SLOT;
    }

    @Override
    public void registerAbilities(List<IItemHandlerModifiable> abilityList) {
        abilityList.add(this.importItems);
    }

    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.initializeAbilities();
    }

    private void initializeAbilities() {
        List<IItemHandlerModifiable> itemHandlerCollection = new ArrayList<>();
        itemHandlerCollection.addAll(getAbilities(TJMultiblockAbility.CIRCUIT_SLOT));
        itemHandlerCollection.addAll(getAbilities(MultiblockAbility.IMPORT_ITEMS));

        this.inputInventory = new ItemHandlerList(itemHandlerCollection);
        this.inputFluidInventory = new FluidTankList(allowSameFluidFillForOutputs(), getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.outputFluidInventory = new FluidTankList(allowSameFluidFillForOutputs(), getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
    }

    @Override
    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        int height = 0;
        int[][] barMatrix = null;
        height += this.getHolder().getMetaTileEntity() instanceof IProgressBar && (barMatrix = ((IProgressBar) this.getHolder().getMetaTileEntity()).getBarMatrix()) != null ? barMatrix.length * 10 : 0;
        ModularUI.Builder builder = ModularUI.extendedBuilder();
        WidgetTabBuilder tabBuilder = new WidgetTabBuilder()
                .setTabListRenderer(() -> new TJHorizontoalTabListRenderer(LEFT, BOTTOM))
                .setPosition(-10, 1)
                .offsetPosition(0, height)
                .offsetY(132);
        if (height > 0)
            builder.image(-10, 132, 200, height, TJGuiTextures.MULTIBLOCK_DISPLAY_SLICE);
        builder.widget(new TJLabelWidget(-1, -38, 184, 20, TJGuiTextures.MACHINE_LABEL)
                .setItemLabel(this.getStackForm())
                .setLocale(this.getMetaFullName()));
        builder.image(-10, -20, 200, 152, TJGuiTextures.MULTIBLOCK_DISPLAY_SCREEN);
        builder.image(-10, 132 + height, 200, 85, TJGuiTextures.MULTIBLOCK_DISPLAY_SLOTS);
        this.addTabs(tabBuilder);
        if (barMatrix != null)
            this.addBars(barMatrix, builder);
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT ,-3, 134 + height);
        builder.widget(tabBuilder.build());
        return builder;
    }

    private void addBars(int[][] barMatrix, ModularUI.Builder builder) {
        Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars = new ArrayDeque<>();
        ((IProgressBar) this.getHolder().getMetaTileEntity()).getProgressBars(bars);
        for (int i = 0; i < barMatrix.length; i++) {
            int[] column = barMatrix[i];
            for (int j = 0; j < column.length; j++) {
                ProgressBar bar = bars.poll().apply(new ProgressBar.ProgressBarBuilder()).build();
                int height = 188 / column.length;
                builder.widget(new TJProgressBarWidget(-3 + (j * height), 132 + (i * 10), height, 10, bar.getProgress(), bar.getMaxProgress(), bar.isFluid())
                        .setStartTexture(TJGuiTextures.FLUID_BAR_START).setEndTexture(TJGuiTextures.FLUID_BAR_END)
                        .setTexture(TJGuiTextures.FLUID_BAR).setBarTexture(bar.getBarTexture())
                        .setLocale(bar.getLocale(), bar.getParams())
                        .setFluid(bar.getFluidStackSupplier()));
            }
        }
    }

    @OverridingMethodsMustInvokeSuper
    protected void addTabs(WidgetTabBuilder tabBuilder) {
        tabBuilder.addTab("tj.multiblock.tab.display", this.getStackForm(), this::mainDisplayTab);
        tabBuilder.addTab("tj.multiblock.tab.maintenance", GATileEntities.MAINTENANCE_HATCH[0].getStackForm(), maintenanceTab ->
                maintenanceTab.add(new AdvancedTextWidget(10, -13, textList -> {
                    MultiblockDisplaysUtility.mufflerDisplay(textList, !this.hasMufflerHatch() || this.isMufflerFaceFree());
                    MultiblockDisplaysUtility.maintenanceDisplay(textList, this.maintenance_problems, this.hasProblems());
                }, 0xFFFFFF).setMaxWidthLimit(180)));
    }

    protected void mainDisplayTab(List<Widget> widgetGroup) {
        widgetGroup.add(new ScrollableDisplayWidget(10, -15, 183, 142)
                .addDisplayWidget(new AdvancedDisplayWidget(0, 2, this::addDisplayText, 0xFFFFFF)
                        .setClickHandler(this::handleDisplayClick)
                        .setMaxWidthLimit(180))
                .setScrollPanelWidth(3));
        widgetGroup.add(new GhostCircuitWidget(this.importItems, 175, 191));
        widgetGroup.add(new ImageWidget(174, 190, 20, 20, GuiTextures.INT_CIRCUIT_OVERLAY));
        widgetGroup.add(new ToggleButtonWidget(175, 169, 18, 18, TJGuiTextures.POWER_BUTTON, this::getToggleMode, this::setToggleRunning)
                .setTooltipText("machine.universal.toggle.run.mode"));
        widgetGroup.add(new ToggleButtonWidget(175, 151, 18, 18, TJGuiTextures.DISTINCT_BUTTON, this::getDistinctMode, this::setDistinctMode)
                .setTooltipText("machine.universal.toggle.distinct.mode"));
        widgetGroup.add(new ToggleButtonWidget(175, 133, 18, 18, TJGuiTextures.CAUTION_BUTTON, this::getDoStructureCheck, this::setDoStructureCheck)
                .setTooltipText("machine.universal.toggle.check.mode"));
    }

    protected void addDisplayText(UIDisplayBuilder builder) {
        if (!this.isStructureFormed()) {
            ITextComponent tooltip = new TextComponentTranslation("gregtech.multiblock.invalid_structure.tooltip");
            tooltip.setStyle(new Style().setColor(TextFormatting.GRAY));
            builder.customLine(text -> text.addTextComponent(new TextComponentTranslation("gregtech.multiblock.invalid_structure")
                    .setStyle(new Style().setColor(TextFormatting.RED)
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)))));
        } else builder.voltageInLine(this.energyContainer)
                .energyInputLine(this.energyContainer, this.recipeMapWorkable.getRecipeEUt())
                .customLine(text -> {
                    if (this.canDistinct) {
                        ITextComponent buttonText = new TextComponentTranslation("gtadditions.multiblock.universal.distinct");
                        buttonText.appendText(" ");
                        ITextComponent button = withButton((isDistinct ?
                                new TextComponentTranslation("gtadditions.multiblock.universal.distinct.yes") :
                                new TextComponentTranslation("gtadditions.multiblock.universal.distinct.no")), "distinct");
                        withHoverTextTranslate(button, "gtadditions.multiblock.universal.distinct.info");
                        buttonText.appendSibling(button);
                        text.addTextComponent(buttonText);
                    }
                    if (ConfigHolder.debug_options_for_caching) {
                        text.addTextComponent(new TextComponentString(String.format("Cache size (%s) hit (%s) miss (%s)", this.recipeMapWorkable.previousRecipe.getCachedRecipeCount(), this.recipeMapWorkable.previousRecipe.getCacheHit(), this.recipeMapWorkable.previousRecipe.getCacheMiss()))
                                .setStyle(new Style().setColor(TextFormatting.WHITE)));
                    }
                }).addTextComponent(new TextComponentTranslation("gregtech.multiblock.universal.framework", this.maxVoltage))
                .isWorkingLine(this.recipeMapWorkable.isWorkingEnabled(), this.recipeMapWorkable.isActive(), this.recipeMapWorkable.getProgress(), this.recipeMapWorkable.getMaxProgress(), 999)
                .addRecipeOutputLine(this.recipeMapWorkable, 1000);
    }

    protected boolean getToggleMode() {
        return this.recipeMapWorkable.isWorkingEnabled();
    }

    protected void setToggleRunning(boolean running) {
        this.recipeMapWorkable.setWorkingEnabled(running);
    }

    protected boolean getDoStructureCheck() {
        if (isStructureFormed())
            this.doStructureCheck = false;
        return this.doStructureCheck;
    }

    protected void setDoStructureCheck(boolean check) {
        if (isStructureFormed()) {
            this.doStructureCheck = true;
            invalidateStructure();
            this.structurePattern = createStructurePattern();
        }
    }

    protected boolean getDistinctMode() {
        return isDistinct;
    }

    protected void setDistinctMode(boolean distinct) {
        isDistinct = distinct;
        markDirty();
    }

    @Override
    public boolean isAttachedToMultiBlock() {
        return false;
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase multiblockControllerBase) {}

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase multiblockControllerBase) {}
}
