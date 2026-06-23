package tj.integration.ae2.tile;

import appeng.api.AEApi;
import appeng.api.config.*;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.Api;
import appeng.core.settings.TickRates;
import appeng.helpers.DualityInterface;
import appeng.me.GridAccessException;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.misc.TileInterface;
import appeng.util.item.AEItemStack;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.blocks.block.TJBlocks;
import tj.integration.ae2.helpers.DualitySuperInterface;
import tj.mui.TJGuiTextures;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.impl.*;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;


public class TileStockingInterface extends TileInterface implements ITileEntityUI {

    private int tickTime = 100;

    public TileStockingInterface() {
        ObfuscationReflectionHelper.setPrivateValue(TileInterface.class, this, new DualitySuperInterface(this.getProxy(), this, 10, 36, 9), "duality");
    }

    public void openUI(EntityPlayer player, TileEntity tileEntity) {
        this.openUI(player, tileEntity, null);
    }

    public void openUI(EntityPlayer player, TileEntity tileEntity, EnumFacing facing) {
        TileEntityHolder holder = new TileEntityHolder(tileEntity);
        holder.setFacing(facing);
        holder.openUI((EntityPlayerMP) player);
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.Interface.getMin(), TickRates.Interface.getMax(), this.getInterfaceDuality().getConfigManager().getSetting(Settings.BLOCK) == YesNo.NO, false);
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!this.getProxy().isActive())
            return TickRateModulation.SLEEP;
        final TickRateModulation tickRateModulation = super.tickingRequest(node, ticksSinceLastCall);
        if (this.getInterfaceDuality().getConfigManager().getSetting(Settings.BLOCK) == YesNo.YES) {
            try {
                int index = 0;
                final int stackSize = (int) Math.min(Integer.MAX_VALUE, 1024L << this.getInstalledUpgrades(Upgrades.CAPACITY) * 2);
                final IItemList<?> iItemList = this.getProxy().getStorage().getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)).getStorageList();
                for (IAEStack<?> items : iItemList) {
                    if (index < this.getInterfaceDuality().getConfig().getSlots()) {
                        if (!items.isItem()) continue;
                        final AEItemStack aeItemStack = (AEItemStack) items;
                        final ItemStack itemStack = aeItemStack.createItemStack();
                        if (itemStack.isEmpty()) continue;
                        itemStack.setCount(Math.min(itemStack.getCount(), stackSize));
                        ((AppEngInternalAEInventory) this.getInterfaceDuality().getConfig()).setStackInSlot(index++, itemStack);
                    } else break;
                }
            } catch (GridAccessException ignored) {}
        }
        return TickRateModulation.values()[Math.max(tickRateModulation.ordinal(), this.tickTime > ticksSinceLastCall ? TickRateModulation.SLOWER.ordinal() : this.tickTime < ticksSinceLastCall ? TickRateModulation.FASTER.ordinal() : TickRateModulation.SAME.ordinal())];
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("tickTime", this.tickTime);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.tickTime = Math.max(1, data.getInteger("tickTime"));
    }

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        final DualityInterface duality = this.getInterfaceDuality();
        final DualitySuperInterface.DualityUpgradeInventory upgradeHandler = (DualitySuperInterface.DualityUpgradeInventory) duality.getInventoryByName("upgrades");
        final ButtonPopUpWidget<?> buttonPopUpWidget = new ButtonPopUpWidget<>();
        final ButtonPopUpWidget<?> buttonPopUpTickWidget = new ButtonPopUpWidget<>();
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(0, 0, 0, 0);
        final ImageWidget stackSizeDisplay = new ImageWidget(7, 107, 162, 100, GuiTextures.BORDERED_BACKGROUND);
        final LabelWidget stackSizeLabel = new LabelWidget(14, 112, "machine.universal.stack_size");
        final ClickButtonWidget clickButtonAdd1 = new ClickButtonWidget(15, 127, 25, 20, "+1", data -> this.setStackSize(String.valueOf(Long.parseLong(this.getStackSize(selectionWidgetGroup.getIndex())) + 1), String.valueOf(selectionWidgetGroup.getIndex())));
        final ClickButtonWidget clickButtonAdd10 = new ClickButtonWidget(45, 127, 30, 20, "+10", data -> this.setStackSize(String.valueOf(Long.parseLong(this.getStackSize(selectionWidgetGroup.getIndex())) + 10), String.valueOf(selectionWidgetGroup.getIndex())));
        final ClickButtonWidget clickButtonAdd100 = new ClickButtonWidget(80, 127, 35, 20, "+100", data -> this.setStackSize(String.valueOf(Long.parseLong(this.getStackSize(selectionWidgetGroup.getIndex())) + 100), String.valueOf(selectionWidgetGroup.getIndex())));
        final ClickButtonWidget clickButtonAdd1000 = new ClickButtonWidget(120, 127, 40, 20, "+1000", data -> this.setStackSize(String.valueOf(Long.parseLong(this.getStackSize(selectionWidgetGroup.getIndex())) + 1000), String.valueOf(selectionWidgetGroup.getIndex())));
        final ClickButtonWidget clickButtonSub1 = new ClickButtonWidget(15, 177, 25, 20, "-1", data -> this.setStackSize(String.valueOf(Long.parseLong(this.getStackSize(selectionWidgetGroup.getIndex())) - 1), String.valueOf(selectionWidgetGroup.getIndex())));
        final ClickButtonWidget clickButtonSub10 = new ClickButtonWidget(45, 177, 30, 20, "-10", data -> this.setStackSize(String.valueOf(Long.parseLong(this.getStackSize(selectionWidgetGroup.getIndex())) - 10), String.valueOf(selectionWidgetGroup.getIndex())));
        final ClickButtonWidget clickButtonSub100 = new ClickButtonWidget(80, 177, 35, 20, "-100", data -> this.setStackSize(String.valueOf(Long.parseLong(this.getStackSize(selectionWidgetGroup.getIndex())) - 100), String.valueOf(selectionWidgetGroup.getIndex())));
        final ClickButtonWidget clickButtonSub1000 = new ClickButtonWidget(120, 177, 40, 20, "-1000", data -> this.setStackSize(String.valueOf(Long.parseLong(this.getStackSize(selectionWidgetGroup.getIndex())) - 1000), String.valueOf(selectionWidgetGroup.getIndex())));
        final NewTextFieldWidget<?> stackSizeTextField = new NewTextFieldWidget<>(14, 153, 148, 18, true, null, this::setStackSize)
                .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                .setUpdateOnTyping(true)
                .setMaxStringLength(11);
        stackSizeTextField.setTextSupplier(() -> this.getStackSize((int) stackSizeTextField.getTextIdLong()));
        selectionWidgetGroup.setIndexListener(stackSizeTextField::setTextIdLong);
        final ModularUI.Builder builder = ModularUI.builder(TJGuiTextures.SUPER_INTERFACE, 211, 292);
        for (int i = 0; i < duality.getConfig().getSlots(); i++) {
            builder.widget(new TJPhantomItemSlotWidget(7 + (18 * (i % 9)), 34 + (36 * (i / 9)), 18, 18, i, duality.getConfig())
                    .setBackgroundTextures(TJGuiTextures.SLOT_DOWN));
            selectionWidgetGroup.addSubWidget(i, stackSizeDisplay);
            selectionWidgetGroup.addSubWidget(i, stackSizeLabel);
            selectionWidgetGroup.addSubWidget(i, clickButtonAdd1);
            selectionWidgetGroup.addSubWidget(i, clickButtonAdd10);
            selectionWidgetGroup.addSubWidget(i, clickButtonAdd100);
            selectionWidgetGroup.addSubWidget(i, clickButtonAdd1000);
            selectionWidgetGroup.addSubWidget(i, clickButtonSub1);
            selectionWidgetGroup.addSubWidget(i, clickButtonSub10);
            selectionWidgetGroup.addSubWidget(i, clickButtonSub100);
            selectionWidgetGroup.addSubWidget(i, clickButtonSub1000);
            selectionWidgetGroup.addSubWidget(i, stackSizeTextField);
            selectionWidgetGroup.addSelectionBox(i, 7 + (18 * (i % 9)), 34 + (36 * (i / 9)), 18, 18);
        }
        builder.widget(new LabelWidget(7, 181, "gui.appliedenergistics2.StoredItems"))
                .widget(new LabelWidget(7, 23, "gui.appliedenergistics2.Config"));
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            builder.widget(new TJSlotWidget<>(upgradeHandler, i, 186, 7 + (18 * i))
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY));
        }
        for (int i = 0; i < duality.getStorage().getSlots(); i++) {
            builder.widget(new TJSlotWidget<>(duality.getStorage(), i, 7 + (18 * (i % 9)), 52 + (36 * (i / 9)))
                    .setActiveSupplier(() -> buttonPopUpWidget.getIndex() == 0 && buttonPopUpTickWidget.getIndex() == 0)
                    .setInactiveBackgroundTexture(GuiTextures.SLOT)
                    .setActiveBackgroundTexture(GuiTextures.SLOT));
        }
        return builder.widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getItemStackRepresentation()).setLocale(this.getItemStackRepresentation().getDisplayName()))
                .widget(new TJLabelWidget(4, 0, 162, 18, null)
                        .setDynamicLocale(this::getCustomInventoryName)
                        .setCentered(false)
                        .setCanSlide(false))
                .widget(new LabelWidget(7, 181, "gui.appliedenergistics2.StoredItems"))
                .widget(new LabelWidget(7, 23, "gui.appliedenergistics2.Config"))
                .widget(new TJToggleButtonWidget(-18, 35, 16, 16, () -> duality.getConfigManager().getSetting(Settings.BLOCK).ordinal() == 0, this::setAutoPull)
                        .setToggleTooltipHoverText("tile.me.stocking_interface.auto_pull", "tile.me.stocking_interface.auto_pull")
                        .setToggleTexture(TJGuiTextures.TOGGLE_AUTO_PULL)
                        .useToggleTexture(true))
                .widget(selectionWidgetGroup)
                .widget(buttonPopUpTickWidget.addPopup(widgetGroup -> true)
                        .addPopup(new ButtonWidget<>(132, 0, 22, 22)
                                .setBackgroundTextures(TJGuiTextures.INTERFACE_SETTINGS_LEFT)
                                .setHoverTooltipText("machine.universal.ticks.operation")
                                .setItemDisplay(new ItemStack(Items.CLOCK)), widgetGroup -> {
                            widgetGroup.addWidget(new ImageWidget(7, 107, 162, 100, GuiTextures.BORDERED_BACKGROUND));
                            widgetGroup.addWidget(new LabelWidget(14, 112, "machine.universal.ticks.operation"));
                            widgetGroup.addWidget(new NewTextFieldWidget<>(14, 153, 148, 18, true, () -> String.valueOf(this.tickTime), this::setTickTime)
                                    .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new ClickButtonWidget(15, 127, 25, 20, "+1", data -> this.setTickTime(String.valueOf((long) this.tickTime + 1), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(45, 127, 30, 20, "+10", data -> this.setTickTime(String.valueOf((long) this.tickTime + 10), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(80, 127, 35, 20, "+100", data -> this.setTickTime(String.valueOf((long) this.tickTime + 100), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(120, 127, 40, 20, "+1000", data -> this.setTickTime(String.valueOf((long) this.tickTime + 1000), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(15, 177, 25, 20, "-1", data -> this.setTickTime(String.valueOf((long) this.tickTime - 1), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(45, 177, 30, 20, "-10", data -> this.setTickTime(String.valueOf((long) this.tickTime - 10), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(80, 177, 35, 20, "-100", data -> this.setTickTime(String.valueOf((long) this.tickTime - 100), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(120, 177, 40, 20, "-1000", data -> this.setTickTime(String.valueOf((long) this.tickTime - 1000), "")));
                            return false;
                        })).widget(buttonPopUpWidget.addPopup(widgetGroup -> true)
                        .addPopup(new ButtonWidget<>(154, 0, 22, 22)
                                .setItemDisplay(Api.INSTANCE.definitions().items().certusQuartzWrench().maybeStack(1).orElse(ItemStack.EMPTY))
                                .setBackgroundTextures(TJGuiTextures.INTERFACE_SETTINGS_EDGE_RIGHT)
                                .setTitleHoverTooltipText("gui.appliedenergistics2.Priority"), widgetGroup -> {
                            widgetGroup.addWidget(new ImageWidget(7, 107, 162, 100, GuiTextures.BORDERED_BACKGROUND));
                            widgetGroup.addWidget(new LabelWidget(14, 112, "gui.appliedenergistics2.Priority"));
                            widgetGroup.addWidget(new NewTextFieldWidget<>(14, 153, 148, 18, true, () -> String.valueOf(duality.getPriority()), this::setPriority)
                                    .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new ClickButtonWidget(15, 127, 25, 20, "+1", data -> this.setPriority(String.valueOf((long) duality.getPriority() + 1), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(45, 127, 30, 20, "+10", data -> this.setPriority(String.valueOf((long) duality.getPriority() + 10), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(80, 127, 35, 20, "+100", data -> this.setPriority(String.valueOf((long) duality.getPriority() + 100), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(120, 127, 40, 20, "+1000", data -> this.setPriority(String.valueOf((long) duality.getPriority() + 1000), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(15, 177, 25, 20, "-1", data -> this.setPriority(String.valueOf((long) duality.getPriority() - 1), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(45, 177, 30, 20, "-10", data -> this.setPriority(String.valueOf((long) duality.getPriority() - 10), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(80, 177, 35, 20, "-100", data -> this.setPriority(String.valueOf((long) duality.getPriority() - 100), "")));
                            widgetGroup.addWidget(new ClickButtonWidget(120, 177, 40, 20, "-1000", data -> this.setPriority(String.valueOf((long) duality.getPriority() - 1000), "")));
                            return false;
                        }))
                .bindPlayerInventory(player.inventory, 209)
                .build(holder, player);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return TJBlocks.STOCKING_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
    }

    private void setAutoPull(boolean blockingMode) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.BLOCK, blockingMode ? YesNo.YES : YesNo.NO);
        this.markDirty();
    }

    private void setStackSize(String text, String id) {
        final int slot = Integer.parseInt(id);
        final int maxSize = this.getInterfaceDuality().getConfig().getSlotLimit(0);
        final int stackSize = (int) Math.max(1, Math.min(Long.parseLong(text), maxSize));
        final ItemStack itemStack = this.getInterfaceDuality().getConfig().extractItem(slot, Integer.MAX_VALUE, false);
        if (itemStack.isEmpty()) return;
        itemStack.setCount(stackSize);
        ((AppEngInternalAEInventory) this.getInterfaceDuality().getConfig()).setStackInSlot(slot, itemStack);
        this.markDirty();
    }

    private String getStackSize(int index) {
        return String.valueOf(this.getInterfaceDuality().getConfig().getStackInSlot(index).getCount());
    }

    private void setTickTime(String text, String id) {
        this.tickTime = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        this.markDirty();
    }

    private void setPriority(String text, String id) {
        this.getInterfaceDuality().setPriority((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
        this.markDirty();
    }
}
