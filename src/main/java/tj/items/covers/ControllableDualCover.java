package tj.items.covers;

import gregicadditions.GAValues;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.util.Position;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.TransferMode;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;
import tj.builder.WidgetTabBuilder;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.PopUpWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.TJSlotWidget;
import tj.gui.widgets.impl.SelectionWidgetGroup;
import tj.gui.widgets.impl.TJPhantomFluidSlotWidget;
import tj.gui.widgets.impl.TJPhantomItemSlotWidget;
import tj.util.Counter;
import tj.util.TJItemUtils;

import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static gregicadditions.item.GAMetaItems.*;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation.LEFT;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner.TOP;
import static gregtech.common.items.MetaItems.*;
import static tj.items.TJMetaItems.*;

public class ControllableDualCover extends DualCover {

    private final Object2ObjectMap<Item, Counter> itemExact = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<FluidStack, Counter> fluidExact = new Object2ObjectOpenHashMap<>();
    private TransferMode robotArmMode = TransferMode.TRANSFER_ANY;
    private TransferMode regulatorMode = TransferMode.TRANSFER_ANY;
    private int itemSupplyThroughput;
    private int fluidSupplyThroughput;

    public ControllableDualCover(ICoverable coverHolder, EnumFacing attachedSide, int tier) {
        super(coverHolder, attachedSide, tier);
        this.itemFilterSlot.setItemStackPredicate((slot, itemStack) -> ITEM_FILTER.isItemEqual(itemStack) || SMART_FILTER.isItemEqual(itemStack) || ORE_DICTIONARY_FILTER.isItemEqual(itemStack))
                .setOnContentsChangedPre((slot, itemStack, insert) -> {
                    if (!insert) return;
                    this.itemFilterType = ITEM_FILTER.isItemEqual(itemStack) ? FilterType.NORMAL : SMART_FILTER.isItemEqual(itemStack) ? FilterType.SMART : ORE_DICTIONARY_FILTER.isItemEqual(itemStack) ? FilterType.ORE_DICT : null;
                });
        this.fluidFilterSlot.setItemStackPredicate((slot, itemStack) -> FLUID_FILTER.isItemEqual(itemStack) || SMART_FILTER.isItemEqual(itemStack))
                .setOnContentsChangedPre((slot, itemStack, insert) -> {
                    if (!insert) return;
                    this.fluidFilterType = FLUID_FILTER.isItemEqual(itemStack) ? FilterType.NORMAL : SMART_FILTER.isItemEqual(itemStack) ? FilterType.SMART : null;
                });
    }

    @Override
    public void onAttached(ItemStack itemStack) {
        super.onAttached(itemStack);
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("init");
        if (compound.hasKey("robotArmMode"))
            this.robotArmMode = TransferMode.values()[compound.getInteger("robotArmMode")];
        if (compound.hasKey("regulatorMode"))
            this.regulatorMode = TransferMode.values()[compound.getInteger("regulatorMode")];
        if (compound.hasKey("itemSupplyThroughput"))
            this.itemSupplyThroughput = compound.getInteger("itemSupplyThroughput");
        if (compound.hasKey("fluidSupplyThroughput"))
            this.fluidSupplyThroughput = compound.getInteger("fluidSupplyThroughput");
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        final MetaItem<?>.MetaValueItem[] robotArms = {null, ROBOT_ARM_LV, ROBOT_ARM_MV, ROBOT_ARM_HV, ROBOT_ARM_EV, ROBOT_ARM_IV, ROBOT_ARM_LUV, ROBOT_ARM_ZPM, ROBOT_ARM_UV, ROBOT_ARM_UHV, ROBOT_ARM_UEV, ROBOT_ARM_UIV, ROBOT_ARM_UMV, ROBOT_ARM_UXV, ROBOT_ARM_MAX};
        final MetaItem<?>.MetaValueItem[] regulators = {null, FLUID_REGULATOR_LV, FLUID_REGULATOR_MV, FLUID_REGULATOR_HV, FLUID_REGULATOR_EV, FLUID_REGULATOR_IV, FLUID_REGULATOR_LUV, FLUID_REGULATOR_ZPM, FLUID_REGULATOR_UV, FLUID_REGULATOR_UHV, null, null, FLUID_REGULATOR_UMV, null, FLUID_REGULATOR_MAX};
        final WidgetGroup itemWidgetGroup = new WidgetGroup(new Position(7, 95));
        final WidgetGroup fluidWidgetGroup = new WidgetGroup(new Position(7, 115));
        final SelectionWidgetGroup itemSelectionWidgetGroup = new SelectionWidgetGroup(7, 95, 72, 72);
        final SelectionWidgetGroup fluidSelectionWidgetGroup = new SelectionWidgetGroup(7, 115, 72, 72);
        for (int i = 0; i < this.itemFilter.getSlots(); i++) {
            final int index = i;
            final BiConsumer<String, String> setItemCount = (text, id) -> {
                ItemStack stack = this.itemFilter.extractItem(index, Integer.MAX_VALUE, true);
                if (stack.isEmpty()) return;
                stack = this.itemFilter.extractItem(index, Integer.MAX_VALUE, false);
                stack.setCount(Math.max(1, (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
                this.itemFilter.insertItem(index, stack, false);
                this.itemType.put(stack.getItem(), stack);
            };
            itemWidgetGroup.addWidget(new TJPhantomItemSlotWidget(18 * (i % 4), 18 * (i / 4), 18, 18, i, this.itemFilter, item -> {
                if (!item.isEmpty())
                    this.itemType.put(item.getItem(), item);
            }, item -> this.itemType.remove(item.getItem())).setBackgroundTextures(GuiTextures.SLOT)
                    .setPutItemsPredicate(item -> this.itemType.get(item.getItem()) == null));
            itemSelectionWidgetGroup.addSubWidget(i, new NewTextFieldWidget<>(84, 0, 76, 18, true, () -> String.valueOf(itemFilter.getStackInSlot(index).getCount()), setItemCount)
                    .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                    .setUpdateOnTyping(true));
            itemSelectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(84, 18, 38, 18, "/2", data -> setItemCount.accept(String.valueOf((long) this.itemFilter.getStackInSlot(index).getCount() / 2), "")));
            itemSelectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(122, 18, 38, 18, "*2", data -> setItemCount.accept(String.valueOf((long) this.itemFilter.getStackInSlot(index).getCount() * 2), "")));
            itemSelectionWidgetGroup.addSelectionBox(i, 18 * (i % 4), 18 * (i / 4), 18, 18);
        }
        for (int i = 0; i < this.fluidFilter.getTanks(); i++) {
            final int index = i;
            final BiConsumer<String, String> setFluidCount = (text, id) -> {
                FluidStack stack = this.fluidFilter.getTankAt(index).drain(Integer.MAX_VALUE, false);
                if (stack == null) return;
                stack = this.fluidFilter.getTankAt(index).drain(Integer.MAX_VALUE, true);
                if (stack == null) return;
                stack.amount = Math.max(1, (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
                this.fluidFilter.getTankAt(index).fill(stack, true);
                this.fluidType.put(stack, stack);
            };
            fluidWidgetGroup.addWidget(new TJPhantomFluidSlotWidget(18 * (i % 4), 18 * (i / 4), 18, 18, i, this.fluidFilter, fluid -> {
                if (fluid != null)
                    this.fluidType.put(fluid, fluid);
            }, this.fluidType::remove).setBackgroundTexture(GuiTextures.FLUID_SLOT)
                    .setPutFluidsPredicate(fluid -> this.fluidType.get(fluid) == null));
            fluidSelectionWidgetGroup.addSubWidget(i, new NewTextFieldWidget<>(81, 0, 76, 18, true, () -> String.valueOf(fluidFilter.getTankAt(index).getFluidAmount()), setFluidCount)
                    .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                    .setUpdateOnTyping(true));
            fluidSelectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(81, 18, 38, 18, "/2", data -> setFluidCount.accept(String.valueOf((long) this.fluidFilter.getTankAt(index).getFluidAmount() / 2), "")));
            fluidSelectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(119, 18, 38, 18, "*2", data -> setFluidCount.accept(String.valueOf((long) this.fluidFilter.getTankAt(index).getFluidAmount() * 2), "")));
            fluidSelectionWidgetGroup.addSelectionBox(i, 18 * (i % 4), 18 * (i / 4), 18, 18);
        }
        final PopUpWidget<?> itemFilterPopup = new PopUpWidget<>()
                .setIndexSupplier(() -> {
                    final ItemStack itemStack = this.itemFilterSlot.getStackInSlot(0);
                    return ITEM_FILTER.isItemEqual(itemStack) ? 1 : SMART_FILTER.isItemEqual(itemStack) ? 2 : ORE_DICTIONARY_FILTER.isItemEqual(itemStack) ? 3 : 0;
                }).addPopup(widgetGroup -> {
                    widgetGroup.addWidget(new NewTextFieldWidget<>(91, 95, 76, 18, true, () -> String.valueOf(this.itemSupplyThroughput), this::setItemSupplyThroughput)
                            .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setUpdateOnTyping(true));
                    widgetGroup.addWidget(new ClickButtonWidget(91, 113, 38, 18, "/2", data -> this.setItemSupplyThroughput(String.valueOf((long) this.itemSupplyThroughput / 2), "")));
                    widgetGroup.addWidget(new ClickButtonWidget(129, 113, 38, 18, "*2", data -> this.setItemSupplyThroughput(String.valueOf((long) this.itemSupplyThroughput * 2), "")));
                    return false;
                }).addPopup(widgetGroup -> {
                    widgetGroup.addWidget(itemWidgetGroup);
                    widgetGroup.addWidget(itemSelectionWidgetGroup);
                    return false;
                }).addPopup(widgetGroup -> false)
                .addPopup(widgetGroup -> false);
        final PopUpWidget<?> fluidFilterPopup = new PopUpWidget<>()
                .setIndexSupplier(() -> {
                    final ItemStack itemStack = this.fluidFilterSlot.getStackInSlot(0);
                    return FLUID_FILTER.isItemEqual(itemStack) ? 1 : SMART_FILTER.isItemEqual(itemStack) ? 2 : 0;
                })
                .addPopup(widgetGroup -> {
                    widgetGroup.addWidget(new NewTextFieldWidget<>(88, 115, 76, 18, true, () -> String.valueOf(this.fluidSupplyThroughput), this::setFluidSupplyThroughput)
                            .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setUpdateOnTyping(true));
                    widgetGroup.addWidget(new ClickButtonWidget(88, 133, 38, 18, "/2", data -> this.setFluidSupplyThroughput(String.valueOf((long) this.fluidSupplyThroughput / 2), "")));
                    widgetGroup.addWidget(new ClickButtonWidget(126, 133, 38, 18, "*2", data -> this.setFluidSupplyThroughput(String.valueOf((long) this.fluidSupplyThroughput * 2), "")));
                    return false;
                }).addPopup(widgetGroup -> {
                    widgetGroup.addWidget(fluidWidgetGroup);
                    widgetGroup.addWidget(fluidSelectionWidgetGroup);
                    return false;
                }).addPopup(widgetGroup -> false);
        final WidgetTabBuilder tabBuilder = new WidgetTabBuilder()
                .setTabListRenderer(() -> new VerticalTabListRenderer(TOP, LEFT))
                .addTab(String.format("metaitem.robot.arm.%s.name", GAValues.VN[this.tier].toLowerCase()), this.tier > 0 ? robotArms[this.tier].getStackForm() : this.getPickItem(), tab -> {
                    tab.add(new ClickButtonWidget(10, 20, 20, 20, "-10", data -> this.setItemTransferRate(this.itemTransferRate - (data.isShiftClick ? 100 : 10))));
                    tab.add(new ClickButtonWidget(146, 20, 20, 20, "+10", data -> this.setItemTransferRate(this.itemTransferRate + (data.isShiftClick ? 100 : 10))));
                    tab.add(new ClickButtonWidget(30, 20, 20, 20, "-1", data -> this.setItemTransferRate(this.itemTransferRate - (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(126, 20, 20, 20, "+1", data -> this.setItemTransferRate(this.itemTransferRate + (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(50, 20, 38, 20,"/2", data -> this.setItemTransferRate(this.itemTransferRate / 2D)));
                    tab.add(new ClickButtonWidget(88, 20, 38, 20, "*2", data -> this.setItemTransferRate(this.itemTransferRate * 2)));
                    tab.add(new NewTextFieldWidget<>(10, 40, 156, 18, true, () -> String.valueOf(this.itemTransferRate), this::setItemTransferRate)
                            .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setUpdateOnTyping(true));
                    tab.add(new CycleButtonWidget(10, 65, 75, 20, CoverConveyor.ConveyorMode.class, () -> this.conveyorMode, this::setConveyorMode));
                    tab.add(new CycleButtonWidget(91, 65, 76, 20, TransferMode.class, () -> this.robotArmMode, this::setRobotArmMode)
                            .setTooltipHoverString("cover.robotic_arm.transfer_mode.description"));
                    tab.add(new TJSlotWidget<>(this.itemFilterSlot, 0, 91, 131)
                            .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));
                    tab.add(itemFilterPopup);
                    tab.add(new ToggleButtonWidget(91, 149, 18, 18, GuiTextures.BUTTON_BLACKLIST, () -> this.isItemBlacklist, this::setItemBlacklist)
                            .setTooltipText("cover.filter.blacklist"));
                    tab.add(new ToggleButtonWidget(151, 168, 18, 18, TJGuiTextures.POWER_BUTTON, () -> this.isConveyorWorking, this::setConveyorWorking)
                            .setTooltipText("machine.universal.toggle.run.mode"));
                }).addTab(String.format("metaitem.fluid.regulator.%s.name", GAValues.VN[this.tier].toLowerCase()), this.tier > 0 ? regulators[this.tier].getStackForm() : this.getPickItem(), tab -> {
                    tab.add(new ClickButtonWidget(10, 20, 34, 20, "-100", data -> this.setFluidTransferRate(this.fluidTransferRate - (data.isShiftClick ? 500 : 100))));
                    tab.add(new ClickButtonWidget(128, 20, 34, 20, "+100", data -> this.setFluidTransferRate(this.fluidTransferRate + (data.isShiftClick ? 500 : 100))));
                    tab.add(new ClickButtonWidget(44, 20, 22, 20, "-10", data -> this.setFluidTransferRate(this.fluidTransferRate - (data.isShiftClick ? 50 : 10))));
                    tab.add(new ClickButtonWidget(106, 20, 24, 20, "+10", data -> this.setFluidTransferRate(this.fluidTransferRate + (data.isShiftClick ? 50 : 10))));
                    tab.add(new ClickButtonWidget(66, 20, 20, 20, "-1", data -> this.setFluidTransferRate(this.fluidTransferRate - (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(86, 20, 20, 20, "+1", data -> this.setFluidTransferRate(this.fluidTransferRate + (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(10, 40, 76, 20, "/2", data -> this.setFluidTransferRate(this.fluidTransferRate / 2D)));
                    tab.add(new ClickButtonWidget(86, 40, 76, 20, "*2", data -> this.setFluidTransferRate(this.fluidTransferRate * 2)));
                    tab.add(new NewTextFieldWidget<>(10, 60, 156, 18, true, () -> String.valueOf(this.fluidTransferRate), this::setFluidTransferRate)
                            .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setUpdateOnTyping(true));
                    tab.add(new CycleButtonWidget(10, 85, 75, 18, CoverPump.PumpMode.class, () -> this.pumpMode, this::setPumpMode));
                    tab.add(new CycleButtonWidget(88, 85, 76, 18, TransferMode.class, () -> this.regulatorMode, this::setRegulatorMode)
                            .setTooltipHoverString("cover.fluid_regulator.transfer_mode.description"));
                    tab.add(new TJSlotWidget<>(this.fluidFilterSlot, 0, 88, 151)
                            .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));
                    tab.add(fluidFilterPopup);
                    tab.add(new ToggleButtonWidget(88, 169, 18, 18, GuiTextures.BUTTON_BLACKLIST, () -> this.isFluidBlacklist, this::setFluidBlacklist)
                            .setTooltipText("cover.filter.blacklist"));
                    tab.add(new ToggleButtonWidget(151, 168, 18, 18, TJGuiTextures.POWER_BUTTON, () -> this.isPumpWorking, this::setPumpWorking)
                            .setTooltipText("machine.universal.toggle.run.mode"));
                });
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 272)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(this.getPickItem()).setLocale(String.format("metaitem.controllable_dual_cover.%s.name", GAValues.VN[this.tier].toLowerCase())))
                .widget(tabBuilder.build())
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 190)
                .build(this, player);
    }

    @Override
    protected void transferItems(IItemHandler itemHandler, IItemHandler destItemHandler) {
        switch (this.robotArmMode) {
            case TRANSFER_ANY:
                super.transferItems(itemHandler, destItemHandler);
                break;
            case TRANSFER_EXACT:
                if (this.itemSupplyThroughput > this.maxItemTransferRate) break;
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    final ItemStack stack = itemHandler.getStackInSlot(i);
                    ItemStack filterStack = null;
                    if (!stack.isEmpty() && (this.itemFilterType == null || this.isItemBlacklist == ((filterStack = this.itemType.get(stack.getItem())) == null))) {
                        final int inserted = TJItemUtils.insertIntoItemHandler(destItemHandler, stack, true).getCount();
                        final int extract = filterStack != null ? filterStack.getCount() : this.itemSupplyThroughput;
                        final ItemStack otherStack = itemHandler.extractItem(i, Math.min(extract, stack.getCount() - inserted), true);
                        if (stack.getCount() >= extract) {
                            itemHandler.extractItem(i, extract, false);
                            TJItemUtils.insertIntoItemHandler(destItemHandler, otherStack, false);
                        }
                    }
                }
                break;
            case KEEP_EXACT:
                for (int i = 0; i < destItemHandler.getSlots(); i++) {
                    final ItemStack stack = destItemHandler.getStackInSlot(i);
                    this.itemExact.computeIfAbsent(stack.getItem(), k -> new Counter(0))
                            .increment(stack.getCount());
                }
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    final ItemStack stack = itemHandler.getStackInSlot(i);
                    ItemStack filterStack = null;
                    if (!stack.isEmpty() && (this.itemFilterType == null || this.isItemBlacklist == ((filterStack = this.itemType.get(stack.getItem())) == null))) {
                        final int inserted = TJItemUtils.insertIntoItemHandler(destItemHandler, stack, true).getCount();
                        final ItemStack otherStack = itemHandler.extractItem(i, (int) Math.max(0, Math.min(stack.getCount() - inserted, Math.min(this.itemTransferRate, (filterStack != null ? filterStack.getCount() : this.itemTransferRate) - this.itemExact.getOrDefault(stack.getItem(), Counter.DUMMY_COUNTER).getValue()))), false);
                        TJItemUtils.insertIntoItemHandler(destItemHandler, otherStack, false);
                    }
                }
                this.itemExact.clear();
        }
    }

    @Override
    protected void transferFluids(IFluidHandler fluidHandler, IFluidHandler destFluidHandler) {
        switch (this.regulatorMode) {
            case TRANSFER_ANY:
                super.transferFluids(fluidHandler, destFluidHandler);
                break;
            case TRANSFER_EXACT:
                if (this.itemSupplyThroughput > this.maxFluidTransferRate) break;
                final IFluidTankProperties[] tanks = fluidHandler.getTankProperties();
                for (IFluidTankProperties tank : tanks) {
                    FluidStack fluidStack = tank.getContents();
                    FluidStack filterStack = null;
                    if (fluidStack != null && (this.fluidFilterType == null || this.isFluidBlacklist == ((filterStack = this.fluidType.get(fluidStack)) == null))) {
                        fluidStack = fluidHandler.drain(fluidStack, false);
                        if (fluidStack == null) continue;
                        final int extract = filterStack != null ? filterStack.amount : this.fluidSupplyThroughput;
                        if (fluidStack.amount >= extract) {
                            fluidStack.amount = Math.min(fluidStack.amount, extract);
                            fluidStack.amount = destFluidHandler.fill(fluidStack, true);
                            fluidHandler.drain(fluidStack, true);
                        }
                    }
                }
                break;
            case KEEP_EXACT:
                for (IFluidTankProperties tank : destFluidHandler.getTankProperties()) {
                    final FluidStack stack = tank.getContents();
                    if (stack != null) {
                        this.fluidExact.computeIfAbsent(stack, k -> new Counter(0))
                                .increment(stack.amount);
                    }
                }
                final IFluidTankProperties[] tanks1 = fluidHandler.getTankProperties();
                for (IFluidTankProperties tank : tanks1) {
                    FluidStack fluidStack = tank.getContents();
                    FluidStack filterStack = null;
                    if (fluidStack != null && (this.fluidFilterType == null || this.isFluidBlacklist == ((filterStack = this.fluidType.get(fluidStack)) == null))) {
                        fluidStack = fluidHandler.drain(fluidStack, false);
                        if (fluidStack == null) continue;
                        fluidStack.amount = (int) Math.min(fluidStack.amount, Math.min(this.fluidTransferRate, (filterStack != null ? filterStack.amount : this.itemSupplyThroughput) - this.fluidExact.getOrDefault(fluidStack, Counter.DUMMY_COUNTER).getValue()));
                        if (fluidStack.amount > 0) {
                            fluidStack.amount = destFluidHandler.fill(fluidStack, true);
                            fluidHandler.drain(fluidStack, true);
                        }
                    }
                }
                this.fluidExact.clear();
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("robotArmMode", this.robotArmMode.ordinal());
        tagCompound.setInteger("regulatorMode", this.regulatorMode.ordinal());
        tagCompound.setInteger("itemSupplyThroughput", this.itemSupplyThroughput);
        tagCompound.setInteger("fluidSupplyThroughput", this.fluidSupplyThroughput);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.robotArmMode = TransferMode.values()[tagCompound.getInteger("robotArmMode")];
        this.regulatorMode = TransferMode.values()[tagCompound.getInteger("regulatorMode")];
        this.itemSupplyThroughput = tagCompound.getInteger("itemSupplyThroughput");
        this.fluidSupplyThroughput = tagCompound.getInteger("fluidSupplyThroughput");
    }

    public void setItemSupplyThroughput(String text, String id) {
        this.itemSupplyThroughput = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        this.markAsDirty();
    }

    public void setFluidSupplyThroughput(String text, String id) {
        this.fluidSupplyThroughput = (int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
        this.markAsDirty();
    }

    public void setRobotArmMode(TransferMode robotArmMode) {
        this.robotArmMode = robotArmMode;
        this.markAsDirty();
    }

    public void setRegulatorMode(TransferMode regulatorMode) {
        this.regulatorMode = regulatorMode;
        this.markAsDirty();
    }
}
