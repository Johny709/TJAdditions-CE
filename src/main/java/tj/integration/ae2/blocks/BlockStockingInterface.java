package tj.integration.ae2.blocks;

import appeng.api.config.Settings;
import appeng.block.misc.BlockInterface;
import appeng.core.Api;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.LabelWidget;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.integration.ae2.ISuperInterface;
import tj.integration.ae2.helpers.DualitySuperInterface;
import tj.integration.ae2.tile.TileStockingInterface;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.TJModularUI;
import tj.mui.widgets.impl.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockStockingInterface extends BlockInterface {

    public static final DualitySuperInterface DUALITY_INSTANCE = (DualitySuperInterface) new TileStockingInterface().getInterfaceDuality();

    public BlockStockingInterface() {
        this.setTileEntity(TileStockingInterface.class);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack is, World world, List<String> lines, ITooltipFlag advancedItemTooltips) {
        if (DUALITY_INSTANCE != null) {
            lines.add(I18n.format("tile.me.super_interface.storage_slots", DUALITY_INSTANCE.getStorage().getSlots()));
            lines.add(I18n.format("tile.me.super_interface.upgrade_slots", DUALITY_INSTANCE.getInventoryByName("upgrades").getSlots()));
        }
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean onActivated(World world, BlockPos pos, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        final TileStockingInterface superInterface = this.getTileEntity(world, pos);
        if (superInterface != null) {
            if (!world.isRemote) {
                superInterface.openUI(player, superInterface);
            }
            return true;
        }
        return true;
    }

    public static ModularUI createInterfaceGUI(TileEntityHolder holder, EntityPlayer player, ISuperInterface superInterface) {
        final DualitySuperInterface duality = (DualitySuperInterface) superInterface.getInterfaceDuality();
        final DualitySuperInterface.DualityUpgradeInventory upgradeHandler = (DualitySuperInterface.DualityUpgradeInventory) duality.getInventoryByName("upgrades");
        final SelectionWidgetGroup selectionWidgetGroup = new SelectionWidgetGroup(0, 0, 0, 0);
        final TJModularUI ui = TJModularUI.builder(holder, player)
                .image(179, 0, 32, 194, GuiTextures.BORDERED_BACKGROUND);
        for (int i = 0; i < duality.getConfig().getSlots(); i++) {
            ui.widget(new TJPhantomItemSlotWidget(7 + (18 * (i % 9)), 34 + (36 * (i / 9)), 18, 18, i, duality.getConfig())
                    .setBackgroundTextures(TJGuiTextures.SLOT_DOWN));
            selectionWidgetGroup.addSelectionBox(i, 7 + (18 * (i % 9)), 34 + (36 * (i / 9)), 18, 18);
        }
        ui.widget(new LabelWidget(7, 181, "gui.appliedenergistics2.StoredItems"))
                .widget(new LabelWidget(7, 23, "gui.appliedenergistics2.Config"));
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            ui.widget(new TJSlotWidget<>(upgradeHandler, i, 186, 7 + (18 * i))
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY)
                    .setSlotProtection(true));
        }
        for (int i = 0; i < duality.getStorage().getSlots(); i++) {
            ui.widget(new TJSlotWidget<>(duality.getStorage(), i, 7 + (18 * (i % 9)), 52 + (36 * (i / 9)))
                    .setInactiveBackgroundTexture(GuiTextures.SLOT)
                    .setActiveBackgroundTexture(GuiTextures.SLOT));
        }
        final PanelWidgetGroup panelWidgetGroup = TJGuiUtils.createPriorityPanel(superInterface);
        final PanelWidgetGroup panelWidgetGroup1 = TJGuiUtils.createTickPanel(superInterface);
        return ui.setWidthHeight(176, 292)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(superInterface.getItemStackRepresentation()).setLocale(superInterface.getItemStackRepresentation().getDisplayName()))
                .widget(new TJLabelWidget(4, 0, 162, 18, null)
                        .setDynamicLocale(superInterface::getCustomInventoryName)
                        .setCentered(false)
                        .setCanSlide(false))
                .widget(new LabelWidget(7, 181, "gui.appliedenergistics2.StoredItems"))
                .widget(new LabelWidget(7, 23, "gui.appliedenergistics2.Config"))
                .widget(new TJToggleButtonWidget(-18, 35, 16, 16, TJGuiTextures.TOGGLE_AUTO_PULL, () -> duality.getConfigManager().getSetting(Settings.BLOCK).ordinal() == 0, superInterface::setItemAutoPull)
                        .setToggleTooltipHoverText("tile.me.stocking_interface.auto_pull", "tile.me.stocking_interface.auto_pull"))
                .widget(new TJToggleButtonWidget(-18, 53, 16, 16, TJGuiTextures.TOGGLE_BLOCKING_MODE, () -> duality.getConfigManager().getSetting(Settings.STICKY_MODE).ordinal() == 0, superInterface::setItemAutoPush)
                        .setToggleTooltipHoverText("tile.me.stocking_interface.auto_push", "tile.me.stocking_interface.auto_push")
                        .setInvertTexture(true))
                .widget(selectionWidgetGroup)
                .widget(new ButtonWidget<>(154, 0, 22, 22)
                        .setItemDisplay(Api.INSTANCE.definitions().items().certusQuartzWrench().maybeStack(1).orElse(ItemStack.EMPTY))
                        .setBackgroundTextures(TJGuiTextures.INTERFACE_SETTINGS_EDGE_RIGHT)
                        .setTitleHoverTooltipText("gui.appliedenergistics2.Priority")
                        .setButtonResponder(s -> {
                            if (panelWidgetGroup.isPanelOpen()) {
                                panelWidgetGroup.closePanel();
                            } else panelWidgetGroup.openPanel();
                        }))
                .widget(new ButtonWidget<>(132, 0, 22, 22)
                        .setBackgroundTextures(TJGuiTextures.INTERFACE_SETTINGS_LEFT)
                        .setHoverTooltipText("machine.universal.ticks.operation")
                        .setItemDisplay(new ItemStack(Items.CLOCK))
                        .setButtonResponder(s -> {
                            if (panelWidgetGroup1.isPanelOpen()) {
                                panelWidgetGroup1.closePanel();
                            } else panelWidgetGroup1.openPanel();
                        }))
                .widget(panelWidgetGroup)
                .widget(panelWidgetGroup1)
                .widget(TJGuiUtils.createItemConfigPanel(superInterface, selectionWidgetGroup))
                .build();
    }
}
