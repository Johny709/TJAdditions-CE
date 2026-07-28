package tj.integration.ae2.part;

import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.tile.networking.TileCableBus;
import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import tj.integration.ae2.ISuperInterfaceTerminal;
import tj.integration.ae2.items.ItemWirelessSuperInterfaceTerminal;
import tj.integration.ae2.tile.*;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.impl.AEGhostItemListWidget;

import java.util.function.LongUnaryOperator;


public class PartSuperInterfaceTerminal extends PartInterfaceTerminal implements ITileEntityUI, ISuperInterfaceTerminal {

    public PartSuperInterfaceTerminal(ItemStack is) {
        super(is);
    }

    @Override
    public boolean onPartActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        final TileCableBus tileCableBus = (TileCableBus) this.getTile();
        if (tileCableBus != null && !player.getEntityWorld().isRemote) {
            TileEntityHolder holder = new TileEntityHolder(tileCableBus);
            holder.setFacing(this.getSide().getFacing());
            holder.openUI((EntityPlayerMP) player);
        }
        return true;
    }

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        return ItemWirelessSuperInterfaceTerminal.createWirelessSuperInterfaceGUI(holder, player, this.getGridNode(), this);
    }

    @Override
    public void setItemStackSize(AEGhostItemListWidget<?> ghostItemListWidget, LongUnaryOperator unaryOperator) {
        final ItemStack itemStack = ghostItemListWidget.getItemAt(ghostItemListWidget.getSelectedIndex());
        if (itemStack.isEmpty()) return;
        final ItemStack newStack = itemStack.copy();
        newStack.setCount((int) Math.max(1, Math.min(Integer.MAX_VALUE, unaryOperator.applyAsLong(itemStack.getCount()))));
        ghostItemListWidget.setItemAt(ghostItemListWidget.getSelectedIndex(), newStack);
    }

    @Override
    public int getItemStackSize(AEGhostItemListWidget<?> ghostItemListWidget) {
        return ghostItemListWidget.getItemAt(ghostItemListWidget.getSelectedIndex(), false).getCount();
    }
}
