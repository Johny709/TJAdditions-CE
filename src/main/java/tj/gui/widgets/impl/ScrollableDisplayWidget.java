package tj.gui.widgets.impl;

import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ScrollableListWidget;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.AdvancedDisplayWidget;

public class ScrollableDisplayWidget extends ScrollableListWidget {

    private boolean canScroll;
    private boolean autoScroll;
    private int autoScrollY;

    public ScrollableDisplayWidget(int xPosition, int yPosition, int width, int height) {
        super(xPosition, yPosition, width, height);
    }

    public ScrollableDisplayWidget setScrollPanelWidth(int scrollPanelWidth) {
        this.scrollPaneWidth = scrollPanelWidth;
        return this;
    }

    public ScrollableDisplayWidget addTextWidget(AdvancedTextWidget widget) {
        this.addWidget(widget);
        return this;
    }

    public ScrollableDisplayWidget addDisplayWidget(AdvancedDisplayWidget widget) {
        this.addWidget(widget);
        return this;
    }

    @Override
    public boolean isWidgetClickable(Widget widget) {
        return true; // this ScrollWidget will only add one widget so checks are unnecessary if position changes.
    }

    private void setScrollOffset(int offset) {
        this.scrollOffset = MathHelper.clamp(this.scrollOffset + offset, 0, this.totalListHeight - this.getSize().getHeight());
        super.recomputeSize();
    }

    @Override
    protected boolean recomputeSize() {
        this.scrollOffset = Math.min(this.scrollOffset, Math.max(0, this.totalListHeight - this.getSize().getHeight()));
        this.canScroll = this.checkContainedWidgetSize();
        if (!this.canScroll)
            this.autoScroll = false;
        return super.recomputeSize();
    }

    private boolean checkContainedWidgetSize() {
        int widgetHeight = 0;
        for (Widget widget : this.widgets) {
            widgetHeight = Math.max(widget.getSize().getHeight(), widgetHeight);
        }
        return widgetHeight > this.getSize().getHeight();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        int scroll = mouseY - this.autoScrollY;
        if (this.autoScroll) {
            if (scroll != 0) {
                this.setScrollOffset(mouseY - this.autoScrollY);
                if (scroll > 0)
                    TJGuiTextures.AUTOSCROLL_DOWN.draw(mouseX - 8, mouseY - 8, 16, 16);
                else TJGuiTextures.AUTOSCROLL_UP.draw(mouseX - 8, mouseY - 8, 16, 16);
            } else TJGuiTextures.AUTOSCROLL.draw(mouseX - 8, mouseY - 8, 16, 16);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (!this.canScroll) {
            return this.widgets.stream().filter(this::isWidgetClickable).anyMatch(it -> it.mouseWheelMove(mouseX, mouseY, wheelDelta));
        } else return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (!this.canScroll) {
            return this.widgets.stream().filter(this::isWidgetClickable).anyMatch(it -> it.mouseDragged(mouseX, mouseY, button, timeDragged));
        } else return super.mouseDragged(mouseX, mouseY, button, timeDragged);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!this.canScroll)
            return this.widgets.stream().filter(this::isWidgetClickable).anyMatch(it -> it.mouseClicked(mouseX, mouseY, button));
        if (this.autoScroll) {
            this.autoScroll = false;
        } else if (button == 2) {
            this.autoScrollY = mouseY;
            this.autoScroll = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
