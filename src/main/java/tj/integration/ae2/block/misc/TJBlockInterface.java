package tj.integration.ae2.block.misc;

import appeng.api.util.AEPartLocation;
import appeng.block.misc.BlockInterface;
import appeng.tile.misc.TileInterface;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tj.TJValues;

import javax.annotation.Nullable;

public class TJBlockInterface extends BlockInterface {

    @Override
    public boolean onActivated(final World w, final BlockPos pos, final EntityPlayer p, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (p.isSneaking()) {
            return false;
        }

        final TileInterface tg = this.getTileEntity(w, pos);
        if (tg != null) {
            if (Platform.isServer()) {
                Platform.openGUI(p, tg, AEPartLocation.fromFacing(side), TJValues.GUI_SUPER_INTERFACE);
            }
            return true;
        }
        return false;
    }
}
