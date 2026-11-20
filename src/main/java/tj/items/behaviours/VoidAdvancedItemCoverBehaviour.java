package tj.items.behaviours;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.util.Position;
import gregtech.api.util.function.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import tj.integration.ae2.IItemConfigSetter;
import tj.integration.ae2.ITickSetter;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.TJModularUI;
import tj.mui.widgets.impl.*;
import tj.items.TJMetaItems;
import tj.items.covers.VoidMode;
import tj.items.handlers.LargeItemStackHandler;
import tj.util.TJItemUtils;
import tj.util.map.Strategies;

import java.util.regex.Pattern;

public class VoidAdvancedItemCoverBehaviour extends VoidItemCoverBehaviour {

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final ItemStack itemStack = player.getHeldItemMainhand();
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("voidFilter");
        final Object2ObjectMap<ItemStack, ItemStack> itemType = new Object2ObjectOpenCustomHashMap<>(Strategies.ITEMSTACK_STRATEGY);
        final LargeItemStackHandler itemFilter = new LargeItemStackHandler(9, Integer.MAX_VALUE);
        final ITickSetter tickSetter = new ITickSetter() {
            @Override
            public void setTickTime(long tickTime) {
                compound.setInteger("tickTime", (int) Math.max(1, Math.min(Integer.MAX_VALUE, tickTime)));
            }

            @Override
            public int getTickTime() {
                return compound.getInteger("tickTime");
            }
        };
        final IItemConfigSetter itemConfigSetter = new IItemConfigSetter() {
            @Override
            public void setItemCount(long count, int index) {
                if (index < 0 || index >= itemFilter.getSlots()) return;
                final ItemStack stack = itemFilter.getStackInSlot(index);
                if (stack.isEmpty()) return;
                stack.setCount((int) Math.min(Integer.MAX_VALUE, count));
                compound.setTag("slot:" + index, stack.serializeNBT());
            }

            @Override
            public int getItemCount(int index) {
                return index >= 0 && index < itemFilter.getSlots() ? itemFilter.getStackInSlot(index).getCount() : 0;
            }
        };
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(61, 26));
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(61, 26, 54, 54);
        for (int i = 0; i < itemFilter.getSlots(); i++) {
            final int index = i;
            widgetGroup.addWidget(new TJPhantomItemSlotWidget(18 * (i % 3), 18 * (i / 3), 18, 18, i, itemFilter, item -> {
                if (!item.isEmpty()) {
                    compound.setTag("slot:" + index, item.serializeNBT());
                    itemType.put(item, item);
                } else compound.removeTag("slot:" + index);
            }, itemType::remove).setPutItemsPredicate(item -> !itemType.containsKey(item))
                    .setBackgroundTextures(GuiTextures.SLOT));
            selectionWidgetGroup.addSelectionBox(i, 18 * (i % 3), 18 * (i / 3), 18, 18);
        }
        final PanelWidgetGroup panelWidgetGroup = TJGuiUtils.createTickPanel(tickSetter);
        return TJModularUI.builder(holder, player)
                .setWidthHeight(200, 208)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJMetaItems.VOID_ADVANCED_ITEM_COVER.getStackForm()).setLocale("metaitem.void_advanced_item_cover.name"))
                .widget(new TJCycleButtonWidget<>(7, 84, 162, 18, VoidMode.class,
                        () -> VoidMode.values()[compound.getInteger("voidMode")],  value ->
                        compound.setInteger("voidMode", value.ordinal())).setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                .widget(new TJToggleButtonWidget(174, 126, 18, 18, TJGuiTextures.TOGGLE_RESET_BUTTON,
                        () -> false, b -> compound.setInteger("tickTime", 1))
                        .setTitleHoverTooltipText("machine.universal.toggle.reset.disabled"))
                .widget(new TJToggleButtonWidget(174, 184, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON,
                        () -> compound.getBoolean("isWorking"), b -> compound.setBoolean("isWorking", !compound.getBoolean("isWorking")))
                        .setToggleTitleTooltipHoverText("machine.universal.toggle.run.mode.disabled",
                                "machine.universal.toggle.run.mode.enabled"))
                .widget(new NewTextFieldWidget<>(7, 103, 162, 18, true, () -> String.valueOf(tickSetter.getTickTime()),
                        (text, id) -> tickSetter.setTickTime(Long.parseLong(text)))
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true)
                        .setMaxStringLength(11))
                .widget(new TJToggleButtonWidget(174, 103, 18, 18, panelWidgetGroup::isPanelOpen, (BooleanConsumer) value -> {
                    if (!value && panelWidgetGroup.isPanelOpen()) {
                        panelWidgetGroup.closePanel();
                    } else if (value) {
                        panelWidgetGroup.openPanel();
                    }
                }).setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                        .setItemDisplay(TJItemUtils.getItemStackFromName("enderio:item_material", 1, 11)) // infinity bimetal gear
                        .setHoverTooltipText("tj.machine.universal.ticks.operation_configure")
                        .useToggleTexture(true))
                .widget(widgetGroup)
                .widget(selectionWidgetGroup)
                .widget(TJGuiUtils.createItemConfigPanel(itemConfigSetter, selectionWidgetGroup))
                .widget(panelWidgetGroup)
                .bindPlayerInventory(itemStack)
                .bindOpenListener(() -> {
                    for (int i = 0; i < itemFilter.getSlots(); i++) {
                        if (compound.hasKey("slot:" + i)) {
                            itemFilter.setStackInSlot(i, new ItemStack(compound.getCompoundTag("slot:" + i)));
                            itemType.put(itemFilter.getStackInSlot(i), itemFilter.getStackInSlot(i));
                        }
                    }
                }).bindCloseListener(() -> itemStack.getOrCreateSubCompound("voidFilter").merge(compound))
                .build();
    }
}
