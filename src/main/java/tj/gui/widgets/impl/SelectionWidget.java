package tj.gui.widgets.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

public class SelectionWidget extends WidgetGroup {

    private TextureArea[] backgroundTextures;
    private TextureArea[] nonActiveTextures;
    private TextureArea[] activeTextures;
    private boolean active;

    public SelectionWidget(int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
    }

    /**
     * These textures will be displayed regardless if this widget is on or off.
     */
    public SelectionWidget setBackgroundTextures(TextureArea... backgroundTextures) {
        this.backgroundTextures = backgroundTextures;
        return this;
    }

    /**
     * These textures will be displayed when the widget is on or active.
     */
    public SelectionWidget setNonActiveTextures(TextureArea... nonActiveTextures) {
        this.nonActiveTextures = nonActiveTextures;
        return this;
    }

    /**
     * These textures will be displayed when the widget is off or non-active.
     */
    public SelectionWidget setActiveTextures(TextureArea... activeTextures) {
        this.activeTextures = activeTextures;
        return this;
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        return this.active ? super.getPhantomTargets(ingredient) : Collections.emptyList();
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        return this.active ? super.getIngredientOverMouse(mouseX, mouseY) : null;
    }

    @Override
    public void detectAndSendChanges() {
        if (this.active)
            super.detectAndSendChanges();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateScreen() {
        if (this.active)
            super.updateScreen();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (this.active)
            super.drawInForeground(mouseX, mouseY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        Size size = this.getSize();
        Position pos = this.getPosition();
        if (this.backgroundTextures != null) for (TextureArea textureArea : this.backgroundTextures) {
            textureArea.draw(pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
        }
        if (!this.active && this.nonActiveTextures != null) for (TextureArea textureArea : this.nonActiveTextures) {
            textureArea.draw(pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
        }
        if (this.active && this.activeTextures != null) for (TextureArea textureArea : this.activeTextures) {
            textureArea.draw(pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
        }
        if (this.active)
            super.drawInBackground(mouseX, mouseY, context);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        return this.active && super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        this.active = this.isMouseOverElement(mouseX, mouseY);
        if (this.active)
            super.mouseClicked(mouseX, mouseY, button);
        this.writeClientAction(2, buffer -> buffer.writeBoolean(this.active));
        return this.active;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        return this.active && super.mouseDragged(mouseX, mouseY, button, timeDragged);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        return this.active && super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 2) {
            this.active = buffer.readBoolean();
        }
    }
}
