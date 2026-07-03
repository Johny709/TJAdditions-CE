package tj.integration.ae2.part;

import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.tile.networking.TileCableBus;
import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import tj.items.item.TJItems;
import tj.mui.TJGuiTextures;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.impl.AEItemListWidget;
import tj.mui.widgets.impl.TJLabelWidget;

public class PartSuperInterfaceTerminal extends PartInterfaceTerminal implements ITileEntityUI {

    public PartSuperInterfaceTerminal(ItemStack is) {
        super(is);
    }

    @Override
    public boolean onPartActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        final TileCableBus tileCableBus = (TileCableBus) this.getTile();
        if (tileCableBus != null) {
            if (!player.getEntityWorld().isRemote) {
                TileEntityHolder holder = new TileEntityHolder(tileCableBus);
                holder.setFacing(this.getSide().getFacing());
                holder.openUI((EntityPlayerMP) player);
            }
            return true;
        }
        return true;
    }

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        return ModularUI.builder(TJGuiTextures.SUPER_INTERFACE, 176, 292)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2)
                        .setItemLabel(TJItems.PART_SUPER_INTERFACE_TERMINAL.maybeStack(1).orElse(ItemStack.EMPTY))
                        .setLocale("item.me.part.super_interface_terminal.name"))
                .widget(new TJLabelWidget(4, 0, 162, 18, null)
                        .setDynamicLocale(this::getCustomInventoryName)
                        .setCentered(false)
                        .setCanSlide(false))
                .widget(new AEItemListWidget(7, 34, 166, 162, this.getGridNode() != null ? this.getGridNode().getGrid() : null))
                .bindPlayerInventory(player.inventory, 209)
                .build(holder, player);
    }
}
