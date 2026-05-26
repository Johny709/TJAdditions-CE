package tj.rendering;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IHaveModel {

    @SideOnly(Side.CLIENT)
    void initModel();
}
