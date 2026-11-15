package tj.multiblockpart.utility;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.GAValues;
import gregicadditions.machines.multi.multiblockpart.GAMetaTileEntityMultiblockPart;
import gregtech.api.capability.IInventoryNotifiable;
import gregtech.api.capability.impl.NotifiableItemHandlerList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.render.Textures;
import gregtech.api.util.Position;
import gregtech.common.gui.widget.GhostCircuitWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import tj.TJValues;
import tj.integration.ae2.IItemConfigSetter;
import tj.items.handlers.NotifiableLargeItemStackHandler;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.TJModularUI;
import tj.mui.widgets.impl.*;
import tj.textures.TJTextures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class MetaTileEntityCreativeItemBus extends GAMetaTileEntityMultiblockPart implements IItemConfigSetter,
        IMultiblockAbilityPart<IItemHandlerModifiable> {

    private final ItemStackHandler circuitInventory = new NotifiableItemStackHandler(this, 1);
    private final IItemHandlerModifiable combinedInventory;

    public MetaTileEntityCreativeItemBus(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GAValues.MAX);
        this.combinedInventory = new NotifiableItemHandlerList(this, Arrays.asList(this.circuitInventory, this.importItems));
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityCreativeItemBus(this.metaTileEntityId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("cover.creative.only"));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableLargeItemStackHandler(this, 16, Integer.MAX_VALUE) {

            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (amount == Integer.MIN_VALUE)
                    return super.extractItem(slot, amount, simulate);
                final ItemStack stack = this.getStackInSlot(slot).copy();
                stack.setCount(Math.min(stack.getCount(), amount));
                return stack;
            }
        };
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(43, 24));
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(43, 24, 72, 72);
        for (int i = 0; i < this.importItems.getSlots(); i++) {
            final int x = 18 + 18 * (i % 4);
            final int y = 18 * (i / 4);
            widgetGroup.addWidget(new TJPhantomItemSlotWidget(x, y, 18, 18, i, this.importItems, item -> {})
                    .setBackgroundTextures(GuiTextures.SLOT)
                    .setSpecialExtractingMode(true));
            selectionWidgetGroup.addSelectionBox(i, x, y, 18, 18);
        }
        return TJModularUI.builder(this.getHolder(), player)
                .setWidthHeight(196, 184)
                .widget(new TJLabelWidget(7, -19, 180, 19, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(new GhostCircuitWidget(this.circuitInventory, 151, 78))
                .widget(widgetGroup)
                .widget(selectionWidgetGroup)
                .widget(TJGuiUtils.createItemConfigPanel(this, selectionWidgetGroup))
                .build();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.getController() == null) {
            final int oldBaseColor = renderState.baseColour;
            final int oldAlphaOverride = renderState.alphaOverride;

            // TODO get better MAX color overlay. use UMV color overlay for the time being
            renderState.baseColour = TJValues.VC[this.getTier() - 2] << 8;
            renderState.alphaOverride = 0xFF;

            for (EnumFacing facing : EnumFacing.VALUES)
                TJTextures.SUPER_HATCH_OVERLAY.renderSided(facing, renderState, translation, pipeline);

            renderState.baseColour = oldBaseColor;
            renderState.alphaOverride = oldAlphaOverride;
        }
        Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.ITEM_HATCH_INPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("ghostCircuit", this.circuitInventory.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.circuitInventory.deserializeNBT(data.getCompoundTag("ghostCircuit"));
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        if (this.combinedInventory instanceof IInventoryNotifiable)
            ((IInventoryNotifiable) this.combinedInventory).addMetaTileEntityToNotifiable(controllerBase);
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        if (this.combinedInventory instanceof IInventoryNotifiable)
            ((IInventoryNotifiable) this.combinedInventory).removeMetaTileEntityFromNotifiable(controllerBase);
    }

    @Override
    public void registerAbilities(List<IItemHandlerModifiable> list) {
        list.add(this.combinedInventory);
    }

    @Override
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return MultiblockAbility.IMPORT_ITEMS;
    }

    @Override
    public void setItemCount(String text, int index) {
        if (index < 0 || index >= this.importItems.getSlots()) return;
        final ItemStack stack = this.importItems.getStackInSlot(index);
        if (stack.isEmpty()) return;
        stack.setCount((int) Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        // to notify MTEs that inventory contents have changed
        this.importItems.setStackInSlot(index, stack);
        this.markDirty();
    }

    @Override
    public String getItemCount(int index) {
        return String.valueOf(index >= 0 && index < this.importItems.getSlots() ? this.importItems.getStackInSlot(index).getCount() : 0);
    }
}
