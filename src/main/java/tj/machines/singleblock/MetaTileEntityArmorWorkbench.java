package tj.machines.singleblock;

import gregicadditions.item.GAMetaItems;
import gregicadditions.machines.overrides.GAMetaTileEntityBatteryBuffer;
import gregtech.api.GregTechAPI;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.util.Position;
import gregtech.common.items.MetaItems;
import gregtech.common.metatileentities.electric.MetaTileEntityBatteryBuffer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.PopUpWidget;
import tj.gui.widgets.impl.TJLabelWidget;
import tj.gui.widgets.impl.TJSlotWidget;
import tj.items.TJMetaItems;
import tj.items.handlers.FilteredItemStackHandler;
import tj.util.TJItemUtils;

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
                final NBTTagCompound compound = TJItemUtils.getCompoundFromStack(itemStack);
                final FilteredItemStackHandler armorSlot = this.armorSlotMap.get(slot);
                for (int i = 0; i < armorSlot.getSlots(); i++) {
                    if (compound.hasKey("slot:" + i))
                        armorSlot.setStackInSlot(i, new ItemStack(compound.getCompoundTag("slot:" + i)));
                }
            }).setOnContentsChangedPost((slot, itemStack) -> {
                if (!itemStack.isEmpty()) return;
                final FilteredItemStackHandler armorSlot = this.armorSlotMap.get(slot);
                for (int i = 0; i < armorSlot.getSlots(); i++) {
                    armorSlot.setStackInSlot(i, ItemStack.EMPTY);
                }
            });

    /**
     * Remove tags if the item in the slot is empty from being extracted otherwise just update the NBT values.
     */
    private String[] removeTags = new String[0];

    public MetaTileEntityArmorWorkbench(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 1);
        for (int i = 0; i < this.armorSlots.getSlots(); i++) {
            final int index = i;
            this.armorSlotMap.put(i, new FilteredItemStackHandler(this, 7, 1)
                    .setItemStackPredicate((slot, itemStack) -> this.setUpgrades(EntityEquipmentSlot.values()[5 - index], slot, itemStack, this.armorSlots.getStackInSlot(index), true, true))
                    .setOnContentsChangedPre((slot, itemStack, insert) -> this.setUpgrades(EntityEquipmentSlot.values()[5 - index], slot, itemStack, this.armorSlots.getStackInSlot(index), false, insert))
                    .setOnContentsChangedPost((slot, itemStack) -> {
                        final ItemStack armorStack = this.armorSlots.getStackInSlot(index);
                        if (itemStack.isEmpty()) {
                            final NBTTagCompound compound = TJItemUtils.getCompoundFromStack(armorStack);
                            compound.removeTag("slot:" + slot);
                            for (String tag : this.removeTags)
                                compound.removeTag(tag);
                        } else this.setUpgrades(EntityEquipmentSlot.values()[5 - index], slot, itemStack, armorStack, false, true);
                    }));
        }
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
                    .setActiveBackgroundTexture(GuiTextures.SLOT));
        }
        final PopUpWidget<?> headPopup = new PopUpWidget<>(43, 10, 0, 0)
                .setIndexSupplier(() -> TJMetaItems.MODULAR_ARMOR_HEAD.isItemEqual(this.armorSlots.getStackInSlot(0)) ? 1 : 0)
                .addPopup(widgetGroup -> false)
                .addPopup(widgetGroup -> {
                    final FilteredItemStackHandler armorSlot = this.armorSlotMap.get(0);
                    for (int i = 0; i < armorSlot.getSlots(); i++) {
                        widgetGroup.addWidget(new TJSlotWidget<>(armorSlot, i, 18 * i, 0)
                                .setActiveBackgroundTexture(GuiTextures.SLOT));
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
                                .setActiveBackgroundTexture(GuiTextures.SLOT));
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
                                .setActiveBackgroundTexture(GuiTextures.SLOT));
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
                                .setActiveBackgroundTexture(GuiTextures.SLOT));
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

    private boolean setUpgrades(EntityEquipmentSlot equipmentSlot, int slot, ItemStack upgradeStack, ItemStack armorStack, boolean check, boolean insert) {
        final NBTTagCompound compound = TJItemUtils.getCompoundFromStack(armorStack);
        boolean applied = false;
        switch (equipmentSlot) {
            case HEAD:
                if ((!insert || !compound.hasKey("nightVision")) && GAMetaItems.NIGHTVISION_GOGGLES.isItemEqual(upgradeStack)) {
                    applied = true;
                    if (!check)
                        compound.setBoolean("nightVision", true);
                    if (!insert)
                        this.removeTags = new String[]{"nightVision"};
                }
                if ((!insert || !compound.hasKey("waterBreathing")) && MetaItems.REBREATHER.isItemEqual(upgradeStack)) {
                    applied = true;
                    if (!check)
                        compound.setBoolean("waterBreathing", true);
                    if (!insert)
                        this.removeTags = new String[]{"waterBreathing"};
                }
                break;
            case CHEST:
                break;
            case LEGS:
                break;
            case FEET:
        }
        if ((!insert || !compound.hasKey("MaxCharge")) && upgradeStack.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
            applied = true;
            if (!check) {
                final IElectricItem electricItem = upgradeStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if (electricItem != null) {
                    compound.setLong("MaxCharge", electricItem.getMaxCharge());
                    compound.setLong("Charge", Math.min(compound.getLong("Charge"), electricItem.getMaxCharge()));
                }
            }
            if (!insert)
                this.removeTags = new String[]{"MaxCharge", "Charge"};
        }
        if ((!insert || !compound.hasKey("tier")) && this.getEnergyBufferTier(upgradeStack) >= 0) {
            applied = true;
            if (!check)
                compound.setInteger("tier", this.getEnergyBufferTier(upgradeStack));
            if (!insert)
                this.removeTags = new String[]{"tier"};
        }
        if (!check && applied)
            compound.setTag("slot:" + slot, upgradeStack.serializeNBT());
        return applied;
    }

    private int getEnergyBufferTier(ItemStack stack) {
        if (Block.getBlockFromItem(stack.getItem()) instanceof BlockMachine) {
            final MetaTileEntity metaTileEntity = GregTechAPI.META_TILE_ENTITY_REGISTRY.getObjectById(stack.getMetadata());
            if (metaTileEntity instanceof MetaTileEntityBatteryBuffer) {
                return ((MetaTileEntityBatteryBuffer) metaTileEntity).getTier();
            } else if (metaTileEntity instanceof GAMetaTileEntityBatteryBuffer) {
                return ((GAMetaTileEntityBatteryBuffer) metaTileEntity).getTier();
            }
        }
        return -1;
    }
}
