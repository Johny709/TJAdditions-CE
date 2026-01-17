package tj.machines.singleblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.items.toolitem.ToolMetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.recipes.ModHandler;
import gregtech.api.render.Textures;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.Position;
import gregtech.common.tools.ToolAxe;
import gregtech.common.tools.ToolHoe;
import gregtech.common.tools.ToolSaw;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.builder.handlers.FarmingStationWorkableHandler;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.SlotScrollableWidgetGroup;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.TJSlotWidget;
import tj.items.handlers.FilteredItemStackHandler;
import tj.items.handlers.LargeItemStackHandler;
import tj.textures.TJTextures;
import tj.util.EnumFacingHelper;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static gregicadditions.GAMaterials.OrganicFertilizer;
import static gregtech.api.gui.GuiTextures.*;
import static gregtech.api.gui.GuiTextures.INDICATOR_NO_ENERGY;
import static tj.gui.TJGuiTextures.*;


public class MetaTileEntityFarmingStation extends TJTieredWorkableMetaTileEntity {

    private final FarmingStationWorkableHandler workableHandler = new FarmingStationWorkableHandler(this)
            .setFertilizerInventory(this::getFertilizerInventory)
            .setImportEnergySupplier(this::getEnergyContainer)
            .setImportFluidsSupplier(this::getImportFluids)
            .setImportItemsSupplier(this::getImportItems)
            .setExportItemsSupplier(this::getExportItems)
            .setMaxVoltageSupplier(this::getMaxVoltage)
            .setToolInventory(this::getToolInventory)
            .setTierSupplier(this::getTier);

    private final IItemHandlerModifiable seedInventory = new FilteredItemStackHandler(this, 6, this.getTier() >= GTValues.ZPM ? 256 : this.getTier() >= GTValues.EV ? 128 : 64)
            .setItemStackPredicate((slot, stack) -> stack.getItem() instanceof IPlantable || Block.getBlockFromItem(stack.getItem()) instanceof IPlantable);

    private final IItemHandlerModifiable fertilizerInventory = new FilteredItemStackHandler(this, 2, this.getTier() >= GTValues.ZPM ? 256 : this.getTier() >= GTValues.EV ? 128 : 64)
            .setItemStackPredicate((slot, stack) -> (stack.getItem() instanceof ItemDye && stack.getItem().getMetadata(stack) == 15) || stack.isItemEqual(OreDictUnifier.get(OrePrefix.dust, OrganicFertilizer)));

    private final IItemHandlerModifiable toolInventory = new FilteredItemStackHandler(this, 3, 1)
            .setItemStackPredicate((slot, stack) -> {
                switch (slot) {
                    case 0: return stack.getItem() instanceof ItemHoe || (stack.getItem() instanceof ToolMetaItem<?> && ((ToolMetaItem<?>) stack.getItem()).getItem(stack).getToolStats() instanceof ToolHoe);
                    case 1: return stack.getItem().getToolClasses(stack).contains("axe") || (stack.getItem() instanceof ToolMetaItem<?> && (((ToolMetaItem<?>) stack.getItem()).getItem(stack).getToolStats() instanceof ToolAxe || ((ToolMetaItem<?>) stack.getItem()).getItem(stack).getToolStats() instanceof ToolSaw));
                    case 2: return stack.getItem() instanceof ItemShears;
                    default: return false;
                }
            });

    private final IFluidTank waterTank = new FilteredFluidHandler(this.getTier() >= GTValues.ZPM ? 256000 : this.getTier() >= GTValues.EV ? 128000 : 64000).setFillPredicate(ModHandler::isWater);

    public MetaTileEntityFarmingStation(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        int range = (9 + (2 * tier)) * (9 + (2 * tier));
        this.workableHandler.initialize(range);
        this.initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityFarmingStation(this.metaTileEntityId, this.getTier());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("machine.universal.linked.entity.radius", (9 + (2 * this.getTier())) / 2, (9 + (2 * this.getTier())) / 2));
        tooltip.add(I18n.format("tj.machine.farming_station.description", this.getTier() >= GTValues.ZPM ? 4 : this.getTier() >= GTValues.EV ? 2 : 1));
        tooltip.add(I18n.format("tj.machine.farming_station.fertilizer.tooltip", this.getTier() * 10));
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote)
            this.workableHandler.update();
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return this.seedInventory != null ? new ItemHandlerList(Arrays.asList(this.seedInventory, this.toolInventory, this.fertilizerInventory)) : super.createImportItemHandler();
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new LargeItemStackHandler(6 + (3 * this.getTier()), this.getTier() >= GTValues.ZPM ? 256 : this.getTier() >= GTValues.EV ? 128 : 64);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return this.waterTank != null ? new FluidTankList(true, this.waterTank) : super.createImportFluidHandler();
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        WidgetGroup widgetGroup = new WidgetGroup(new Position(10, 22));
        SlotScrollableWidgetGroup scrollableWidgetGroup = new SlotScrollableWidgetGroup(105, 22, 64, 54, 3);
        for (int i = 0; i < this.seedInventory.getSlots(); i++) {
            widgetGroup.addWidget(new TJSlotWidget<>(this.seedInventory, i, 18 * (i % 2), 18 * (i / 2))
                    .setBackgroundTexture(SLOT, SEEDS_OVERLAY));
        }
        for (int i = 0; i < this.exportItems.getSlots(); i++) {
            scrollableWidgetGroup.addWidget(new SlotWidget(this.exportItems, i, 18 * (i % 3), 18 * (i / 3), true, false)
                    .setBackgroundTexture(SLOT));
        }
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 182)
                .widget(new TJLabelWidget(7, -18, 166, 20, TJGuiTextures.MACHINE_LABEL)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(new ProgressWidget(this.workableHandler::getProgressPercent, 77, 21, 21, 20, PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL))
                .widget(new TJSlotWidget<>(this.toolInventory, 0, 52, 22)
                        .setBackgroundTexture(SLOT, HOE_OVERLAY))
                .widget(new TJSlotWidget<>(this.toolInventory, 1, 52, 40)
                        .setBackgroundTexture(SLOT, AXE_OVERLAY))
                .widget(new TJSlotWidget<>(this.toolInventory, 2, 52, 58)
                        .setBackgroundTexture(SLOT, SHEARS_OVERLAY))
                .widget(new TJSlotWidget<>(this.fertilizerInventory, 0, 52, 78)
                        .setBackgroundTexture(SLOT, BONE_MEAL_OVERLAY))
                .widget(new TJSlotWidget<>(this.fertilizerInventory, 1, 34, 78)
                        .setBackgroundTexture(SLOT, BONE_MEAL_OVERLAY))
                .widget(new DischargerSlotWidget(this.chargerInventory, 0, 79, 78)
                        .setBackgroundTexture(SLOT, CHARGER_OVERLAY))
                .widget(new TankWidget(this.waterTank, 105, 78, 18, 18)
                        .setBackgroundTexture(FLUID_SLOT))
                .widget(new ToggleButtonWidget(133, 78, 18, 18, ITEM_VOID_BUTTON, this.workableHandler::isVoidOutputs, this.workableHandler::setVoidOutputs)
                        .setTooltipText("machine.universal.toggle.item_voiding"))
                .widget(new ToggleButtonWidget(151, 78, 18, 18, POWER_BUTTON, this.workableHandler::isWorkingEnabled, this.workableHandler::setWorkingEnabled)
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .widget(new ToggleButtonWidget(7, 78, 18, 18, BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setItemAutoOutput)
                        .setTooltipText("gregtech.gui.item_auto_output.tooltip"))
                .widget(new ToggleButtonWidget(79, 58, 18, 18, BUTTON_ALLOW_IMPORT_EXPORT, this.workableHandler::isOutputTools, this.workableHandler::setOutputTools)
                        .setTooltipText("tj.machine.farming_station.tool_output.tooltip"))
                .widget(new ImageWidget(79, 42, 18, 18, INDICATOR_NO_ENERGY)
                        .setPredicate(this.workableHandler::hasNotEnoughEnergy))
                .widget(widgetGroup)
                .widget(scrollableWidgetGroup)
                .bindPlayerInventory(player.inventory, 100)
                .build(this.getHolder(), player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        TJTextures.TJ_MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, EnumFacingHelper.getTopFacingFrom(this.getFrontFacing()), this.workableHandler.isActive(), this.workableHandler.hasProblem(), this.workableHandler.isWorkingEnabled());
        Textures.PIPE_OUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        if (this.isAutoOutputItems())
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
    }

    private IItemHandlerModifiable getToolInventory() {
        return this.toolInventory;
    }

    private IItemHandlerModifiable getFertilizerInventory() {
        return this.fertilizerInventory;
    }
}
