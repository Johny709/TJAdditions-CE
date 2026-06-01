package tj.integration.ae2.part;

import appeng.api.config.*;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.parts.IPartModel;
import appeng.core.Api;
import appeng.fluids.helper.DualityFluidInterface;
import appeng.fluids.helper.IConfigurableFluidInventory;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.fluids.util.AEFluidInventory;
import appeng.helpers.DualityInterface;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.misc.PartInterface;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.networking.TileCableBus;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import tj.TJ;
import tj.blocks.block.TJBlocks;
import tj.builder.WidgetTabBuilder;
import tj.gui.TJGuiTextures;
import tj.gui.uifactory.ITileEntityUI;
import tj.gui.uifactory.TileEntityHolder;
import tj.gui.widgets.*;
import tj.gui.widgets.impl.*;
import tj.integration.ae2.helpers.DualitySuperFluidInterface;
import tj.integration.ae2.helpers.DualitySuperInterface;
import tj.integration.ae2.helpers.IDualitySuperFluidInterface;
import tj.items.item.TJItems;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation.LEFT;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner.TOP;

public class PartSuperDualInterface extends PartInterface implements IFluidInterfaceHost, IConfigurableFluidInventory, ITileEntityUI {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(TJ.MODID, "part/me.part.super_dual_interface_base");

    @PartModels
    public static final PartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.super_dual_interface_off"));

    @PartModels
    public static final PartModel MODELS_ON = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.super_dual_interface_on"));

    @PartModels
    public static final PartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(TJ.MODID, "part/me.part.super_dual_interface_has_channel"));

    private final DualitySuperFluidInterface dualityFluid = new DualitySuperFluidInterface(this.getProxy(), this);

    public PartSuperDualInterface(ItemStack is) {
        super(is);
        ObfuscationReflectionHelper.setPrivateValue(PartInterface.class, this, new DualitySuperInterface(this.getProxy(), this), "duality");
    }

    @Override
    public boolean onPartActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        final TileCableBus tileCableBus = (TileCableBus) this.getTile();
        if (tileCableBus != null) {
            if (!player.getEntityWorld().isRemote) {
                TileEntityHolder holder = new TileEntityHolder(tileCableBus);
                holder.setFacing(this.getSide().getFacing());
                holder.openUI((EntityPlayerMP) player);
            }
            return true;
        }
        return true;
    }

    @Override
    public void gridChanged() {
        super.gridChanged();
        this.dualityFluid.gridChanged();
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        final NBTTagCompound compound = new NBTTagCompound();
        this.dualityFluid.writeToNBT(compound);
        data.setTag("dualityFluid", compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.dualityFluid.readFromNBT(data.getCompoundTag("dualityFluid"));
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        this.dualityFluid.tickingRequest(node, ticksSinceLastCall);
    return super.tickingRequest(node, ticksSinceLastCall);
    }

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        final DualityInterface duality = this.getInterfaceDuality();
        final ButtonPopUpWidget<?> buttonPopUpWidget = new ButtonPopUpWidget<>();
        final WidgetTabBuilder tabBuilder = new WidgetTabBuilder()
                .setTabListRenderer(() -> new VerticalTabListRenderer(TOP, LEFT))
                .addTab("tile.me.super_interface.name", TJBlocks.SUPER_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY), this::createInterfaceTab)
                .addTab("tile.me.super_fluid_interface.name", TJBlocks.SUPER_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY), widgets -> this.createFluidInterfaceTab(widgets, buttonPopUpWidget));
        return ModularUI.builder(TJGuiTextures.SUPER_INTERFACE, 211, 292)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getItemStackRepresentation()).setLocale(this.getItemStackRepresentation().getDisplayName()))
                .widget(tabBuilder.build())
                .widget(buttonPopUpWidget.addPopup(widgetGroup -> true)
                        .addPopup(new ButtonWidget<>(154, 0, 22, 22)
                                .setBackgroundTextures(TJGuiTextures.INTERFACE_SETTINGS)
                                .setTooltipText("gui.appliedenergistics2.Priority"), widgetGroup -> {
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

    private void createInterfaceTab(List<Widget> tab) {
        final DualityInterface duality = this.getInterfaceDuality();
        final SlotScrollableWidgetGroup scrollableWidgetGroup = new SlotScrollableWidgetGroup(7, 133, 166, 72, 9)
                .setScrollWidth(4);
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(0, 0, 0, 0);
        final ButtonPopUpWidget<?> buttonPopUpWidget = new ButtonPopUpWidget<>();
        final IItemHandler patternHandler = duality.getInventoryByName("patterns");
        final DualitySuperInterface.DualityUpgradeInventory upgradeHandler = (DualitySuperInterface.DualityUpgradeInventory) duality.getInventoryByName("upgrades");
        for (int i = 0; i < duality.getConfig().getSlots(); i++) {
            final int index = i;
            tab.add(new TJPhantomItemSlotWidget(7 + (18 * (i % 9)), 34 + (36 * (i / 9)), 18, 18, i, duality.getConfig())
                    .setBackgroundTextures(TJGuiTextures.SLOT_DOWN));
            selectionWidgetGroup.addSubWidget(i, new ImageWidget(7, 107, 162, 100, GuiTextures.BORDERED_BACKGROUND));
            selectionWidgetGroup.addSubWidget(i, new LabelWidget(14, 112, "machine.universal.stack_size"));
            selectionWidgetGroup.addSubWidget(i, new NewTextFieldWidget<>(14, 153, 148, 18, true, () -> String.valueOf(duality.getConfig().getStackInSlot(index).getCount()), this::setStackSize)
                    .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                    .setTextId(String.valueOf(index))
                    .setUpdateOnTyping(true));
            selectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(15, 127, 25, 20, "+1", data -> this.setStackSize(String.valueOf((long) duality.getConfig().getStackInSlot(index).getCount() + 1), String.valueOf(index))));
            selectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(45, 127, 30, 20, "+10", data -> this.setStackSize(String.valueOf((long) duality.getConfig().getStackInSlot(index).getCount() + 10), String.valueOf(index))));
            selectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(80, 127, 35, 20, "+100", data -> this.setStackSize(String.valueOf((long) duality.getConfig().getStackInSlot(index).getCount() + 100), String.valueOf(index))));
            selectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(120, 127, 40, 20, "+1000", data -> this.setStackSize(String.valueOf((long) duality.getConfig().getStackInSlot(index).getCount() + 1000), String.valueOf(index))));
            selectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(15, 177, 25, 20, "-1", data -> this.setStackSize(String.valueOf((long) duality.getConfig().getStackInSlot(index).getCount() - 1), String.valueOf(index))));
            selectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(45, 177, 30, 20, "-10", data -> this.setStackSize(String.valueOf((long) duality.getConfig().getStackInSlot(index).getCount() - 10), String.valueOf(index))));
            selectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(80, 177, 35, 20, "-100", data -> this.setStackSize(String.valueOf((long) duality.getConfig().getStackInSlot(index).getCount() - 100), String.valueOf(index))));
            selectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(120, 177, 40, 20, "-1000", data -> this.setStackSize(String.valueOf((long) duality.getConfig().getStackInSlot(index).getCount() - 1000), String.valueOf(index))));
            selectionWidgetGroup.addSelectionBox(i, 7 + (18 * (i % 9)), 34 + (36 * (i / 9)), 18, 18);
        }
        for (int i = 0; i < duality.getStorage().getSlots(); i++) {
            tab.add(new TJSlotWidget<>(duality.getStorage(), i, 7 + (18 * (i % 9)), 52 + (36 * (i / 9)))
                    .setActiveBackgroundTexture(GuiTextures.SLOT));
        }
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            tab.add(new TJSlotWidget<>(upgradeHandler, i, 186, 7 + (18 * i))
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY));
        }
        for (int i = 0; i < patternHandler.getSlots(); i++) {
            final int index = i;
            scrollableWidgetGroup.addWidget(new TJSlotWidget<>(patternHandler, i, 18 * (i % 9), 18 * (i / 9))
                    .setPutItemsPredicate(item -> item.isItemEqual(Api.INSTANCE.definitions().items().encodedPattern().maybeStack(1).orElse(ItemStack.EMPTY)) || item.isItemEqual(TJItemUtils.getItemStackFromName("ae2fc:dense_encoded_pattern")))
                    .setActiveSupplier(() -> index / 9 <= upgradeHandler.getInstalledUpgrades(Upgrades.PATTERN_EXPANSION) && selectionWidgetGroup.getIndex() < 0 && buttonPopUpWidget.getIndex() == 0)
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.PATTERN_OVERLAY)
                    .setInactiveBackgroundTexture(TJGuiTextures.BLANK_SLOT)
                    .setActiveInit(false));
        }
        tab.add(new LabelWidget(7, 109, "gui.appliedenergistics2.StoredItems"));
        tab.add(new LabelWidget(7, 123, "gui.appliedenergistics2.Patterns"));
        tab.add(new LabelWidget(7, 23, "gui.appliedenergistics2.Config"));
        tab.add(new TJToggleButtonWidget(-18, 58, 16, 16, () -> duality.getConfigManager().getSetting(Settings.BLOCK).ordinal() == 0, this::setBlockingMode)
                        .setToggleTooltipHoverText("gui.tooltips.appliedenergistics2.NonBlocking", "gui.tooltips.appliedenergistics2.Blocking")
                        .setToggleTexture(TJGuiTextures.TOGGLE_BLOCKING_MODE)
                        .useToggleTexture(true));
        tab.add(new TJToggleButtonWidget(-18, 76, 16, 16, () -> duality.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL).ordinal() == 0, this::setInterfaceTerminal)
                        .setTooltipText("gui.appliedenergistics2.InterfaceTerminalHint")
                        .setToggleTexture(TJGuiTextures.TOGGLE_INTERFACE_TERMINAL)
                        .useToggleTexture(true));
        tab.add(new TJToggleButtonWidget(-18, 94, 16, 16, () -> duality.getConfigManager().getSetting(Settings.OPERATION_MODE).ordinal() == 0, this::setFluidPacket)
                        .setToggleTooltipHoverText("ae2fc.tooltip.real_fluid.hint", "ae2fc.tooltip.fake_packet.hint")
                        .setToggleTexture(TJGuiTextures.TOGGLE_SEND_FLUID)
                        .useToggleTexture(true));
        tab.add(new TJToggleButtonWidget(-18, 112, 16, 16, () -> duality.getConfigManager().getSetting(Settings.LEVEL_TYPE).ordinal() == 0, this::setSplittingItemsFluids)
                        .setToggleTooltipHoverText("ae2fc.tooltip.allow_splitting.hint", "ae2fc.tooltip.prevent_splitting.hint")
                        .setToggleTexture(TJGuiTextures.TOGGLE_SPLITTING_ITEMS_FLUIDS)
                        .useToggleTexture(true));
        tab.add(new TJCycleButtonWidget<>(-18, 130, 16, 16, (EnumSet<CondenserOutput>) Settings.CONDENSER_OUTPUT.getPossibleValues(), () -> (Enum<CondenserOutput>) duality.getConfigManager().getSetting(Settings.CONDENSER_OUTPUT), this::setBlockModeEx)
                        .setCycleHoverTooltipText("ae2fc.tooltip.block_all.hint", "ae2fc.tooltip.block_item.hint", "ae2fc.tooltip.block_fluid.hint")
                        .setCycleTexture(TJGuiTextures.CYCLE_BLOCKING_MODE_EX));
        tab.add(scrollableWidgetGroup);
        tab.add(selectionWidgetGroup);
    }

    private void createFluidInterfaceTab(List<Widget> tab, ButtonPopUpWidget<?> buttonPopUpWidget) {
        final DualityFluidInterface duality = this.getDualityFluidInterface();
        final IItemHandler upgradeHandler = duality.getInventoryByName("upgrades");
        for (int i = 0; i < duality.getConfig().getSlots(); i++) {
            final int index = i;
            tab.add(new TJPhantomAEFluidSlotWidget(7 + (18 * (i % 9)), 34 + (72 * (i / 9)), 18, 18, i, duality.getConfig(), fluidStack -> ((IDualitySuperFluidInterface) duality).onFluidInventoryHasChanged(duality.getConfig(), index, null, null, null))
                    .setBackgroundTexture(TJGuiTextures.SLOT_DOWN));
        }
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            tab.add(new TJSlotWidget<>(upgradeHandler, i, 186, 7 + (18 * i))
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY));
        }
        for (int i = 0; i < duality.getTanks().getSlots(); i++) {
            tab.add(new AEFluidTankWidget((AEFluidInventory) duality.getTanks(), i, 7 + (18 * (i % 9)), 52 + (72 * (i / 9)), 18, 54)
                    .setActiveSupplier(() -> buttonPopUpWidget.getIndex() == 0)
                    .setBackgroundTextures(GuiTextures.SLOT));
        }
        tab.add(new LabelWidget(7, 181, "gui.appliedenergistics2.StoredFluids"));
        tab.add(new LabelWidget(7, 23, "gui.appliedenergistics2.Config"));
        tab.add(new LabelWidget(7, 198, "container.inventory"));
    }

    @Override
    public boolean hasCapability(Capability<?> capabilityClass) {
        return super.hasCapability(capabilityClass) || this.dualityFluid.hasCapability(capabilityClass, null);
    }

    @Override
    public <T> T getCapability(Capability<T> capabilityClass) {
        final T capability = super.getCapability(capabilityClass);
        return capability != null ? capability : this.dualityFluid.getCapability(capabilityClass, null);
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return TJItems.PART_SUPER_DUAL_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Override
    public DualityFluidInterface getDualityFluidInterface() {
        return this.dualityFluid;
    }

    @Override
    public IFluidHandler getFluidInventoryByName(String name) {
        return this.dualityFluid.getFluidInventoryByName(name);
    }

    @Override
    public void getDrops(List<ItemStack> drops, boolean wrenched) {
        super.getDrops(drops, wrenched);
        this.dualityFluid.addDrops(drops);
    }

    private void setBlockingMode(boolean blockingMode) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.BLOCK, blockingMode ? YesNo.YES : YesNo.NO);
        this.getTile().markDirty();
    }

    private void setInterfaceTerminal(boolean interfaceTerminal) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.INTERFACE_TERMINAL, interfaceTerminal ? YesNo.YES : YesNo.NO);
        this.getTile().markDirty();
    }

    private void setFluidPacket(boolean fluidPacket) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.OPERATION_MODE, fluidPacket ? OperationMode.FILL : OperationMode.EMPTY);
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this.getInterfaceDuality(), fluidPacket, "fluidPacket");
        this.getTile().markDirty();
    }

    private void setSplittingItemsFluids(boolean splittingItemsFluids) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.LEVEL_TYPE, splittingItemsFluids ? LevelType.ITEM_LEVEL : LevelType.ENERGY_LEVEL);
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this.getInterfaceDuality(), splittingItemsFluids, "allowSplitting");
        this.getTile().markDirty();
    }

    private void setBlockModeEx(CondenserOutput blockModeEx) {
        this.getInterfaceDuality().getConfigManager().putSetting(Settings.CONDENSER_OUTPUT, blockModeEx);
        ObfuscationReflectionHelper.setPrivateValue(DualityInterface.class, this.getInterfaceDuality(), blockModeEx.ordinal(), "blockModeEx");
        this.getTile().markDirty();
    }

    private void setStackSize(String text, String id) {
        final int slot = Integer.parseInt(id);
        final int maxSize = this.getInterfaceDuality().getConfig().getSlotLimit(0);
        final int stackSize = (int) Math.max(1, Math.min(Long.parseLong(text), maxSize));
        final ItemStack itemStack = this.getInterfaceDuality().getConfig().extractItem(slot, Integer.MAX_VALUE, false);
        if (itemStack.isEmpty()) return;
        itemStack.setCount(stackSize);
        ((AppEngInternalAEInventory) this.getInterfaceDuality().getConfig()).setStackInSlot(slot, itemStack);
        this.getTile().markDirty();
    }

    private void setPriority(String text, String id) {
        this.getInterfaceDuality().setPriority((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
        this.getTile().markDirty();
    }
}
