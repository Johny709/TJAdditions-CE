package tj.integration.ae2.tile.misc;

import appeng.tile.misc.TileInterface;
import gregtech.api.util.GTLog;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tj.integration.ae2.helpers.TJDualityInterface;

import java.lang.reflect.Field;

public class TJTileInterface extends TileInterface {

    public TJTileInterface() {
        Field duality = ObfuscationReflectionHelper.findField(TileInterface.class, "duality");
        try {
            duality.set(this, new TJDualityInterface(this.getProxy(), this));
        } catch (IllegalAccessException e) {
            GTLog.logger.error("Error when trying to reflect on class {} for field duality", TileInterface.class.getName());
        }
    }
}
