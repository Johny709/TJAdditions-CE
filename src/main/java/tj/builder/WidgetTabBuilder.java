package tj.builder;

import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.gui.widgets.tab.ItemTabInfo;
import gregtech.api.gui.widgets.tab.TabListRenderer;
import gregtech.api.util.Position;
import net.minecraft.item.ItemStack;
import tj.gui.TJTabGroup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WidgetTabBuilder {

    private Position position = Position.ORIGIN;
    private Supplier<TabListRenderer> tabListRenderer;
    private final LinkedHashMap<ItemTabInfo, AbstractWidgetGroup> tabs = new LinkedHashMap<>();
    private final List<Widget> widgetGroup = new ArrayList<>();

    public WidgetTabBuilder setTabListRenderer(Supplier<TabListRenderer> tabListRenderer) {
        this.tabListRenderer = tabListRenderer;
        return this;
    }

    public WidgetTabBuilder setPosition(int x, int y) {
        this.position = new Position(x, y);
        return this;
    }

    /**
     * Offset from the last set position. Offsets from x:0, y:0 if no prior position has been set.
     */
    public WidgetTabBuilder offsetPosition(int x, int y) {
        this.position = new Position(this.position.getX() + x, this.position.getY() + y);
        return this;
    }

    /**
     * These widgets affect all tabs. Added widgets are used for building widget group.
     */
    public WidgetTabBuilder addWidget(Widget widget) {
        this.widgetGroup.add(widget);
        return this;
    }

    public WidgetTabBuilder addTab(String name, ItemStack itemDisplay, Consumer<WidgetGroup> widgetGroupConsumer) {
        WidgetGroup widgets = new WidgetGroup();
        widgetGroupConsumer.accept(widgets);
        this.tabs.put(new ItemTabInfo(name, itemDisplay), widgets);
        return this;
    }

    public WidgetGroup buildWidgetGroup() {
        WidgetGroup widgetGroup = new WidgetGroup(this.position);
        this.widgetGroup.forEach(widgetGroup::addWidget);
        return widgetGroup;
    }

    public Widget build() {
        TJTabGroup tabGroup = new TJTabGroup(this.tabListRenderer, this.position);
        this.tabs.forEach(tabGroup::addTab);
        return tabGroup;
    }
}
