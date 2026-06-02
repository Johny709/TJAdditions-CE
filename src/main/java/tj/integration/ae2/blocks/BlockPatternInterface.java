package tj.integration.ae2.blocks;

import appeng.block.misc.BlockInterface;
import tj.integration.ae2.tile.TilePatternInterface;

public class BlockPatternInterface extends BlockInterface {

    public BlockPatternInterface() {
        this.setTileEntity(TilePatternInterface.class);
    }
}
