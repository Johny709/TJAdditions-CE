package tj.capability;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IMuffler {

    boolean isActive();

    @SideOnly(Side.CLIENT)
    void runMufflerEffect(float xPos, float yPos, float zPos, float xSpd, float ySpd, float zSpd);
}
