package tj.multiblockpart.utility;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregicadditions.machines.multi.multiblockpart.GAMetaTileEntityMultiblockPart;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.PhantomSlotWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.render.Textures;
import gregtech.api.util.Position;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.SlotScrollableWidgetGroup;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.ButtonPopUpWidget;
import tj.gui.widgets.impl.TJToggleButtonWidget;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityFilteredBus extends GAMetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IItemHandlerModifiable> {

    private final ItemStackHandler filterInventory;
    private final boolean isOutput;

    public MetaTileEntityFilteredBus(ResourceLocation metaTileEntityId, int tier, boolean isOutput) {
        super(metaTileEntityId, tier);
        this.isOutput = isOutput;
        this.filterInventory = new ItemStackHandler(this.getTierSlots(tier));
        this.initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityFilteredBus(this.metaTileEntityId, this.getTier(), this.isOutput);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("machine.universal.slots", this.getTierSlots(this.getTier())));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return this.isOutput ? super.createImportItemHandler() : new ItemStackHandler(this.getTierSlots(this.getTier())) {
            @Override
            @Nonnull
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                ItemStack filterStack = filterInventory.getStackInSlot(slot);
                if (!filterStack.isItemEqual(stack) || ItemStack.areItemStackShareTagsEqual(filterStack, stack))
                    return stack;
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return this.isOutput ? new ItemStackHandler(this.getTierSlots(this.getTier())) {
            @Override
            @Nonnull
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                ItemStack filterStack = filterInventory.getStackInSlot(slot);
                if (!filterStack.isItemEqual(stack) || ItemStack.areItemStackShareTagsEqual(filterStack, stack))
                    return stack;
                return super.insertItem(slot, stack, simulate);
            }
        } : super.createExportItemHandler();
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        int startX = Math.max(7, 79 - (9 * (this.getTier() - 1)));
        ButtonPopUpWidget<?> popUpWidget = new ButtonPopUpWidget<>(0, 0, 0, 0)
                .addPopup(widgetGroup -> {
                    WidgetGroup slotGroup = new WidgetGroup(new Position(0, 7));
                    SlotScrollableWidgetGroup slotScrollGroup = new SlotScrollableWidgetGroup(0, 7, 193, 180, 10)
                            .setScrollWidth(5);
                    for (int i = 0; i < this.importItems.getSlots(); i++) {
                        SlotWidget slotWidget = new SlotWidget(this.importItems, i, startX + (18 * (i % Math.min(10, this.getTier() + 1))), 18 * (i / Math.min(10, this.getTier() + 1)))
                                .setBackgroundTexture(GuiTextures.SLOT);
                        if (this.getTier() > 9)
                            slotScrollGroup.addWidget(slotWidget);
                        else slotGroup.addWidget(slotWidget);
                    }
                    widgetGroup.addWidget(this.getTier() > 9 ? slotScrollGroup : slotGroup);
                    return true;
                }).addPopup(new TJToggleButtonWidget(172, 113 + Math.min(144, 18 * (this.getTier() - 1)), 18, 18)
                        .setBackgroundTextures(TJGuiTextures.ITEM_FILTER)
                        .setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                        .useToggleTexture(true), widgetGroup -> {
                    WidgetGroup slotGroup = new WidgetGroup(new Position(0, 7));
                    SlotScrollableWidgetGroup slotScrollGroup = new SlotScrollableWidgetGroup(0, 7, 193, 180, 10)
                            .setScrollWidth(5);
                    for (int i = 0; i < this.filterInventory.getSlots(); i++) {
                        PhantomSlotWidget slotWidget = new PhantomSlotWidget(this.filterInventory, i, startX + (18 * (i % Math.min(10, this.getTier() + 1))), 18 * (i / Math.min(10, this.getTier() + 1)));
                        slotWidget.setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY);
                        if (this.getTier() > 9)
                            slotScrollGroup.addWidget(slotWidget);
                        else slotGroup.addWidget(slotWidget);
                    }
                    widgetGroup.addWidget(this.getTier() > 9 ? slotScrollGroup : slotGroup);
                    return false;
                });
        return ModularUI.builder(GuiTextures.BACKGROUND, 196, 137 + Math.min(144, 18 * (this.getTier() - 1)))
                .widget(new TJLabelWidget(7, -18, 178, 20, TJGuiTextures.MACHINE_LABEL)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(popUpWidget)
                .bindPlayerInventory(player.inventory, 55 + Math.min(144, 18 * (this.getTier() - 1)))
                .build(this.getHolder(), player);
    }

    @Override
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return this.isOutput ? MultiblockAbility.EXPORT_ITEMS : MultiblockAbility.IMPORT_ITEMS;
    }

    @Override
    public void registerAbilities(List<IItemHandlerModifiable> list) {
        list.add(this.isOutput ? this.getExportItems() : this.getImportItems());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.isOutput) {
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            Textures.ITEM_HATCH_OUTPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        } else {
            Textures.ITEM_HATCH_INPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
            Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setTag("filterInventory", this.filterInventory.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.filterInventory.deserializeNBT(data.getCompoundTag("filterInventory"));
    }

    private int getTierSlots(int tier) {
        tier++;
        return tier * tier;
    }
}
