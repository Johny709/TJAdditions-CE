package tj.builder.multicontrollers;

import gregicadditions.capabilities.GregicAdditionsCapabilities;
import gregicadditions.machines.GATileEntities;
import gregicadditions.machines.multi.GAFueledMultiblockController;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.recipes.machines.FuelRecipeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import tj.builder.WidgetTabBuilder;
import tj.gui.TJGuiTextures;
import tj.gui.TJHorizontoalTabListRenderer;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static tj.gui.TJHorizontoalTabListRenderer.HorizontalStartCorner.LEFT;
import static tj.gui.TJHorizontoalTabListRenderer.VerticalLocation.BOTTOM;

public abstract class TJFueledMultiblockControllerBase extends GAFueledMultiblockController {

    private boolean doStructureCheck;

    public TJFueledMultiblockControllerBase(ResourceLocation metaTileEntityId, FuelRecipeMap recipeMap, long maxVoltage) {
        super(metaTileEntityId, recipeMap, maxVoltage);
    }

    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        int maintenanceCount = abilities.getOrDefault(GregicAdditionsCapabilities.MAINTENANCE_HATCH, Collections.emptyList()).size();
        return maintenanceCount == 1;
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
        tabBuilder.addTab("tj.multiblock.tab.maintenance", GATileEntities.MAINTENANCE_HATCH[0].getStackForm(), maintenanceTab ->
                maintenanceTab.addWidget(new AdvancedTextWidget(10, -2, textList -> {
                    MultiblockDisplaysUtility.mufflerDisplay(textList, !this.hasMufflerHatch() || this.isMufflerFaceFree());
                    MultiblockDisplaysUtility.maintenanceDisplay(textList, this.maintenance_problems, this.hasProblems());
                }, 0xFFFFFF).setMaxWidthLimit(180)));
    }

    protected void mainDisplayTab(WidgetGroup widgetGroup) {
        widgetGroup.addWidget(new AdvancedTextWidget(10, -2, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(180).setClickHandler(this::handleDisplayClick));
        widgetGroup.addWidget(new ToggleButtonWidget(172, 169, 18, 18, TJGuiTextures.POWER_BUTTON, this::isWorkingEnabled, this::setWorkingEnabled)
                .setTooltipText("machine.universal.toggle.run.mode"));
        widgetGroup.addWidget(new ToggleButtonWidget(172, 133, 18, 18, TJGuiTextures.CAUTION_BUTTON, this::getDoStructureCheck, this::setDoStructureCheck)
                .setTooltipText("machine.universal.toggle.check.mode"));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplaysUtility.recipeMapWorkable(textList, isStructureFormed(), workableHandler);
        MultiblockDisplaysUtility.isInvalid(textList, isStructureFormed());
    }

    public boolean isWorkingEnabled() {
        return this.workableHandler.isWorkingEnabled();
    }

    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.workableHandler.setWorkingEnabled(isActivationAllowed);
        this.markDirty();
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
}
