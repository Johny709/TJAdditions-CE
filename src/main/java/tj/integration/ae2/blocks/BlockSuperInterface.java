package tj.integration.ae2.blocks;

import appeng.api.config.CondenserOutput;
import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.block.misc.BlockInterface;
import appeng.core.Api;
import baubles.api.BaublesApi;
import com.circulation.random_complement.client.RCSettings;
import com.circulation.random_complement.common.interfaces.RCIConfigurableObject;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.function.BooleanConsumer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.integration.ae2.ISuperInterface;
import tj.integration.ae2.helpers.DualitySuperInterface;
import tj.integration.ae2.tile.TileSuperInterface;
import tj.items.handlers.FilteredItemStackHandler;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.impl.*;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class BlockSuperInterface extends BlockInterface {

    public BlockSuperInterface() {
        this.setTileEntity(TileSuperInterface.class);
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean onActivated(final World world, final BlockPos pos, final EntityPlayer player, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        final TileSuperInterface superInterface = this.getTileEntity(world, pos);
        if (superInterface != null) {
            if (!world.isRemote) {
                superInterface.openUI(player, superInterface);
            }
            return true;
        }
        return false;
    }

    public static ModularUI createInterfaceGUI(TileEntityHolder holder, EntityPlayer player, ISuperInterface superInterface) {
        final DualitySuperInterface duality = (DualitySuperInterface) superInterface.getInterfaceDuality();
        final DualitySuperInterface.DualityUpgradeInventory upgradeHandler = (DualitySuperInterface.DualityUpgradeInventory) duality.getInventoryByName("upgrades");
        final ItemStack patternMultiTool = Optional.of(player.inventory.mainInventory)
                .map(inventory -> {
                    for (ItemStack stack : inventory)
                        if (stack.isItemEqual(TJItemUtils.getItemStackFromName("nae2:pattern_multiplier")))
                            return stack;
                    final IItemHandlerModifiable baubleSlots = BaublesApi.getBaublesHandler(player);
                    for (int i = 0; i < baubleSlots.getSlots(); i++)
                        if (baubleSlots.getStackInSlot(i).isItemEqual(TJItemUtils.getItemStackFromName("nae2:pattern_multiplier")))
                            return baubleSlots.getStackInSlot(i);
                    return ItemStack.EMPTY;
                }).get();
        final NBTTagCompound compound = TJItemUtils.getCompoundFromStack(patternMultiTool);
        final NBTTagCompound invTag = compound.getCompoundTag("inv");
        final NBTTagCompound upgradeTag = compound.getCompoundTag("upgrades");
        final FilteredItemStackHandler multiPatternSlots = new FilteredItemStackHandler(null, 36, 64)
                .setItemStackPredicate((slot, itemStack) -> itemStack.isItemEqual(Api.INSTANCE.definitions().materials().blankPattern().maybeStack(1).orElse(ItemStack.EMPTY)) ||
                        itemStack.isItemEqual(Api.INSTANCE.definitions().items().encodedPattern().maybeStack(1).orElse(ItemStack.EMPTY)) || itemStack.isItemEqual(TJItemUtils.getItemStackFromName("ae2fc:dense_encoded_pattern")));
        multiPatternSlots.setOnContentsChangedPost((slot, itemStack) -> writePatternMultiToolToNBT(multiPatternSlots, invTag));
        final FilteredItemStackHandler multiUpgradeSlots = new FilteredItemStackHandler(null, 3, 1)
                .setItemStackPredicate((slot, itemStack) -> itemStack.isItemEqual(Api.INSTANCE.definitions().materials().cardCapacity().maybeStack(1).orElse(ItemStack.EMPTY)));
        multiUpgradeSlots.setOnContentsChangedPost((slot, itemStack) -> writePatternMultiToolToNBT(multiUpgradeSlots, upgradeTag));

        final SlotScrollableWidgetGroup patternScrollableSlotGroup = new SlotScrollableWidgetGroup(7, 133, 166, 72, 9)
                .setItemStackTransfer(itemStack -> TJItemUtils.insertIntoItemHandler(multiPatternSlots, itemStack, false))
                .setItemHandler(duality.getPatterns())
                .setScrollWidth(4);
        final ButtonPopUpWidget<?> buttonPopUpWidget = new ButtonPopUpWidget<>();
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(0, 0, 0, 0);
        final ImageWidget stackSizeDisplay = new ImageWidget(7, 107, 162, 100, GuiTextures.BORDERED_BACKGROUND);
        final LabelWidget stackSizeLabel = new LabelWidget(14, 112, "machine.universal.stack_size");
        final ButtonWidget<?> clickButtonAdd1 = new ButtonWidget<>(15, 127, 25, 20, "+1", data -> superInterface.setStackSize(String.valueOf(Long.parseLong(superInterface.getStackSize(selectionWidgetGroup.getIndex())) + 1), String.valueOf(selectionWidgetGroup.getIndex())));
        final ButtonWidget<?> clickButtonAdd10 = new ButtonWidget<>(45, 127, 30, 20, "+10", data -> superInterface.setStackSize(String.valueOf(Long.parseLong(superInterface.getStackSize(selectionWidgetGroup.getIndex())) + 10), String.valueOf(selectionWidgetGroup.getIndex())));
        final ButtonWidget<?> clickButtonAdd100 = new ButtonWidget<>(80, 127, 35, 20, "+100", data -> superInterface.setStackSize(String.valueOf(Long.parseLong(superInterface.getStackSize(selectionWidgetGroup.getIndex())) + 100), String.valueOf(selectionWidgetGroup.getIndex())));
        final ButtonWidget<?> clickButtonAdd1000 = new ButtonWidget<>(120, 127, 40, 20, "+1000", data -> superInterface.setStackSize(String.valueOf(Long.parseLong(superInterface.getStackSize(selectionWidgetGroup.getIndex())) + 1000), String.valueOf(selectionWidgetGroup.getIndex())));
        final ButtonWidget<?> clickButtonSub1 = new ButtonWidget<>(15, 177, 25, 20, "-1", data -> superInterface.setStackSize(String.valueOf(Long.parseLong(superInterface.getStackSize(selectionWidgetGroup.getIndex())) - 1), String.valueOf(selectionWidgetGroup.getIndex())));
        final ButtonWidget<?> clickButtonSub10 = new ButtonWidget<>(45, 177, 30, 20, "-10", data -> superInterface.setStackSize(String.valueOf(Long.parseLong(superInterface.getStackSize(selectionWidgetGroup.getIndex())) - 10), String.valueOf(selectionWidgetGroup.getIndex())));
        final ButtonWidget<?> clickButtonSub100 = new ButtonWidget<>(80, 177, 35, 20, "-100", data -> superInterface.setStackSize(String.valueOf(Long.parseLong(superInterface.getStackSize(selectionWidgetGroup.getIndex())) - 100), String.valueOf(selectionWidgetGroup.getIndex())));
        final ButtonWidget<?> clickButtonSub1000 = new ButtonWidget<>(120, 177, 40, 20, "-1000", data -> superInterface.setStackSize(String.valueOf(Long.parseLong(superInterface.getStackSize(selectionWidgetGroup.getIndex())) - 1000), String.valueOf(selectionWidgetGroup.getIndex())));
        final NewTextFieldWidget<?> stackSizeTextField = new NewTextFieldWidget<>(14, 153, 148, 18, true, null, superInterface::setStackSize)
                .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                .setUpdateOnTyping(true)
                .setMaxStringLength(11);
        stackSizeTextField.setTextSupplier(() -> superInterface.getStackSize((int) stackSizeTextField.getTextIdLong()));
        selectionWidgetGroup.setIndexListener(stackSizeTextField::setTextIdLong);
        final ModularUI.Builder builder = ModularUI.builder(TJGuiTextures.SUPER_INTERFACE, 211, 292);
        for (int i = 0; i < duality.getConfig().getSlots(); i++) {
            builder.widget(new TJPhantomItemSlotWidget(7 + (18 * (i % 9)), 34 + (36 * (i / 9)), 18, 18, i, duality.getConfig())
                    .setBackgroundTextures(TJGuiTextures.SLOT_DOWN));
            selectionWidgetGroup.addSubWidget(i, stackSizeDisplay);
            selectionWidgetGroup.addSubWidget(i, stackSizeLabel);
            selectionWidgetGroup.addSubWidget(i, clickButtonAdd1.setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            selectionWidgetGroup.addSubWidget(i, clickButtonAdd10.setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            selectionWidgetGroup.addSubWidget(i, clickButtonAdd100.setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            selectionWidgetGroup.addSubWidget(i, clickButtonAdd1000.setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            selectionWidgetGroup.addSubWidget(i, clickButtonSub1.setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            selectionWidgetGroup.addSubWidget(i, clickButtonSub10.setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            selectionWidgetGroup.addSubWidget(i, clickButtonSub100.setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            selectionWidgetGroup.addSubWidget(i, clickButtonSub1000.setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            selectionWidgetGroup.addSubWidget(i, stackSizeTextField);
            selectionWidgetGroup.addSelectionBox(i, 7 + (18 * (i % 9)), 34 + (36 * (i / 9)), 18, 18);
        }
        for (int i = 0; i < duality.getPatterns().getSlots(); i++) {
            final int index = i;
            patternScrollableSlotGroup.addWidget(new AEPatternSlotWidget(duality.getPatterns(), i, 18 * (i % 9), 18 * (i / 9))
                    .setActiveSupplier(() -> index / 9 <= upgradeHandler.getInstalledUpgrades(Upgrades.PATTERN_EXPANSION) && selectionWidgetGroup.getIndex() < 0 && buttonPopUpWidget.getIndex() == 0)
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.PATTERN_OVERLAY)
                    .setInactiveBackgroundTexture(TJGuiTextures.BLANK_SLOT)
                    .setWidgetGroup(patternScrollableSlotGroup)
                    .setActiveInit(false));
        }
        createPatternMultiToolGUI(builder.widget(new LabelWidget(7, 109, "gui.appliedenergistics2.StoredItems"))
                .widget(new LabelWidget(7, 123, "gui.appliedenergistics2.Patterns"))
                .widget(new LabelWidget(7, 23, "gui.appliedenergistics2.Config"))
                .widget(patternScrollableSlotGroup)
                .widget(selectionWidgetGroup), patternMultiTool, multiUpgradeSlots, duality.getPatterns(), multiPatternSlots, invTag);
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            builder.widget(new TJSlotWidget<>(upgradeHandler, i, 186, 7 + (18 * i))
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY));
        }
        for (int i = 0; i < duality.getStorage().getSlots(); i++) {
            builder.widget(new TJSlotWidget<>(duality.getStorage(), i, 7 + (18 * (i % 9)), 52 + (36 * (i / 9)))
                    .setActiveBackgroundTexture(GuiTextures.SLOT));
        }
        return builder.widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(superInterface.getItemStackRepresentation()).setLocale(superInterface.getItemStackRepresentation().getDisplayName()))
                .widget(new TJLabelWidget(4, 0, 162, 18, null)
                        .setDynamicLocale(superInterface::getCustomInventoryName)
                        .setCentered(false)
                        .setCanSlide(false))
                .widget(new TJToggleButtonWidget(-18, 8, 16, 16, () -> duality.getConfigManager().getSetting(Settings.BLOCK).ordinal() == 0, superInterface::setBlockingMode)
                        .setToggleTitleTooltipHoverText("gui.tooltips.appliedenergistics2.InterfaceBlockingMode", "gui.tooltips.appliedenergistics2.InterfaceBlockingMode")
                        .setToggleTooltipHoverText("gui.tooltips.appliedenergistics2.NonBlocking", "gui.tooltips.appliedenergistics2.Blocking")
                        .setToggleTexture(TJGuiTextures.TOGGLE_BLOCKING_MODE)
                        .useToggleTexture(true))
                .widget(new TJCycleButtonWidget<>(-18, 26, 16, 16, (EnumSet<LockCraftingMode>) Settings.UNLOCK.getPossibleValues(), () -> (Enum<LockCraftingMode>) duality.getConfigManager().getSetting(Settings.UNLOCK), superInterface::setLockCrafting)
                        .setCycleHoverTooltipText("gui.tooltips.appliedenergistics2.LockCraftingModeNone", "gui.tooltips.appliedenergistics2.LockCraftingUntilRedstonePulse", "gui.tooltips.appliedenergistics2.LockCraftingWhileRedstoneHigh", "gui.tooltips.appliedenergistics2.LockCraftingWhileRedstoneLow", "gui.tooltips.appliedenergistics2.LockCraftingUntilResultReturned")
                        .setCycleTitleHoverTooltipText("gui.tooltips.appliedenergistics2.LockCraftingMode", "gui.tooltips.appliedenergistics2.LockCraftingMode", "gui.tooltips.appliedenergistics2.LockCraftingMode", "gui.tooltips.appliedenergistics2.LockCraftingMode", "gui.tooltips.appliedenergistics2.LockCraftingMode")
                        .setCycleTexture(TJGuiTextures.CYCLE_LOCK_CRAFTING))
                .widget(new TJToggleButtonWidget(-18, 44, 16, 16, () -> duality.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL).ordinal() == 0, superInterface::setInterfaceTerminal)
                        .setToggleTitleTooltipHoverText("item.appliedenergistics2.multi_part.interface_terminal.name", "item.appliedenergistics2.multi_part.interface_terminal.name")
                        .setToggleTooltipHoverText("gui.appliedenergistics2.InterfaceTerminalHint", "gui.appliedenergistics2.InterfaceTerminalHint")
                        .setToggleTexture(TJGuiTextures.TOGGLE_INTERFACE_TERMINAL)
                        .setInvertTexture(true)
                        .useToggleTexture(true))
                .widget(new TJToggleButtonWidget(-18, 62, 16, 16, () -> duality.getConfigManager().getSetting(Settings.OPERATION_MODE).ordinal() == 0, superInterface::setFluidPacket)
                        .setToggleTitleTooltipHoverText("ae2fc.tooltip.real_fluid", "ae2fc.tooltip.fake_packet")
                        .setToggleTooltipHoverText("ae2fc.tooltip.real_fluid.hint", "ae2fc.tooltip.fake_packet.hint")
                        .setToggleTexture(TJGuiTextures.TOGGLE_SEND_FLUID)
                        .useToggleTexture(true))
                .widget(new TJToggleButtonWidget(-18, 80, 16, 16, () -> duality.getConfigManager().getSetting(Settings.LEVEL_TYPE).ordinal() == 0, superInterface::setSplittingItemsFluids)
                        .setToggleTooltipHoverText("ae2fc.tooltip.allow_splitting.hint", "ae2fc.tooltip.prevent_splitting.hint")
                        .setToggleTitleTooltipHoverText("ae2fc.tooltip.allow_splitting", "ae2fc.tooltip.allow_splitting")
                        .setToggleTexture(TJGuiTextures.TOGGLE_SPLITTING_ITEMS_FLUIDS)
                        .useToggleTexture(true))
                .widget(new TJCycleButtonWidget<>(-18, 98, 16, 16, (EnumSet<CondenserOutput>) Settings.CONDENSER_OUTPUT.getPossibleValues(), () -> (Enum<CondenserOutput>) duality.getConfigManager().getSetting(Settings.CONDENSER_OUTPUT), superInterface::setBlockModeEx)
                        .setCycleHoverTooltipText("ae2fc.tooltip.block_all.hint", "ae2fc.tooltip.block_item.hint", "ae2fc.tooltip.block_fluid.hint")
                        .setCycleTitleHoverTooltipText("ae2fc.tooltip.block_all", "ae2fc.tooltip.block_item", "ae2fc.tooltip.block_fluid")
                        .setCycleTexture(TJGuiTextures.CYCLE_BLOCKING_MODE_EX))
                .widget(new TJToggleButtonWidget(-18, 116, 16, 16, () -> ((RCIConfigurableObject) duality).r$getConfigManager().getSetting(RCSettings.IntelligentBlocking).ordinal() == 0, superInterface::setIntelligentBlocking)
                        .setToggleTitleTooltipHoverText("gui.intelligent_blocking.name", "gui.intelligent_blocking.name")
                        .setToggleTooltipHoverText("gui.intelligent_blocking.CLOSE.text", "gui.intelligent_blocking.OPEN.text")
                        .setToggleTexture(TJGuiTextures.TOGGLE_BLOCKING_MODE)
                        .setInvertTexture(true)
                        .useToggleTexture(true))
                .widget(new TJToggleButtonWidget(-18, 134, 16, 16, () -> false, (BooleanConsumer) bool -> changePatternAmount(duality.getPatterns(), 2, patternScrollableSlotGroup, () -> updatePatterns(duality.getPatterns())))
                        .setToggleTooltipHoverText("gui.pattern_term.auto_fill_pattern.MULTIPLY_2.text", "gui.pattern_term.auto_fill_pattern.MULTIPLY_2.text")
                        .setToggleTitleTooltipHoverText("gui.action.MULTIPLY_2.name", "gui.action.MULTIPLY_2.name")
                        .setActiveTexture(TJGuiTextures.AE2_MULTIPLY2_BUTTON, TJGuiTextures.AE2_MULTIPLY2_BUTTON))
                .widget(new TJToggleButtonWidget(-18, 152, 16, 16, () -> false, (BooleanConsumer) bool -> changePatternAmount(duality.getPatterns(), -2, patternScrollableSlotGroup, () -> updatePatterns(duality.getPatterns())))
                        .setToggleTooltipHoverText("gui.pattern_term.auto_fill_pattern.DIVIDE_2.text", "gui.pattern_term.auto_fill_pattern.DIVIDE_2.text")
                        .setToggleTitleTooltipHoverText("gui.action.DIVIDE_2.name", "gui.action.DIVIDE_2.name")
                        .setActiveTexture(TJGuiTextures.AE2_DIVIDE2_BUTTON, TJGuiTextures.AE2_DIVIDE2_BUTTON))
                .widget(new TJToggleButtonWidget(-18, 170, 16, 16, () -> false, (BooleanConsumer) bool -> changePatternAmount(duality.getPatterns(), 3, patternScrollableSlotGroup, () -> updatePatterns(duality.getPatterns())))
                        .setToggleTooltipHoverText("gui.pattern_term.auto_fill_pattern.MULTIPLY_3.text", "gui.pattern_term.auto_fill_pattern.MULTIPLY_3.text")
                        .setToggleTitleTooltipHoverText("gui.action.MULTIPLY_3.name", "gui.action.MULTIPLY_3.name")
                        .setActiveTexture(TJGuiTextures.AE2_MULTIPLY3_BUTTON, TJGuiTextures.AE2_MULTIPLY3_BUTTON))
                .widget(new TJToggleButtonWidget(-18, 188, 16, 16, () -> false, (BooleanConsumer) bool -> changePatternAmount(duality.getPatterns(), -3, patternScrollableSlotGroup, () -> updatePatterns(duality.getPatterns())))
                        .setToggleTooltipHoverText("gui.pattern_term.auto_fill_pattern.DIVIDE_3.text", "gui.pattern_term.auto_fill_pattern.DIVIDE_3.text")
                        .setToggleTitleTooltipHoverText("gui.action.DIVIDE_3.name", "gui.action.DIVIDE_3.name")
                        .setActiveTexture(TJGuiTextures.AE2_DIVIDE3_BUTTON, TJGuiTextures.AE2_DIVIDE3_BUTTON))
                .widget(buttonPopUpWidget.addPopup(widgetGroup -> true)
                        .addPopup(new ButtonWidget<>(154, 0, 22, 22)
                                .setItemDisplay(Api.INSTANCE.definitions().items().certusQuartzWrench().maybeStack(1).orElse(ItemStack.EMPTY))
                                .setBackgroundTextures(TJGuiTextures.INTERFACE_SETTINGS_EDGE_RIGHT)
                                .setTitleHoverTooltipText("gui.appliedenergistics2.Priority"), widgetGroup -> {
                            widgetGroup.addWidget(new ImageWidget(7, 107, 162, 100, GuiTextures.BORDERED_BACKGROUND));
                            widgetGroup.addWidget(new LabelWidget(14, 112, "gui.appliedenergistics2.Priority"));
                            widgetGroup.addWidget(new NewTextFieldWidget<>(14, 153, 148, 18, true, () -> String.valueOf(duality.getPriority()), superInterface::setPriority)
                                    .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new ButtonWidget<>(15, 127, 25, 20, "+1", data -> superInterface.setPriority(String.valueOf((long) duality.getPriority() + 1), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(45, 127, 30, 20, "+10", data -> superInterface.setPriority(String.valueOf((long) duality.getPriority() + 10), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(80, 127, 35, 20, "+100", data -> superInterface.setPriority(String.valueOf((long) duality.getPriority() + 100), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(120, 127, 40, 20, "+1000", data -> superInterface.setPriority(String.valueOf((long) duality.getPriority() + 1000), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(15, 177, 25, 20, "-1", data -> superInterface.setPriority(String.valueOf((long) duality.getPriority() - 1), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(45, 177, 30, 20, "-10", data -> superInterface.setPriority(String.valueOf((long) duality.getPriority() - 10), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(80, 177, 35, 20, "-100", data -> superInterface.setPriority(String.valueOf((long) duality.getPriority() - 100), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(120, 177, 40, 20, "-1000", data -> superInterface.setPriority(String.valueOf((long) duality.getPriority() - 1000), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            return false;
                        }))
                .widget(TJGuiUtils.bindPlayerInventory(new WidgetGroup(), player.inventory, 7, 209, patternMultiTool))
                .bindOpenListener(() -> {
                    if (!patternMultiTool.isEmpty()) {
                        readPatternMultiToolNBT(multiPatternSlots, invTag.getTagList("Items", 10));
                        readPatternMultiToolNBT(multiUpgradeSlots, upgradeTag.getTagList("Items", 10));
                        if (patternMultiTool.getTagCompound() == null || patternMultiTool.getTagCompound().isEmpty()) {
                            compound.setTag("inv", invTag);
                            compound.setTag("upgrades", upgradeTag);
                            patternMultiTool.setTagCompound(compound);
                        }
                    }
                }).build(holder, player);
    }

    private static void createPatternMultiToolGUI(ModularUI.Builder builder, ItemStack patternMultiTool, FilteredItemStackHandler multiUpgradeSlots, IItemHandler patternSlots, IItemHandler multiPatternSlots, NBTTagCompound invTag) {
        if (!patternMultiTool.isEmpty()) {
            final SlotScrollableWidgetGroup multiPatternSlotGroup = new SlotScrollableWidgetGroup(-118, 14, 72, 162, 4)
                    .setItemStackTransfer(itemStack -> TJItemUtils.insertIntoItemHandler(patternSlots, itemStack, false))
                    .setItemHandler(multiPatternSlots)
                    .setScrollWidth(0);
            builder.widget(new ImageWidget(-125, 0, 105, 218, GuiTextures.BORDERED_BACKGROUND))
                    .widget(new LabelWidget(-118, 4, "item.nae2.pattern_multiplier.name"))
                    .widget(new ButtonWidget<>(-118, 176, 18, 18, "*2", data -> changePatternAmount(multiPatternSlots, 2, multiPatternSlotGroup, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setTitleHoverTooltipText("gui.action.MULTIPLY_2.name").setHoverTooltipText("gui.pattern_term.auto_fill_pattern.MULTIPLY_2.text").setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                    .widget(new ButtonWidget<>(-118, 194, 18, 18, "/2", data -> changePatternAmount(multiPatternSlots, -2, multiPatternSlotGroup, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setTitleHoverTooltipText("gui.action.DIVIDE_2.name").setHoverTooltipText("gui.pattern_term.auto_fill_pattern.DIVIDE_2.text").setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                    .widget(new ButtonWidget<>(-100, 176, 18, 18, "*3", data -> changePatternAmount(multiPatternSlots, 3, multiPatternSlotGroup, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setTitleHoverTooltipText("gui.action.MULTIPLY_3.name").setHoverTooltipText("gui.pattern_term.auto_fill_pattern.MULTIPLY_3.text").setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                    .widget(new ButtonWidget<>(-100, 194, 18, 18, "/3", data -> changePatternAmount(multiPatternSlots, -3, multiPatternSlotGroup, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setTitleHoverTooltipText("gui.action.DIVIDE_3.name").setHoverTooltipText("gui.pattern_term.auto_fill_pattern.DIVIDE_3.text").setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                    .widget(new ButtonWidget<>(-82, 176, 18, 18, "*4", data -> changePatternAmount(multiPatternSlots, 4, multiPatternSlotGroup, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                    .widget(new ButtonWidget<>(-82, 194, 18, 18, "/4", data -> changePatternAmount(multiPatternSlots, -4, multiPatternSlotGroup, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setBackgroundTextures(GuiTextures.VANILLA_BUTTON))
                    .widget(new ButtonWidget<>(-64, 176, 36, 36, "X", data -> clearPatterns(multiPatternSlots, () -> writePatternMultiToolToNBT(multiPatternSlots, invTag)))
                            .setTitleHoverTooltipText("nae2.pattern_multiplier.unencode").setHoverTooltipText("nae2.pattern_multiplier.unencode.desc").setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
            for (int i = 0; i < multiPatternSlots.getSlots(); i++) {
                final int index = i;
                multiPatternSlotGroup.addWidget(new AEPatternSlotWidget(multiPatternSlots, i, 18 * (i / 9), 18 * (i % 9))
                        .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.PATTERN_OVERLAY)
                        .setActiveSupplier(() -> index / 9 <= multiUpgradeSlots.getSlotsFilled())
                        .setSlotLocationInfo(true, false)
                        .setInactiveBackgroundTexture(TJGuiTextures.BLANK_SLOT)
                        .setWidgetGroup(multiPatternSlotGroup));
            }
            builder.widget(multiPatternSlotGroup);
            for (int i = 0; i < multiUpgradeSlots.getSlots(); i++) {
                builder.widget(new TJSlotWidget<>(multiUpgradeSlots, i, -46, 14 + (i * 18))
                        .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY));
            }
        }
    }

    private static void writePatternMultiToolToNBT(IItemHandler itemHandler, NBTTagCompound compound) {
        final NBTTagList tagList = new NBTTagList();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            final ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                final NBTTagCompound tagCompound = stack.serializeNBT();
                tagCompound.setInteger("Slot", i);
                tagList.appendTag(tagCompound);
            }
        }
        compound.setTag("Items", tagList);
    }

    private static void readPatternMultiToolNBT(IItemHandlerModifiable itemHandler, NBTTagList tagList) {
        for (int i = 0; i < tagList.tagCount(); i++) {
            final NBTTagCompound compound = tagList.getCompoundTagAt(i);
            if (compound.hasKey("Slot")) {
                final ItemStack patternStack = TJItemUtils.getItemStackFromName(compound.getString("id"), compound.getInteger("Count"), compound.getShort("Damage"));
                patternStack.setTagCompound(compound.getCompoundTag("tag"));
                itemHandler.setStackInSlot(compound.getInteger("Slot"), patternStack);
            }
        }
    }

    private static void changePatternAmount(IItemHandler patternSlots, int multiplier, SlotScrollableWidgetGroup patternSlotWidgets, Runnable callback) {
        final boolean divide = multiplier < 0;
        if (divide)
            multiplier = Math.abs(multiplier);
        final int finalMultiplier = multiplier;
        for (int i = 0; i < patternSlots.getSlots(); i++) {
            final ItemStack stack = patternSlots.getStackInSlot(i);
            final NBTTagCompound compound = stack.getTagCompound();
            if (stack.isEmpty() || compound == null) continue;
            final ResourceLocation resourcelocation = Item.REGISTRY.getNameForObject(stack.getItem());
            final String id = resourcelocation != null ? resourcelocation.toString() : "minecraft:air";
            final NBTTagList inputList = compound.getTagList(id.equals("ae2fc:dense_encoded_pattern") ? "Inputs" : "in", 10);
            final NBTTagList outputList = compound.getTagList(id.equals("ae2fc:dense_encoded_pattern") ? "Outputs" : "out", 10);
            final NBTTagList newInputList = new NBTTagList(), newOutputList = new NBTTagList();
            final Predicate<Boolean> setPatternInputs = simulate -> {
                for (int j = 0; j < inputList.tagCount(); j++) {
                    final NBTTagCompound patternCompound = inputList.getCompoundTagAt(j);
                    final long amount = patternCompound.hasKey("Cnt") ? patternCompound.getLong("Cnt") : patternCompound.getInteger("Count");
                    final long newAmount = divide ? amount / finalMultiplier : amount * finalMultiplier;
                    if (patternCompound.isEmpty()) {
                        if (!simulate)
                            newInputList.appendTag(patternCompound);
                        continue;
                    }
                    if (newAmount > 0 && newAmount <= Integer.MAX_VALUE) {
                        if (!simulate) {
                            if (id.equals("ae2fc:dense_encoded_pattern")) {
                                patternCompound.setLong("Cnt", newAmount);
                            } else patternCompound.setInteger("Count", (int) newAmount);
                            newInputList.appendTag(patternCompound);
                        }
                    } else return false;
                }
                for (int j = 0; j < outputList.tagCount(); j++) {
                    final NBTTagCompound patternCompound = outputList.getCompoundTagAt(j);
                    final long amount = patternCompound.hasKey("Cnt") ? patternCompound.getLong("Cnt") : patternCompound.getInteger("Count");
                    final long newAmount = divide ? amount / finalMultiplier : amount * finalMultiplier;
                    if (patternCompound.isEmpty()) {
                        if (!simulate)
                            newOutputList.appendTag(patternCompound);
                        continue;
                    }
                    if (newAmount > 0 && newAmount <= Integer.MAX_VALUE) {
                        if (!simulate) {
                            if (id.equals("ae2fc:dense_encoded_pattern")) {
                                patternCompound.setLong("Cnt", newAmount);
                            } else patternCompound.setInteger("Count", (int) newAmount);
                            newOutputList.appendTag(patternCompound);
                        }
                    } else return false;
                }
                if (!simulate) {
                    compound.setTag("in", newInputList);
                    compound.setTag("out", newOutputList);
                    if (id.equals("ae2fc:dense_encoded_pattern")) {
                        compound.setTag("Inputs", newInputList);
                        compound.setTag("Outputs", newOutputList);
                    }
                }
                return true;
            };
            if (setPatternInputs.test(true))
                setPatternInputs.test(false);
        }
        callback.run();
        patternSlotWidgets.getNativeWidgets().stream()
                .filter(widget -> widget instanceof TJSlotWidget<?>)
                .forEach(slot -> ((TJSlotWidget<?>) slot).forceUpdate());
    }

    private static void clearPatterns(IItemHandler patternSlots, Runnable callback) {
        for (int i = 0; i < patternSlots.getSlots(); i++) {
            ItemStack pattern = patternSlots.extractItem(i, Integer.MAX_VALUE, false);
            pattern = Api.INSTANCE.definitions().materials().blankPattern().maybeStack(pattern.getCount()).orElse(ItemStack.EMPTY);
            patternSlots.insertItem(i, pattern, false);
        }
        callback.run();
    }

    private static void updatePatterns(IItemHandler patternSlots) {
        final NonNullList<ItemStack> itemStacks = NonNullList.create();
        for (int i = 0; i < patternSlots.getSlots(); i++)
            itemStacks.add(patternSlots.extractItem(i, Integer.MAX_VALUE, false));
        for (int i = 0; i < patternSlots.getSlots(); i++)
            patternSlots.insertItem(i, itemStacks.get(i), false);
    }
}
