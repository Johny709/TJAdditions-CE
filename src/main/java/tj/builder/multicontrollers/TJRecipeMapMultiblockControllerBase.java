package tj.builder.multicontrollers;

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
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import tj.builder.WidgetTabBuilder;
import tj.gui.TJGuiTextures;
import tj.gui.TJHorizontoalTabListRenderer;
import tj.gui.widgets.impl.GhostCircuitWidget;
import tj.multiblockpart.TJMultiblockAbility;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.List;

import static tj.gui.TJHorizontoalTabListRenderer.HorizontalStartCorner.LEFT;
import static tj.gui.TJHorizontoalTabListRenderer.VerticalLocation.BOTTOM;

public abstract class TJRecipeMapMultiblockControllerBase extends RecipeMapMultiblockController implements IMultiblockAbilityPart<IItemHandlerModifiable> {

    protected boolean doStructureCheck = false;

    public TJRecipeMapMultiblockControllerBase(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
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
        ModularUI.Builder builder = ModularUI.extendedBuilder();
        WidgetTabBuilder tabBuilder = new WidgetTabBuilder()
                .setTabListRenderer(() -> new TJHorizontoalTabListRenderer(LEFT, BOTTOM))
                .setPosition(-10, 1);
        this.addTabs(tabBuilder);
        builder.image(-10, -20, 195, 237, TJGuiTextures.NEW_MULTIBLOCK_DISPLAY);
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT ,-3, 134);
        builder.widget(new LabelWidget(0, -13, getMetaFullName(), 0xFFFFFF));
        builder.widget(tabBuilder.build());
        return builder;
    }

    @OverridingMethodsMustInvokeSuper
    protected void addTabs(WidgetTabBuilder tabBuilder) {
        tabBuilder.addTab("tj.multiblock.tab.display", this.getStackForm(), this::mainDisplayTab);
    }

    protected void mainDisplayTab(List<Widget> widgetGroup) {
        widgetGroup.add(new AdvancedTextWidget(10, -2, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(180)
                .setClickHandler(this::handleDisplayClick));
        widgetGroup.add(new GhostCircuitWidget(this.importItems, 172, 191));
        widgetGroup.add(new ImageWidget(171, 190, 20, 20, GuiTextures.INT_CIRCUIT_OVERLAY));
        widgetGroup.add(new ToggleButtonWidget(172, 169, 18, 18, TJGuiTextures.POWER_BUTTON, this::getToggleMode, this::setToggleRunning)
                .setTooltipText("machine.universal.toggle.run.mode"));
        widgetGroup.add(new ToggleButtonWidget(172, 133, 18, 18, TJGuiTextures.CAUTION_BUTTON, this::getDoStructureCheck, this::setDoStructureCheck)
                .setTooltipText("machine.universal.toggle.check.mode"));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplaysUtility.recipeMapWorkable(textList, this.isStructureFormed(), this.recipeMapWorkable);
        MultiblockDisplaysUtility.isInvalid(textList, this.isStructureFormed());
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

    @Override
    public boolean isAttachedToMultiBlock() {
        return false;
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase multiblockControllerBase) {}

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase multiblockControllerBase) {}
}
