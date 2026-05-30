package tj.integration.ae2.blocks;

import appeng.block.misc.BlockInterface;
import tj.integration.ae2.tile.TileSuperDualInterface;
import tj.rendering.IItemMeshing;

public class BlockSuperDualInterface extends BlockInterface implements IItemMeshing {

    public BlockSuperDualInterface() {
        this.setTileEntity(TileSuperDualInterface.class);
    }
}
