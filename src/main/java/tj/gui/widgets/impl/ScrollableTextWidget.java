package tj.gui.widgets.impl;

import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ScrollableListWidget;

public class ScrollableTextWidget extends ScrollableListWidget {

    public ScrollableTextWidget(int xPosition, int yPosition, int width, int height) {
        super(xPosition, yPosition, width, height);
    }

    @Override
    public boolean isWidgetClickable(Widget widget) {
        return true; // this ScrollWidget will only add one widget so checks are unnecessary if position changes.
    }

    public ScrollableTextWidget addTextWidget(AdvancedTextWidget widget) {
        this.addWidget(widget);
        return this;
    }
}
