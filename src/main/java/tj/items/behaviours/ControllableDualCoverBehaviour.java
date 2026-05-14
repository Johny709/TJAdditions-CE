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
import static gregicadditions.item.GAMetaItems.CONVEYOR_MODULE_MAX;
import static gregicadditions.item.GAMetaItems.CONVEYOR_MODULE_UMV;
import static gregicadditions.item.GAMetaItems.CONVEYOR_MODULE_UXV;
import static gregicadditions.item.GAMetaItems.ELECTRIC_PUMP_MAX;
import static gregicadditions.item.GAMetaItems.ELECTRIC_PUMP_UEV;
import static gregicadditions.item.GAMetaItems.ELECTRIC_PUMP_UHV;
import static gregicadditions.item.GAMetaItems.ELECTRIC_PUMP_UIV;
import static gregicadditions.item.GAMetaItems.ELECTRIC_PUMP_UMV;
import static gregicadditions.item.GAMetaItems.ELECTRIC_PUMP_UXV;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation.LEFT;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner.TOP;
import static gregtech.common.items.MetaItems.*;
import static gregtech.common.items.MetaItems.ELECTRIC_PUMP_UV;

public class ControllableDualCoverBehaviour extends DualCoverBehaviour {

    public ControllableDualCoverBehaviour(int tier) {
        super(tier);
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final MetaItem<?>.MetaValueItem[] conveyors = {null, CONVEYOR_MODULE_LV, CONVEYOR_MODULE_MV, CONVEYOR_MODULE_HV, CONVEYOR_MODULE_EV, CONVEYOR_MODULE_IV, CONVEYOR_MODULE_LUV, CONVEYOR_MODULE_ZPM, CONVEYOR_MODULE_UV, CONVEYOR_MODULE_UHV, CONVEYOR_MODULE_UEV, CONVEYOR_MODULE_UIV, CONVEYOR_MODULE_UMV, CONVEYOR_MODULE_UXV, CONVEYOR_MODULE_MAX};
        final MetaItem<?>.MetaValueItem[] pumps = {null, ELECTRIC_PUMP_LV, ELECTRIC_PUMP_MV, ELECTRIC_PUMP_HV, ELECTRIC_PUMP_EV, ELECTRIC_PUMP_IV, ELECTRIC_PUMP_LUV, ELECTRIC_PUMP_ZPM, ELECTRIC_PUMP_UV, ELECTRIC_PUMP_UHV, ELECTRIC_PUMP_UEV, ELECTRIC_PUMP_UIV, ELECTRIC_PUMP_UMV, ELECTRIC_PUMP_UXV, ELECTRIC_PUMP_MAX};
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
        final LargeItemStackHandler itemFilter = new LargeItemStackHandler(16, 1);
        final FluidTankList fluidFilter = new FluidTankList(true, IntStream.range(0, 16)
                .mapToObj(i -> new FluidTank(Integer.MAX_VALUE))
                .collect(Collectors.toList()));
        final Object2ObjectMap<Item, ItemStack> itemType = new Object2ObjectOpenHashMap<>();
        final Set<FluidStack> fluidType = new HashSet<>();
        final WidgetGroup itemWidgetGroup = new WidgetGroup(new Position(10, 95));
        final WidgetGroup fluidWidgetGroup = new WidgetGroup(new Position(10, 115));
        for (int i = 0; i < itemFilter.getSlots(); i++) {
            final int index = i;
            itemWidgetGroup.addWidget(new TJPhantomItemSlotWidget(18 * (i % 4), 18 * (i / 4), 18, 18, i, itemFilter, item -> {
                if (!item.isEmpty()) {
                    itemType.put(item.getItem(), item);
                    compound.setTag("itemSlot:" + index, item.serializeNBT());
                } else compound.removeTag("itemSlot:" + index);
            }, item -> itemType.remove(item.getItem())).setBackgroundTextures(GuiTextures.SLOT)
                    .setPutItemsPredicate(item -> !itemType.containsKey(item.getItem())));
        }
        for (int i = 0; i < fluidFilter.getTanks(); i++) {
            final int index = i;
            fluidWidgetGroup.addWidget(new TJPhantomFluidSlotWidget(18 * (i % 4), 18 * (i / 4), 18, 18, i, fluidFilter, fluid -> {
                if (fluid != null) {
                    fluidType.add(fluid);
                    compound.setTag("fluidSlot:" + index, fluid.writeToNBT(new NBTTagCompound()));
                } else compound.removeTag("fluidSlot:" + index);
            }, fluidType::remove).setBackgroundTexture(GuiTextures.FLUID_SLOT)
                    .setPutFluidsPredicate(fluid -> !fluidType.contains(fluid)));
        }
        final PopUpWidget<?> itemFilterPopup = new PopUpWidget<>()
                .setIndexSupplier(() -> {
                    final ItemStack itemStack1 = itemFilterSlot.getStackInSlot(0);
                    return ITEM_FILTER.isItemEqual(itemStack1) ? 1 : SMART_FILTER.isItemEqual(itemStack1) ? 2 : ORE_DICTIONARY_FILTER.isItemEqual(itemStack1) ? 3 : 0;
                }).addPopup(widgetGroup -> true)
                .addPopup(widgetGroup -> {
                    widgetGroup.addWidget(itemWidgetGroup);
                    return false;
                }).addPopup(widgetGroup -> false)
                .addPopup(widgetGroup -> false);
        final PopUpWidget<?> fluidFilterPopup = new PopUpWidget<>()
                .setIndexSupplier(() -> {
                    final ItemStack itemStack1 = fluidFilterSlot.getStackInSlot(0);
                    return FLUID_FILTER.isItemEqual(itemStack1) ? 1 : SMART_FILTER.isItemEqual(itemStack1) ? 2 : 0;
                })
                .addPopup(widgetGroup -> true)
                .addPopup(widgetGroup -> {
                    widgetGroup.addWidget(fluidWidgetGroup);
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
                .addTab(String.format("metaitem.conveyor.module.%s.name", GAValues.VN[tier].toLowerCase()), this.tier > 0 ? conveyors[this.tier].getStackForm() : null, tab -> {
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
                    tab.add(new CycleButtonWidget(91, 65, 75, 20, TransferMode.class, robotArmMode::getValue, robotArmMode1 -> {
                        robotArmMode.setValue(robotArmMode1);
                        compound.setInteger("robotArmMode", robotArmMode1.ordinal());
                    }).setTooltipHoverString("cover.robotic_arm.transfer_mode.description"));
                    tab.add(new TJSlotWidget<>(itemFilterSlot, 0, 91, 95)
                            .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));
                    tab.add(itemFilterPopup);
                    tab.add(new ToggleButtonWidget(91, 113, 18, 18, GuiTextures.BUTTON_BLACKLIST, itemBlacklist::isValue, b -> {
                        itemBlacklist.setValue(b);
                        compound.setBoolean("itemBlacklist", itemBlacklist.isValue());
                    }).setTooltipText("cover.filter.blacklist"));
                    tab.add(new ToggleButtonWidget(151, 168, 18, 18, TJGuiTextures.POWER_BUTTON, itemWorking::isValue, w -> {
                        itemWorking.setValue(w);
                        compound.setBoolean("itemWorking", itemWorking.isValue());
                    }).setTooltipText("machine.universal.toggle.run.mode"));
                }).addTab(String.format("metaitem.electric.pump.%s.name", GAValues.VN[tier].toLowerCase()), this.tier > 0 ? pumps[this.tier].getStackForm() : null, tab -> {
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
                    tab.add(new CycleButtonWidget(88, 85, 75, 18, TransferMode.class, regulatorMode::getValue, regulatorMode1 -> {
                        regulatorMode.setValue(regulatorMode1);
                        compound.setInteger("regulatorMode", regulatorMode1.ordinal());
                    }).setTooltipHoverString("cover.fluid_regulator.transfer_mode.description"));
                    tab.add(new TJSlotWidget<>(fluidFilterSlot, 0, 88, 115)
                            .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));
                    tab.add(fluidFilterPopup);
                    tab.add(new ToggleButtonWidget(88, 133, 18, 18, GuiTextures.BUTTON_BLACKLIST, fluidBlacklist::isValue, b -> {
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
                        .setLocale(String.format("metaitem.dual_cover.%s.name", GAValues.VN[this.tier].toLowerCase())))
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
                        itemFilterSlot.deserializeNBT(compound.getCompoundTag("itemFilterSlot"));
                    if (compound.hasKey("fluidFilterSlot"))
                        fluidFilterSlot.deserializeNBT(compound.getCompoundTag("fluidFilterSlot"));
                    for (int i = 0; i < itemFilter.getSlots(); i++) {
                        if (compound.hasKey("itemSlot:" + i)) {
                            itemFilter.setStackInSlot(i, new ItemStack(compound.getCompoundTag("itemSlot" + i)));
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
