package tj.integration.ae2.blocks;

import appeng.fluids.block.BlockFluidInterface;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tj.integration.ae2.tile.TileStockingFluidInterface;

import javax.annotation.Nullable;

public class BlockStockingFluidInterface extends BlockFluidInterface {

    public BlockStockingFluidInterface() {
        this.setTileEntity(TileStockingFluidInterface.class);
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
}
