package tj.mixin.gregtech;

import gregicadditions.machines.GATileEntities;
import gregicadditions.machines.multi.GAFueledMultiblockController;
import gregicadditions.machines.multi.IMaintenance;
import gregtech.api.capability.impl.FuelRecipeLogic;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.common.metatileentities.multi.electric.generator.FueledMultiblockController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tj.TJConfig;
import tj.builder.WidgetTabBuilder;
import tj.builder.multicontrollers.MultiblockDisplaysUtility;
import tj.builder.multicontrollers.TJFueledMultiblockControllerBase;
import tj.capability.impl.TJFuelRecipeLogic;
import tj.gui.TJGuiTextures;
import tj.gui.TJHorizontoalTabListRenderer;
import tj.gui.widgets.impl.TJToggleButtonWidget;

import java.util.List;

import static tj.gui.TJGuiTextures.CAUTION_BUTTON;
import static tj.gui.TJGuiTextures.POWER_BUTTON;
import static tj.gui.TJHorizontoalTabListRenderer.HorizontalStartCorner.LEFT;
import static tj.gui.TJHorizontoalTabListRenderer.VerticalLocation.BOTTOM;

@Mixin(value = MultiblockWithDisplayBase.class, remap = false)
public abstract class MultiblockWithDisplayBaseMixin extends MultiblockControllerBase {

    @Unique
    private boolean structureCheck;

    public MultiblockWithDisplayBaseMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Shadow
    protected abstract void addDisplayText(List<ITextComponent> textList);

    @Shadow
    protected abstract void handleDisplayClick(String componentData, Widget.ClickData clickData);

    @Inject(method = "createUITemplate", at = @At("HEAD"), cancellable = true)
    private void injectCreateUITemplate(EntityPlayer entityPlayer, CallbackInfoReturnable<ModularUI.Builder> cir) {
        if (TJConfig.machines.multiblockUIOverrides) {
            ModularUI.Builder builder = ModularUI.extendedBuilder();
            WidgetTabBuilder tabBuilder = new WidgetTabBuilder()
                    .setTabListRenderer(() -> new TJHorizontoalTabListRenderer(LEFT, BOTTOM))
                    .setPosition(-10, 1);
            this.addNewTabs(tabBuilder);
            builder.image(-10, -20, 195, 237, TJGuiTextures.NEW_MULTIBLOCK_DISPLAY);
            builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT ,-3, 134);
            builder.widget(new LabelWidget(0, -13, this.getMetaFullName(), 0xFFFFFF));
            builder.widget(tabBuilder.build());
            cir.setReturnValue(builder);
        }
    }

    @Unique
    private void addNewTabs(WidgetTabBuilder tabBuilder) {
        tabBuilder.addTab("tj.multiblock.tab.display", this.getStackForm(), this::addMainDisplayTab);
        tabBuilder.addTab("tj.multiblock.tab.maintenance", GATileEntities.MAINTENANCE_HATCH[0].getStackForm(), maintenanceTab ->
                maintenanceTab.addWidget(new AdvancedTextWidget(10, -2, textList -> {
                    if (this.getHolder().getMetaTileEntity() instanceof GAFueledMultiblockController) {
                        GAFueledMultiblockController controller = (GAFueledMultiblockController) this.getHolder().getMetaTileEntity();
                        MultiblockDisplaysUtility.mufflerDisplay(textList, !controller.hasMufflerHatch() || controller.isMufflerFaceFree());
                    }
                    if (this.getHolder().getMetaTileEntity() instanceof IMaintenance) {
                        IMaintenance maintenance = (IMaintenance) this.getHolder().getMetaTileEntity();
                        MultiblockDisplaysUtility.maintenanceDisplay(textList, maintenance.getProblems(), maintenance.hasProblems());
                    }
                }, 0xFFFFFF).setMaxWidthLimit(180)));
    }

    @Unique
    private void addMainDisplayTab(WidgetGroup widgetGroup) {
        widgetGroup.addWidget(new AdvancedTextWidget(10, -2, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(180)
                .setClickHandler(this::handleDisplayClick));
        widgetGroup.addWidget(new ToggleButtonWidget(172, 133, 18, 18, CAUTION_BUTTON, this::isStructureCheck, this::doStructureCheck)
                .setTooltipText("machine.universal.toggle.check.mode"));
        if (this.getHolder().getMetaTileEntity() instanceof RecipeMapMultiblockController) {
            MultiblockRecipeLogic recipeLogic = ((IRecipeMapMultiblockControllerMixin) this.getHolder().getMetaTileEntity()).getRecipeLogic();
            widgetGroup.addWidget(new ToggleButtonWidget(172, 169, 18, 18, POWER_BUTTON, recipeLogic::isWorkingEnabled, recipeLogic::setWorkingEnabled)
                    .setTooltipText("machine.universal.toggle.run.mode"));
        } else if (this.getHolder().getMetaTileEntity() instanceof FueledMultiblockController) {
            FuelRecipeLogic recipeLogic = ((IFueledMultiblockControllerMixin) this.getHolder().getMetaTileEntity()).getFuelRecipeLogic();
            widgetGroup.addWidget(new ToggleButtonWidget(172, 169, 18, 18, POWER_BUTTON, recipeLogic::isWorkingEnabled, recipeLogic::setWorkingEnabled)
                    .setTooltipText("machine.universal.toggle.run.mode"));
            if (recipeLogic instanceof TJFuelRecipeLogic) {
                widgetGroup.addWidget(new TJToggleButtonWidget(172, 151, 18, 18)
                        .setToggleButtonResponder(((TJFuelRecipeLogic) recipeLogic)::setVoidEnergy)
                        .setButtonSupplier(((TJFuelRecipeLogic) recipeLogic)::isVoidEnergy)
                        .setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                        .setBackgroundTextures(TJGuiTextures.ENERGY_VOID)
                        .useToggleTexture(true));
            }
        }
    }

    @Unique
    private boolean isStructureCheck() {
        if (this.isStructureFormed())
            this.structureCheck = false;
        return this.structureCheck;
    }

    @Unique
    private void doStructureCheck(boolean check) {
        if (this.isStructureFormed()) {
            this.structureCheck = true;
            this.invalidateStructure();
            this.structurePattern = this.createStructurePattern();
        }
    }
}
