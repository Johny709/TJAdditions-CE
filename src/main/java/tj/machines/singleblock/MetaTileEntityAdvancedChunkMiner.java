package tj.machines.singleblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.client.ClientHandler;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.render.Textures;
import gregtech.api.unification.material.Materials;
import gregtech.common.covers.filter.OreDictionaryItemFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import tj.capability.impl.handler.IMinerHandler;
import tj.capability.impl.workable.MinerWorkableHandler;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.TJProgressBarWidget;
import tj.gui.widgets.impl.RecipeOutputDisplayWidget;

import java.util.Objects;

import static gregtech.api.gui.GuiTextures.BUTTON_ITEM_OUTPUT;
import static gregtech.api.gui.GuiTextures.PROGRESS_BAR_ARROW;
import static tj.gui.TJGuiTextures.POWER_BUTTON;

public class MetaTileEntityAdvancedChunkMiner extends TJTieredWorkableMetaTileEntity implements IMinerHandler {

    private final MinerWorkableHandler workableHandler = new MinerWorkableHandler(this);
    private final OreDictionaryItemFilter itemFilter = new OreDictionaryItemFilter();
    private final FluidStack drillingFluid;

    public MetaTileEntityAdvancedChunkMiner(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.drillingFluid = Materials.DrillingFluid.getFluid(1 << tier - 1);
        this.workableHandler.initialize(1);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityAdvancedChunkMiner(this.metaTileEntityId, this.getTier());
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(2);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(9);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false,  new FilteredFluidHandler(64000)
                .setFillPredicate(fluidStack -> Objects.requireNonNull(Materials.DrillingFluid.getFluid(0)).isFluidEqual(fluidStack)));
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote)
            this.workableHandler.update();
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        RecipeOutputDisplayWidget displayWidget = new RecipeOutputDisplayWidget(90, 30, 21, 20)
                .setFluidOutputSupplier(this.workableHandler::getFluidOutputs)
                .setItemOutputSupplier(this.workableHandler::getItemOutputs)
                .setItemOutputInventorySupplier(this::getExportItems)
                .setFluidOutputTankSupplier(this::getExportFluids);
        TankWidget tankWidget = new TankWidget(this.importFluids.getTankAt(0), 69, 52, 18, 18)
                .setHideTooltip(true).setAlwaysShowFull(true);
        return ModularUI.defaultBuilder()
                .image(-28, 0, 26, 104, GuiTextures.BORDERED_BACKGROUND)
                .image(-28, 120, 26, 44, GuiTextures.BORDERED_BACKGROUND)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(new TJProgressBarWidget(-24, 4, 18, 78, this.energyContainer::getEnergyStored, this.energyContainer::getEnergyCapacity, ProgressWidget.MoveType.VERTICAL)
                        .setLocale("tj.multiblock.bars.energy", null)
                        .setBarTexture(TJGuiTextures.BAR_YELLOW)
                        .setTexture(TJGuiTextures.FLUID_BAR)
                        .setInverted(true))
                .widget(new ProgressWidget(this.workableHandler::getProgressPercent, 90, 30, 21, 20, PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL))
                .widget(new DischargerSlotWidget(this.chargerInventory, 0, -24, 82)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY))
                .widget(new ToggleButtonWidget(-24, 124, 18, 18, BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setItemAutoOutput)
                        .setTooltipText("gregtech.gui.item_auto_output.tooltip"))
                .widget(new ToggleButtonWidget(-24, 142, 18, 18, POWER_BUTTON, this.workableHandler::isWorkingEnabled, this.workableHandler::setWorkingEnabled)
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .image(7, 6, 81, 65, GuiTextures.DISPLAY)
                .widget(tankWidget)
                .label(11, 10, "gregtech.gui.fluid_amount", 0xFFFFFF)
                .dynamicLabel(11, 20, tankWidget::getFormattedFluidAmount, 0xFFFFFF)
                .dynamicLabel(11, 30, tankWidget::getFluidLocalizedName, 0xFFFFFF)
                .widget(new FluidContainerSlotWidget(this.getImportItemInventory(), 0, 90, 8, true)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY))
                .widget(new SlotWidget(this.getImportItemInventory(), 1, 90, 54, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY))
                .bindPlayerInventory(player.inventory)
                .widget(displayWidget)
                .build(this.getHolder(), player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.SCREEN.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        for (EnumFacing facing : EnumFacing.HORIZONTALS)
            if (facing != this.getOutputFacing())
                ClientHandler.CHUNK_MINER_OVERLAY.renderSided(facing, renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        if (this.isAutoOutputItems())
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
    }

    @Override
    public IItemHandlerModifiable getExportItemInventory() {
        TileEntity tileEntity = this.getWorld().getTileEntity(this.getPos().offset(this.getOutputFacing()));
        IItemHandler itemHandler = tileEntity != null ? tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.getOutputFacing()) : null;
        return itemHandler instanceof IItemHandlerModifiable ? (IItemHandlerModifiable) itemHandler : super.getExportItemInventory();
    }

    @Override
    public EnumFacing getInitialFacing() {
        return this.frontFacing;
    }

    @Override
    public int getFortuneLvl() {
        return 0;
    }

    @Override
    public FluidStack getDrillingFluid() {
        return this.drillingFluid;
    }

    @Override
    public OreDictionaryItemFilter getOreDictionaryItemFIlter() {
        return this.itemFilter;
    }
}
