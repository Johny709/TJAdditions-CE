package tj.integration.appeng.block.misc;

import appeng.api.util.AEPartLocation;
import appeng.block.misc.BlockInterface;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tj.gui.TJGuiUtils;
import tj.integration.appeng.core.sync.TJGuiBridge;
import tj.integration.appeng.tile.misc.TileSuperInterface;

import javax.annotation.Nullable;

public class BlockSuperInterface extends BlockInterface {

    @Override
    public boolean onActivated(final World w, final BlockPos pos, final EntityPlayer p, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (p.isSneaking()) {
            return false;
        }

        final TileSuperInterface tg = this.getTileEntity(w, pos);
        if (tg != null) {
            if (!w.isRemote) {
                TJGuiUtils.openAEGui(p, tg, AEPartLocation.fromFacing(side), TJGuiBridge.GUI_SUPER_INTERFACE);
            }
            return true;
        }
        return false;
    }
}
