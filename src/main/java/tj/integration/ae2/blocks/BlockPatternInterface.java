package tj.integration.ae2.blocks;

import appeng.block.misc.BlockInterface;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tj.TJ;
import tj.gui.GuiHandler;
import tj.integration.ae2.tile.TilePatternInterface;

import javax.annotation.Nullable;

public class BlockPatternInterface extends BlockInterface {

    public BlockPatternInterface() {
        this.setTileEntity(TilePatternInterface.class);
    }

    @Override
    public boolean onActivated(World world, BlockPos pos, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (player.isSneaking())
            return false;
        if (!world.isRemote)
            player.openGui(TJ.INSTANCE, GuiHandler.PATTERN_INTERFACE, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }
}
