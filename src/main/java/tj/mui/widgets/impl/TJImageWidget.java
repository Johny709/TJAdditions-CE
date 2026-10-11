package tj.mui.widgets.impl;

import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ImageWidget;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * More advanced form of {@link ImageWidget} that works in tangent with {@link tj.mui.widgets.TJWidgetGroup}
 * which can be used to prevent clicking widgets behind it.
 */
public class TJImageWidget extends ImageWidget {

    public TJImageWidget(int xPosition, int yPosition, int width, int height, TextureArea area) {
        super(xPosition, yPosition, width, height, area);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return this.isMouseOverElement(mouseX, mouseY);
    }
}
