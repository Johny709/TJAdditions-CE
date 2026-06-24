package tj.integration.ae2.blocks;

import appeng.api.config.Settings;
import appeng.block.misc.BlockInterface;
import appeng.core.Api;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import tj.integration.ae2.ISuperInterface;
import tj.integration.ae2.helpers.DualitySuperInterface;
import tj.integration.ae2.tile.TileStockingInterface;
import tj.mui.TJGuiTextures;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.impl.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Pattern;

public class BlockStockingInterface extends BlockInterface {

    public BlockStockingInterface() {
        this.setTileEntity(TileStockingInterface.class);
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
        final ButtonPopUpWidget<?> buttonPopUpWidget = new ButtonPopUpWidget<>();
        final ButtonPopUpWidget<?> buttonPopUpTickWidget = new ButtonPopUpWidget<>();
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
        builder.widget(new LabelWidget(7, 181, "gui.appliedenergistics2.StoredItems"))
                .widget(new LabelWidget(7, 23, "gui.appliedenergistics2.Config"));
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            builder.widget(new TJSlotWidget<>(upgradeHandler, i, 186, 7 + (18 * i))
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY));
        }
        for (int i = 0; i < duality.getStorage().getSlots(); i++) {
            builder.widget(new TJSlotWidget<>(duality.getStorage(), i, 7 + (18 * (i % 9)), 52 + (36 * (i / 9)))
                    .setActiveSupplier(() -> buttonPopUpWidget.getIndex() == 0 && buttonPopUpTickWidget.getIndex() == 0)
                    .setInactiveBackgroundTexture(GuiTextures.SLOT)
                    .setActiveBackgroundTexture(GuiTextures.SLOT));
        }
        return builder.widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(superInterface.getItemStackRepresentation()).setLocale(superInterface.getItemStackRepresentation().getDisplayName()))
                .widget(new TJLabelWidget(4, 0, 162, 18, null)
                        .setDynamicLocale(superInterface::getCustomInventoryName)
                        .setCentered(false)
                        .setCanSlide(false))
                .widget(new LabelWidget(7, 181, "gui.appliedenergistics2.StoredItems"))
                .widget(new LabelWidget(7, 23, "gui.appliedenergistics2.Config"))
                .widget(new TJToggleButtonWidget(-18, 35, 16, 16, () -> duality.getConfigManager().getSetting(Settings.BLOCK).ordinal() == 0, superInterface::setAutoPull)
                        .setToggleTooltipHoverText("tile.me.stocking_interface.auto_pull", "tile.me.stocking_interface.auto_pull")
                        .setToggleTexture(TJGuiTextures.TOGGLE_AUTO_PULL)
                        .useToggleTexture(true))
                .widget(selectionWidgetGroup)
                .widget(buttonPopUpTickWidget.addPopup(widgetGroup -> true)
                        .addPopup(new ButtonWidget<>(132, 0, 22, 22)
                                .setBackgroundTextures(TJGuiTextures.INTERFACE_SETTINGS_LEFT)
                                .setHoverTooltipText("machine.universal.ticks.operation")
                                .setItemDisplay(new ItemStack(Items.CLOCK)), widgetGroup -> {
                            widgetGroup.addWidget(new ImageWidget(7, 107, 162, 100, GuiTextures.BORDERED_BACKGROUND));
                            widgetGroup.addWidget(new LabelWidget(14, 112, "machine.universal.ticks.operation"));
                            widgetGroup.addWidget(new NewTextFieldWidget<>(14, 153, 148, 18, true, () -> String.valueOf(superInterface.getTickTime()), superInterface::setTickTime)
                                    .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new ButtonWidget<>(15, 127, 25, 20, "+1", data -> superInterface.setTickTime(String.valueOf((long) superInterface.getTickTime() + 1), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(45, 127, 30, 20, "+10", data -> superInterface.setTickTime(String.valueOf((long) superInterface.getTickTime() + 10), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(80, 127, 35, 20, "+100", data -> superInterface.setTickTime(String.valueOf((long) superInterface.getTickTime() + 100), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(120, 127, 40, 20, "+1000", data -> superInterface.setTickTime(String.valueOf((long) superInterface.getTickTime() + 1000), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(15, 177, 25, 20, "-1", data -> superInterface.setTickTime(String.valueOf((long) superInterface.getTickTime() - 1), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(45, 177, 30, 20, "-10", data -> superInterface.setTickTime(String.valueOf((long) superInterface.getTickTime() - 10), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(80, 177, 35, 20, "-100", data -> superInterface.setTickTime(String.valueOf((long) superInterface.getTickTime() - 100), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(120, 177, 40, 20, "-1000", data -> superInterface.setTickTime(String.valueOf((long) superInterface.getTickTime() - 1000), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            return false;
                        })).widget(buttonPopUpWidget.addPopup(widgetGroup -> true)
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
                .bindPlayerInventory(player.inventory, 209)
                .build(holder, player);
    }
}
