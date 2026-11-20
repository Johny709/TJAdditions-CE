package tj.items.behaviours;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.util.Position;
import gregtech.api.util.function.BooleanConsumer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import tj.integration.ae2.IItemConfigSetter;
import tj.integration.ae2.ITickSetter;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.TJModularUI;
import tj.mui.widgets.impl.*;
import tj.items.TJMetaItems;
import tj.items.handlers.LargeItemStackHandler;
import tj.util.TJItemUtils;

import java.util.List;
import java.util.regex.Pattern;

public class CreativeItemCoverBehaviour implements IItemBehaviour, ItemUIFactory {

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote)
            PlayerInventoryHolder.openHandItemUI(player, hand);
        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItemMainhand());
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final ItemStack itemStack = player.getHeldItem(EnumHand.MAIN_HAND);
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("init");
        final LargeItemStackHandler itemFilter = new LargeItemStackHandler(9, Integer.MAX_VALUE);
        final ITickSetter tickSetter = new ITickSetter() {
            @Override
            public void setTickTime(long tickTime) {
                compound.setInteger("speed", (int) Math.max(1, Math.min(Integer.MAX_VALUE, tickTime)));
            }

            @Override
            public int getTickTime() {
                return compound.getInteger("speed");
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
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(61, 25));
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(61, 25, 54, 54);
        for (int i = 0; i < itemFilter.getSlots(); i++) {
            final int index = i;
            widgetGroup.addWidget(new TJPhantomItemSlotWidget(18 * (i % 3), 18 * (i / 3), 18, 18, i, itemFilter, item -> {
                if (!item.isEmpty()) {
                    compound.setTag("slot:" + index, item.serializeNBT());
                } else compound.removeTag("slot:" + index);
            }).setBackgroundTextures(GuiTextures.SLOT));
            selectionWidgetGroup.addSelectionBox(i, 18 * (i % 3), 18 * (i / 3), 18, 18);
        }
        final PanelWidgetGroup panelWidgetGroup = TJGuiUtils.createTickPanel(tickSetter);
        return TJModularUI.builder(holder, player)
                .setWidthHeight(200, 187)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJMetaItems.CREATIVE_ITEM_COVER.getStackForm()).setLocale("cover.creative_item.title"))
                .widget(new TJToggleButtonWidget(174, 105, 18, 18, TJGuiTextures.TOGGLE_RESET_BUTTON,
                        () -> false, b -> compound.setInteger("speed", 1))
                        .setTitleHoverTooltipText("machine.universal.toggle.reset.disabled"))
                .widget(new TJToggleButtonWidget(174, 163, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON,
                        () -> compound.getBoolean("power"), b -> compound.setBoolean("power", !compound.getBoolean("power")))
                        .setToggleTitleTooltipHoverText("machine.universal.toggle.run.mode.disabled",
                                "machine.universal.toggle.run.mode.enabled"))
                .widget(new NewTextFieldWidget<>(7, 82, 162, 18, true, () -> String.valueOf(tickSetter.getTickTime()),
                        (text, id) -> tickSetter.setTickTime(Long.parseLong(text)))
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true)
                        .setMaxStringLength(11))
                .widget(new TJToggleButtonWidget(174, 82, 18, 18, panelWidgetGroup::isPanelOpen, (BooleanConsumer) value -> {
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
                    for (int i = 0; i < 9; i++) {
                        if (compound.hasKey("slot:" + i))
                            itemFilter.setStackInSlot(i, new ItemStack(compound.getCompoundTag("slot:" + i)));
                    }
                }).bindCloseListener(() -> itemStack.getOrCreateSubCompound("init").merge(compound))
                .build();
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("cover.creative.only"));
        lines.add(I18n.format("metaitem.creative.item.cover.description"));
        lines.add(I18n.format("cover.creative.description"));
    }
}
