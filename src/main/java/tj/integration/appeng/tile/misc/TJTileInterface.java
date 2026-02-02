package tj.integration.appeng.tile.misc;

import appeng.tile.misc.TileInterface;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.integration.appeng.helpers.TJDualityInterface;


public class TJTileInterface extends TileInterface {

    public TJTileInterface() {
        ObfuscationReflectionHelper.setPrivateValue(TileInterface.class, this, new TJDualityInterface(this.getProxy(), this), "duality");
    }
}
