package tj.gui.widgets;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IRecipeClickArea {

    @SideOnly(Side.CLIENT)
    String getRecipeUid(int mouseX, int mouseY, int mouseButton);
}
