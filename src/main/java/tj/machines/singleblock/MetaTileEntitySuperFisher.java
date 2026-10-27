package tj.machines.singleblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.render.Textures;
import gregtech.api.util.Position;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.capability.impl.workable.FisherWorkableHandler;
import tj.mui.TJGuiTextures;
import tj.mui.widgets.impl.*;
import tj.textures.TJTextures;

import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.gui.GuiTextures.*;
import static gregtech.api.gui.GuiTextures.BOXED_OVERLAY;
import static gregtech.api.gui.GuiTextures.BUTTON_FLUID_OUTPUT;
import static gregtech.api.gui.GuiTextures.BUTTON_ITEM_OUTPUT;
import static gregtech.api.gui.GuiTextures.CHARGER_OVERLAY;
import static gregtech.api.gui.GuiTextures.INDICATOR_NO_ENERGY;
import static gregtech.api.gui.GuiTextures.SLOT;
import static tj.mui.TJGuiTextures.TOGGLE_POWER_BUTTON;

public class MetaTileEntitySuperFisher extends TJTieredWorkableMetaTileEntity {

    private final FisherWorkableHandler workableHandler = new FisherWorkableHandler(this);
    private final int parallel;

    public MetaTileEntitySuperFisher(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.parallel = this.getTier();
        this.workableHandler.initialize(1);
        // reinitialize inventory because the value "tier", which is being used in creation of item handlers is not initialized yet
        // to the correct value until sub constructor is called.
        this.initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntitySuperFisher(this.metaTileEntityId, this.getTier());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("tj.multiblock.parallel", this.parallel));
        tooltip.add(I18n.format("gregtech.machine.fisher.speed", 1600 / 2 << this.getTier() * 2));
        tooltip.add(I18n.format("gregtech.machine.fisher.requirement", (int) Math.sqrt(25)));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 1);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, this.getSlotGrid(this.getTier()));
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote)
            this.workableHandler.update();
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        int offsetY = 0;
        final int slotLength = Math.min(GTValues.ZPM, this.getTier() + 1);
        final RecipeOutputDisplayWidget displayWidget = new RecipeOutputDisplayWidget(25, 7, 21, 20)
                .setFluidOutputSupplier(this.workableHandler::getFluidOutputs)
                .setItemOutputSupplier(this.workableHandler::getItemOutputs)
                .setItemOutputInventorySupplier(this::getExportItems)
                .setFluidOutputTankSupplier(this::getExportFluids);
        final WidgetGroup slotWidgetGroup = this.getTier() > GTValues.LuV ? new ScrollingWidgetGroup<>(43, 7, 126, 126)
                : new WidgetGroup(new Position(43, 7));
        for (int i = 0; i < this.exportItems.getSlots(); i++) {
            final int x = 18 * (i % slotLength);
            final int y = 18 * (i / slotLength);
            offsetY = Math.max(offsetY, Math.min(y, 108));
            slotWidgetGroup.addWidget(new SlotWidget(this.exportItems, i, x, y, true, false)
                    .setBackgroundTexture(SLOT));
            slotWidgetGroup.addWidget(new RecipeOutputSlotWidget(i, x, y, 18, 18, displayWidget::getItemOutputAt, null));
        }
        return ModularUI.builder(BORDERED_BACKGROUND, 176, 130 + offsetY)
                .image(-28, 0, 26, 104, BORDERED_BACKGROUND)
                .image(-28, 138, 26, 26, BORDERED_BACKGROUND)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(new TJProgressBarWidget(-24, 4, 18, 78, this.energyContainer::getEnergyStored, this.energyContainer::getEnergyCapacity, ProgressWidget.MoveType.VERTICAL)
                        .setLocale("tj.multiblock.bars.energy", null)
                        .setBarTexture(TJGuiTextures.BAR_YELLOW)
                        .setTexture(TJGuiTextures.FLUID_BAR)
                        .setInverted(true))
                .widget(new ProgressWidget(this.workableHandler::getProgressPercent, 25, 7, 21, 20, PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL))
                .widget(new SlotWidget(this.importItems, 0, 7, 7, true, true)
                        .setBackgroundTexture(SLOT, BOXED_OVERLAY))
                .widget(new DischargerSlotWidget(this.chargerInventory, 0, -24, 82)
                        .setBackgroundTexture(SLOT, CHARGER_OVERLAY))
                .widget(new TJToggleButtonWidget(-24, 142, 18, 18, TOGGLE_POWER_BUTTON, this.workableHandler::isWorkingEnabled, this.workableHandler::setWorkingEnabled)
                        .setToggleTitleTooltipHoverText("machine.universal.toggle.run.mode.disabled", "machine.universal.toggle.run.mode.enabled"))
                .widget(new TJToggleButtonWidget(7, 29 + offsetY, 18, 18, BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setItemAutoOutput)
                        .setToggleTitleTooltipHoverText("gregtech.gui.item_auto_output.tooltip.disabled", "gregtech.gui.item_auto_output.tooltip.enabled"))
                .widget(new TJToggleButtonWidget(25, 29 + offsetY, 18, 18, BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setFluidAutoOutput)
                        .setToggleTitleTooltipHoverText("gregtech.gui.fluid_auto_output.tooltip.disabled", "gregtech.gui.fluid_auto_output.tooltip.enabled"))
                .widget(new ImageWidget(79, 27, 18, 18, INDICATOR_NO_ENERGY)
                        .setPredicate(this.workableHandler::hasNotEnoughEnergy))
                .widget(slotWidgetGroup)
                .bindPlayerInventory(player.inventory, 51 + offsetY)
                .widget(displayWidget)
                .build(this.getHolder(), player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.SCREEN.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        TJTextures.TJ_MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, this.frontFacing, this.workableHandler.isActive(), this.workableHandler.isHasProblems(), this.workableHandler.isWorkingEnabled());
        Textures.PIPE_OUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        if (this.isAutoOutputItems())
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
    }

    private int getSlotGrid(int tier) {
        tier++;
        return tier * tier;
    }
}
