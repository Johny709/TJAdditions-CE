package tj.mui.widgets.impl;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.RenderUtil;
import gregtech.api.util.Size;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.mui.TJGuiTextures;
import tj.mui.widgets.TJWidgetGroup;

import java.awt.*;

public class ScrollingWidgetGroup<R extends TJWidgetGroup<R>> extends TJWidgetGroup<R> {

    protected final int posX;
    protected TextureArea scrollSliderTexture;
    protected TextureArea scrollBarTexture;
    protected Rectangle scrollSliderRec;
    protected Rectangle scrollBarRec;
    protected int scrollBarXOffset;
    protected int scrollOffset;
    protected int scrollHeight;
    protected int autoScrollY;
    protected boolean autoScroll;
    protected boolean initialized;

    public ScrollingWidgetGroup(int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        this.setScrollSlider(1, 1, 2, 10, GuiTextures.BORDERED_BACKGROUND);
        this.setScrollbar(0, 0, 4, height, GuiTextures.SLOT);
        this.posX = x;
    }

    /**
     *
     * @param x x offset at the end of the main scroll widget length.
     * @param y y offset at the end of the main scroll widget length.
     * @param width length of scroll bar.
     * @param height height of scroll bar.
     * @param scrollbarTexture texture of scroll bar.
     */
    public R setScrollbar(int x, int y, int width, int height, TextureArea scrollbarTexture) {
        this.scrollBarRec = new Rectangle(x, y, width, height);
        this.scrollBarTexture = scrollbarTexture;
        this.scrollBarXOffset = x;
        return this.thisWidget();
    }

    /**
     *
     * @param x x offset at the end of the main scroll widget length.
     * @param y y offset at the end of the main scroll widget length.
     * @param width length of scroll bar slider.
     * @param height height of scroll bar slider.
     * @param scrollSliderTexture texture of scroll bar slider.
     */
    public R setScrollSlider(int x, int y, int width, int height, TextureArea scrollSliderTexture) {
        this.scrollSliderRec = new Rectangle(x, y, width, height);
        this.scrollSliderTexture = scrollSliderTexture;
        return this.thisWidget();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (this.autoScroll) {
            int scroll = mouseY - this.autoScrollY;
            if (scroll > 4 || scroll < -4) {
                this.addScrollOffset(scroll / 5);
                if (scroll > 0) {
                    TJGuiTextures.AUTOSCROLL_DOWN.draw(mouseX - 8, mouseY - 8, 16, 16);
                } else TJGuiTextures.AUTOSCROLL_UP.draw(mouseX - 8, mouseY - 8, 16, 16);
            } else TJGuiTextures.AUTOSCROLL.draw(mouseX - 8, mouseY - 8, 16, 16);
        }
        if (this.isMouseOverElement(mouseX, mouseY))
            super.drawInForeground(mouseX, mouseY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        final Size size = this.getSize();
        final Position pos = this.getPosition();
        if (!this.initialized) {
            this.initialized = true;
            this.scrollBarRec = new Rectangle(this.scrollBarRec.x + pos.getX() + size.getWidth(), this.scrollBarRec.y + pos.getY(), this.scrollBarRec.width, this.scrollBarRec.height);
            this.scrollSliderRec = new Rectangle(this.scrollBarRec.x + this.scrollSliderRec.x, this.scrollBarRec.y + this.scrollSliderRec.y, this.scrollSliderRec.width, this.scrollSliderRec.height);
        }
        RenderUtil.useScissor(pos.getX(), pos.getY(), size.getWidth(), size.getHeight(), () -> {
            super.drawInBackground(mouseX, mouseY, context);
        });
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        GlStateManager.enableBlend();
        if (this.scrollBarTexture != null)
            this.scrollBarTexture.draw(this.scrollBarRec.x, this.scrollBarRec.y, this.scrollBarRec.width, this.scrollBarRec.height);
        if (this.scrollSliderTexture == null) return;
        final double heightDiff = (double) this.scrollBarRec.height / (this.scrollHeight - size.height);
        final int scrollOffset = this.scrollSliderRec.y + (int) Math.round(this.scrollOffset * heightDiff);
        final int sliderY = Math.max(this.scrollBarRec.y, Math.min(this.scrollBarRec.y + this.scrollBarRec.height - this.scrollSliderRec.height, scrollOffset));
        this.scrollSliderTexture.draw(this.scrollSliderRec.x, sliderY, this.scrollSliderRec.width, this.scrollSliderRec.height);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            if (this.autoScroll) {
                this.autoScroll = false;
            } else if (button == 2) {
                this.autoScrollY = mouseY;
                this.autoScroll = true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        } else if (this.scrollBarRec.contains(mouseX, mouseY)) {
            final double heightDiff = (double) this.scrollBarRec.height / (this.scrollHeight - this.getSize().height);
            this.scrollOffset = (int) Math.max(0, (mouseY - this.scrollBarRec.y) / heightDiff);
            this.setSubWidgetOffSet(this.scrollOffset);
            this.writeClientAction(2, buffer -> buffer.writeInt(this.scrollOffset));
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if ((button == 0 || button == 1) && this.scrollBarRec.contains(mouseX, mouseY)) {
            final double heightDiff = (double) this.scrollBarRec.height / (this.scrollHeight - this.getSize().height);
            this.scrollOffset = (int) Math.max(0, (mouseY - this.scrollBarRec.y) / heightDiff);
            this.setSubWidgetOffSet(this.scrollOffset);
            this.writeClientAction(2, buffer -> buffer.writeInt(this.scrollOffset));
        }
        if (this.isMouseOverElement(mouseX, mouseY))
            super.mouseDragged(mouseX, mouseY, button, timeDragged);
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (this.isMouseInWidget(mouseX, mouseY)) {
            this.addScrollOffset(MathHelper.clamp(wheelDelta, -1, 1) * -18);
            super.mouseWheelMove(mouseX, mouseY, wheelDelta);
        }
        return false;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 2) {
            this.scrollOffset = buffer.readInt();
            this.setSubWidgetOffSet(this.scrollOffset);
        }
    }

    @Override
    public boolean isWidgetClickable(final Widget widget) {
        if (!super.isWidgetClickable(widget)) {
            return false;
        }
        return this.isWidgetOverlapsScissor(widget);
    }

    private boolean isPositionInsideScissor(int mouseX, int mouseY) {
        return this.isMouseOverElement(mouseX, mouseY) && !this.scrollBarRec.contains(mouseX, mouseY);
    }

    private boolean isWidgetOverlapsScissor(Widget widget) {
        final Position position = widget.getPosition();
        final Size size = widget.getSize();
        final int x0 = position.x;
        final int y0 = position.y;
        final int x1 = position.x + size.width - 1;
        final int y1 = position.y + size.height - 1;
        return this.isPositionInsideScissor(x0, y0) ||
                this.isPositionInsideScissor(x0, y1) ||
                this.isPositionInsideScissor(x1, y0) ||
                this.isPositionInsideScissor(x1, y1);
    }

    private void addScrollOffset(int delta) {
        this.scrollOffset += delta;
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, this.scrollHeight - this.getSize().height));
        this.setSubWidgetOffSet(this.scrollOffset);
        this.writeClientAction(2, buffer -> buffer.writeInt(this.scrollOffset));
    }

    private boolean isMouseInWidget(int mouseX, int mouseY) {
        final Size size = this.getSize();
        final int posY = this.getPosition().getY();
        return mouseX >= this.posX && mouseX <= this.posX + size.getWidth() && mouseY >= posY && mouseY <= posY + size.getHeight();
    }

    private void setSubWidgetOffSet(int offSet) {
        final Position pos = this.getPosition();
        for (Widget widget : this.widgets)
            widget.setParentPosition(new Position(pos.getX(), pos.getY() - offSet));
    }
}
