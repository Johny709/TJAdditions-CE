package tj.integration.ae2.tile.misc;

import appeng.tile.misc.TileInterface;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.integration.ae2.helpers.TJDualityInterface;


public class TJTileInterface extends TileInterface {

    public TJTileInterface() {
        ObfuscationReflectionHelper.setPrivateValue(TileInterface.class, this, new TJDualityInterface(this.getProxy(), this), "duality");
    }
}
