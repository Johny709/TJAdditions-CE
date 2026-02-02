package tj.integration.ae2.tile.misc;

import appeng.fluids.tile.TileFluidInterface;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.integration.ae2.helpers.TJDualityFluidInterface;


public class TJTileFluidInterface extends TileFluidInterface {

    public TJTileFluidInterface() {
        ObfuscationReflectionHelper.setPrivateValue(TileFluidInterface.class, this, new TJDualityFluidInterface(this.getProxy(), this), "duality");
    }
}
