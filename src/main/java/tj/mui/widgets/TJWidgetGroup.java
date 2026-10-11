package tj.mui.widgets;

import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class TJWidgetGroup<R extends TJWidgetGroup<R>> extends WidgetGroup {

    public TJWidgetGroup() {
        this(Position.ORIGIN, Size.ZERO);
    }

    public TJWidgetGroup(Position position) {
        super(position);
    }

    public TJWidgetGroup(Position position, Size size) {
        super(position, size);
    }

    public R addSubWidget(Widget widget) {
        this.addWidget(widget);
        return this.thisWidget();
    }

    /**
     * @return True to cancel subsequent {@link #mouseClicked(int, int, int)} calls in this widget group.
     * Make sure to return true in widget within this widget group to cancel.
     * Can be used to prevent clicking event if widget is behind another widget or not visible.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        // Reversed iteration cause the widgets called last for rendering are displayed in front so check those first.
        for (int i = this.widgets.size() - 1; i > -1; i--) {
            final Widget widget = this.widgets.get(i);
            if (!this.isWidgetClickable(widget)) continue;
            if (widget.mouseClicked(mouseX, mouseY, button))
                return true;
        }
        return false;
    }

    /**
     * @return True to cancel subsequent {@link #mouseReleased(int, int, int)} calls in this widget group.
     * Make sure to return true in widget within this widget group to cancel.
     * Can be used to prevent mouse release event if widget is behind another widget or not visible.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        // Reversed iteration cause the widgets called last for rendering are displayed in front so check those first.
        for (int i = this.widgets.size() - 1; i > -1; i--) {
            final Widget widget = this.widgets.get(i);
            if (!this.isWidgetClickable(widget)) continue;
            if (widget.mouseReleased(mouseX, mouseY, button))
                return true;
        }
        return false;
    }

    /**
     * @return True to cancel subsequent {@link #mouseDragged(int, int, int, long)} calls in this widget group.
     * Make sure to return true in widget within this widget group to cancel.
     * Can be used to prevent mouse dragging event if widget is behind another widget or not visible.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        // Reversed iteration cause the widgets called last for rendering are displayed in front so check those first.
        for (int i = this.widgets.size() - 1; i > -1; i--) {
            final Widget widget = this.widgets.get(i);
            if (!this.isWidgetClickable(widget)) continue;
            if (widget.mouseDragged(mouseX, mouseY, button, timeDragged))
                return true;
        }
        return false;
    }

    /**
     * @return True to cancel subsequent {@link #mouseDragged(int, int, int, long)} calls in this widget group.
     * Make sure to return true in widget within this widget group to cancel.
     * Can be used to prevent mouse scrolling event if widget is behind another widget or not visible.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        // Reversed iteration cause the widgets called last for rendering are displayed in front so check those first.
        for (int i = this.widgets.size() - 1; i > -1; i--) {
            final Widget widget = this.widgets.get(i);
            if (!this.isWidgetClickable(widget)) continue;
            if (widget.mouseWheelMove(mouseX, mouseY, wheelDelta))
                return true;
        }
        return false;
    }

    /**
     * @return True to cancel subsequent {@link #mouseDragged(int, int, int, long)} calls in this widget group.
     * Make sure to return true in widget within this widget group to cancel.
     * Can be used to prevent key typing event if widget is behind another widget or not visible.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean keyTyped(char charTyped, int keyCode) {
        // Reversed iteration cause the widgets called last for rendering are displayed in front so check those first.
        for (int i = this.widgets.size() - 1; i > -1; i--) {
            final Widget widget = this.widgets.get(i);
            if (!this.isWidgetClickable(widget)) continue;
            if (widget.keyTyped(charTyped, keyCode))
                return true;
        }
        return false;
    }

    /**
     * To remove unchecked cast warnings spam.
     * @return this widget object.
     */
    protected R thisWidget() {
        return (R) this;
    }
}
