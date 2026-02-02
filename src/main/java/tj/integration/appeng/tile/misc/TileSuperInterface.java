package tj.integration.appeng.tile.misc;

import appeng.tile.misc.TileInterface;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.integration.appeng.helpers.SuperDualityInterface;


public class TileSuperInterface extends TileInterface {

    public TileSuperInterface() {
        ObfuscationReflectionHelper.setPrivateValue(TileInterface.class, this, new SuperDualityInterface(this.getProxy(), this), "duality");
    }
}
