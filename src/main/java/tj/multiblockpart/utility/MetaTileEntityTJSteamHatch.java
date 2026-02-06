package tj.multiblockpart.utility;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.machines.multi.multiblockpart.GAMetaTileEntityMultiblockPart;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.FluidContainerSlotWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.recipes.ModHandler;
import gregtech.api.render.Textures;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import tj.TJValues;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.TJLabelWidget;
import tj.textures.TJTextures;

import javax.annotation.Nullable;
import java.util.List;

import static tj.multiblockpart.TJMultiblockAbility.STEAM_OUTPUT;
import static gregicadditions.capabilities.GregicAdditionsCapabilities.STEAM;

public class MetaTileEntityTJSteamHatch extends GAMetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IFluidTank> {

    private final boolean isExport;
    private final IFluidTank steamTank;

    public MetaTileEntityTJSteamHatch(ResourceLocation metaTileEntityId, int tier, boolean isExport) {
        super(metaTileEntityId, tier);
        this.isExport = isExport;
        int capacity = (int) (Math.pow(4, tier) * 16000);
        this.steamTank = new FilteredFluidHandler(Math.min(capacity, Integer.MAX_VALUE)).setFillPredicate(ModHandler::isSteam);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityTJSteamHatch(metaTileEntityId, getTier(), isExport);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        int capacity = (int) (Math.pow(4, getTier()) * 16000);
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_storage_capacity", capacity));
        tooltip.add(I18n.format("gtadditions.machine.steam.steam_hatch.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(1);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return isExport ? super.createImportFluidHandler() : new FluidTankList(false, steamTank);
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return isExport ? new FluidTankList(false, steamTank) : super.createExportFluidHandler();
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote)
            return;
        if (isExport) {
            fillContainerFromInternalTank(importItems, exportItems, 0, 0);
            pushFluidsIntoNearbyHandlers(getFrontFacing());
        } else {
            fillInternalTankFromFluidContainer(importItems, exportItems, 0, 0);
            pullFluidsFromNearbyHandlers(getFrontFacing());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.getController() == null) {
            int oldBaseColor = renderState.baseColour;
            int oldAlphaOverride = renderState.alphaOverride;

            renderState.baseColour = TJValues.VC[this.getTier()] << 8;
            renderState.alphaOverride = 0xFF;

            for (EnumFacing facing : EnumFacing.VALUES)
                TJTextures.SUPER_HATCH_OVERLAY.renderSided(facing, renderState, translation, pipeline);

            renderState.baseColour = oldBaseColor;
            renderState.alphaOverride = oldAlphaOverride;
        }
        Textures.PUMP_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        if (this.isExport) {
            Textures.FLUID_HATCH_OUTPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            Textures.FLUID_OUTPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        } else {
            Textures.FLUID_HATCH_INPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        IFluidTank fluidTank = isExport ? exportFluids.getTankAt(0) : importFluids.getTankAt(0);
        return createTankUI(fluidTank, entityPlayer)
                .build(getHolder(), entityPlayer);
    }

    public ModularUI.Builder createTankUI(IFluidTank fluidTank, EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.defaultBuilder()
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .image(7, 16, 81, 55, GuiTextures.DISPLAY);
        TankWidget tankWidget = new TankWidget(fluidTank, 69, 52, 18, 18)
                .setHideTooltip(true).setAlwaysShowFull(true);
        builder.widget(tankWidget)
                .label(11, 20, "gregtech.gui.fluid_amount", 0xFFFFFF)
                .dynamicLabel(11, 30, tankWidget::getFormattedFluidAmount, 0xFFFFFF)
                .dynamicLabel(11, 40, tankWidget::getFluidLocalizedName, 0xFFFFFF);
        return builder.widget(new FluidContainerSlotWidget(importItems, 0, 90, 17, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.IN_SLOT_OVERLAY))
                .widget(new ImageWidget(91, 36, 14, 15, GuiTextures.TANK_ICON))
                .widget(new SlotWidget(exportItems, 0, 90, 54, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.OUT_SLOT_OVERLAY))
                .bindPlayerInventory(entityPlayer.inventory);
    }

    @Override
    public MultiblockAbility<IFluidTank> getAbility() {
        return isExport ? STEAM_OUTPUT : STEAM;
    }

    @Override
    public void registerAbilities(List<IFluidTank> abilityList) {
        abilityList.addAll(isExport ? exportFluids.getFluidTanks() : importFluids.getFluidTanks());
    }
}
