package tj.items.behaviours;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.util.Position;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import tj.gui.TJGuiTextures;
import tj.gui.TJGuiUtils;
import tj.gui.widgets.NewTextFieldWidget;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.impl.TJPhantomItemSlotWidget;
import tj.items.handlers.LargeItemStackHandler;
import tj.util.map.Strategies;
import tj.util.references.IntegerReference;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public class VoidItemCoverBehaviour implements IItemBehaviour, ItemUIFactory {

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote)
            PlayerInventoryHolder.openHandItemUI(player, hand);
        return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItemMainhand());
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final ItemStack itemStack = player.getHeldItemMainhand();
        final NBTTagCompound compound = itemStack.getOrCreateSubCompound("voidFilter");
        final WidgetGroup widgetGroup = new WidgetGroup(new Position(53, 27));
        final Object2ObjectMap<ItemStack, ItemStack> itemType = new Object2ObjectOpenCustomHashMap<>(Strategies.ITEMSTACK_STRATEGY);
        final LargeItemStackHandler itemFilter = new LargeItemStackHandler(9, Integer.MAX_VALUE);
        for (int i = 0; i < itemFilter.getSlots(); i++) {
            final int index = i;
            widgetGroup.addWidget(new TJPhantomItemSlotWidget(10 + 18 * (i % 3), 18 * (i / 3), 18, 18, i, itemFilter, item -> {
                if (!item.isEmpty()) {
                    compound.setTag("slot:" + index, item.serializeNBT());
                    itemType.put(item, item);
                } else compound.removeTag("slot:" + index);
            }, itemType::remove).setPutItemsPredicate(item -> !itemType.containsKey(item))
                    .setBackgroundTextures(GuiTextures.SLOT));
        }
        final IntegerReference tickTime = new IntegerReference(20);
        final BiConsumer<String, String> setTickTime = (text, id) -> {
            tickTime.setValue((int) Math.max(1, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
            compound.setInteger("tickTime", tickTime.getValue());
        };
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 105 + 82)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setLocale("metaitem.void_item_cover.name"))
                .widget(new NewTextFieldWidget<>(45, 7, 90, 18, true, () -> String.valueOf(tickTime.getValue()), setTickTime)
                        .setValidator(str -> Pattern.compile("\\*?[0-9_]*\\*?").matcher(str).matches())
                        .setTooltipText("machine.universal.ticks.operation")
                        .setUpdateOnTyping(true))
                .widget(new ClickButtonWidget(27, 7, 18, 18, "/2", data -> setTickTime.accept(String.valueOf((long) tickTime.getValue() / 2), "")))
                .widget(new ClickButtonWidget(135, 7, 18, 18, "*2", data -> setTickTime.accept(String.valueOf((long) tickTime.getValue() * 2), "")))
                .widget(widgetGroup)
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 105, itemStack))
                .bindOpenListener(() -> {
                    if (compound.hasKey("tickTime"))
                        tickTime.setValue(compound.getInteger("tickTime"));
                    for (int i = 0; i < itemFilter.getSlots(); i++) {
                        if (compound.hasKey("slot:" + i)) {
                            itemFilter.setStackInSlot(i, new ItemStack(compound.getCompoundTag("slot:" + i)));
                            itemType.put(itemFilter.getStackInSlot(i), itemFilter.getStackInSlot(i));
                        }
                    }
                }).bindCloseListener(() -> itemStack.getOrCreateSubCompound("voidFilter").merge(compound))
                .build(holder, player);
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("metaitem.void_item_cover.description"));
        lines.add(I18n.format("cover.creative.description"));
    }
}
