package tj.gui.uifactory;

import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TileEntityHolder implements IUIHolder {

    private final TileEntity tileEntity;
    private EnumFacing facing;

    public TileEntityHolder(TileEntity tileEntity) {
        this.tileEntity = tileEntity;
    }

    public void setFacing(EnumFacing facing) {
        this.facing = facing;
    }

    protected ModularUI createUI(EntityPlayer player) {
        ITileEntityUI factory = (ITileEntityUI) this.tileEntity;
        return factory.createUI(this, player);
    }

    public void openUI(EntityPlayerMP player) {
        TileEntityUIFactory.INSTANCE.openUI(this, player);
    }

    public EnumFacing getFacing() {
        return this.facing;
    }

    public TileEntity getTileEntity() {
        return this.tileEntity;
    }

    @Override
    public boolean isValid() {
        return !this.tileEntity.isInvalid();
    }

    @Override
    public boolean isRemote() {
        return this.tileEntity.getWorld().isRemote;
    }

    @Override
    public void markAsDirty() {
        this.tileEntity.markDirty();
    }
}
