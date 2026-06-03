package tj.mui.uifactory;

import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;

public interface ITileEntityUI {

    ModularUI createUI(TileEntityHolder holder, EntityPlayer player);
}
