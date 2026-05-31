package tj.integration.ae2.blocks;

import appeng.block.misc.BlockInterface;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tj.integration.ae2.tile.TileSuperDualInterface;
import tj.rendering.IItemMeshing;

import javax.annotation.Nullable;

public class BlockSuperDualInterface extends BlockInterface implements IItemMeshing {

    public BlockSuperDualInterface() {
        this.setTileEntity(TileSuperDualInterface.class);
    }

    @Override
    public boolean onActivated(final World world, final BlockPos pos, final EntityPlayer player, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        final TileSuperDualInterface superInterface = this.getTileEntity(world, pos);
        if (superInterface != null) {
            if (!world.isRemote) {
                superInterface.openUI(player, superInterface);
            }
            return true;
        }
        return false;
    }
}
