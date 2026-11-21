package tj.mui.widgets.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.mui.widgets.TJWidgetGroup;

import java.awt.*;
import java.util.Collections;
import java.util.List;


public class PanelWidgetGroup extends TJWidgetGroup<PanelWidgetGroup> {

    private final TextureArea backgroundTexture;
    private boolean panelOpen;
    private boolean dragging;
    private int dragX;
    private int dragY;

    public PanelWidgetGroup(int x, int y, int width, int height, TextureArea backgroundTexture) {
        super(new Position(x, y), new Size(width, height));
        this.backgroundTexture = backgroundTexture;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateScreen() {
        if (this.panelOpen)
            super.updateScreen();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (this.panelOpen)
            super.drawInForeground(mouseX, mouseY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        if (!this.panelOpen) return;
        this.backgroundTexture.draw(this.getPosition().getX(), this.getPosition().getY(), this.getSize().getWidth(), this.getSize().getHeight());
        super.drawInBackground(mouseX, mouseY, context);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!this.panelOpen)
            return false;
        if (this.isMouseOverElement(mouseX, mouseY)) {
            this.dragging = true;
            this.dragX = mouseX;
            this.dragY = mouseY;
        }
        final boolean clicked = super.mouseClicked(mouseX, mouseY, button);
        this.dragging = !clicked;
        return clicked || this.isMouseOverElement(mouseX, mouseY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (!this.panelOpen)
            return false;
        if (this.dragging && timeDragged > 0 && this.isMouseOverElement(mouseX, mouseY))
            this.setSelfPosition(new Position(mouseX - this.dragX, mouseY - this.dragY));
        return super.mouseDragged(mouseX, mouseY, button, timeDragged) || this.isMouseOverElement(mouseX, mouseY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        this.dragging = false;
        if (!this.panelOpen)
            return false;
        return super.mouseReleased(mouseX, mouseY, button) || this.isMouseOverElement(mouseX, mouseY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        return this.panelOpen && super.mouseWheelMove(mouseX, mouseY, wheelDelta) || this.isMouseOverElement(mouseX, mouseY);
    }

    @Override
    public void detectAndSendChanges() {
        if (this.panelOpen)
            super.detectAndSendChanges();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 2)
            this.panelOpen = buffer.readBoolean();
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 2)
            this.panelOpen = buffer.readBoolean();
    }

    /**
     * Opens this panel both on client and server
     */
    public void openPanel() {
        if (this.panelOpen) return;
        this.panelOpen = true;
        if (isClientSide())
            this.writeClientAction(2, buffer -> buffer.writeBoolean(this.panelOpen));
        this.writeUpdateInfo(2, buffer -> buffer.writeBoolean(this.panelOpen));
    }

    /**
     * Closes this panel both on client and server
     */
    public void closePanel() {
        if (!this.panelOpen) return;
        this.panelOpen = false;
        if (isClientSide())
            this.writeClientAction(2, buffer -> buffer.writeBoolean(this.panelOpen));
        this.writeUpdateInfo(2, buffer -> buffer.writeBoolean(this.panelOpen));
    }

    public boolean isPanelOpen() {
        return this.panelOpen;
    }

    @Override
    public Rectangle toRectangleBox() {
        return this.panelOpen ? super.toRectangleBox() : new Rectangle();
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        return this.panelOpen ? super.getIngredientOverMouse(mouseX, mouseY) : null;
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        return this.panelOpen ? super.getPhantomTargets(ingredient) : Collections.emptyList();
    }
}
