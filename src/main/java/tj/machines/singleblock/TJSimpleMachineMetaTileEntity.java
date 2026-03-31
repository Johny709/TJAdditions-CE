package tj.machines.singleblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.Gregicality;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.render.OrientedOverlayRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import tj.capability.IProcessorProvider;
import tj.capability.IRecipeMap;
import tj.capability.impl.handler.IRecipeHandler;
import tj.capability.impl.workable.BasicRecipeLogic;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.TJProgressBarWidget;
import tj.gui.widgets.impl.RecipeOutputDisplayWidget;
import tj.textures.TJTextures;
import tj.util.EnumFacingHelper;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TJSimpleMachineMetaTileEntity extends TJTieredWorkableMetaTileEntity implements IRecipeHandler, IProcessorProvider {

    protected final BasicRecipeLogic<? extends IRecipeHandler> recipeLogic = this.createRecipeLogic();
    protected final OrientedOverlayRenderer renderer;
    protected final RecipeMap<?> recipeMap;

    public TJSimpleMachineMetaTileEntity(ResourceLocation metaTileEntityId, int tier, RecipeMap<?> recipeMap, OrientedOverlayRenderer renderer) {
        super(metaTileEntityId, tier);
        this.recipeMap = recipeMap;
        this.renderer = renderer;
        this.initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new TJSimpleMachineMetaTileEntity(this.metaTileEntityId, this.getTier(), this.recipeMap, this.renderer);
    }

    private BasicRecipeLogic<? extends IRecipeHandler> createRecipeLogic() {
        return new BasicRecipeLogic<>(this);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(this.recipeMap.getMaxInputs());
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(this.recipeMap.getMaxOutputs());
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, IntStream.range(0, this.recipeMap.getMaxFluidInputs())
                .mapToObj(i -> new FluidTank(64000))
                .collect(Collectors.toList()));
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false, IntStream.range(0, this.recipeMap.getMaxFluidOutputs())
                .mapToObj(i -> new FluidTank(64000))
                .collect(Collectors.toList()));
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote)
            this.recipeLogic.update();
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        RecipeOutputDisplayWidget displayWidget = new RecipeOutputDisplayWidget(77, 22, 21, 20)
                .setFluidOutputSupplier(this.recipeLogic::getFluidOutputs)
                .setItemOutputSupplier(this.recipeLogic::getItemOutputs)
                .setItemOutputInventorySupplier(this::getExportItems)
                .setFluidOutputTankSupplier(this::getExportFluids);
        ModularUI.Builder newBuilder = ((IRecipeMap) this.getRecipeMap()).createUITemplateAdvanced(this.recipeLogic::getProgressPercent, this.importItems, this.exportItems, this.importFluids, this.exportFluids, displayWidget)
                .image(-28, 0, 26, 104, GuiTextures.BORDERED_BACKGROUND)
                .image(-28, 138, 26, 26, GuiTextures.BORDERED_BACKGROUND)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL, () -> Gregicality.MODID + ":" + this.getRecipeMap().getUnlocalizedName())
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(new TJProgressBarWidget(-24, 4, 18, 78, this.energyContainer::getEnergyStored, this.energyContainer::getEnergyCapacity, ProgressWidget.MoveType.VERTICAL)
                        .setLocale("tj.multiblock.bars.energy", null)
                        .setBarTexture(TJGuiTextures.BAR_YELLOW)
                        .setTexture(TJGuiTextures.FLUID_BAR)
                        .setInverted(true))
                .widget(new DischargerSlotWidget(this.chargerInventory, 0, -24, 82)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY))
                .widget(new ImageWidget(79, 42, 18, 18, GuiTextures.INDICATOR_NO_ENERGY)
                        .setPredicate(() -> this.energyContainer.getEnergyStored() < this.recipeLogic.getEnergyPerTick()))
                .widget(new ToggleButtonWidget(-24, 142, 18, 18, TJGuiTextures.POWER_BUTTON, this.recipeLogic::isWorkingEnabled, this.recipeLogic::setWorkingEnabled)
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .bindPlayerInventory(player.inventory)
                .widget(displayWidget);

        int leftButtonStartX = 7;
        if (this.getRecipeMap() instanceof SimpleMachineMetaTileEntity.RecipeMapWithConfigButton) {
            leftButtonStartX += ((SimpleMachineMetaTileEntity.RecipeMapWithConfigButton) this.getRecipeMap()).getLeftButtonOffset();
        }
        if (this.exportItems.getSlots() > 0) {
            newBuilder.widget(new ToggleButtonWidget(leftButtonStartX, 62, 18, 18,
                    GuiTextures.BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setItemAutoOutput)
                    .setTooltipText("gregtech.gui.item_auto_output.tooltip"));
            leftButtonStartX += 18;
        }
        if (this.exportFluids.getTanks() > 0) {
            newBuilder.widget(new ToggleButtonWidget(leftButtonStartX, 62, 18, 18,
                    GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setFluidAutoOutput)
                    .setTooltipText("gregtech.gui.fluid_auto_output.tooltip"));
        }
        return newBuilder.build(this.getHolder(), player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.renderer.render(renderState, translation, pipeline, this.getFrontFacing(), this.recipeLogic.isActive());
        if (this.renderTJLogoOverlay()) {
            TJTextures.TJ_LOGO.renderSided(EnumFacingHelper.getLeftFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
            TJTextures.TJ_LOGO.renderSided(EnumFacingHelper.getRightFacingFrom(this.getFrontFacing()), renderState, translation, pipeline);
            TJTextures.TJ_LOGO.renderSided(this.getFrontFacing().getOpposite(), renderState, translation, pipeline);
        }
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return this.recipeMap;
    }

    @Override
    public OrientedOverlayRenderer getRendererOverlay() {
        return this.renderer;
    }

    @Override
    public int getMachineTier() {
        return this.getTier();
    }

    public boolean renderTJLogoOverlay() {
        return false;
    }
}
