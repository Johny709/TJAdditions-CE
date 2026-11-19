package tj.integration.ae2.blocks;

import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.block.misc.BlockInterface;
import appeng.core.Api;
import com.circulation.random_complement.client.RCSettings;
import com.circulation.random_complement.common.interfaces.RCIConfigurableObject;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.util.function.BooleanConsumer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.integration.ae2.ISuperInterface;
import tj.integration.ae2.helpers.DualitySuperInterface;
import tj.integration.ae2.tile.TileSuperInterface;
import tj.items.handlers.FilteredItemStackHandler;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.TJModularUI;
import tj.mui.widgets.impl.*;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


public class BlockSuperInterface extends BlockInterface {

    public static final DualitySuperInterface DUALITY_INSTANCE = (DualitySuperInterface) new TileSuperInterface().getInterfaceDuality();

    public BlockSuperInterface() {
        this.setTileEntity(TileSuperInterface.class);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack is, World world, List<String> lines, ITooltipFlag advancedItemTooltips) {
        if (DUALITY_INSTANCE != null) {
            lines.add(I18n.format("tile.me.super_interface.pattern_slots", DUALITY_INSTANCE.getPatterns().getSlots()));
            lines.add(I18n.format("tile.me.super_interface.storage_slots", DUALITY_INSTANCE.getStorage().getSlots()));
            lines.add(I18n.format("tile.me.super_interface.upgrade_slots", DUALITY_INSTANCE.getInventoryByName("upgrades").getSlots()));
        }
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
        final ItemStack patternMultiTool = TJGuiUtils.getPatternMultiTool(player);
        final NBTTagCompound compound = TJItemUtils.getCompoundFromStack(patternMultiTool);
        final NBTTagCompound invTag = compound.getCompoundTag("inv");
        final NBTTagCompound upgradeTag = compound.getCompoundTag("upgrades");
        final FilteredItemStackHandler multiPatternSlots = new FilteredItemStackHandler(null, 36, 64)
                .setItemStackPredicate((slot, itemStack) -> itemStack.isItemEqual(Api.INSTANCE.definitions().materials().blankPattern().maybeStack(1).orElse(ItemStack.EMPTY)) ||
                        itemStack.isItemEqual(Api.INSTANCE.definitions().items().encodedPattern().maybeStack(1).orElse(ItemStack.EMPTY)) || itemStack.isItemEqual(TJItemUtils.getItemStackFromName("ae2fc:dense_encoded_pattern")));
        multiPatternSlots.setOnContentsChangedPost((slot, itemStack) -> TJGuiUtils.writePatternMultiToolToNBT(multiPatternSlots, invTag));
        final FilteredItemStackHandler multiUpgradeSlots = new FilteredItemStackHandler(null, 3, 1)
                .setItemStackPredicate((slot, itemStack) -> itemStack.isItemEqual(Api.INSTANCE.definitions().materials().cardCapacity().maybeStack(1).orElse(ItemStack.EMPTY)));
        multiUpgradeSlots.setOnContentsChangedPost((slot, itemStack) -> TJGuiUtils.writePatternMultiToolToNBT(multiUpgradeSlots, upgradeTag));

        final SlotWidgetGroup patternScrollableSlotGroup = new SlotWidgetGroup(7, 133, 162, 72, 9)
                .setItemStackTransfer(itemStack -> TJItemUtils.insertIntoItemHandler(multiPatternSlots, itemStack, false))
                .setItemHandler(duality.getPatterns());
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(0, 0, 0, 0);
        final TJModularUI ui = TJModularUI.builder(holder, player)
                .image(179, 0, 32, 194, GuiTextures.BORDERED_BACKGROUND);
        for (int i = 0; i < duality.getConfig().getSlots(); i++) {
            ui.widget(new TJPhantomItemSlotWidget(7 + (18 * (i % 9)), 34 + (36 * (i / 9)), 18, 18, i, duality.getConfig())
                    .setBackgroundTextures(TJGuiTextures.SLOT_DOWN));
            selectionWidgetGroup.addSelectionBox(i, 7 + (18 * (i % 9)), 34 + (36 * (i / 9)), 18, 18);
        }
        for (int i = 0; i < duality.getPatterns().getSlots(); i++) {
            final int index = i;
            patternScrollableSlotGroup.addWidget(new AEPatternSlotWidget(duality.getPatterns(), i, 18 * (i % 9), 18 * (i / 9))
                    .setActiveSupplier(() -> index / 9 <= upgradeHandler.getInstalledUpgrades(Upgrades.PATTERN_EXPANSION))
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.PATTERN_OVERLAY)
                    .setInactiveBackgroundTexture(TJGuiTextures.BLANK_SLOT)
                    .setWidgetGroup(patternScrollableSlotGroup)
                    .setActiveInit(false));
        }
        TJGuiUtils.createPatternMultiToolGUI(ui, patternMultiTool, multiUpgradeSlots, duality.getPatterns(), multiPatternSlots, invTag);
        ui.widget(new LabelWidget(7, 109, "gui.appliedenergistics2.StoredItems"))
                .widget(new LabelWidget(7, 123, "gui.appliedenergistics2.Patterns"))
                .widget(new LabelWidget(7, 23, "gui.appliedenergistics2.Config"))
                .widget(patternScrollableSlotGroup)
                .widget(selectionWidgetGroup);
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            ui.widget(new TJSlotWidget<>(upgradeHandler, i, 186, 7 + (18 * i))
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY)
                    .setSlotProtection(true));
        }
        for (int i = 0; i < duality.getStorage().getSlots(); i++) {
            ui.widget(new TJSlotWidget<>(duality.getStorage(), i, 7 + (18 * (i % 9)), 52 + (36 * (i / 9)))
                    .setActiveBackgroundTexture(GuiTextures.SLOT));
        }
        final PanelWidgetGroup panelWidgetGroup = TJGuiUtils.createPriorityPanel(superInterface);
        return ui.setWidthHeight(176, 292)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
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
                .widget(new TJCycleButtonWidget<>(-18, 26, 16, 16, Settings.UNLOCK.getPossibleValues(), () -> duality.getConfigManager().getSetting(Settings.UNLOCK), superInterface::setLockCrafting)
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
                .widget(new TJCycleButtonWidget<>(-18, 98, 16, 16, Settings.CONDENSER_OUTPUT.getPossibleValues(), () -> duality.getConfigManager().getSetting(Settings.CONDENSER_OUTPUT), superInterface::setBlockModeEx)
                        .setCycleHoverTooltipText("ae2fc.tooltip.block_all.hint", "ae2fc.tooltip.block_item.hint", "ae2fc.tooltip.block_fluid.hint")
                        .setCycleTitleHoverTooltipText("ae2fc.tooltip.block_all", "ae2fc.tooltip.block_item", "ae2fc.tooltip.block_fluid")
                        .setCycleTexture(TJGuiTextures.CYCLE_BLOCKING_MODE_EX))
                .widget(new TJToggleButtonWidget(-18, 116, 16, 16, () -> ((RCIConfigurableObject) duality).r$getConfigManager().getSetting(RCSettings.IntelligentBlocking).ordinal() == 0, superInterface::setIntelligentBlocking)
                        .setToggleTitleTooltipHoverText("gui.intelligent_blocking.name", "gui.intelligent_blocking.name")
                        .setToggleTooltipHoverText("gui.intelligent_blocking.CLOSE.text", "gui.intelligent_blocking.OPEN.text")
                        .setToggleTexture(TJGuiTextures.TOGGLE_BLOCKING_MODE)
                        .setInvertTexture(true)
                        .useToggleTexture(true))
                .widget(new TJToggleButtonWidget(-18, 134, 16, 16, () -> false, (BooleanConsumer) bool -> TJGuiUtils.changePatternAmount(duality.getPatterns(), patternScrollableSlotGroup, m -> m * 2, () -> TJGuiUtils.updatePatterns(duality.getPatterns())))
                        .setToggleTooltipHoverText("gui.pattern_term.auto_fill_pattern.MULTIPLY_2.text", "gui.pattern_term.auto_fill_pattern.MULTIPLY_2.text")
                        .setToggleTitleTooltipHoverText("gui.action.MULTIPLY_2.name", "gui.action.MULTIPLY_2.name")
                        .setActiveTexture(TJGuiTextures.AE2_MULTIPLY2_BUTTON, TJGuiTextures.AE2_MULTIPLY2_BUTTON))
                .widget(new TJToggleButtonWidget(-18, 152, 16, 16, () -> false, (BooleanConsumer) bool -> TJGuiUtils.changePatternAmount(duality.getPatterns(), patternScrollableSlotGroup, m -> m / 2, () -> TJGuiUtils.updatePatterns(duality.getPatterns())))
                        .setToggleTooltipHoverText("gui.pattern_term.auto_fill_pattern.DIVIDE_2.text", "gui.pattern_term.auto_fill_pattern.DIVIDE_2.text")
                        .setToggleTitleTooltipHoverText("gui.action.DIVIDE_2.name", "gui.action.DIVIDE_2.name")
                        .setActiveTexture(TJGuiTextures.AE2_DIVIDE2_BUTTON, TJGuiTextures.AE2_DIVIDE2_BUTTON))
                .widget(new TJToggleButtonWidget(-18, 170, 16, 16, () -> false, (BooleanConsumer) bool -> TJGuiUtils.changePatternAmount(duality.getPatterns(), patternScrollableSlotGroup, m -> m * 3, () -> TJGuiUtils.updatePatterns(duality.getPatterns())))
                        .setToggleTooltipHoverText("gui.pattern_term.auto_fill_pattern.MULTIPLY_3.text", "gui.pattern_term.auto_fill_pattern.MULTIPLY_3.text")
                        .setToggleTitleTooltipHoverText("gui.action.MULTIPLY_3.name", "gui.action.MULTIPLY_3.name")
                        .setActiveTexture(TJGuiTextures.AE2_MULTIPLY3_BUTTON, TJGuiTextures.AE2_MULTIPLY3_BUTTON))
                .widget(new TJToggleButtonWidget(-18, 188, 16, 16, () -> false, (BooleanConsumer) bool -> TJGuiUtils.changePatternAmount(duality.getPatterns(), patternScrollableSlotGroup, m -> m / 3, () -> TJGuiUtils.updatePatterns(duality.getPatterns())))
                        .setToggleTooltipHoverText("gui.pattern_term.auto_fill_pattern.DIVIDE_3.text", "gui.pattern_term.auto_fill_pattern.DIVIDE_3.text")
                        .setToggleTitleTooltipHoverText("gui.action.DIVIDE_3.name", "gui.action.DIVIDE_3.name")
                        .setActiveTexture(TJGuiTextures.AE2_DIVIDE3_BUTTON, TJGuiTextures.AE2_DIVIDE3_BUTTON))
                .widget(new ButtonWidget<>(154, 0, 22, 22)
                        .setItemDisplay(Api.INSTANCE.definitions().items().certusQuartzWrench().maybeStack(1).orElse(ItemStack.EMPTY))
                        .setBackgroundTextures(TJGuiTextures.INTERFACE_SETTINGS_EDGE_RIGHT)
                        .setTitleHoverTooltipText("gui.appliedenergistics2.Priority")
                        .setButtonResponder(s -> {
                            if (panelWidgetGroup.isPanelOpen()) {
                                panelWidgetGroup.closePanel();
                            } else panelWidgetGroup.openPanel();
                        }))
                .widget(panelWidgetGroup)
                .widget(TJGuiUtils.createItemConfigPanel(superInterface, selectionWidgetGroup))
                .bindOpenListener(() -> {
                    if (!patternMultiTool.isEmpty()) {
                        TJGuiUtils.readPatternMultiToolNBT(multiPatternSlots, invTag.getTagList("Items", 10));
                        TJGuiUtils.readPatternMultiToolNBT(multiUpgradeSlots, upgradeTag.getTagList("Items", 10));
                        if (patternMultiTool.getTagCompound() == null || patternMultiTool.getTagCompound().isEmpty()) {
                            compound.setTag("inv", invTag);
                            compound.setTag("upgrades", upgradeTag);
                            patternMultiTool.setTagCompound(compound);
                        }
                    }
                }).bindPlayerInventory(patternMultiTool)
                .build();
    }
}
