package tj.items.behaviours;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.util.Position;
import gregtech.api.util.function.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.impl.NewTextFieldWidget;
import tj.mui.widgets.impl.TJLabelWidget;
import tj.mui.widgets.impl.SelectionWidgetGroup;
import tj.mui.widgets.impl.TJPhantomItemSlotWidget;
import tj.items.TJMetaItems;
import tj.items.covers.VoidMode;
import tj.items.handlers.LargeItemStackHandler;
import tj.util.map.Strategies;
import tj.util.references.BooleanReference;
import tj.util.references.IntegerReference;
import tj.util.references.ObjectReference;

import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public class VoidAdvancedItemCoverBehaviour extends VoidItemCoverBehaviour {

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final ItemStack itemStack = player.getHeldItemMainhand();
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("voidFilter");
        final Object2ObjectMap<ItemStack, ItemStack> itemType = new Object2ObjectOpenCustomHashMap<>(Strategies.ITEMSTACK_STRATEGY);
        final LargeItemStackHandler itemFilter = new LargeItemStackHandler(9, Integer.MAX_VALUE);
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(63, 48));
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(63, 48, 54, 54);
        for (int i = 0; i < itemFilter.getSlots(); i++) {
            final int index = i;
            final BiConsumer<String, String> setItemCount = (text, id) -> {
                ItemStack stack = itemFilter.extractItem(index, Integer.MAX_VALUE, true);
                if (stack.isEmpty()) return;
                stack = itemFilter.extractItem(index, Integer.MAX_VALUE, false);
                stack.setCount(Math.max(1, (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
                compound.setTag("slot:" + index, stack.serializeNBT());
                itemFilter.insertItem(index, stack, false);
            };
            widgetGroup.addWidget(new TJPhantomItemSlotWidget(18 * (i % 3), 18 * (i / 3), 18, 18, i, itemFilter, item -> {
                if (!item.isEmpty()) {
                    compound.setTag("slot:" + index, item.serializeNBT());
                    itemType.put(item, item);
                } else compound.removeTag("slot:" + index);
            }, itemType::remove).setPutItemsPredicate(item -> !itemType.containsKey(item))
                    .setBackgroundTextures(GuiTextures.SLOT));
            selectionWidgetGroup.addSubWidget(i, new NewTextFieldWidget<>(-37, -20, 124, 18, true, () -> String.valueOf(itemFilter.getStackInSlot(index).getCount()), setItemCount)
                    .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                    .setUpdateOnTyping(true)
                    .setMaxStringLength(11));
            selectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(-56, -20, 18, 18, "/2", data -> setItemCount.accept(String.valueOf((long) itemFilter.getStackInSlot(index).getCount() / 2), String.valueOf(index))));
            selectionWidgetGroup.addSubWidget(i, new ClickButtonWidget(88, -20, 18, 18, "*2", data -> setItemCount.accept(String.valueOf((long) itemFilter.getStackInSlot(index).getCount() * 2), String.valueOf(index))));
            selectionWidgetGroup.addSelectionBox(i, 18 * (i % 3), 18 * (i / 3), 18, 18);
        }
        final ObjectReference<VoidMode> voidMode = new ObjectReference<>(VoidMode.NORMAL);
        final BooleanReference isWorking = new BooleanReference();
        final IntegerReference tickTime = new IntegerReference(20);
        final BiConsumer<String, String> setTickTime = (text, id) -> {
            tickTime.setValue((int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
            compound.setInteger("tickTime", tickTime.getValue());
        };
        final BooleanConsumer setWorking = working -> {
            isWorking.setValue(working);
            compound.setBoolean("isWorking", working);
        };
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 208)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJMetaItems.VOID_ADVANCED_ITEM_COVER.getStackForm()).setLocale("metaitem.void_advanced_item_cover.name"))
                .widget(new NewTextFieldWidget<>(26, 7, 124, 18, true, () -> String.valueOf(tickTime.getValue()), setTickTime)
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true))
                .widget(new ClickButtonWidget(7, 7, 18, 18, "/2", data -> setTickTime.accept(String.valueOf((long) tickTime.getValue() / 2), "")))
                .widget(new ClickButtonWidget(151, 7, 18, 18, "*2", data -> setTickTime.accept(String.valueOf((long) tickTime.getValue() * 2), "")))
                .widget(new CycleButtonWidget(43, 106, 90, 18, VoidMode.class, voidMode::getValue, value -> {
                    compound.setInteger("voidMode", value.ordinal());
                    voidMode.setValue(value);
                }))
                .widget(new ToggleButtonWidget(151, 106, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON, isWorking::isValue, setWorking)
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .widget(widgetGroup)
                .widget(selectionWidgetGroup)
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 126, itemStack))
                .bindOpenListener(() -> {
                    if (compound.hasKey("isWorking"))
                        isWorking.setValue(compound.getBoolean("isWorking"));
                    if (compound.hasKey("tickTime"))
                        tickTime.setValue(compound.getInteger("tickTime"));
                    if (compound.hasKey("voidMode"))
                        voidMode.setValue(VoidMode.values()[compound.getInteger("voidMode")]);
                    for (int i = 0; i < itemFilter.getSlots(); i++) {
                        if (compound.hasKey("slot:" + i)) {
                            itemFilter.setStackInSlot(i, new ItemStack(compound.getCompoundTag("slot:" + i)));
                            itemType.put(itemFilter.getStackInSlot(i), itemFilter.getStackInSlot(i));
                        }
                    }
                }).bindCloseListener(() -> itemStack.getOrCreateSubCompound("voidFilter").merge(compound))
                .build(holder, player);
    }
}
