package tj.integration.ae2.blocks;

import appeng.core.Api;
import appeng.fluids.block.BlockFluidInterface;
import appeng.fluids.util.AEFluidInventory;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.LabelWidget;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import tj.integration.ae2.ISuperFluidInterface;
import tj.integration.ae2.helpers.DualitySuperFluidInterface;
import tj.integration.ae2.helpers.IDualitySuperFluidInterface;
import tj.integration.ae2.tile.TileSuperFluidInterface;
import tj.mui.TJGuiTextures;
import tj.mui.TJGuiUtils;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.TJModularUI;
import tj.mui.widgets.impl.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockSuperFluidInterface extends BlockFluidInterface {

    public static final DualitySuperFluidInterface FLUID_DUALITY_INSTANCE = (DualitySuperFluidInterface) new TileSuperFluidInterface().getDualityFluidInterface();

    public BlockSuperFluidInterface() {
        this.setTileEntity(TileSuperFluidInterface.class);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack is, World world, List<String> lines, ITooltipFlag advancedItemTooltips) {
        if (FLUID_DUALITY_INSTANCE != null) {
            lines.add(I18n.format("tile.me.super_fluid_interface.fluid_tanks", FLUID_DUALITY_INSTANCE.getTanks().getSlots()));
            lines.add(I18n.format("tile.me.super_fluid_interface.upgrade_slots", FLUID_DUALITY_INSTANCE.getInventoryByName("upgrades").getSlots()));
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
        final TileSuperFluidInterface superInterface = this.getTileEntity(world, pos);
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
        final IItemHandler upgradeHandler = duality.getInventoryByName("upgrades");
        final TJModularUI ui = TJModularUI.builder(holder, player)
                .image(179, 0, 32, 194, GuiTextures.BORDERED_BACKGROUND);
        for (int i = 0; i < duality.getConfig().getSlots(); i++) {
            final int index = i;
            ui.widget(new TJPhantomAEFluidSlotWidget(7 + (18 * (i % 9)), 34 + (72 * (i / 9)), 18, 18, i, duality.getConfig(), fluidStack -> ((IDualitySuperFluidInterface) duality).onFluidInventoryHasChanged(duality.getConfig(), index, null, null, null))
                    .setBackgroundTexture(TJGuiTextures.SLOT_DOWN));
        }
        for (int i = 0; i < duality.getTanks().getSlots(); i++) {
            ui.widget(new AEFluidTankWidget((AEFluidInventory) duality.getTanks(), i, 7 + (18 * (i % 9)), 52 + (72 * (i / 9)), 18, 54)
                    .onContentsChanged(slot -> ((IDualitySuperFluidInterface) duality).onFluidInventoryHasChanged(duality.getTanks(), slot, null, null, null))
                    .setBackgroundTextures(GuiTextures.SLOT));
        }
        for (int i = 0; i < upgradeHandler.getSlots(); i++) {
            ui.widget(new TJSlotWidget<>(upgradeHandler, i, 186, 7 + (18 * i))
                    .setActiveBackgroundTexture(GuiTextures.SLOT, TJGuiTextures.UPGRADE_OVERLAY)
                    .setSlotProtection(true));
        }
        final PanelWidgetGroup panelWidgetGroup = TJGuiUtils.createPriorityPanel(superFluidInterface);
        return ui.setWidthHeight(176, 292)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(superFluidInterface.getItemStackRepresentation()).setLocale(superFluidInterface.getItemStackRepresentation().getDisplayName()))
                .widget(new TJLabelWidget(4, 0, 162, 18, null)
                        .setDynamicLocale(superFluidInterface::getCustomInventoryName)
                        .setCentered(false)
                        .setCanSlide(false))
                .widget(new LabelWidget(7, 181, "gui.appliedenergistics2.StoredFluids"))
                .widget(new LabelWidget(7, 23, "gui.appliedenergistics2.Config"))
                .widget(new LabelWidget(7, 198, "container.inventory"))
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
                .build();
    }
}
