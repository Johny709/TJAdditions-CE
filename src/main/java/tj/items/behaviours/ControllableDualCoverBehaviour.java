package tj.items.behaviours;

import gregicadditions.GAValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.util.Position;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.TransferMode;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import tj.builder.WidgetTabBuilder;
import tj.gui.TJGuiTextures;
import tj.gui.TJGuiUtils;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.PopUpWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.TJSlotWidget;
import tj.gui.widgets.impl.SelectionWidgetGroup;
import tj.gui.widgets.impl.TJPhantomFluidSlotWidget;
import tj.gui.widgets.impl.TJPhantomItemSlotWidget;
import tj.items.handlers.FilteredItemStackHandler;
import tj.items.handlers.LargeItemStackHandler;
import tj.util.references.BooleanReference;
import tj.util.references.IntegerReference;
import tj.util.references.ObjectReference;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static gregicadditions.item.GAMetaItems.*;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation.LEFT;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner.TOP;
import static gregtech.common.items.MetaItems.*;
import static tj.items.TJMetaItems.*;

public class ControllableDualCoverBehaviour extends DualCoverBehaviour {

    public ControllableDualCoverBehaviour(int tier) {
        super(tier);
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final MetaItem<?>.MetaValueItem[] robotArms = {null, ROBOT_ARM_LV, ROBOT_ARM_MV, ROBOT_ARM_HV, ROBOT_ARM_EV, ROBOT_ARM_IV, ROBOT_ARM_LUV, ROBOT_ARM_ZPM, ROBOT_ARM_UV, ROBOT_ARM_UHV, ROBOT_ARM_UEV, ROBOT_ARM_UIV, ROBOT_ARM_UMV, ROBOT_ARM_UXV, ROBOT_ARM_MAX};
        final MetaItem<?>.MetaValueItem[] regulators = {null, FLUID_REGULATOR_LV, FLUID_REGULATOR_MV, FLUID_REGULATOR_HV, FLUID_REGULATOR_EV, FLUID_REGULATOR_IV, FLUID_REGULATOR_LUV, FLUID_REGULATOR_ZPM, FLUID_REGULATOR_UV, FLUID_REGULATOR_UHV, null, null, FLUID_REGULATOR_UMV, null, FLUID_REGULATOR_MAX};
        final ObjectReference<CoverConveyor.ConveyorMode> conveyorMode = new ObjectReference<>(CoverConveyor.ConveyorMode.EXPORT);
        final ObjectReference<CoverPump.PumpMode> pumpMode = new ObjectReference<>(CoverPump.PumpMode.EXPORT);
        final ObjectReference<TransferMode> robotArmMode = new ObjectReference<>(TransferMode.TRANSFER_ANY);
        final ObjectReference<TransferMode> regulatorMode = new ObjectReference<>(TransferMode.TRANSFER_ANY);
        final IntegerReference itemTransferRate = new IntegerReference();
        final IntegerReference fluidTransferRate = new IntegerReference();
        final BooleanReference itemBlacklist = new BooleanReference();
        final BooleanReference fluidBlacklist = new BooleanReference();
        final BooleanReference itemWorking = new BooleanReference();
        final BooleanReference fluidWorking = new BooleanReference();
        final ItemStack itemStack = player.getHeldItemMainhand();
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("init");
        final FilteredItemStackHandler itemFilterSlot = new FilteredItemStackHandler(null, 1, 1)
                .setItemStackPredicate((slot, itemStack1) -> ITEM_FILTER.isItemEqual(itemStack1) || SMART_FILTER.isItemEqual(itemStack1) || ORE_DICTIONARY_FILTER.isItemEqual(itemStack1))
                .setOnContentsChangedPre((slot, itemStack1, insert) -> {
                    if (!insert) return;
                    compound.setTag("itemFilterSlot", itemStack1.serializeNBT());
                }).setOnContentsChangedPost((slot, itemStack1) -> {
                    if (itemStack1.isEmpty())
                        compound.removeTag("itemFilterSlot");
                });
        final FilteredItemStackHandler fluidFilterSlot = new FilteredItemStackHandler(null, 1, 1)
                .setItemStackPredicate((slot, itemStack1) -> FLUID_FILTER.isItemEqual(itemStack1) || SMART_FILTER.isItemEqual(itemStack1))
                .setOnContentsChangedPre((slot, itemStack1, insert) -> {
                    if (!insert) return;
                    compound.setTag("fluidFilterSlot", itemStack1.serializeNBT());
                }).setOnContentsChangedPost((slot, itemStack1) -> {
                    if (itemStack1.isEmpty())
                        compound.removeTag("fluidFilterSlot");
                });
        final LargeItemStackHandler itemFilter = new LargeItemStackHandler(16, Integer.MAX_VALUE);
        final FluidTankList fluidFilter = new FluidTankList(true, IntStream.range(0, 16)
                .mapToObj(i -> new FluidTank(Integer.MAX_VALUE))
                .collect(Collectors.toList()));
        final Object2ObjectMap<Item, ItemStack> itemType = new Object2ObjectOpenHashMap<>();
        final Set<FluidStack> fluidType = new HashSet<>();
        final WidgetGroup itemWidgetGroup = new WidgetGroup(new Position(7, 95));
        final WidgetGroup fluidWidgetGroup = new WidgetGroup(new Position(7, 115));
        final SelectionWidgetGroup itemSelectionWidgetGroup = new SelectionWidgetGroup(7, 95, 72, 72);
        final SelectionWidgetGroup fluidSelectionWidgetGroup = new SelectionWidgetGroup(7, 115, 72, 72);
        for (int i = 0; i < itemFilter.getSlots(); i++) {
            final int index = i;
            final BiConsumer<String, String> setItemCount = (text, id) -> {
                ItemStack stack = itemFilter.extractItem(index, Integer.MAX_VALUE, true);
                if (stack.isEmpty()) return;
                stack = itemFilter.extractItem(index, Integer.MAX_VALUE, false);
                stack.setCount(Math.max(1, (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
                compound.setTag("itemSlot:" + index, stack.serializeNBT());
                itemFilter.insertItem(index, stack, false);
            };
            itemWidgetGroup.addWidget(new TJPhantomItemSlotWidget(18 * (i % 4), 18 * (i / 4), 18, 18, i, itemFilter, item -> {
                if (!item.isEmpty()) {
                    itemType.put(item.getItem(), item);
                    compound.setTag("itemSlot:" + index, item.serializeNBT());
                } else compound.removeTag("itemSlot:" + index);
            }, item -> itemType.remove(item.getItem())).setBackgroundTextures(GuiTextures.SLOT)
                    .setPutItemsPredicate(item -> !itemType.containsKey(item.getItem())));
            itemSelectionWidgetGroup.addSubWidget(i, new NewTextFieldWidget<>(83, 0, 75, 18, true, () -> String.valueOf(itemFilter.getStackInSlot(index).getCount()), setItemCount)
                    .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                    .setUpdateOnTyping(true));
            itemSelectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(83, 18, 38, 18, "/2", data -> setItemCount.accept(String.valueOf((long) itemFilter.getStackInSlot(index).getCount() / 2), "")));
            itemSelectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(121, 18, 38, 18, "*2", data -> setItemCount.accept(String.valueOf((long) itemFilter.getStackInSlot(index).getCount() * 2), "")));
            itemSelectionWidgetGroup.addSelectionBox(i, 18 * (i % 4), 18 * (i / 4), 18, 18);
        }
        for (int i = 0; i < fluidFilter.getTanks(); i++) {
            final int index = i;
            final BiConsumer<String, String> setFluidCount = (text, id) -> {
                FluidStack stack = fluidFilter.getTankAt(index).drain(Integer.MAX_VALUE, false);
                if (stack == null) return;
                stack = fluidFilter.getTankAt(index).drain(Integer.MAX_VALUE, true);
                if (stack == null) return;
                stack.amount = Math.max(1, (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text)));
                compound.setTag("fluidSlot:" + index, stack.writeToNBT(new NBTTagCompound()));
                fluidFilter.getTankAt(index).fill(stack, true);
            };
            fluidWidgetGroup.addWidget(new TJPhantomFluidSlotWidget(18 * (i % 4), 18 * (i / 4), 18, 18, i, fluidFilter, fluid -> {
                if (fluid != null) {
                    fluidType.add(fluid);
                    compound.setTag("fluidSlot:" + index, fluid.writeToNBT(new NBTTagCompound()));
                } else compound.removeTag("fluidSlot:" + index);
            }, fluidType::remove).setBackgroundTexture(GuiTextures.FLUID_SLOT)
                    .setPutFluidsPredicate(fluid -> !fluidType.contains(fluid)));
            fluidSelectionWidgetGroup.addSubWidget(i, new NewTextFieldWidget<>(81, 0, 75, 18, true, () -> String.valueOf(fluidFilter.getTankAt(index).getFluidAmount()), setFluidCount)
                    .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                    .setUpdateOnTyping(true));
            fluidSelectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(81, 18, 38, 18, "/2", data -> setFluidCount.accept(String.valueOf((long) fluidFilter.getTankAt(index).getFluidAmount() / 2), "")));
            fluidSelectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(119, 18, 38, 18, "*2", data -> setFluidCount.accept(String.valueOf((long) fluidFilter.getTankAt(index).getFluidAmount() * 2), "")));
            fluidSelectionWidgetGroup.addSelectionBox(i, 18 * (i % 4), 18 * (i / 4), 18, 18);
        }
        final PopUpWidget<?> itemFilterPopup = new PopUpWidget<>()
                .setIndexSupplier(() -> {
                    final ItemStack itemStack1 = itemFilterSlot.getStackInSlot(0);
                    return ITEM_FILTER.isItemEqual(itemStack1) ? 1 : SMART_FILTER.isItemEqual(itemStack1) ? 2 : ORE_DICTIONARY_FILTER.isItemEqual(itemStack1) ? 3 : 0;
                }).addPopup(widgetGroup -> false)
                .addPopup(widgetGroup -> {
                    widgetGroup.addWidget(itemWidgetGroup);
                    widgetGroup.addWidget(itemSelectionWidgetGroup);
                    return false;
                }).addPopup(widgetGroup -> false)
                .addPopup(widgetGroup -> false);
        final PopUpWidget<?> fluidFilterPopup = new PopUpWidget<>()
                .setIndexSupplier(() -> {
                    final ItemStack itemStack1 = fluidFilterSlot.getStackInSlot(0);
                    return FLUID_FILTER.isItemEqual(itemStack1) ? 1 : SMART_FILTER.isItemEqual(itemStack1) ? 2 : 0;
                })
                .addPopup(widgetGroup -> false)
                .addPopup(widgetGroup -> {
                    widgetGroup.addWidget(fluidWidgetGroup);
                    widgetGroup.addWidget(fluidSelectionWidgetGroup);
                    return false;
                }).addPopup(widgetGroup -> false);
        final BiConsumer<String, String> setItemTransferRate2 = (text, id) -> {
            itemTransferRate.setValue((int) Math.max(1, Math.min(this.maxItemTransferRate, Double.parseDouble(text))));
            compound.setInteger("itemTransferRate", itemTransferRate.getValue());
        };
        final BiConsumer<String, String> setFluidTransferRate2 = (text, id) -> {
            fluidTransferRate.setValue((int) Math.max(1, Math.min(this.maxFluidTransferRate, Double.parseDouble(text))));
            compound.setInteger("fluidTransferRate", fluidTransferRate.getValue());
        };
        final WidgetTabBuilder tabBuilder = new WidgetTabBuilder()
                .setTabListRenderer(() -> new VerticalTabListRenderer(TOP, LEFT))
                .addTab(String.format("metaitem.fluid.regulator.%s.name", GAValues.VN[tier].toLowerCase()), this.tier > 0 ? robotArms[this.tier].getStackForm() : null, tab -> {
                    tab.add(new ClickButtonWidget(10, 20, 20, 20, "-10", data -> this.setItemTransferRate(itemTransferRate, compound, itemTransferRate.getValue() - (data.isShiftClick ? 100 : 10))));
                    tab.add(new ClickButtonWidget(146, 20, 20, 20, "+10", data -> this.setItemTransferRate(itemTransferRate, compound, itemTransferRate.getValue() + (data.isShiftClick ? 100 : 10))));
                    tab.add(new ClickButtonWidget(30, 20, 20, 20, "-1", data -> this.setItemTransferRate(itemTransferRate, compound, itemTransferRate.getValue() - (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(126, 20, 20, 20, "+1", data -> this.setItemTransferRate(itemTransferRate, compound, itemTransferRate.getValue() + (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(50, 20, 38, 20,"/2", data -> this.setItemTransferRate(itemTransferRate, compound, itemTransferRate.getValue() / 2D)));
                    tab.add(new ClickButtonWidget(88, 20, 38, 20, "*2", data -> this.setItemTransferRate(itemTransferRate, compound, itemTransferRate.getValue() * 2)));
                    tab.add(new NewTextFieldWidget<>(10, 40, 156, 18, true, () -> String.valueOf(itemTransferRate.getValue()), setItemTransferRate2)
                            .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setUpdateOnTyping(true));
                    tab.add(new CycleButtonWidget(10, 65, 75, 20, CoverConveyor.ConveyorMode.class, conveyorMode::getValue, conveyorMode1 -> {
                        conveyorMode.setValue(conveyorMode1);
                        compound.setInteger("conveyorMode", conveyorMode1.ordinal());
                    }));
                    tab.add(new CycleButtonWidget(91, 65, 76, 20, TransferMode.class, robotArmMode::getValue, robotArmMode1 -> {
                        robotArmMode.setValue(robotArmMode1);
                        compound.setInteger("robotArmMode", robotArmMode1.ordinal());
                    }).setTooltipHoverString("cover.robotic_arm.transfer_mode.description"));
                    tab.add(new TJSlotWidget<>(itemFilterSlot, 0, 91, 131)
                            .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));
                    tab.add(itemFilterPopup);
                    tab.add(new ToggleButtonWidget(91, 149, 18, 18, GuiTextures.BUTTON_BLACKLIST, itemBlacklist::isValue, b -> {
                        itemBlacklist.setValue(b);
                        compound.setBoolean("itemBlacklist", itemBlacklist.isValue());
                    }).setTooltipText("cover.filter.blacklist"));
                    tab.add(new ToggleButtonWidget(151, 168, 18, 18, TJGuiTextures.POWER_BUTTON, itemWorking::isValue, w -> {
                        itemWorking.setValue(w);
                        compound.setBoolean("itemWorking", itemWorking.isValue());
                    }).setTooltipText("machine.universal.toggle.run.mode"));
                }).addTab(String.format("metaitem.electric.pump.%s.name", GAValues.VN[tier].toLowerCase()), this.tier > 0 ? regulators[this.tier].getStackForm() : null, tab -> {
                    tab.add(new ClickButtonWidget(10, 20, 34, 20, "-100", data -> this.setFluidTransferRate(fluidTransferRate, compound, fluidTransferRate.getValue() - (data.isShiftClick ? 500 : 100))));
                    tab.add(new ClickButtonWidget(128, 20, 34, 20, "+100", data -> this.setFluidTransferRate(fluidTransferRate, compound, fluidTransferRate.getValue() + (data.isShiftClick ? 500 : 100))));
                    tab.add(new ClickButtonWidget(44, 20, 22, 20, "-10", data -> this.setFluidTransferRate(fluidTransferRate, compound, fluidTransferRate.getValue() - (data.isShiftClick ? 50 : 10))));
                    tab.add(new ClickButtonWidget(106, 20, 24, 20, "+10", data -> this.setFluidTransferRate(fluidTransferRate, compound, fluidTransferRate.getValue() + (data.isShiftClick ? 50 : 10))));
                    tab.add(new ClickButtonWidget(66, 20, 20, 20, "-1", data -> this.setFluidTransferRate(fluidTransferRate, compound, fluidTransferRate.getValue() - (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(86, 20, 20, 20, "+1", data -> this.setFluidTransferRate(fluidTransferRate, compound, fluidTransferRate.getValue() + (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(10, 40, 76, 20, "/2", data -> this.setFluidTransferRate(fluidTransferRate, compound, fluidTransferRate.getValue() / 2D)));
                    tab.add(new ClickButtonWidget(86, 40, 76, 20, "*2", data -> this.setFluidTransferRate(fluidTransferRate, compound, fluidTransferRate.getValue() * 2)));
                    tab.add(new NewTextFieldWidget<>(10, 60, 156, 18, true, () -> String.valueOf(fluidTransferRate.getValue()), setFluidTransferRate2)
                            .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setUpdateOnTyping(true));
                    tab.add(new CycleButtonWidget(10, 85, 75, 18, CoverPump.PumpMode.class, pumpMode::getValue, pumpMode1 -> {
                        pumpMode.setValue(pumpMode1);
                        compound.setInteger("pumpMode", pumpMode1.ordinal());
                    }));
                    tab.add(new CycleButtonWidget(88, 85, 76, 18, TransferMode.class, regulatorMode::getValue, regulatorMode1 -> {
                        regulatorMode.setValue(regulatorMode1);
                        compound.setInteger("regulatorMode", regulatorMode1.ordinal());
                    }).setTooltipHoverString("cover.fluid_regulator.transfer_mode.description"));
                    tab.add(new TJSlotWidget<>(fluidFilterSlot, 0, 88, 151)
                            .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));
                    tab.add(fluidFilterPopup);
                    tab.add(new ToggleButtonWidget(88, 169, 18, 18, GuiTextures.BUTTON_BLACKLIST, fluidBlacklist::isValue, b -> {
                        fluidBlacklist.setValue(b);
                        compound.setBoolean("fluidBlacklist", fluidBlacklist.isValue());
                    }).setTooltipText("cover.filter.blacklist"));
                    tab.add(new ToggleButtonWidget(151, 168, 18, 18, TJGuiTextures.POWER_BUTTON, fluidWorking::isValue, w -> {
                        fluidWorking.setValue(w);
                        compound.setBoolean("fluidWorking", fluidWorking.isValue());
                    }).setTooltipText("machine.universal.toggle.run.mode"));
                });
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 272)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setLocale(String.format("metaitem.controllable_dual_cover.%s.name", GAValues.VN[this.tier].toLowerCase())))
                .widget(tabBuilder.build())
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 190, itemStack))
                .bindOpenListener(() -> {
                    if (compound.hasKey("conveyorMode"))
                        conveyorMode.setValue(CoverConveyor.ConveyorMode.values()[compound.getInteger("conveyorMode")]);
                    if (compound.hasKey("pumpMode"))
                        pumpMode.setValue(CoverPump.PumpMode.values()[compound.getInteger("pumpMode")]);
                    if (compound.hasKey("robotArmMode"))
                        robotArmMode.setValue(TransferMode.values()[compound.getInteger("robotArmMode")]);
                    if (compound.hasKey("regulatorMode"))
                        regulatorMode.setValue(TransferMode.values()[compound.getInteger("regulatorMode")]);
                    if (compound.hasKey("itemTransferRate"))
                        itemTransferRate.setValue(compound.getInteger("itemTransferRate"));
                    if (compound.hasKey("fluidTransferRate"))
                        fluidTransferRate.setValue(compound.getInteger("fluidTransferRate"));
                    if (compound.hasKey("itemBlacklist"))
                        itemBlacklist.setValue(compound.getBoolean("itemBlacklist"));
                    if (compound.hasKey("fluidBlacklist"))
                        fluidBlacklist.setValue(compound.getBoolean("fluidBlacklist"));
                    if (compound.hasKey("itemWorking"))
                        itemWorking.setValue(compound.getBoolean("itemWorking"));
                    if (compound.hasKey("fluidWorking"))
                        fluidWorking.setValue(compound.getBoolean("fluidWorking"));
                    if (compound.hasKey("itemFilterSlot"))
                        itemFilterSlot.insertItem(0, new ItemStack(compound.getCompoundTag("itemFilterSlot")), false);
                    if (compound.hasKey("fluidFilterSlot"))
                        fluidFilterSlot.insertItem(0, new ItemStack(compound.getCompoundTag("fluidFilterSlot")), false);
                    for (int i = 0; i < itemFilter.getSlots(); i++) {
                        if (compound.hasKey("itemSlot:" + i)) {
                            itemFilter.setStackInSlot(i, new ItemStack(compound.getCompoundTag("itemSlot:" + i)));
                            itemType.put(itemFilter.getStackInSlot(i).getItem(), itemFilter.getStackInSlot(i));
                        }
                    }
                    for (int i = 0; i < fluidFilter.getTanks(); i++) {
                        if (compound.hasKey("fluidSlot:" + i)) {
                            fluidFilter.getTankAt(i).fill(FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("fluidSlot:" + i)), true);
                            fluidType.add(fluidFilter.getTankAt(i).getFluid());
                        }
                    }
                }).bindCloseListener(() -> itemStack.getOrCreateSubCompound("init").merge(compound))
                .build(holder, player);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("metaitem.controllable_dual_cover.description"));
        lines.add(I18n.format("cover.creative.description"));
    }
}
