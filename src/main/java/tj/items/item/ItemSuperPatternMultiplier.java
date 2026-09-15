package tj.items.item;

import appeng.core.Api;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import tj.items.handlers.FilteredItemStackHandler;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.impl.AEPatternSlotWidget;
import tj.mui.widgets.impl.SlotScrollableWidgetGroup;
import tj.mui.widgets.impl.TJLabelWidget;
import tj.mui.widgets.impl.TJSlotWidget;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;

public class ItemSuperPatternMultiplier extends Item implements ItemUIFactory {

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer player) {
        final ItemStack patternMultiTool = player.getHeldItemMainhand();
        final NBTTagCompound compound = TJItemUtils.getCompoundFromStack(patternMultiTool);
        final NBTTagCompound invTag = compound.getCompoundTag("inv");
        final NBTTagCompound upgradeTag = compound.getCompoundTag("upgrades");
        final FilteredItemStackHandler multiPatternSlots = new FilteredItemStackHandler(null, 72, 64)
                .setItemStackPredicate((slot, itemStack) ->
                        itemStack.isItemEqual(Api.INSTANCE.definitions().materials().blankPattern().maybeStack(1).orElse(ItemStack.EMPTY)) ||
                                itemStack.isItemEqual(Api.INSTANCE.definitions().items().encodedPattern().maybeStack(1).orElse(ItemStack.EMPTY)) ||
                                itemStack.isItemEqual(TJItemUtils.getItemStackFromName("ae2fc:dense_encoded_pattern")));
        multiPatternSlots.setOnContentsChangedPost((slot, itemStack) -> TJGuiUtils.writePatternMultiToolToNBT(multiPatternSlots, invTag));
        final FilteredItemStackHandler multiUpgradeSlots = new FilteredItemStackHandler(null, 7, 1)
                .setItemStackPredicate((slot, itemStack) ->
                        itemStack.isItemEqual(Api.INSTANCE.definitions().materials().cardCapacity().maybeStack(1).orElse(ItemStack.EMPTY)));
        multiUpgradeSlots.setOnContentsChangedPost((slot, itemStack) -> TJGuiUtils.writePatternMultiToolToNBT(multiUpgradeSlots, upgradeTag));

        final SlotScrollableWidgetGroup patternSlotGroup = new SlotScrollableWidgetGroup(7, 7, 166, 72, 9)
                .setItemHandler(multiPatternSlots)
                .setScrollWidth(4);
        final SlotScrollableWidgetGroup upgradeSlotGroup = new SlotScrollableWidgetGroup(186, 7, 22, 126, 1)
                .setItemHandler(multiUpgradeSlots)
                .setScrollWidth(4);
        for (int i = 0; i < multiPatternSlots.getSlots(); i++) {
            final int index = i;
            patternSlotGroup.addWidget(new AEPatternSlotWidget(multiPatternSlots, i, 18 * (i % 9), 18 * (i / 9))
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.PATTERN_OVERLAY)
                    .setActiveSupplier(() -> index / 9 <= multiUpgradeSlots.getSlotsFilled())
                    .setSlotLocationInfo(true, false)
                    .setInactiveBackgroundTexture(TJGuiTextures.BLANK_SLOT)
                    .setWidgetGroup(patternSlotGroup));
        }
        for (int i = 0; i < multiUpgradeSlots.getSlots(); i++) {
            upgradeSlotGroup.addWidget(new TJSlotWidget<>(multiUpgradeSlots, i, 0, 18 * i)
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY));
        }
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 200)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setLocale(patternMultiTool.getDisplayName())
                        .setItemLabel(patternMultiTool))
                .image(179, 0, 32, 140, GuiTextures.BORDERED_BACKGROUND)
                .widget(patternSlotGroup)
                .widget(upgradeSlotGroup)
                .widget(new ButtonWidget<>(7, 80, 18, 18, "*2", data ->
                        TJGuiUtils.changePatternAmount(multiPatternSlots, patternSlotGroup, m -> m * 2,
                                () -> TJGuiUtils.writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                        .setHoverTooltipText("gui.pattern_term.auto_fill_pattern.MULTIPLY_2.text")
                        .setTitleHoverTooltipText("gui.action.MULTIPLY_2.name")
                        .setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                .widget(new ButtonWidget<>(25, 80, 18, 18, "*3", data ->
                        TJGuiUtils.changePatternAmount(multiPatternSlots, patternSlotGroup, m -> m * 3,
                                () -> TJGuiUtils.writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                        .setHoverTooltipText("gui.pattern_term.auto_fill_pattern.MULTIPLY_3.text")
                        .setTitleHoverTooltipText("gui.action.MULTIPLY_3.name")
                        .setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                .widget(new ButtonWidget<>(43, 80, 18, 18, "+1", data ->
                        TJGuiUtils.changePatternAmount(multiPatternSlots, patternSlotGroup, m -> m + 1,
                                () -> TJGuiUtils.writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                        .setTitleHoverTooltipText("gui.tooltips.appliedenergistics2.IncreaseByOne")
                        .setHoverTooltipText("gui.tooltips.appliedenergistics2.IncreaseByOneDesc")
                        .setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                .widget(new ButtonWidget<>(7, 98, 18, 18, "/2", data ->
                        TJGuiUtils.changePatternAmount(multiPatternSlots, patternSlotGroup, m -> m / 2,
                                () -> TJGuiUtils.writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                        .setHoverTooltipText("gui.pattern_term.auto_fill_pattern.DIVIDE_2.text")
                        .setTitleHoverTooltipText("gui.action.DIVIDE_2.name")
                        .setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                .widget(new ButtonWidget<>(25, 98, 18, 18, "/3", data ->
                        TJGuiUtils.changePatternAmount(multiPatternSlots, patternSlotGroup, m -> m / 3,
                                () -> TJGuiUtils.writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                        .setHoverTooltipText("gui.pattern_term.auto_fill_pattern.DIVIDE_3.text")
                        .setTitleHoverTooltipText("gui.action.DIVIDE_3.name")
                        .setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                .widget(new ButtonWidget<>(43, 98, 18, 18, "-1", data ->
                        TJGuiUtils.changePatternAmount(multiPatternSlots, patternSlotGroup, m -> m - 1,
                                () -> TJGuiUtils.writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                        .setTitleHoverTooltipText("gui.tooltips.appliedenergistics2.DecreaseByOne")
                        .setHoverTooltipText("gui.tooltips.appliedenergistics2.DecreaseByOneDesc")
                        .setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                .widget(new ButtonWidget<>(61, 80, 36, 36, "X", data ->
                        TJGuiUtils.clearPatterns(multiPatternSlots, () -> TJGuiUtils.writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                        .setTitleHoverTooltipText("nae2.pattern_multiplier.unencode")
                        .setHoverTooltipText("nae2.pattern_multiplier.unencode.desc")
                        .setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 118, player.getHeldItemMainhand()))
                .bindOpenListener(() -> {
                    TJGuiUtils.readPatternMultiToolNBT(multiPatternSlots, invTag.getTagList("Items", 10));
                    TJGuiUtils.readPatternMultiToolNBT(multiUpgradeSlots, upgradeTag.getTagList("Items", 10));
                    if (patternMultiTool.getTagCompound() == null || patternMultiTool.getTagCompound().isEmpty()) {
                        compound.setTag("inv", invTag);
                        compound.setTag("upgrades", upgradeTag);
                        patternMultiTool.setTagCompound(compound);
                    }
                }).build(holder, player);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        if (!worldIn.isRemote && TJItems.SUPER_PATTERN_MULTITOOL.isSameAs(playerIn.getHeldItemMainhand()))
            PlayerInventoryHolder.openHandItemUI(playerIn, handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
    }
}
