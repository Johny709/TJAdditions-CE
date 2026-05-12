package tj.items.behaviours;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.util.Position;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import tj.gui.TJGuiTextures;
import tj.gui.TJGuiUtils;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.SelectionWidgetGroup;
import tj.gui.widgets.impl.TJPhantomItemSlotWidget;
import tj.items.handlers.LargeItemStackHandler;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class CreativeItemCoverBehaviour implements IItemBehaviour, ItemUIFactory {

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote)
            PlayerInventoryHolder.openHandItemUI(player, hand);
        return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItemMainhand());
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final ItemStack itemStack = player.getHeldItem(EnumHand.MAIN_HAND);
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("init");
        final LargeItemStackHandler itemFilter = new LargeItemStackHandler(9, Integer.MAX_VALUE);
        final Consumer<Widget.ClickData> onIncrement = clickData -> {
            final int speed = compound.getInteger("speed");
            final int value = clickData.isCtrlClick ? 100
                    : clickData.isShiftClick ? 10
                    : 1;
            compound.setInteger("speed", MathHelper.clamp(speed + value, 1, Integer.MAX_VALUE));
        };
        final Consumer<Widget.ClickData> onDecrement = clickData -> {
            final int speed = compound.getInteger("speed");
            final int value = clickData.isCtrlClick ? 100
                    : clickData.isShiftClick ? 10
                    : 1;
            compound.setInteger("speed", MathHelper.clamp(speed - value, 1, Integer.MAX_VALUE));
        };
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(61, 25));
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(61, 25, 54, 54);
        for (int i = 0; i < itemFilter.getSlots(); i++) {
            final int index = i;
            widgetGroup.addWidget(new TJPhantomItemSlotWidget(18 * (i % 3), 18 * (i / 3), 18, 18, i, itemFilter, item -> {
                if (!item.isEmpty())
                    compound.setTag("slot:" + index, item.serializeNBT());
                else compound.removeTag("slot:" + index);
            }).setBackgroundTextures(GuiTextures.SLOT));
            selectionWidgetGroup.addSubWidget(i, new NewTextFieldWidget<>(0, -20, 54, 18, true, () -> String.valueOf(itemFilter.getStackInSlot(index).getCount()), (text, id) -> {
                ItemStack stack = itemFilter.extractItem(index, Integer.MAX_VALUE, true);
                if (stack.isEmpty()) return;
                stack = itemFilter.extractItem(index, Integer.MAX_VALUE, false);
                stack.setCount(Math.max(1, (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
                compound.setTag("slot:" + index, stack.serializeNBT());
                itemFilter.insertItem(index, stack, false);
            }).setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                    .setUpdateOnTyping(true)
                    .setMaxStringLength(11));
            selectionWidgetGroup.addSelectionBox(i, 18 * (i % 3), 18 * (i / 3), 18, 18);
        }
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 187)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setLocale("cover.creative_item.title"))
                .widget(new ImageWidget(61, 80, 55, 18, GuiTextures.DISPLAY))
                .widget(new AdvancedTextWidget(63, 85, textList -> textList.add(new TextComponentTranslation("metaitem.creative.cover.display.ticks", compound.getInteger("speed"))), 0xFFFFFF))
                .widget(new ClickButtonWidget(43, 80, 18, 18, "+", onIncrement))
                .widget(new ClickButtonWidget(116, 80, 18, 18, "-", onDecrement))
                .widget(new ToggleButtonWidget(134, 80, 18, 18, TJGuiTextures.RESET_BUTTON, () -> false, b -> compound.setInteger("speed", 1))
                        .setTooltipText("machine.universal.toggle.reset"))
                .widget(new ToggleButtonWidget(152, 80, 18, 18, TJGuiTextures.POWER_BUTTON, () -> compound.getBoolean("power"), b -> compound.setBoolean("power", !compound.getBoolean("power")))
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .widget(widgetGroup)
                .widget(selectionWidgetGroup)
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 105, itemStack))
                .bindOpenListener(() -> {
                    for (int i = 0; i < 9; i++) {
                        if (compound.hasKey("slot:" + i))
                            itemFilter.setStackInSlot(i, new ItemStack(compound.getCompoundTag("slot:" + i)));
                    }
                }).bindCloseListener(() -> itemStack.getOrCreateSubCompound("init").merge(compound))
                .build(holder, player);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("cover.creative.only"));
        lines.add(I18n.format("metaitem.creative.item.cover.description"));
        lines.add(I18n.format("cover.creative.description"));
    }
}
