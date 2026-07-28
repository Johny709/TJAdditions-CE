package tj.integration.ae2.part;

import appeng.parts.reporting.PartInterfaceTerminal;
import appeng.tile.networking.TileCableBus;
import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.FluidStack;
import tj.integration.ae2.ISuperFluidInterfaceTerminal;
import tj.integration.ae2.items.ItemWirelessFluidStorageBusTerminal;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;
import tj.mui.widgets.impl.AEGhostFluidListWidget;

import java.util.function.LongUnaryOperator;

public class PartFluidStorageBusTerminal extends PartInterfaceTerminal implements ITileEntityUI, ISuperFluidInterfaceTerminal {

    public PartFluidStorageBusTerminal(ItemStack is) {
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
        return ItemWirelessFluidStorageBusTerminal.createFluidStorageBusTerminalGUI(holder, player, this.getGridNode(), this);
    }

    @Override
    public void setFluidStackSize(AEGhostFluidListWidget<?> ghostFluidListWidget, LongUnaryOperator unaryOperator) {
        final FluidStack fluidStack = ghostFluidListWidget.getFluidAt(ghostFluidListWidget.getSelectedIndex());
        if (fluidStack == null) return;
        final FluidStack newStack = fluidStack.copy();
        newStack.amount = (int) Math.max(1, Math.min(Integer.MAX_VALUE, unaryOperator.applyAsLong(fluidStack.amount)));
        ghostFluidListWidget.setFluidAt(ghostFluidListWidget.getSelectedIndex(), newStack);
    }

    @Override
    public int getFluidStackSize(AEGhostFluidListWidget<?> ghostFluidListWidget) {
        final FluidStack fluidStack = ghostFluidListWidget.getFluidAt(ghostFluidListWidget.getSelectedIndex());
        return fluidStack != null ? fluidStack.amount : 0;
    }
}
