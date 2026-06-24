package tj.integration.ae2.tile;

import appeng.fluids.tile.TileFluidInterface;
import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.blocks.block.TJBlocks;
import tj.integration.ae2.ISuperFluidInterface;
import tj.integration.ae2.blocks.BlockSuperFluidInterface;
import tj.mui.uifactory.ITileEntityUI;
import tj.mui.uifactory.TileEntityHolder;
import tj.integration.ae2.helpers.DualitySuperFluidInterface;


public class TileSuperFluidInterface extends TileFluidInterface implements ITileEntityUI, ISuperFluidInterface {

    public TileSuperFluidInterface() {
        ObfuscationReflectionHelper.setPrivateValue(TileFluidInterface.class, this, new DualitySuperFluidInterface(this.getProxy(), this, 18), "duality");
    }

    public void openUI(EntityPlayer player, TileEntity tileEntity) {
        this.openUI(player, tileEntity, null);
    }

    public void openUI(EntityPlayer player, TileEntity tileEntity, EnumFacing facing) {
        TileEntityHolder holder = new TileEntityHolder(tileEntity);
        holder.setFacing(facing);
        holder.openUI((EntityPlayerMP) player);
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return TJBlocks.SUPER_FLUID_INTERFACE.maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Override
    public ModularUI createUI(TileEntityHolder holder, EntityPlayer player) {
        return BlockSuperFluidInterface.createFluidInterfaceGUI(holder, player, this);
    }

    @Override
    public void setPriority(String text, String id) {
        this.getDualityFluidInterface().setPriority((int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, Long.parseLong(text))));
        this.markDirty();
    }

    @Override
    public void setAutoPull(boolean autoPull) {
        // No such features
    }
}
