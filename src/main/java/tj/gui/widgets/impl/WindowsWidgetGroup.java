package tj.gui.widgets.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class WindowsWidgetGroup extends AbstractWidgetGroup {

    private final TextureArea backgroundTexture;
    private boolean dragging;
    private int dragX;
    private int dragY;

    public WindowsWidgetGroup(int x, int y, int width, int height, TextureArea backgroundTexture) {
        super(new Position(x, y), new Size(width, height));
        this.backgroundTexture = backgroundTexture;
    }

    public WindowsWidgetGroup addSubWidget(Widget widget) {
        this.addWidget(widget);
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        this.backgroundTexture.draw(this.getPosition().getX(), this.getPosition().getY(), this.getSize().getWidth(), this.getSize().getHeight());
        super.drawInBackground(mouseX, mouseY, context);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            this.dragging = true;
            this.dragX = mouseX;
            this.dragY = mouseY;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (this.dragging)
            this.setSelfPosition(new Position(mouseX - this.dragX, mouseY - this.dragY));
        return super.mouseDragged(mouseX, mouseY, button, timeDragged);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        this.dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
