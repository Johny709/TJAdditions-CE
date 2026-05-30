package tj.items.behaviours;

import gregicadditions.GAValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.util.Position;
import gregtech.common.covers.CoverConveyor;
import gregtech.common.covers.CoverPump;
import gregtech.common.covers.TransferMode;
import gregtech.common.covers.filter.OreDictionaryItemFilter;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.apache.commons.lang3.ArrayUtils;
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
import tj.items.TJMetaItems;
import tj.items.covers.ControllableDualCover;
import tj.items.handlers.FilteredItemStackHandler;
import tj.items.handlers.LargeItemStackHandler;
import tj.util.map.Strategies;
import tj.util.references.BooleanReference;
import tj.util.references.IntegerReference;
import tj.util.references.ObjectReference;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation.LEFT;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner.TOP;
import static gregtech.common.items.MetaItems.*;
import static tj.items.TJMetaItems.CONTROLLABLE_DUAL_COVERS;

public class ControllableDualCoverBehaviour extends DualCoverBehaviour {

    public ControllableDualCoverBehaviour(int tier) {
        super(tier);
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final ObjectReference<CoverConveyor.ConveyorMode> conveyorMode = new ObjectReference<>(CoverConveyor.ConveyorMode.EXPORT);
        final ObjectReference<CoverPump.PumpMode> pumpMode = new ObjectReference<>(CoverPump.PumpMode.EXPORT);
        final ObjectReference<TransferMode> robotArmMode = new ObjectReference<>(TransferMode.TRANSFER_ANY);
        final ObjectReference<TransferMode> regulatorMode = new ObjectReference<>(TransferMode.TRANSFER_ANY);
        final ObjectReference<ControllableDualCover.RecipeMode> itemRecipeMode = new ObjectReference<>(ControllableDualCover.RecipeMode.ELECTROLYZER);
        final ObjectReference<ControllableDualCover.RecipeMode> fluidRecipeMode = new ObjectReference<>(ControllableDualCover.RecipeMode.ELECTROLYZER);
        final IntegerReference itemTicks = new IntegerReference(20);
        final IntegerReference fluidTicks = new IntegerReference(20);
        final IntegerReference itemSupplyThroughput = new IntegerReference();
        final IntegerReference fluidSupplyThroughput = new IntegerReference();
        final IntegerReference itemTransferRate = new IntegerReference(this.maxItemTransferRate);
        final IntegerReference fluidTransferRate = new IntegerReference(this.maxFluidTransferRate);
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
                    if (!itemStack1.isEmpty()) return;
                    compound.removeTag("itemFilterSlot");
                });
        final FilteredItemStackHandler fluidFilterSlot = new FilteredItemStackHandler(null, 1, 1)
                .setItemStackPredicate((slot, itemStack1) -> FLUID_FILTER.isItemEqual(itemStack1) || SMART_FILTER.isItemEqual(itemStack1))
                .setOnContentsChangedPre((slot, itemStack1, insert) -> {
                    if (!insert) return;
                    compound.setTag("fluidFilterSlot", itemStack1.serializeNBT());
                }).setOnContentsChangedPost((slot, itemStack1) -> {
                    if (!itemStack1.isEmpty()) return;
                    compound.removeTag("fluidFilterSlot");
                });
        final LargeItemStackHandler itemFilter = new LargeItemStackHandler(16, Integer.MAX_VALUE);
        final FluidTankList fluidFilter = new FluidTankList(true, IntStream.range(0, 16)
                .mapToObj(i -> new FluidTank(Integer.MAX_VALUE))
                .collect(Collectors.toList()));
        final Object2ObjectMap<ItemStack, ItemStack> itemType = new Object2ObjectOpenCustomHashMap<>(Strategies.ITEMSTACK_STRATEGY);
        final Set<FluidStack> fluidType = new HashSet<>();
        final OreDictionaryItemFilter oreDictionaryItemFilter = new OreDictionaryItemFilter() {
            @Override
            public void initUI(Consumer<Widget> widgetGroup) {
                widgetGroup.accept(new LabelWidget(10, 90, "cover.ore_dictionary_filter.title1"));
                widgetGroup.accept(new LabelWidget(10, 100, "cover.ore_dictionary_filter.title2"));
                widgetGroup.accept(new TextFieldWidget(10, 115, 100, 12, true, () -> this.oreDictionaryFilter, oreDictionaryFilter -> {
                    this.oreDictionaryFilter = oreDictionaryFilter;
                    final NBTTagCompound tagCompound = new NBTTagCompound();
                    this.writeToNBT(tagCompound);
                    compound.setTag("oreDictFilter", tagCompound);
                    this.markDirty();
                }).setMaxStringLength(64)
                        .setValidator(str -> Pattern.compile("\\*?[a-zA-Z0-9_]*\\*?").matcher(str).matches()));
            }
        };
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
                    itemType.put(item, item);
                    compound.setTag("itemSlot:" + index, item.serializeNBT());
                } else compound.removeTag("itemSlot:" + index);
            }, itemType::remove).setBackgroundTextures(GuiTextures.SLOT)
                    .setPutItemsPredicate(item -> !itemType.containsKey(item)));
            itemSelectionWidgetGroup.addSubWidget(i, new NewTextFieldWidget<>(84, 0, 76, 18, true, () -> String.valueOf(itemFilter.getStackInSlot(index).getCount()), setItemCount)
                    .setTooltipText("tj.machine.universal.item_amount").setTooltipFormat(() -> new String[]{String.valueOf(itemFilter.getStackInSlot(index).getCount())})
                    .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                    .setUpdateOnTyping(true));
            itemSelectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(84, 18, 38, 18, "/2", data -> setItemCount.accept(String.valueOf((long) itemFilter.getStackInSlot(index).getCount() / 2), "")));
            itemSelectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(122, 18, 38, 18, "*2", data -> setItemCount.accept(String.valueOf((long) itemFilter.getStackInSlot(index).getCount() * 2), "")));
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
            fluidSelectionWidgetGroup.addSubWidget(i, new NewTextFieldWidget<>(85, 0, 76, 18, true, () -> String.valueOf(fluidFilter.getTankAt(index).getFluidAmount()), setFluidCount)
                    .setTooltipText("tj.machine.universal.fluid_amount").setTooltipFormat(() -> new String[]{String.valueOf(fluidFilter.getTankAt(index).getFluidAmount())})
                    .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                    .setUpdateOnTyping(true));
            fluidSelectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(85, 18, 38, 18, "/2", data -> setFluidCount.accept(String.valueOf((long) fluidFilter.getTankAt(index).getFluidAmount() / 2), "")));
            fluidSelectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(123, 18, 38, 18, "*2", data -> setFluidCount.accept(String.valueOf((long) fluidFilter.getTankAt(index).getFluidAmount() * 2), "")));
            fluidSelectionWidgetGroup.addSelectionBox(i, 18 * (i % 4), 18 * (i / 4), 18, 18);
        }
        final BiConsumer<String, String> setItemSupplyThroughput = (text, id) -> {
            itemSupplyThroughput.setValue((int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
            compound.setInteger("itemSupplyThroughput", itemSupplyThroughput.getValue());
        };
        final WidgetGroup itemSupplyWidgetGroup = new WidgetGroup();
        itemSupplyWidgetGroup.addWidget(new NewTextFieldWidget<>(92, 95, 76, 18, true, () -> String.valueOf(itemSupplyThroughput.getValue()), setItemSupplyThroughput)
                .setTooltipFormat(() -> ArrayUtils.toArray(String.valueOf(itemSupplyThroughput.getValue())))
                .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                .setTooltipText("tj.machine.universal.item_amount")
                .setUpdateOnTyping(true));
        itemSupplyWidgetGroup.addWidget(new ClickButtonWidget(92, 113, 38, 18, "/2", data -> setItemSupplyThroughput.accept(String.valueOf((long) itemSupplyThroughput.getValue() / 2), "")));
        itemSupplyWidgetGroup.addWidget(new ClickButtonWidget(130, 113, 38, 18, "*2", data -> setItemSupplyThroughput.accept(String.valueOf((long) itemSupplyThroughput.getValue() * 2), "")));
        final PopUpWidget<?> itemFilterPopup = new PopUpWidget<>()
                .setClickToDefault(false)
                .setIndexSupplier(() -> {
                    final ItemStack itemStack1 = itemFilterSlot.getStackInSlot(0);
                    return ITEM_FILTER.isItemEqual(itemStack1) ? 1 : SMART_FILTER.isItemEqual(itemStack1) ? 2 : ORE_DICTIONARY_FILTER.isItemEqual(itemStack1) ? 3 : 0;
                }).addPopup(widgetGroup -> {
                    widgetGroup.addWidget(itemSupplyWidgetGroup);
                    return false;
                }).addPopup(widgetGroup -> {
                    widgetGroup.addWidget(itemWidgetGroup);
                    widgetGroup.addWidget(itemSelectionWidgetGroup);
                    return false;
                }).addPopup(widgetGroup -> {
                    widgetGroup.addWidget(new CycleButtonWidget(10, 133, 76, 18, ControllableDualCover.RecipeMode.class, itemRecipeMode::getValue, itemRecipeMode1 -> {
                        itemRecipeMode.setValue(itemRecipeMode1);
                        compound.setInteger("itemRecipeMode", itemRecipeMode1.ordinal());
                    }));
                    return false;
                }).addPopup(widgetGroup -> {
                    widgetGroup.addWidget(itemSupplyWidgetGroup);
                    oreDictionaryItemFilter.initUI(widgetGroup::addWidget);
                    return false;
                });
        final PopUpWidget<?> fluidFilterPopup = new PopUpWidget<>()
                .setClickToDefault(false)
                .setIndexSupplier(() -> {
                    final ItemStack itemStack1 = fluidFilterSlot.getStackInSlot(0);
                    return FLUID_FILTER.isItemEqual(itemStack1) ? 1 : SMART_FILTER.isItemEqual(itemStack1) ? 2 : 0;
                })
                .addPopup(widgetGroup -> {
                    final BiConsumer<String, String> setFluidSupplyThroughput = (text, id) -> {
                        fluidSupplyThroughput.setValue((int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
                        compound.setInteger("fluidSupplyThroughput", fluidSupplyThroughput.getValue());
                    };
                    widgetGroup.addWidget(new NewTextFieldWidget<>(92, 115, 76, 18, true, () -> String.valueOf(fluidSupplyThroughput.getValue()), setFluidSupplyThroughput)
                            .setTooltipFormat(() -> ArrayUtils.toArray(String.valueOf(fluidSupplyThroughput.getValue())))
                            .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setTooltipText("tj.machine.universal.fluid_amount")
                            .setUpdateOnTyping(true));
                    widgetGroup.addWidget(new ClickButtonWidget(92, 133, 38, 18, "/2", data -> setFluidSupplyThroughput.accept(String.valueOf((long) fluidSupplyThroughput.getValue() / 2), "")));
                    widgetGroup.addWidget(new ClickButtonWidget(130, 133, 38, 18, "*2", data -> setFluidSupplyThroughput.accept(String.valueOf((long) fluidSupplyThroughput.getValue() * 2), "")));
                    return false;
                }).addPopup(widgetGroup -> {
                    widgetGroup.addWidget(fluidWidgetGroup);
                    widgetGroup.addWidget(fluidSelectionWidgetGroup);
                    return false;
                }).addPopup(widgetGroup -> {
                    widgetGroup.addWidget(new CycleButtonWidget(10, 151, 76, 18, ControllableDualCover.RecipeMode.class, fluidRecipeMode::getValue, fluidRecipeMode1 -> {
                        fluidRecipeMode.setValue(fluidRecipeMode1);
                        compound.setInteger("fluidRecipeMode", fluidRecipeMode1.ordinal());
                    }));
                    return false;
                });
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
                .addTab(String.format("metaitem.fluid.regulator.%s.name", GAValues.VN[this.tier].toLowerCase()), TJMetaItems.ROBOT_ARMS[this.tier].getStackForm(), tab -> {
                    tab.add(new LabelWidget(7, 5, "cover.robotic_arm.title", GAValues.VN[this.tier]));
                    tab.add(new ClickButtonWidget(7, 20, 23, 20, "-10", data -> this.setItemTransferRate(itemTransferRate, compound, itemTransferRate.getValue() - (data.isShiftClick ? 100 : 10))));
                    tab.add(new ClickButtonWidget(146, 20, 23, 20, "+10", data -> this.setItemTransferRate(itemTransferRate, compound, itemTransferRate.getValue() + (data.isShiftClick ? 100 : 10))));
                    tab.add(new ClickButtonWidget(30, 20, 20, 20, "-1", data -> this.setItemTransferRate(itemTransferRate, compound, itemTransferRate.getValue() - (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(126, 20, 20, 20, "+1", data -> this.setItemTransferRate(itemTransferRate, compound, itemTransferRate.getValue() + (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(50, 20, 38, 20,"/2", data -> this.setItemTransferRate(itemTransferRate, compound, itemTransferRate.getValue() / 2D)));
                    tab.add(new ClickButtonWidget(88, 20, 38, 20, "*2", data -> this.setItemTransferRate(itemTransferRate, compound, itemTransferRate.getValue() * 2)));
                    tab.add(new NewTextFieldWidget<>(7, 40, 162, 18, true, () -> String.valueOf(itemTransferRate.getValue()), setItemTransferRate2)
                            .setTooltipText("tj.machine.universal.item_throughput").setTooltipFormat(() -> new String[]{String.valueOf(itemTransferRate.getValue())})
                            .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setUpdateOnTyping(true));
                    tab.add(new CycleButtonWidget(7, 65, 76, 20, CoverConveyor.ConveyorMode.class, conveyorMode::getValue, conveyorMode1 -> {
                        conveyorMode.setValue(conveyorMode1);
                        compound.setInteger("conveyorMode", conveyorMode1.ordinal());
                    }));
                    tab.add(new CycleButtonWidget(92, 65, 76, 20, TransferMode.class, robotArmMode::getValue, robotArmMode1 -> {
                        robotArmMode.setValue(robotArmMode1);
                        compound.setInteger("robotArmMode", robotArmMode1.ordinal());
                    }).setTooltipHoverString("cover.robotic_arm.transfer_mode.description"));
                    tab.add(new ImageWidget(-28, 127, 26, 44, GuiTextures.BORDERED_BACKGROUND));
                    tab.add(new TJSlotWidget<>(itemFilterSlot, 0, -24, 131)
                            .setActiveBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));
                    tab.add(itemFilterPopup);
                    tab.add(new ToggleButtonWidget(-24, 149, 18, 18, GuiTextures.BUTTON_BLACKLIST, itemBlacklist::isValue, b -> {
                        itemBlacklist.setValue(b);
                        compound.setBoolean("itemBlacklist", itemBlacklist.isValue());
                    }).setTooltipText("cover.filter.blacklist"));
                    final BiConsumer<String, String> setItemTicks = (text, id) -> {
                        itemTicks.setValue((int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
                        compound.setInteger("itemTicks", itemTicks.getValue());
                    };
                    tab.add(new NewTextFieldWidget<>(92, 133, 76, 18, true, () -> String.valueOf(itemTicks.getValue()), setItemTicks)
                            .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setTooltipText("machine.universal.ticks.operation")
                            .setUpdateOnTyping(true));
                    tab.add(new ClickButtonWidget(92, 151, 38, 18, "/2", data -> setItemTicks.accept(String.valueOf((long) itemTicks.getValue() / 2), "")));
                    tab.add(new ClickButtonWidget(130, 151, 38, 18, "*2", data -> setItemTicks.accept(String.valueOf((long) itemTicks.getValue() * 2), "")));
                    tab.add(new ImageWidget(-28, 244, 26, 26, GuiTextures.BORDERED_BACKGROUND));
                    tab.add(new ToggleButtonWidget(-24, 248, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON, itemWorking::isValue, w -> {
                        itemWorking.setValue(w);
                        compound.setBoolean("itemWorking", itemWorking.isValue());
                    }).setTooltipText("machine.universal.toggle.run.mode"));
                }).addTab(String.format("metaitem.electric.pump.%s.name", GAValues.VN[this.tier].toLowerCase()), TJMetaItems.FLUID_REGULATORS[this.tier].getStackForm(), tab -> {
                    tab.add(new LabelWidget(7, 5, "cover.fluid_regulator.title", GAValues.VN[this.tier]));
                    tab.add(new ClickButtonWidget(7, 20, 37, 20, "-100", data -> this.setFluidTransferRate(fluidTransferRate, compound, fluidTransferRate.getValue() - (data.isShiftClick ? 500 : 100))));
                    tab.add(new ClickButtonWidget(132, 20, 37, 20, "+100", data -> this.setFluidTransferRate(fluidTransferRate, compound, fluidTransferRate.getValue() + (data.isShiftClick ? 500 : 100))));
                    tab.add(new ClickButtonWidget(44, 20, 24, 20, "-10", data -> this.setFluidTransferRate(fluidTransferRate, compound, fluidTransferRate.getValue() - (data.isShiftClick ? 50 : 10))));
                    tab.add(new ClickButtonWidget(108, 20, 24, 20, "+10", data -> this.setFluidTransferRate(fluidTransferRate, compound, fluidTransferRate.getValue() + (data.isShiftClick ? 50 : 10))));
                    tab.add(new ClickButtonWidget(68, 20, 20, 20, "-1", data -> this.setFluidTransferRate(fluidTransferRate, compound, fluidTransferRate.getValue() - (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(88, 20, 20, 20, "+1", data -> this.setFluidTransferRate(fluidTransferRate, compound, fluidTransferRate.getValue() + (data.isShiftClick ? 5 : 1))));
                    tab.add(new ClickButtonWidget(7, 40, 81, 20, "/2", data -> this.setFluidTransferRate(fluidTransferRate, compound, fluidTransferRate.getValue() / 2D)));
                    tab.add(new ClickButtonWidget(88, 40, 81, 20, "*2", data -> this.setFluidTransferRate(fluidTransferRate, compound, fluidTransferRate.getValue() * 2)));
                    tab.add(new NewTextFieldWidget<>(7, 60, 162, 18, true, () -> String.valueOf(fluidTransferRate.getValue()), setFluidTransferRate2)
                            .setTooltipText("tj.machine.universal.fluid_throughput").setTooltipFormat(() -> new String[]{String.valueOf(fluidTransferRate.getValue())})
                            .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setUpdateOnTyping(true));
                    tab.add(new CycleButtonWidget(7, 85, 76, 18, CoverPump.PumpMode.class, pumpMode::getValue, pumpMode1 -> {
                        pumpMode.setValue(pumpMode1);
                        compound.setInteger("pumpMode", pumpMode1.ordinal());
                    }));
                    tab.add(new CycleButtonWidget(92, 85, 76, 18, TransferMode.class, regulatorMode::getValue, regulatorMode1 -> {
                        regulatorMode.setValue(regulatorMode1);
                        compound.setInteger("regulatorMode", regulatorMode1.ordinal());
                    }).setTooltipHoverString("cover.fluid_regulator.transfer_mode.description"));
                    tab.add(new ImageWidget(-28, 147, 26, 44, GuiTextures.BORDERED_BACKGROUND));
                    tab.add(new TJSlotWidget<>(fluidFilterSlot, 0, -24, 151)
                            .setActiveBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));
                    tab.add(fluidFilterPopup);
                    tab.add(new ToggleButtonWidget(-24, 169, 18, 18, GuiTextures.BUTTON_BLACKLIST, fluidBlacklist::isValue, b -> {
                        fluidBlacklist.setValue(b);
                        compound.setBoolean("fluidBlacklist", fluidBlacklist.isValue());
                    }).setTooltipText("cover.filter.blacklist"));
                    final BiConsumer<String, String> setFluidTicks = (text, id) -> {
                        fluidTicks.setValue((int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
                        compound.setInteger("fluidTicks", fluidTicks.getValue());
                    };
                    tab.add(new NewTextFieldWidget<>(92, 151, 76, 18, true, () -> String.valueOf(fluidTicks.getValue()), setFluidTicks)
                            .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                            .setTooltipText("machine.universal.ticks.operation")
                            .setUpdateOnTyping(true));
                    tab.add(new ClickButtonWidget(92, 169, 38, 18, "/2", data -> setFluidTicks.accept(String.valueOf((long) fluidTicks.getValue() / 2), "")));
                    tab.add(new ClickButtonWidget(130, 169, 38, 18, "*2", data -> setFluidTicks.accept(String.valueOf((long) fluidTicks.getValue() * 2), "")));
                    tab.add(new ImageWidget(-28, 244, 26, 26, GuiTextures.BORDERED_BACKGROUND));
                    tab.add(new ToggleButtonWidget(-24, 248, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON, fluidWorking::isValue, w -> {
                        fluidWorking.setValue(w);
                        compound.setBoolean("fluidWorking", fluidWorking.isValue());
                    }).setTooltipText("machine.universal.toggle.run.mode"));
                });
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 272)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(CONTROLLABLE_DUAL_COVERS[this.tier].getStackForm()).setItemLabel(CONTROLLABLE_DUAL_COVERS[this.tier].getStackForm()).setLocale(String.format("metaitem.controllable_dual_cover.%s.name", GAValues.VN[this.tier].toLowerCase())))
                .widget(tabBuilder.build())
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 190, itemStack))
                .bindOpenListener(() -> {
                    if (compound.hasKey("itemTicks"))
                        itemTicks.setValue(compound.getInteger("itemTicks"));
                    if (compound.hasKey("fluidTicks"))
                        fluidTicks.setValue(compound.getInteger("fluidTicks"));
                    if (compound.hasKey("conveyorMode"))
                        conveyorMode.setValue(CoverConveyor.ConveyorMode.values()[compound.getInteger("conveyorMode")]);
                    if (compound.hasKey("pumpMode"))
                        pumpMode.setValue(CoverPump.PumpMode.values()[compound.getInteger("pumpMode")]);
                    if (compound.hasKey("robotArmMode"))
                        robotArmMode.setValue(TransferMode.values()[compound.getInteger("robotArmMode")]);
                    if (compound.hasKey("regulatorMode"))
                        regulatorMode.setValue(TransferMode.values()[compound.getInteger("regulatorMode")]);
                    if (compound.hasKey("itemRecipeMode"))
                        itemRecipeMode.setValue(ControllableDualCover.RecipeMode.values()[compound.getInteger("itemRecipeMode")]);
                    if (compound.hasKey("fluidRecipeMode"))
                        fluidRecipeMode.setValue(ControllableDualCover.RecipeMode.values()[compound.getInteger("fluidRecipeMode")]);
                    if (compound.hasKey("itemSupplyThroughput"))
                        itemSupplyThroughput.setValue(compound.getInteger("itemSupplyThroughput"));
                    if (compound.hasKey("fluidSupplyThroughput"))
                        fluidSupplyThroughput.setValue(compound.getInteger("fluidSupplyThroughput"));
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
                    if (compound.hasKey("oreDictFilter"))
                        oreDictionaryItemFilter.readFromNBT(compound.getCompoundTag("oreDictFilter"));
                    for (int i = 0; i < itemFilter.getSlots(); i++) {
                        if (compound.hasKey("itemSlot:" + i)) {
                            itemFilter.setStackInSlot(i, new ItemStack(compound.getCompoundTag("itemSlot:" + i)));
                            itemType.put(itemFilter.getStackInSlot(i), itemFilter.getStackInSlot(i));
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
        lines.add(I18n.format("tj.machine.universal.item_throughput", this.maxItemTransferRate));
        lines.add(I18n.format("tj.machine.universal.fluid_throughput", this.maxFluidTransferRate));
    }
}
