package tj.gui.uifactory;

import gregtech.api.gui.ModularUI;
import net.minecraft.entity.player.EntityPlayer;

public interface IPlayerUI {

    ModularUI createUI(PlayerHolder holder, EntityPlayer player);
}
