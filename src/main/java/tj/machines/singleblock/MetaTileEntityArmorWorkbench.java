package tj.machines.singleblock;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.util.Position;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.PopUpWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.TJSlotWidget;
import tj.items.TJMetaItems;
import tj.items.handlers.FilteredItemStackHandler;

public class MetaTileEntityArmorWorkbench extends TieredMetaTileEntity {

    private final Int2ObjectMap<FilteredItemStackHandler> armorSlotMap = new Int2ObjectOpenHashMap<>();

    private final FilteredItemStackHandler armorSlots = new FilteredItemStackHandler(this, 4, 1)
            .setItemStackPredicate((slot, itemStack) -> {
                switch (slot) {
                    case 0: return TJMetaItems.MODULAR_ARMOR_HEAD.isItemEqual(itemStack);
                    case 1: return TJMetaItems.MODULAR_ARMOR_CHEST.isItemEqual(itemStack);
                    case 2: return TJMetaItems.MODULAR_ARMOR_LEGS.isItemEqual(itemStack);
                    case 3: return TJMetaItems.MODULAR_ARMOR_BOOTS.isItemEqual(itemStack);
                    default: return false;
                }
            }).setOnContentsChangedPre((slot, itemStack, insert) -> {

            }).setOnContentsChangedPost((slot, itemStack) -> {
                if (itemStack.isEmpty()) {
                    final FilteredItemStackHandler armorSlot = this.armorSlotMap.get(slot);
                    for (int i = 0; i < armorSlot.getSlots(); i++) {
                        armorSlot.extractItem(i, Integer.MAX_VALUE, false);
                    }
                }
            });

    public MetaTileEntityArmorWorkbench(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 1);
        this.armorSlotMap.put(0, new FilteredItemStackHandler(this, 7, 1));
        this.armorSlotMap.put(1, new FilteredItemStackHandler(this, 7, 1));
        this.armorSlotMap.put(2, new FilteredItemStackHandler(this, 7, 1));
        this.armorSlotMap.put(3, new FilteredItemStackHandler(this, 7, 1));
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityArmorWorkbench(this.metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer player) {
        final WidgetGroup armorSlotWidgetGroup = new WidgetGroup(new Position(7, 10));
        for (int i = 0; i < this.armorSlots.getSlots(); i++) {
            armorSlotWidgetGroup.addWidget(new TJSlotWidget<>(this.armorSlots, i, 0, 18 * i)
                    .setBackgroundTexture(GuiTextures.SLOT));
        }
        final PopUpWidget<?> headPopup = new PopUpWidget<>(43, 10, 0, 0)
                .setIndexSupplier(() -> TJMetaItems.MODULAR_ARMOR_HEAD.isItemEqual(this.armorSlots.getStackInSlot(0)) ? 1 : 0)
                .addPopup(widgetGroup -> false)
                .addPopup(widgetGroup -> {
                    final FilteredItemStackHandler armorSlot = this.armorSlotMap.get(0);
                    for (int i = 0; i < armorSlot.getSlots(); i++) {
                        widgetGroup.addWidget(new TJSlotWidget<>(armorSlot, i, 18 * i, 0)
                                .setBackgroundTexture(GuiTextures.SLOT));
                    }
                    return false;
                });
        final PopUpWidget<?> chestPopup = new PopUpWidget<>(43, 28, 0, 0)
                .setIndexSupplier(() -> TJMetaItems.MODULAR_ARMOR_CHEST.isItemEqual(this.armorSlots.getStackInSlot(1)) ? 1 : 0)
                .addPopup(widgetGroup -> false)
                .addPopup(widgetGroup -> {
                    final FilteredItemStackHandler armorSlot = this.armorSlotMap.get(1);
                    for (int i = 0; i < armorSlot.getSlots(); i++) {
                        widgetGroup.addWidget(new TJSlotWidget<>(armorSlot, i, 18 * i, 0)
                                .setBackgroundTexture(GuiTextures.SLOT));
                    }
                    return false;
                });
        final PopUpWidget<?> leggingsPopup = new PopUpWidget<>(43, 46, 0, 0)
                .setIndexSupplier(() -> TJMetaItems.MODULAR_ARMOR_LEGS.isItemEqual(this.armorSlots.getStackInSlot(2)) ? 1 : 0)
                .addPopup(widgetGroup -> false)
                .addPopup(widgetGroup -> {
                    final FilteredItemStackHandler armorSlot = this.armorSlotMap.get(2);
                    for (int i = 0; i < armorSlot.getSlots(); i++) {
                        widgetGroup.addWidget(new TJSlotWidget<>(armorSlot, i, 18 * i, 0)
                                .setBackgroundTexture(GuiTextures.SLOT));
                    }
                    return false;
                });
        final PopUpWidget<?> bootsPopup = new PopUpWidget<>(43, 64, 0, 0)
                .setIndexSupplier(() -> TJMetaItems.MODULAR_ARMOR_BOOTS.isItemEqual(this.armorSlots.getStackInSlot(3)) ? 1 : 0)
                .addPopup(widgetGroup -> false)
                .addPopup(widgetGroup -> {
                    final FilteredItemStackHandler armorSlot = this.armorSlotMap.get(3);
                    for (int i = 0; i < armorSlot.getSlots(); i++) {
                        widgetGroup.addWidget(new TJSlotWidget<>(armorSlot, i, 18 * i, 0)
                                .setBackgroundTexture(GuiTextures.SLOT));
                    }
                    return false;
                });
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 166)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(armorSlotWidgetGroup)
                .widget(headPopup)
                .widget(chestPopup)
                .widget(leggingsPopup)
                .widget(bootsPopup)
                .bindPlayerInventory(player.inventory)
                .build(this.getHolder(), player);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setTag("armorSlots", this.armorSlots.serializeNBT());
        for (Int2ObjectMap.Entry<FilteredItemStackHandler> entry : this.armorSlotMap.int2ObjectEntrySet())
            data.setTag("armorSlot:" + entry.getIntKey(), entry.getValue().serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.armorSlots.deserializeNBT(data.getCompoundTag("armorSlots"));
        for (int i = 0; i < this.armorSlots.getSlots(); i++) {
            this.armorSlotMap.get(i).deserializeNBT(data.getCompoundTag("armorSlot:" + i));
        }
    }
}
