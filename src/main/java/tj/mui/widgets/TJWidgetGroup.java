package tj.mui.widgets;

import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;

import java.util.List;

public class TJWidgetGroup<R extends TJWidgetGroup<R>> extends WidgetGroup {

    public TJWidgetGroup() {
        this(Position.ORIGIN, Size.ZERO);
    }

    public TJWidgetGroup(Position position, Size size) {
        super(position, size);
    }

    protected R thisWidget() {
        return (R) this;
    }
}
