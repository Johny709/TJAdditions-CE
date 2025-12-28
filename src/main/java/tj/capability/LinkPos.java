package tj.capability;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface LinkPos extends IPageCapable {

    default boolean isInterDimensional() {
        return false;
    }

    default int dimensionID() {
        return 0;
    }

    default int getDimension(int index) {
        return 0;
    }

    int getRange();

    int getPosSize();

    default BlockPos getPos(int index) {
        return null;
    }

    default void setPos(String name, BlockPos pos, EntityPlayer player, World world, int index) {}

    World world();

    NBTTagCompound getLinkData();

    void setLinkData(NBTTagCompound linkData);
}
