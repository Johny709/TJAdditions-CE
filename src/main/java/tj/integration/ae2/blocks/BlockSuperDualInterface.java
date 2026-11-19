package tj.integration.ae2.blocks;

import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.block.misc.BlockInterface;
import appeng.core.Api;
import appeng.fluids.util.AEFluidInventory;
import com.circulation.random_complement.client.RCSettings;
import com.circulation.random_complement.common.interfaces.RCIConfigurableObject;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.tab.VerticalTabListRenderer;
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
import net.minecraftforge.items.IItemHandler;
import tj.blocks.block.TJBlocks;
import tj.mui.widgets.TJModularUI;
import tj.mui.widgets.WidgetTabBuilder;
import tj.integration.ae2.ISuperDualInterface;
import tj.integration.ae2.helpers.DualitySuperFluidInterface;
import tj.integration.ae2.helpers.DualitySuperInterface;
import tj.integration.ae2.helpers.IDualitySuperFluidInterface;
import tj.integration.ae2.tile.TileSuperDualInterface;
import tj.items.handlers.FilteredItemStackHandler;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.impl.*;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.HorizontalLocation.LEFT;
import static gregtech.api.gui.widgets.tab.VerticalTabListRenderer.VerticalStartCorner.TOP;

public class BlockSuperDualInterface extends BlockInterface {

    public static final DualitySuperInterface DUALITY_INSTANCE;
    public static final DualitySuperFluidInterface FLUID_DUALITY_INSTANCE;

    static {
        final TileSuperDualInterface dualInterface = new TileSuperDualInterface();
        DUALITY_INSTANCE = (DualitySuperInterface) dualInterface.getInterfaceDuality();
        FLUID_DUALITY_INSTANCE = (DualitySuperFluidInterface) dualInterface.getDualityFluidInterface();
    }

    public BlockSuperDualInterface() {
        this.setTileEntity(TileSuperDualInterface.class);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack is, World world, List<String> lines, ITooltipFlag advancedItemTooltips) {
        if (DUALITY_INSTANCE != null && FLUID_DUALITY_INSTANCE != null) {
            lines.add(I18n.format("tile.me.super_interface.pattern_slots", DUALITY_INSTANCE.getPatterns().getSlots()));
            lines.add(I18n.format("tile.me.super_interface.storage_slots", DUALITY_INSTANCE.getStorage().getSlots()));
            lines.add(I18n.format("tile.me.super_interface.upgrade_slots", DUALITY_INSTANCE.getInventoryByName("upgrades").getSlots()));
            lines.add(I18n.format("tile.me.super_fluid_interface.fluid_tanks", FLUID_DUALITY_INSTANCE.getTanks().getSlots()));
            lines.add(I18n.format("tile.me.super_fluid_interface.upgrade_slots", FLUID_DUALITY_INSTANCE.getInventoryByName("upgrades").getSlots()));
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
        final TileSuperDualInterface superInterface = this.getTileEntity(world, pos);
        if (superInterface != null) {
            if (!world.isRemote) {
                superInterface.openUI(player, superInterface);
            }
            return true;
        }
        return false;
    }

    public static ModularUI createDualInterfaceGUI(TileEntityHolder holder, EntityPlayer player, ISuperDualInterface superDualInterface) {
        final DualitySuperInterface duality = (DualitySuperInterface) superDualInterface.getInterfaceDuality();
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
        final WidgetTabBuilder tabBuilder = new WidgetTabBuilder()
                .setTabListRenderer(() -> new VerticalTabListRenderer(TOP, LEFT))
                .addTab("tile.me.super_interface.name", TJBlocks.SUPER_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY), widgets -> createInterfaceTab(widgets, superDualInterface, multiPatternSlots))
                .addTab("tile.me.super_fluid_interface.name", TJBlocks.SUPER_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY), widgets -> createFluidInterfaceTab(widgets, superDualInterface));
        final TJModularUI ui = TJModularUI.builder(holder, player)
                .image(179, 0, 32, 194, GuiTextures.BORDERED_BACKGROUND);
        TJGuiUtils.createPatternMultiToolGUI(ui, patternMultiTool, multiUpgradeSlots, duality.getPatterns(), multiPatternSlots, invTag);
        final PanelWidgetGroup panelWidgetGroup = TJGuiUtils.createPriorityPanel(superDualInterface);
        return ui.setWidthHeight(176, 292)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(superDualInterface.getItemStackRepresentation()).setLocale(superDualInterface.getItemStackRepresentation().getDisplayName()))
                .widget(new TJLabelWidget(4, 0, 162, 18, null)
                        .setDynamicLocale(superDualInterface::getCustomInventoryName)
                        .setCentered(false)
                        .setCanSlide(false))
                .widget(tabBuilder.build())
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

    private static void createInterfaceTab(List<Widget> tab, ISuperDualInterface superDualInterface, IItemHandler multiPatternSlots) {
        final DualitySuperInterface duality = (DualitySuperInterface) superDualInterface.getInterfaceDuality();
        final DualitySuperInterface.DualityUpgradeInventory upgradeHandler = (DualitySuperInterface.DualityUpgradeInventory) duality.getInventoryByName("upgrades");
        final SlotWidgetGroup patternScrollableSlotGroup = new SlotWidgetGroup(7, 133, 162, 72, 9)
                .setItemStackTransfer(itemStack -> TJItemUtils.insertIntoItemHandler(multiPatternSlots, itemStack, false))
                .setItemHandler(duality.getPatterns());
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(0, 0, 0, 0);
        for (int i = 0; i < duality.getConfig().getSlots(); i++) {
            tab.add(new TJPhantomItemSlotWidget(7 + (18 * (i % 9)), 34 + (36 * (i / 9)), 18, 18, i, duality.getConfig())
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
        tab.add(new LabelWidget(7, 109, "gui.appliedenergistics2.StoredItems"));
        tab.add(new LabelWidget(7, 123, "gui.appliedenergistics2.Patterns"));
        tab.add(new LabelWidget(7, 23, "gui.appliedenergistics2.Config"));
        tab.add(patternScrollableSlotGroup);
        tab.add(selectionWidgetGroup);
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            tab.add(new TJSlotWidget<>(upgradeHandler, i, 186, 7 + (18 * i))
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY)
                    .setSlotProtection(true));
        }
        for (int i = 0; i < duality.getStorage().getSlots(); i++) {
            tab.add(new TJSlotWidget<>(duality.getStorage(), i, 7 + (18 * (i % 9)), 52 + (36 * (i / 9)))
                    .setActiveBackgroundTexture(GuiTextures.SLOT));
        }
        tab.add(new TJToggleButtonWidget(-18, 58, 16, 16, () -> duality.getConfigManager().getSetting(Settings.BLOCK).ordinal() == 0, superDualInterface::setBlockingMode)
                .setToggleTitleTooltipHoverText("gui.tooltips.appliedenergistics2.InterfaceBlockingMode", "gui.tooltips.appliedenergistics2.InterfaceBlockingMode")
                .setToggleTooltipHoverText("gui.tooltips.appliedenergistics2.NonBlocking", "gui.tooltips.appliedenergistics2.Blocking")
                .setToggleTexture(TJGuiTextures.TOGGLE_BLOCKING_MODE)
                .useToggleTexture(true));
        tab.add(new TJCycleButtonWidget<>(-18, 76, 16, 16, Settings.UNLOCK.getPossibleValues(), () -> duality.getConfigManager().getSetting(Settings.UNLOCK), superDualInterface::setLockCrafting)
                .setCycleHoverTooltipText("gui.tooltips.appliedenergistics2.LockCraftingModeNone", "gui.tooltips.appliedenergistics2.LockCraftingUntilRedstonePulse", "gui.tooltips.appliedenergistics2.LockCraftingWhileRedstoneHigh", "gui.tooltips.appliedenergistics2.LockCraftingWhileRedstoneLow", "gui.tooltips.appliedenergistics2.LockCraftingUntilResultReturned")
                .setCycleTitleHoverTooltipText("gui.tooltips.appliedenergistics2.LockCraftingMode", "gui.tooltips.appliedenergistics2.LockCraftingMode", "gui.tooltips.appliedenergistics2.LockCraftingMode", "gui.tooltips.appliedenergistics2.LockCraftingMode", "gui.tooltips.appliedenergistics2.LockCraftingMode")
                .setCycleTexture(TJGuiTextures.CYCLE_LOCK_CRAFTING));
        tab.add(new TJToggleButtonWidget(-18, 94, 16, 16, () -> duality.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL).ordinal() == 0, superDualInterface::setInterfaceTerminal)
                .setToggleTitleTooltipHoverText("item.appliedenergistics2.multi_part.interface_terminal.name", "item.appliedenergistics2.multi_part.interface_terminal.name")
                .setToggleTooltipHoverText("gui.appliedenergistics2.InterfaceTerminalHint", "gui.appliedenergistics2.InterfaceTerminalHint")
                .setToggleTexture(TJGuiTextures.TOGGLE_INTERFACE_TERMINAL)
                .setInvertTexture(true)
                .useToggleTexture(true));
        tab.add(new TJToggleButtonWidget(-18, 112, 16, 16, () -> duality.getConfigManager().getSetting(Settings.OPERATION_MODE).ordinal() == 0, superDualInterface::setFluidPacket)
                .setToggleTitleTooltipHoverText("ae2fc.tooltip.real_fluid", "ae2fc.tooltip.fake_packet")
                .setToggleTooltipHoverText("ae2fc.tooltip.real_fluid.hint", "ae2fc.tooltip.fake_packet.hint")
                .setToggleTexture(TJGuiTextures.TOGGLE_SEND_FLUID)
                .useToggleTexture(true));
        tab.add(new TJToggleButtonWidget(-18, 130, 16, 16, () -> duality.getConfigManager().getSetting(Settings.LEVEL_TYPE).ordinal() == 0, superDualInterface::setSplittingItemsFluids)
                .setToggleTooltipHoverText("ae2fc.tooltip.allow_splitting.hint", "ae2fc.tooltip.prevent_splitting.hint")
                .setToggleTitleTooltipHoverText("ae2fc.tooltip.allow_splitting", "ae2fc.tooltip.allow_splitting")
                .setToggleTexture(TJGuiTextures.TOGGLE_SPLITTING_ITEMS_FLUIDS)
                .useToggleTexture(true));
        tab.add(new TJCycleButtonWidget<>(-18, 148, 16, 16, Settings.CONDENSER_OUTPUT.getPossibleValues(), () -> duality.getConfigManager().getSetting(Settings.CONDENSER_OUTPUT), superDualInterface::setBlockModeEx)
                .setCycleHoverTooltipText("ae2fc.tooltip.block_all.hint", "ae2fc.tooltip.block_item.hint", "ae2fc.tooltip.block_fluid.hint")
                .setCycleTitleHoverTooltipText("ae2fc.tooltip.block_all", "ae2fc.tooltip.block_item", "ae2fc.tooltip.block_fluid")
                .setCycleTexture(TJGuiTextures.CYCLE_BLOCKING_MODE_EX));
        tab.add(new TJToggleButtonWidget(-18, 166, 16, 16, () -> ((RCIConfigurableObject) duality).r$getConfigManager().getSetting(RCSettings.IntelligentBlocking).ordinal() == 0, superDualInterface::setIntelligentBlocking)
                .setToggleTitleTooltipHoverText("gui.intelligent_blocking.name", "gui.intelligent_blocking.name")
                .setToggleTooltipHoverText("gui.intelligent_blocking.CLOSE.text", "gui.intelligent_blocking.OPEN.text")
                .setToggleTexture(TJGuiTextures.TOGGLE_BLOCKING_MODE)
                .setInvertTexture(true)
                .useToggleTexture(true));
        tab.add(new TJToggleButtonWidget(-18, 184, 16, 16, () -> false, (BooleanConsumer) bool -> TJGuiUtils.changePatternAmount(duality.getPatterns(), patternScrollableSlotGroup, m -> m * 2, () -> TJGuiUtils.updatePatterns(duality.getPatterns())))
                .setToggleTooltipHoverText("gui.pattern_term.auto_fill_pattern.MULTIPLY_2.text", "gui.pattern_term.auto_fill_pattern.MULTIPLY_2.text")
                .setToggleTitleTooltipHoverText("gui.action.MULTIPLY_2.name", "gui.action.MULTIPLY_2.name")
                .setActiveTexture(TJGuiTextures.AE2_MULTIPLY2_BUTTON, TJGuiTextures.AE2_MULTIPLY2_BUTTON));
        tab.add(new TJToggleButtonWidget(-18, 202, 16, 16, () -> false, (BooleanConsumer) bool -> TJGuiUtils.changePatternAmount(duality.getPatterns(), patternScrollableSlotGroup, m -> m / 2, () -> TJGuiUtils.updatePatterns(duality.getPatterns())))
                .setToggleTooltipHoverText("gui.pattern_term.auto_fill_pattern.DIVIDE_2.text", "gui.pattern_term.auto_fill_pattern.DIVIDE_2.text")
                .setToggleTitleTooltipHoverText("gui.action.DIVIDE_2.name", "gui.action.DIVIDE_2.name")
                .setActiveTexture(TJGuiTextures.AE2_DIVIDE2_BUTTON, TJGuiTextures.AE2_DIVIDE2_BUTTON));
        tab.add(new TJToggleButtonWidget(-18, 220, 16, 16, () -> false, (BooleanConsumer) bool -> TJGuiUtils.changePatternAmount(duality.getPatterns(), patternScrollableSlotGroup, m -> m * 3, () -> TJGuiUtils.updatePatterns(duality.getPatterns())))
                .setToggleTooltipHoverText("gui.pattern_term.auto_fill_pattern.MULTIPLY_3.text", "gui.pattern_term.auto_fill_pattern.MULTIPLY_3.text")
                .setToggleTitleTooltipHoverText("gui.action.MULTIPLY_3.name", "gui.action.MULTIPLY_3.name")
                .setActiveTexture(TJGuiTextures.AE2_MULTIPLY3_BUTTON, TJGuiTextures.AE2_MULTIPLY3_BUTTON));
        tab.add(new TJToggleButtonWidget(-18, 238, 16, 16, () -> false, (BooleanConsumer) bool -> TJGuiUtils.changePatternAmount(duality.getPatterns(), patternScrollableSlotGroup, m -> m / 3, () -> TJGuiUtils.updatePatterns(duality.getPatterns())))
                .setToggleTooltipHoverText("gui.pattern_term.auto_fill_pattern.DIVIDE_3.text", "gui.pattern_term.auto_fill_pattern.DIVIDE_3.text")
                .setToggleTitleTooltipHoverText("gui.action.DIVIDE_3.name", "gui.action.DIVIDE_3.name")
                .setActiveTexture(TJGuiTextures.AE2_DIVIDE3_BUTTON, TJGuiTextures.AE2_DIVIDE3_BUTTON));
        tab.add(TJGuiUtils.createItemConfigPanel(superDualInterface, selectionWidgetGroup));
    }

    private static void createFluidInterfaceTab(List<Widget> tab, ISuperDualInterface superDualInterface) {
        final DualitySuperFluidInterface duality = (DualitySuperFluidInterface) superDualInterface.getDualityFluidInterface();
        final IItemHandler upgradeHandler = duality.getInventoryByName("upgrades");
        for (int i = 0; i < duality.getConfig().getSlots(); i++) {
            final int index = i;
            tab.add(new TJPhantomAEFluidSlotWidget(7 + (18 * (i % 9)), 34 + (72 * (i / 9)), 18, 18, i, duality.getConfig(), fluidStack -> ((IDualitySuperFluidInterface) duality).onFluidInventoryHasChanged(duality.getConfig(), index, null, null, null))
                    .setBackgroundTexture(TJGuiTextures.SLOT_DOWN));
        }
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            tab.add(new TJSlotWidget<>(upgradeHandler, i, 186, 7 + (18 * i))
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY)
                    .setSlotProtection(true));
        }
        for (int i = 0; i < duality.getTanks().getSlots(); i++) {
            tab.add(new AEFluidTankWidget((AEFluidInventory) duality.getTanks(), i, 7 + (18 * (i % 9)), 52 + (72 * (i / 9)), 18, 54)
                    .onContentsChanged(slot-> ((IDualitySuperFluidInterface) duality).onFluidInventoryHasChanged(duality.getTanks(), slot, null, null, null))
                    .setBackgroundTextures(GuiTextures.SLOT));
        }
        tab.add(new LabelWidget(7, 181, "gui.appliedenergistics2.StoredFluids"));
        tab.add(new LabelWidget(7, 23, "gui.appliedenergistics2.Config"));
        tab.add(new LabelWidget(7, 198, "container.inventory"));
    }
}
