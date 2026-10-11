package tj.mui.widgets.impl;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.RenderUtil;
import gregtech.api.util.Size;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.mui.TJGuiTextures;
import tj.mui.widgets.TJWidgetGroup;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class ScrollingWidgetGroup<R extends TJWidgetGroup<R>> extends TJWidgetGroup<R> {

    protected TextureArea scrollVerticalSliderTexture;
    protected TextureArea scrollVerticalBarTexture;
    protected TextureArea scrollHorizontalSliderTexture;
    protected TextureArea scrollHorizontalBarTexture;

    @Nonnull
    protected Rectangle scrollHorizontalSliderRec = new Rectangle(0, 0);

    @Nonnull
    protected Position horizontalSliderPosInit = Position.ORIGIN;

    @Nonnull
    protected Rectangle scrollVerticalSliderRec = new Rectangle(0, 0);

    @Nonnull
    protected Position verticalSliderPosInit = Position.ORIGIN;

    @Nonnull
    protected Rectangle scrollVerticalBarRec = new Rectangle(0, 0);

    @Nonnull
    protected Position verticalBarPosInit = Position.ORIGIN;

    @Nonnull
    protected Rectangle scrollHorizontalBarRec = new Rectangle(0, 0);

    @Nonnull
    protected Position horizontalBarPosInit = Position.ORIGIN;

    protected int scrollVerticalOffset;
    protected int scrollHorizontalOffset;
    protected int lastScrollOffsetY;
    protected int lastScrollOffsetX;
    protected int scrollHeight;
    protected int scrollWidth;
    protected int autoScrollY;
    protected int autoScrollX;
    protected boolean autoScroll;

    public ScrollingWidgetGroup(int x, int y, int width, int height) {
        this(x, y, width, height, false);
    }

    public ScrollingWidgetGroup(int x, int y, int width, int height, boolean horizontal) {
        super(new Position(x, y), new Size(width, height));
        if (!horizontal) {
            this.setScrollVerticalSlider(0, 0, 4, 10, GuiTextures.BORDERED_BACKGROUND);
            this.setScrollVerticalbar(0, 0, 4, height, GuiTextures.SLOT);
        } else {
            this.setScrollHorizontalSlider(0, 0, 10, 4, GuiTextures.BORDERED_BACKGROUND);
            this.setScrollHorizontalBar(0, 0, width, 4, GuiTextures.SLOT);
        }
    }

    /**
     *
     * @param x x offset at the end of the main scroll widget length.
     * @param y y offset at the end of the main scroll widget length.
     * @param width length of scroll bar.
     * @param height height of scroll bar.
     * @param scrollbarTexture texture of scroll bar.
     */
    public R setScrollVerticalbar(int x, int y, int width, int height, TextureArea scrollbarTexture) {
        this.scrollVerticalBarRec = new Rectangle(x, y, width, height);
        this.verticalBarPosInit = new Position(x, y);
        this.scrollVerticalBarTexture = scrollbarTexture;
        return this.thisWidget();
    }

    /**
     *
     * @param x x offset from vertical scroll bar position.
     * @param y y offset from verticial scroll bar position.
     * @param width length of scroll bar slider.
     * @param height height of scroll bar slider.
     * @param scrollSliderTexture texture of scroll bar slider.
     */
    public R setScrollVerticalSlider(int x, int y, int width, int height, TextureArea scrollSliderTexture) {
        this.scrollVerticalSliderRec = new Rectangle(x, y, width, height);
        this.verticalSliderPosInit = new Position(x, y);
        this.scrollVerticalSliderTexture = scrollSliderTexture;
        return this.thisWidget();
    }

    /**
     *
     * @param x x offset at the bottom of the main scroll widget height.
     * @param y y offset at the bottom of the main scroll widget height.
     * @param width length of scroll bar.
     * @param height height of scroll bar.
     * @param scrollbarTexture texture of scroll bar.
     */
    public R setScrollHorizontalBar(int x, int y, int width, int height, TextureArea scrollbarTexture) {
        this.scrollHorizontalBarRec = new Rectangle(x, y, width, height);
        this.horizontalBarPosInit = new Position(x, y);
        this.scrollHorizontalBarTexture = scrollbarTexture;
        return this.thisWidget();
    }

    /**
     *
     * @param x x offset from horizontal scroll bar position.
     * @param y y offset from horizontal scroll bar position.
     * @param width length of scroll bar.
     * @param height height of scroll bar.
     * @param scrollSliderTexture texture of scroll bar.
     */
    public R setScrollHorizontalSlider(int x, int y, int width, int height, TextureArea scrollSliderTexture) {
        this.scrollHorizontalSliderRec = new Rectangle(x, y, width, height);
        this.horizontalSliderPosInit = new Position(x, y);
        this.scrollHorizontalSliderTexture = scrollSliderTexture;
        return this.thisWidget();
    }

    @Override
    public void initWidget() {
        super.initWidget();
        this.reinitializeScrollbars();
    }

    @Override
    public void setParentPosition(Position parentPosition) {
        super.setParentPosition(parentPosition);
        this.reinitializeScrollbars();
    }

    @Override
    protected boolean recomputeSize() {
        this.reinitializeScrollbars();
        return super.recomputeSize();
    }

    @Override
    protected void onPositionUpdate() {
        super.onPositionUpdate();
        this.reinitializeScrollbars();
    }

    @Override
    protected void onSizeUpdate() {
        super.onSizeUpdate();
        this.reinitializeScrollbars();
    }

    private void reinitializeScrollbars() {
        final Size size = this.getSize();
        final Position pos = this.getPosition();
        this.scrollVerticalBarRec = new Rectangle(this.verticalBarPosInit.x + pos.x + size.width,
                this.verticalBarPosInit.y + pos.y,
                this.scrollVerticalBarRec.width,
                this.scrollVerticalBarRec.height);
        this.scrollVerticalSliderRec = new Rectangle(this.scrollVerticalBarRec.x + this.verticalSliderPosInit.x,
                this.scrollVerticalBarRec.y + this.verticalSliderPosInit.y,
                this.scrollVerticalSliderRec.width,
                this.scrollVerticalSliderRec.height);
        this.scrollHorizontalBarRec = new Rectangle(this.horizontalBarPosInit.x + pos.x,
                this.horizontalBarPosInit.y + pos.y + size.height,
                this.scrollHorizontalBarRec.width,
                this.scrollHorizontalBarRec.height);
        this.scrollHorizontalSliderRec = new Rectangle(this.scrollHorizontalBarRec.x + this.horizontalSliderPosInit.x,
                this.scrollHorizontalBarRec.y + this.horizontalSliderPosInit.y,
                this.scrollHorizontalSliderRec.width,
                this.scrollHorizontalSliderRec.height);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (this.autoScroll) {
            final int scrollY = mouseY - this.autoScrollY;
            final int scrollX = mouseX - this.autoScrollX;
            if (scrollY > 4 || scrollY < -4) {
                this.addScrollVerticalOffset(scrollY / 5);
                if (scrollY > 0) {
                    TJGuiTextures.AUTOSCROLL_DOWN.draw(mouseX - 8, mouseY - 8, 16, 16);
                } else TJGuiTextures.AUTOSCROLL_UP.draw(mouseX - 8, mouseY - 8, 16, 16);
            } else TJGuiTextures.AUTOSCROLL.draw(mouseX - 8, mouseY - 8, 16, 16);
            if (scrollX > 4 || scrollX < -4) {
                this.addScrollHorizontalOffset(scrollX / 5);
                if (scrollX > 0) {
                    TJGuiTextures.AUTOSCROLL_RIGHT.draw(mouseX - 8, mouseY - 8, 16, 16);
                } else TJGuiTextures.AUTOSCROLL_LEFT.draw(mouseX - 8, mouseY - 8, 16, 16);
            } else TJGuiTextures.AUTOSCROLL_2.draw(mouseX - 8, mouseY - 8, 16, 16);
        }
        if (this.isMouseOverElement(mouseX, mouseY))
            super.drawInForeground(mouseX, mouseY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        final Size size = this.getSize();
        final Position pos = this.getPosition();
        RenderUtil.useScissor(pos.x, pos.y, size.width, size.height, () -> super.drawInBackground(mouseX, mouseY, context));
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        GlStateManager.enableBlend();
        if (this.scrollVerticalBarTexture != null)
            this.scrollVerticalBarTexture.draw(this.scrollVerticalBarRec.x, this.scrollVerticalBarRec.y, this.scrollVerticalBarRec.width, this.scrollVerticalBarRec.height);
        if (this.scrollHorizontalBarTexture != null)
            this.scrollHorizontalBarTexture.draw(this.scrollHorizontalBarRec.x, this.scrollHorizontalBarRec.y, this.scrollHorizontalBarRec.width, this.scrollHorizontalBarRec.height);
        if (this.scrollVerticalSliderTexture != null) {
            final double heightDiff = (double) this.scrollVerticalBarRec.height / (this.scrollHeight - size.height);
            final int scrollOffset = this.scrollVerticalSliderRec.y + (int) Math.round(this.scrollVerticalOffset * heightDiff);
            final int sliderY = Math.max(this.scrollVerticalBarRec.y, Math.min(this.scrollVerticalBarRec.y + this.scrollVerticalBarRec.height - this.scrollVerticalSliderRec.height, scrollOffset));
            this.scrollVerticalSliderTexture.draw(this.scrollVerticalSliderRec.x, sliderY, this.scrollVerticalSliderRec.width, this.scrollVerticalSliderRec.height);
        }
        if (this.scrollHorizontalBarTexture != null) {
            final double widthDiff = (double) this.scrollHorizontalBarRec.width / (this.scrollWidth - size.width);
            final int scrollOffset = this.scrollHorizontalSliderRec.x + (int) Math.round(this.scrollHorizontalOffset * widthDiff);
            final int sliderX = Math.max(this.scrollHorizontalBarRec.x, Math.min(this.scrollHorizontalBarRec.x + this.scrollHorizontalBarRec.width - this.scrollHorizontalSliderRec.width, scrollOffset));
            this.scrollHorizontalSliderTexture.draw(sliderX, this.scrollHorizontalSliderRec.y, this.scrollHorizontalSliderRec.width, this.scrollHorizontalSliderRec.height);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            if (this.autoScroll) {
                this.autoScroll = false;
            } else if (button == 2) {
                this.autoScrollY = mouseY;
                this.autoScrollX = mouseX;
                this.autoScroll = true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        } else if (this.scrollVerticalBarRec.contains(mouseX, mouseY)) {
            final double heightDiff = (double) this.scrollVerticalBarRec.height / (this.scrollHeight - this.getSize().height);
            this.scrollVerticalOffset = (int) Math.max(0, (mouseY - this.scrollVerticalBarRec.y) / heightDiff);
            this.setSubWidgetYOffSet(this.scrollVerticalOffset);
            this.writeClientAction(2, buffer -> buffer.writeInt(this.scrollVerticalOffset));
        } else if (this.scrollHorizontalBarRec.contains(mouseX, mouseY)) {
            final double widthDiff = (double) this.scrollHorizontalBarRec.width / (this.scrollWidth - this.getSize().width);
            this.scrollHorizontalOffset = (int) Math.max(0, (mouseX - this.scrollHorizontalBarRec.x) / widthDiff);
            this.setSubWidgetXOffset(this.scrollHorizontalOffset);
            this.writeClientAction(3, buffer -> buffer.writeInt(this.scrollHorizontalOffset));
        }
        if (this.autoScroll)
            this.autoScroll = false;
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (button == 0 || button == 1) {
            if (this.scrollVerticalBarRec.contains(mouseX, mouseY)) {
                final double heightDiff = (double) this.scrollVerticalBarRec.height / (this.scrollHeight - this.getSize().height);
                this.scrollVerticalOffset = (int) Math.max(0, (mouseY - this.scrollVerticalBarRec.y) / heightDiff);
                this.setSubWidgetYOffSet(this.scrollVerticalOffset);
                this.writeClientAction(2, buffer -> buffer.writeInt(this.scrollVerticalOffset));
            } else if (this.scrollHorizontalBarRec.contains(mouseX, mouseY)) {
                final double widthDiff = (double) this.scrollHorizontalBarRec.width / (this.scrollWidth - this.getSize().width);
                this.scrollHorizontalOffset = (int) Math.max(0, (mouseX - this.scrollHorizontalBarRec.x) / widthDiff);
                this.setSubWidgetXOffset(this.scrollHorizontalOffset);
                this.writeClientAction(3, buffer -> buffer.writeInt(this.scrollHorizontalOffset));
            }
        }
        if (this.isMouseOverElement(mouseX, mouseY))
            return super.mouseDragged(mouseX, mouseY, button, timeDragged);
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (this.scrollVerticalBarRec.contains(mouseX + this.gui.getGuiLeft(), mouseY)) {
            this.addScrollVerticalOffset(MathHelper.clamp(wheelDelta, -1, 1) * -18);
        } else if (this.scrollHorizontalBarRec.contains(mouseX + this.gui.getGuiLeft(), mouseY)) {
            this.addScrollHorizontalOffset(MathHelper.clamp(wheelDelta, -1, 1) * -18);
        }
        if (this.isMouseOverElement(mouseX + this.gui.getGuiLeft(), mouseY)) {
            this.addScrollVerticalOffset(MathHelper.clamp(wheelDelta, -1, 1) * -18);
            return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
        }
        return false;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 2) {
            this.scrollVerticalOffset = buffer.readInt();
            this.setSubWidgetYOffSet(this.scrollVerticalOffset);
        } else if (id == 3) {
            this.scrollHorizontalOffset = buffer.readInt();
            this.setSubWidgetXOffset(this.scrollHorizontalOffset);
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
        return this.isMouseOverElement(mouseX, mouseY) && !this.scrollVerticalBarRec.contains(mouseX, mouseY) && !this.scrollHorizontalBarRec.contains(mouseX, mouseY);
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

    private void addScrollVerticalOffset(int delta) {
        this.scrollVerticalOffset += delta;
        this.scrollVerticalOffset = Math.max(0, Math.min(this.scrollVerticalOffset, this.scrollHeight - this.getSize().height));
        this.setSubWidgetYOffSet(this.scrollVerticalOffset);
        this.writeClientAction(2, buffer -> buffer.writeInt(this.scrollVerticalOffset));
    }

    private void addScrollHorizontalOffset(int delta) {
        this.scrollHorizontalOffset += delta;
        this.scrollHorizontalOffset = Math.max(0, Math.min(this.scrollHorizontalOffset, this.scrollWidth - this.getSize().width));
        this.setSubWidgetXOffset(this.scrollHorizontalOffset);
        this.writeClientAction(3, buffer -> buffer.writeInt(this.scrollHorizontalOffset));
    }

    private void setSubWidgetYOffSet(int offSet) {
        final Position pos = this.getPosition();
        final Position newPos = new Position(pos.x - this.lastScrollOffsetX, pos.y - offSet);
        this.lastScrollOffsetY = offSet;
        for (Widget widget : this.widgets)
            widget.setParentPosition(newPos);
    }

    private void setSubWidgetXOffset(int offset) {
        final Position pos = this.getPosition();
        final Position newPos = new Position(pos.x - offset, pos.y - this.lastScrollOffsetY);
        this.lastScrollOffsetX = offset;
        for (Widget widget : this.widgets)
            widget.setParentPosition(newPos);
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        return this.isMouseOverElement(mouseX, mouseY) ? super.getIngredientOverMouse(mouseX, mouseY) : null;
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        return super.getPhantomTargets(ingredient).stream()
                .filter(target -> this.isPositionInsideScissor(target.getArea().x, target.getArea().y))
                .collect(Collectors.toList());
    }
}
