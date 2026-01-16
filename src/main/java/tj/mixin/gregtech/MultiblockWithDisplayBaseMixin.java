package tj.mixin.gregtech;

import gregicadditions.machines.GATileEntities;
import gregicadditions.machines.multi.GAFueledMultiblockController;
import gregicadditions.machines.multi.IMaintenance;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.HoverEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tj.TJConfig;
import tj.builder.WidgetTabBuilder;
import tj.builder.multicontrollers.MultiblockDisplaysUtility;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.capability.IProgressBar;
import tj.capability.ProgressBar;
import tj.gui.TJGuiTextures;
import tj.gui.TJHorizontoalTabListRenderer;
import tj.gui.widgets.AdvancedDisplayWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.TJProgressBarWidget;
import tj.gui.widgets.impl.ScrollableDisplayWidget;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.function.UnaryOperator;

import static tj.gui.TJGuiTextures.CAUTION_BUTTON;
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
            builder.widget(new TJLabelWidget(-1, -38, 184, 20, TJGuiTextures.MACHINE_LABEL, this::getJEIRecipeUid)
                    .setItemLabel(this.getStackForm())
                    .setLocale(this.getMetaFullName()));
            builder.image(-10, -20, 200, 152, TJGuiTextures.MULTIBLOCK_DISPLAY_SCREEN)
                    .image(-10, 132 + height, 200, 85, TJGuiTextures.MULTIBLOCK_DISPLAY_SLOTS);
            this.addNewTabs(tabBuilder);
            if (barMatrix != null)
                this.addNewBars(barMatrix, builder);
            builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT ,-3, 134 + height)
                    .widget(tabBuilder.build())
                    .widget(tabBuilder.buildWidgetGroup());
            cir.setReturnValue(builder);
        }
    }

    @Unique
    private void addNewBars(int[][] barMatrix, ModularUI.Builder builder) {
        Queue<UnaryOperator<ProgressBar.ProgressBarBuilder>> bars = new ArrayDeque<>();
        ((IProgressBar) this.getHolder().getMetaTileEntity()).getProgressBars(bars);
        for (int i = 0; i < barMatrix.length; i++) {
            int[] column = barMatrix[i];
            for (int j = 0; j < column.length; j++) {
                ProgressBar bar = bars.poll().apply(new ProgressBar.ProgressBarBuilder()).build();
                int height = 188 / column.length;
                builder.widget(new TJProgressBarWidget(-3 + (j * height), 132 + (i * 10), height, 10, bar.getProgress(), bar.getMaxProgress(), bar.isFluid())
                        .setTexture(TJGuiTextures.FLUID_BAR).setBarTexture(bar.getBarTexture())
                        .setLocale(bar.getLocale(), bar.getParams())
                        .setFluid(bar.getFluidStackSupplier())
                        .setColor(bar.getColor()));
            }
        }
    }

    @Unique
    private void addNewTabs(WidgetTabBuilder tabBuilder) {
        tabBuilder.addTab("tj.multiblock.tab.display", this.getStackForm(), this::addMainDisplayTab);
        tabBuilder.addTab("tj.multiblock.tab.maintenance", GATileEntities.MAINTENANCE_HATCH[0].getStackForm(), maintenanceTab ->
                maintenanceTab.add(new AdvancedTextWidget(10, -13, textList -> {
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
    protected void addMainDisplayTab(List<Widget> widgetGroup) {
        widgetGroup.add(new ScrollableDisplayWidget(10, -15, 183, 142)
                .addDisplayWidget(new AdvancedDisplayWidget(0, 2, this::configureDisplayText, 0xFFFFFF)
                        .setClickHandler(this::handleDisplayClick)
                        .setMaxWidthLimit(180))
                .setScrollPanelWidth(3));
        widgetGroup.add(new ToggleButtonWidget(175, 133, 18, 18, CAUTION_BUTTON, this::isStructureCheck, this::doStructureCheck)
                .setTooltipText("machine.universal.toggle.check.mode"));
    }

    @Unique
    protected void configureDisplayText(UIDisplayBuilder builder) {
        if (!this.isStructureFormed()) {
            ITextComponent tooltip = new TextComponentTranslation("gregtech.multiblock.invalid_structure.tooltip");
            tooltip.setStyle(new Style().setColor(TextFormatting.GRAY));
            builder.customLine(text -> text.addTextComponent(new TextComponentTranslation("gregtech.multiblock.invalid_structure")
                    .setStyle(new Style().setColor(TextFormatting.RED)
                            .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip)))));
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

    @Unique
    public String getJEIRecipeUid() {
        return "";
    }
}
