package tj.gui.widgets.impl;

import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.EnumActionResult;
import org.apache.commons.lang3.tuple.Pair;
import tj.gui.widgets.ButtonWidget;
import tj.gui.widgets.PopUpWidget;
import tj.util.predicates.ActionResultPredicate;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ButtonPopUpWidget<T extends ButtonPopUpWidget<T>> extends PopUpWidget<T> {

    private final Int2ObjectMap<ActionResultPredicate<String>> buttonConditions = new Int2ObjectOpenHashMap<>();

    public ButtonPopUpWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    /**
     * Call this before any of the {@link ButtonPopUpWidget#addPopup(Predicate)} methods. These widgets are bound to the popup defined by calling the {@link ButtonPopUpWidget#addPopup(Predicate) method} mentioned
     * @param button button widgets to close this popup. The position of this button is relative to it's bound popup.
     */
    public T addClosingButton(ButtonWidget<?> button) {
        button.setButtonId(String.valueOf(0));
        button.setButtonResponder(this::handleButtonPress);
        this.pendingWidgets.add(button);
        return (T) this;
    }

    /**
     * return true in the predicate for non-selected widgets to be visible but still can not be interacted. Adds a new popup every time this method is called.
     * call {@link #addClosingButton(ButtonWidget)} before this to add closing buttons to close this popup.
     * @param x X offset of widget group.
     * @param y Y offset of widget group.
     * @param width width of widget group.
     * @param height height of widget group.
     * @param button button widget to activate this popup.
     * @param widgets widgets to add.
     */
    public T addPopup(int x, int y, int width, int height, ButtonWidget<?> button, Predicate<WidgetGroup> widgets) {
        button.appendButtonId(buttonId -> buttonId + ":" + this.selectedIndex)
                .setButtonResponder(this::handleButtonPress)
                .setButtonIdAsLong(this.selectedIndex);
        if (button instanceof TJToggleButtonWidget)
            ((TJToggleButtonWidget) button).setButtonSupplier(() -> this.selectedIndex == button.getButtonIdAsLong());
        WidgetGroup widgetGroup = new WidgetGroup(new Position(x, y), new Size(width, height));
        boolean visible = widgets.test(widgetGroup);
        for (Widget widget : this.pendingWidgets)
            widgetGroup.addWidget(widget);
        this.widgetMap.get(0).getRight().addWidget(button);
        this.addWidget(widgetGroup);
        this.pendingWidgets.clear();
        this.widgetMap.put(this.selectedIndex++, Pair.of(visible, widgetGroup));
        return (T) this;
    }

    /**
     * Activates this popup if the conditions have failed. this won't do anything if {@link #addPopupCondition(ActionResultPredicate)} is not defined.
     * bind this popup by calling this after calling any of {@link #addPopup(int, int, int, int, ButtonWidget, boolean, Predicate)} methods.
     * call {@link #addClosingButton(ButtonWidget)} before this to add closing buttons to close this popup.
     * @param x X offset of widget group.
     * @param y Y offset of widget group.
     * @param width width of widget group
     * @param height height of widget group.
     */
    public T addFailPopup(int x, int y, int width, int height, Consumer<WidgetGroup> widgets) {
        WidgetGroup widgetGroup = new WidgetGroup(new Position(x, y), new Size(width, height));
        widgets.accept(widgetGroup);
        for (Widget widget : this.pendingWidgets)
            widgetGroup.addWidget(widget);
        this.addWidget(widgetGroup);
        this.pendingWidgets.clear();
        this.widgetMap.put(this.selectedIndex++, Pair.of(false, widgetGroup));
        return (T) this;
    }

    public T addPopupCondition(ActionResultPredicate<String> buttonCondition) {
        this.buttonConditions.put(this.selectedIndex, buttonCondition);
        return (T) this;
    }

    /**
     * return true in the predicate for non-selected widgets to be visible but still can not be interacted. Adds a new popup every time this method is called.
     * call {@link #addClosingButton(ButtonWidget)} before this to add closing buttons to close this popup.
     * @param button button widget to activate this popup.
     * @param widgets widgets to add.
     */
    public T addPopup(ButtonWidget<?> button, Predicate<WidgetGroup> widgets) {
        this.addPopup(0, 0, 0, 0, button, widgets);
        return (T) this;
    }

    protected void handleButtonPress(String buttonId) {
        try {
            int i = buttonId.lastIndexOf(":");
            if (i != -1) {
                buttonId = buttonId.substring(i + 1);
                int index = Integer.parseInt(buttonId) + 1;
                EnumActionResult actionResult = null;
                boolean contains = this.buttonConditions.get(i) != null;
                if (contains && (actionResult = this.buttonConditions.get(index).test(buttonId)) == EnumActionResult.PASS)
                    return;
                buttonId = !contains || actionResult == EnumActionResult.SUCCESS ? buttonId : String.valueOf(index);
            }
            this.selectedIndex = Integer.parseInt(buttonId);
            this.writeUpdateInfo(2, buffer -> buffer.writeInt(this.selectedIndex));
        } catch (NumberFormatException ignored) {}
    }
}
