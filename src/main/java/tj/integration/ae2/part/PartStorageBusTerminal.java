package tj.integration.ae2.part;

import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.tile.networking.TileCableBus;
import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import tj.integration.ae2.items.ItemWirelessStorageBusTerminal;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;


public class PartStorageBusTerminal extends PartInterfaceTerminal implements ITileEntityUI {

    public PartStorageBusTerminal(ItemStack is) {
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
        return ItemWirelessStorageBusTerminal.createWirelessStorageBusTerminalGUI(holder, player, this.getGridNode());
    }
}
