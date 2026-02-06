package tj.util;

import gregtech.api.util.BlockInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class TJBlockInfo extends BlockInfo {

    private TileEntity[] tileEntities;
    private final IBlockState[] states;

    public TJBlockInfo(IBlockState... states) {
        super(Blocks.AIR);
        this.states = states;
    }

    public TJBlockInfo(IBlockState[] states, TileEntity[] tileEntities) {
        super(Blocks.AIR.getDefaultState(), tileEntities[0]);
        this.states = states;
        this.tileEntities = tileEntities;
    }

    public TileEntity[] getTileEntities() {
        return this.tileEntities;
    }

    public IBlockState[] getStates() {
        return this.states;
    }

    public void apply(World world, BlockPos pos, int index) {
        index = this.checkIndex(this.states.length, index);
        world.setBlockState(pos, this.states[index]);
        if (this.tileEntities[index] != null) {
            world.setTileEntity(pos, this.tileEntities[index]);
        }
    }

    private int checkIndex(int size, int index) {
        return index >= size ? size - 1 : index;
    }
}
