package tj.mui.widgets;

import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

public class TJWidgetGroup<R extends TJWidgetGroup<R>> extends WidgetGroup {

    public TJWidgetGroup(Position position, Size size) {
        super(position, size);
    }

    protected R thisWidget() {
        return (R) this;
    }
}
