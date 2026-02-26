package tj.items.behaviours;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.util.Position;
import gregtech.common.covers.filter.SimpleItemFilter;
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

import java.util.List;
import java.util.function.Consumer;

public class CreativeItemCoverBehaviour implements IItemBehaviour, ItemUIFactory {

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote && hand == EnumHand.MAIN_HAND) {
            PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
            holder.openUI();
            return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        }
        return IItemBehaviour.super.onItemRightClick(world, player, hand);
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        NBTTagCompound compound = stack.getOrCreateSubCompound("init");
        if (!compound.hasKey("speed"))
            compound.setInteger("speed", 1);
        if (!compound.hasKey("power"))
            compound.setBoolean("power", false);
        SimpleItemFilter itemFilter = new SimpleItemFilter() {
            @Override
            protected void onMaxStackSizeChange() {
                super.onMaxStackSizeChange();
                for (int i = 0; i < this.itemFilterSlots.getSlots(); i++) {
                    ItemStack stack = this.itemFilterSlots.getStackInSlot(i);
                    if (!stack.isEmpty())
                        compound.setTag("slot:" + i, stack.serializeNBT());
                    else compound.removeTag("slot:" + i);
                }
            }
        };
        for (int i = 0; i < 9; i++) {
            if (compound.hasKey("slot:" + i))
                itemFilter.getItemFilterSlots().setStackInSlot(i, new ItemStack(compound.getCompoundTag("slot:" + i)));
        }
        Consumer<Widget.ClickData> onIncrement = clickData -> {
            int speed = compound.getInteger("speed");
            int value = clickData.isCtrlClick ? 100
                    : clickData.isShiftClick ? 10
                    : 1;
            speed = MathHelper.clamp(speed +value, 1, Integer.MAX_VALUE);
            compound.setInteger("speed", speed);
        };
        Consumer<Widget.ClickData> onDecrement = clickData -> {
            int speed = compound.getInteger("speed");
            int value = clickData.isCtrlClick ? 100
                    : clickData.isShiftClick ? 10
                    : 1;
            speed = MathHelper.clamp(speed -value, 1, Integer.MAX_VALUE);
            compound.setInteger("speed", speed);
        };
        WidgetGroup itemFilterGroup = new WidgetGroup(new Position(51, 25));
        itemFilterGroup.addWidget(new LabelWidget(-15, -15, "cover.creative_item.title"));
        itemFilterGroup.addWidget(new ImageWidget(10, 55, 55, 18, GuiTextures.DISPLAY));
        itemFilterGroup.addWidget(new AdvancedTextWidget(12, 60, textList -> textList.add(new TextComponentTranslation("metaitem.creative.cover.display.ticks", compound.getInteger("speed"))), 0xFFFFFF));
        itemFilterGroup.addWidget(new ClickButtonWidget(-8, 55, 18, 18, "+", onIncrement));
        itemFilterGroup.addWidget(new ClickButtonWidget(65, 55, 18, 18, "-", onDecrement));
        itemFilterGroup.addWidget(new ToggleButtonWidget(83, 55, 18, 18, TJGuiTextures.RESET_BUTTON, () -> false, b -> compound.setInteger("speed", 1))
                .setTooltipText("machine.universal.toggle.reset"));
        itemFilterGroup.addWidget(new ToggleButtonWidget(101, 55, 18, 18, TJGuiTextures.POWER_BUTTON, () -> compound.getBoolean("power"), b -> compound.setBoolean("power", !compound.getBoolean("power")))
                .setTooltipText("machine.universal.toggle.run.mode"));
        itemFilter.initUI(itemFilterGroup::addWidget);
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 105 + 82)
                .widget(itemFilterGroup)
                .bindCloseListener(() -> stack.getOrCreateSubCompound("init").merge(compound))
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 105, stack))
                .build(holder, player);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("cover.creative.only"));
        lines.add(I18n.format("metaitem.creative.item.cover.description"));
        lines.add(I18n.format("cover.creative.description"));
    }
}
