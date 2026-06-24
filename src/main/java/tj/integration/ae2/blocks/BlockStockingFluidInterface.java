package tj.integration.ae2.blocks;

import appeng.api.config.Settings;
import appeng.core.Api;
import appeng.fluids.block.BlockFluidInterface;
import appeng.fluids.util.AEFluidInventory;
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
import net.minecraftforge.items.IItemHandler;
import tj.integration.ae2.ISuperFluidInterface;
import tj.integration.ae2.helpers.DualitySuperFluidInterface;
import tj.integration.ae2.helpers.IDualitySuperFluidInterface;
import tj.integration.ae2.tile.TileStockingFluidInterface;
import tj.mui.TJGuiTextures;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.impl.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Pattern;

public class BlockStockingFluidInterface extends BlockFluidInterface {

    public BlockStockingFluidInterface() {
        this.setTileEntity(TileStockingFluidInterface.class);
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
        final TileStockingFluidInterface superInterface = this.getTileEntity(world, pos);
        if (superInterface != null) {
            if (!world.isRemote) {
                superInterface.openUI(player, superInterface);
            }
            return true;
        }
        return false;
    }

    public static ModularUI createFluidInterfaceGUI(TileEntityHolder holder, EntityPlayer player, ISuperFluidInterface superFluidInterface) {
        final DualitySuperFluidInterface duality = (DualitySuperFluidInterface) superFluidInterface.getDualityFluidInterface();
        final ButtonPopUpWidget<?> buttonPopUpWidget = new ButtonPopUpWidget<>();
        final ButtonPopUpWidget<?> buttonPopUpTickWidget = new ButtonPopUpWidget<>();
        final IItemHandler upgradeHandler = duality.getInventoryByName("upgrades");
        final ModularUI.Builder builder = ModularUI.builder(TJGuiTextures.SUPER_INTERFACE, 211, 292);
        for (int i = 0; i < duality.getConfig().getSlots(); i++) {
            final int index = i;
            builder.widget(new TJPhantomAEFluidSlotWidget(7 + (18 * (i % 9)), 34 + (36 * (i / 9)), 18, 18, i, duality.getConfig(), fluidStack -> ((IDualitySuperFluidInterface) duality).onFluidInventoryHasChanged(duality.getConfig(), index, null, null, null))
                    .setBackgroundTexture(TJGuiTextures.SLOT_DOWN));
        }
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            builder.widget(new TJSlotWidget<>(upgradeHandler, i, 186, 7 + (18 * i))
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY));
        }
        for (int i = 0; i < duality.getTanks().getSlots(); i++) {
            builder.widget(new AEFluidTankWidget((AEFluidInventory) duality.getTanks(), i, 7 + (18 * (i % 9)), 52 + (36 * (i / 9)), 18, 18)
                    .setActiveSupplier(() -> buttonPopUpWidget.getIndex() == 0 && buttonPopUpTickWidget.getIndex() == 0)
                    .setBackgroundTextures(GuiTextures.SLOT));
        }
        return builder.widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(superFluidInterface.getItemStackRepresentation()).setLocale(superFluidInterface.getItemStackRepresentation().getDisplayName()))
                .widget(new TJLabelWidget(4, 0, 162, 18, null)
                        .setDynamicLocale(superFluidInterface::getCustomInventoryName)
                        .setCentered(false)
                        .setCanSlide(false))
                .widget(new LabelWidget(7, 181, "gui.appliedenergistics2.StoredFluids"))
                .widget(new LabelWidget(7, 23, "gui.appliedenergistics2.Config"))
                .widget(new TJToggleButtonWidget(-18, 35, 16, 16, () -> duality.getConfigManager().getSetting(Settings.BLOCK).ordinal() == 0, superFluidInterface::setAutoPull)
                        .setToggleTooltipHoverText("tile.me.stocking_fluid_interface.auto_pull", "tile.me.stocking_fluid_interface.auto_pull")
                        .setToggleTexture(TJGuiTextures.TOGGLE_AUTO_PULL)
                        .useToggleTexture(true))
                .widget(buttonPopUpTickWidget.addPopup(widgetGroup -> true)
                        .addPopup(new ButtonWidget<>(132, 0, 22, 22)
                                .setBackgroundTextures(TJGuiTextures.INTERFACE_SETTINGS_LEFT)
                                .setHoverTooltipText("machine.universal.ticks.operation")
                                .setItemDisplay(new ItemStack(Items.CLOCK)), widgetGroup -> {
                            widgetGroup.addWidget(new ImageWidget(7, 107, 162, 100, GuiTextures.BORDERED_BACKGROUND));
                            widgetGroup.addWidget(new LabelWidget(14, 112, "machine.universal.ticks.operation"));
                            widgetGroup.addWidget(new NewTextFieldWidget<>(14, 153, 148, 18, true, () -> String.valueOf(superFluidInterface.getTickTime()), superFluidInterface::setTickTime)
                                    .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new ButtonWidget<>(15, 127, 25, 20, "+1", data -> superFluidInterface.setTickTime(String.valueOf((long) superFluidInterface.getTickTime() + 1), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(45, 127, 30, 20, "+10", data -> superFluidInterface.setTickTime(String.valueOf((long) superFluidInterface.getTickTime() + 10), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(80, 127, 35, 20, "+100", data -> superFluidInterface.setTickTime(String.valueOf((long) superFluidInterface.getTickTime() + 100), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(120, 127, 40, 20, "+1000", data -> superFluidInterface.setTickTime(String.valueOf((long) superFluidInterface.getTickTime() + 1000), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(15, 177, 25, 20, "-1", data -> superFluidInterface.setTickTime(String.valueOf((long) superFluidInterface.getTickTime() - 1), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(45, 177, 30, 20, "-10", data -> superFluidInterface.setTickTime(String.valueOf((long) superFluidInterface.getTickTime() - 10), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(80, 177, 35, 20, "-100", data -> superFluidInterface.setTickTime(String.valueOf((long) superFluidInterface.getTickTime() - 100), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(120, 177, 40, 20, "-1000", data -> superFluidInterface.setTickTime(String.valueOf((long) superFluidInterface.getTickTime() - 1000), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            return false;
                        })).widget(buttonPopUpWidget.addPopup(widgetGroup -> true)
                        .addPopup(new ButtonWidget<>(154, 0, 22, 22)
                                .setItemDisplay(Api.INSTANCE.definitions().items().certusQuartzWrench().maybeStack(1).orElse(ItemStack.EMPTY))
                                .setBackgroundTextures(TJGuiTextures.INTERFACE_SETTINGS_EDGE_RIGHT)
                                .setTitleHoverTooltipText("gui.appliedenergistics2.Priority"), widgetGroup -> {
                            widgetGroup.addWidget(new ImageWidget(7, 107, 162, 100, GuiTextures.BORDERED_BACKGROUND));
                            widgetGroup.addWidget(new LabelWidget(14, 112, "gui.appliedenergistics2.Priority"));
                            widgetGroup.addWidget(new NewTextFieldWidget<>(14, 153, 148, 18, true, () -> String.valueOf(duality.getPriority()), superFluidInterface::setPriority)
                                    .setValidator(str -> Pattern.compile("-*?[0-9_]*\\*?").matcher(str).matches())
                                    .setUpdateOnTyping(true));
                            widgetGroup.addWidget(new ButtonWidget<>(15, 127, 25, 20, "+1", data -> superFluidInterface.setPriority(String.valueOf((long) duality.getPriority() + 1), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(45, 127, 30, 20, "+10", data -> superFluidInterface.setPriority(String.valueOf((long) duality.getPriority() + 10), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(80, 127, 35, 20, "+100", data -> superFluidInterface.setPriority(String.valueOf((long) duality.getPriority() + 100), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(120, 127, 40, 20, "+1000", data -> superFluidInterface.setPriority(String.valueOf((long) duality.getPriority() + 1000), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(15, 177, 25, 20, "-1", data -> superFluidInterface.setPriority(String.valueOf((long) duality.getPriority() - 1), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(45, 177, 30, 20, "-10", data -> superFluidInterface.setPriority(String.valueOf((long) duality.getPriority() - 10), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(80, 177, 35, 20, "-100", data -> superFluidInterface.setPriority(String.valueOf((long) duality.getPriority() - 100), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            widgetGroup.addWidget(new ButtonWidget<>(120, 177, 40, 20, "-1000", data -> superFluidInterface.setPriority(String.valueOf((long) duality.getPriority() - 1000), "")).setBackgroundTextures(GuiTextures.VANILLA_BUTTON));
                            return false;
                        }))
                .bindPlayerInventory(player.inventory, 209)
                .build(holder, player);
    }
}
