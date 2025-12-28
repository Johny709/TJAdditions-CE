package tj.gui.widgets.impl;

import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import org.apache.commons.lang3.tuple.Pair;
import tj.gui.widgets.ButtonWidget;
import tj.gui.widgets.TJAdvancedTextWidget;
import tj.util.predicates.QuadActionResultPredicate;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ClickPopUpWidget extends ButtonPopUpWidget<ClickPopUpWidget> {

    private final Int2ObjectMap<QuadActionResultPredicate<String, String, ClickData, EntityPlayer>> textConditions = new Int2ObjectOpenHashMap<>();

    public ClickPopUpWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    /**
     * return true in the predicate for non-selected widgets to be visible but still can not be interacted. Adds a new popup every time this method is called.
     * call {@link #addClosingButton(ButtonWidget)} before this to add closing buttons to close this popup.
     * @param x X offset of widget group.
     * @param y Y offset of widget group.
     * @param width width of widget group.
     * @param height height of widget group.
     * @param textWidget text widget to activate this popup upon certain click conditions.
     * @param add set to add this text widget to this widget group
     * @param widgets widgets to add.
     */
    public ClickPopUpWidget addPopup(int x, int y, int width, int height, TJAdvancedTextWidget textWidget, boolean add, Predicate<WidgetGroup> widgets) {
        WidgetGroup widgetGroup = new WidgetGroup(new Position(x, y), new Size(width, height));
        boolean visible = widgets.test(widgetGroup);
        textWidget.setTextId(String.valueOf(this.selectedIndex))
                .addClickHandler(this::handleDisplayClick);
        if (add)
            widgetGroup.addWidget(textWidget);
        for (Widget widget : this.pendingWidgets)
            widgetGroup.addWidget(widget);
        this.addWidget(widgetGroup);
        this.pendingWidgets.clear();
        this.widgetMap.put(this.selectedIndex++, Pair.of(visible, widgetGroup));
        return this;
    }

    /**
     * if this predicate returns false, then this will activate the fail popup instead. call this before {@link #addFailPopup(int, int, int, int, Consumer)} method.
     * @param textCondition (componentData, textId, clickData, player) ->
     */
    public ClickPopUpWidget addPopupCondition(QuadActionResultPredicate<String, String, ClickData, EntityPlayer> textCondition) {
        this.textConditions.put(this.selectedIndex, textCondition);
        return this;
    }

    private void handleDisplayClick(String componentData, String textId, ClickData clickData, EntityPlayer player) {
        int index = Integer.parseInt(textId) + 1;
        EnumActionResult actionResult = null;
        boolean contains = this.textConditions.get(index) != null;
        if (contains && (actionResult = this.textConditions.get(index).test(componentData, textId, clickData, player)) == EnumActionResult.PASS)
            return;
        if (!contains || actionResult == EnumActionResult.SUCCESS)
            this.handleButtonPress(textId);
        else this.handleButtonPress(String.valueOf(index));
    }
}
