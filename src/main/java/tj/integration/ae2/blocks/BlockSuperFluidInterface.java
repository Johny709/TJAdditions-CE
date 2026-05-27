package tj.integration.ae2.blocks;

import appeng.api.util.AEPartLocation;
import appeng.core.sync.GuiWrapper;
import appeng.fluids.block.BlockFluidInterface;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import tj.TJ;
import tj.integration.ae2.tile.TileSuperFluidInterface;
import tj.rendering.IItemMeshing;

import javax.annotation.Nullable;

public class BlockSuperFluidInterface extends BlockFluidInterface implements IItemMeshing {

    public BlockSuperFluidInterface() {
        this.setTileEntity(TileSuperFluidInterface.class);
    }

    @Override
    public boolean onActivated(World world, BlockPos pos, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        final TileSuperFluidInterface superInterface = this.getTileEntity(world, pos);
        if (superInterface != null) {
            if (!world.isRemote) {
                Platform.openGUI(player, superInterface, AEPartLocation.fromFacing(side), GuiWrapper.INSTANCE.wrap(() -> new ResourceLocation(TJ.MODID, "me.super_fluid_interface")));
            }
            return true;
        }
        return false;
    }
}
