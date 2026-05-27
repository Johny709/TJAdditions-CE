package tj.integration.ae2.tile;

import appeng.fluids.tile.TileFluidInterface;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.gui.uifactory.TileEntityHolder;
import tj.integration.ae2.helpers.SuperDualityFluidInterface;

public class TileSuperFluidInterface extends TileFluidInterface {

    public TileSuperFluidInterface() {
        ObfuscationReflectionHelper.setPrivateValue(TileFluidInterface.class, this, new SuperDualityFluidInterface(this.getProxy(), this), "duality");
    }

    public void openUI(EntityPlayer player, TileEntity tileEntity) {
        this.openUI(player, tileEntity, null);
    }

    public void openUI(EntityPlayer player, TileEntity tileEntity, EnumFacing facing) {
        TileEntityHolder holder = new TileEntityHolder(tileEntity);
        holder.setFacing(facing);
        holder.openUI((EntityPlayerMP) player);
    }
}
