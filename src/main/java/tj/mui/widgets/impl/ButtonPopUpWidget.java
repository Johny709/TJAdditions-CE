package tj.mui.widgets.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import tj.TJ;
import tj.mui.widgets.ButtonWidget;
import tj.mui.widgets.PopUpWidget;
import tj.mui.widgets.TJWidgetGroup;
import tj.util.predicates.ActionResultPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ButtonPopUpWidget<T extends ButtonPopUpWidget<T>> extends PopUpWidget<T> {

    private final Int2ObjectMap<ActionResultPredicate<String>> buttonConditions = new Int2ObjectOpenHashMap<>();

    /**
     * This is to prevent popup button from disappearing when popup is opened.
     */
    private final List<Widget> buttons = new ArrayList<>();

    public ButtonPopUpWidget() {
        this(0, 0, 0, 0);
    }

    public ButtonPopUpWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    /**
     * Call this before any of the {@link ButtonPopUpWidget#addPopup(Predicate)} methods. These widgets are bound to the popup defined by calling the {@link ButtonPopUpWidget#addPopup(Predicate) method} mentioned
     * @param button button widgets to close this popup. The position of this button is relative to it's bound popup.
     */
    public T addClosingButton(ButtonWidget<?> button) {
        button.setButtonId(String.valueOf(0));
        button.setButtonResponder(this::setPopupIndex);
        this.pendingWidgets.add(button);
        return this.thisWidget();
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
    public T addPopup(int x, int y, int width, int height, ButtonWidget<?> button, Predicate<TJWidgetGroup<?>> widgets) {
        button.appendButtonId(buttonId -> buttonId + ":" + this.selectedIndex)
                .setButtonResponder(this::setPopupIndex)
                .setButtonIdAsLong(this.selectedIndex);
        if (button instanceof TJToggleButtonWidget)
            ((TJToggleButtonWidget) button).setButtonSupplier(() -> this.selectedIndex == button.getButtonIdAsLong());
        final TJWidgetGroup<?> widgetGroup = new TJWidgetGroup<>(new Position(x, y), new Size(width, height));
        final boolean visible = widgets.test(widgetGroup);
        for (Widget widget : this.pendingWidgets)
            widgetGroup.addWidget(widget);
        this.addWidget(widgetGroup);
        this.addWidget(button); // initialize button
        this.pendingWidgets.clear();
        this.widgetMap.put(this.selectedIndex++, Pair.of(visible, widgetGroup));
        this.buttons.add(button);
        return this.thisWidget();
    }

    /**
     * Activates this popup if the conditions have failed. this won't do anything if {@link #addPopupCondition(ActionResultPredicate)} is not defined.
     * bind this popup by calling this after calling any of {@link #addPopup(int, int, int, int, ButtonWidget, Predicate)} methods.
     * call {@link #addClosingButton(ButtonWidget)} before this to add closing buttons to close this popup.
     * @param x X offset of widget group.
     * @param y Y offset of widget group.
     * @param width width of widget group
     * @param height height of widget group.
     */
    public T addFailPopup(int x, int y, int width, int height, Consumer<TJWidgetGroup<?>> widgets) {
        final TJWidgetGroup<?> widgetGroup = new TJWidgetGroup<>(new Position(x, y), new Size(width, height));
        widgets.accept(widgetGroup);
        for (Widget widget : this.pendingWidgets)
            widgetGroup.addWidget(widget);
        this.addWidget(widgetGroup);
        this.pendingWidgets.clear();
        this.widgetMap.put(this.selectedIndex++, Pair.of(false, widgetGroup));
        return this.thisWidget();
    }

    public T addPopupCondition(ActionResultPredicate<String> buttonCondition) {
        this.buttonConditions.put(this.selectedIndex, buttonCondition);
        return this.thisWidget();
    }

    /**
     * return true in the predicate for non-selected widgets to be visible but still can not be interacted. Adds a new popup every time this method is called.
     * call {@link #addClosingButton(ButtonWidget)} before this to add closing buttons to close this popup.
     * @param button button widget to activate this popup.
     * @param widgets widgets to add.
     */
    public T addPopup(ButtonWidget<?> button, Predicate<TJWidgetGroup<?>> widgets) {
        this.addPopup(0, 0, 0, 0, button, widgets);
        return this.thisWidget();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        this.buttons.stream()
                .filter(widget -> !widget.isVisible())
                .forEach(Widget::updateScreen);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        this.buttons.forEach(widget -> widget.drawInForeground(mouseX, mouseY));
        super.drawInForeground(mouseX, mouseY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        this.buttons.forEach(widget -> widget.drawInBackground(mouseX, mouseY, context));
        super.drawInBackground(mouseX, mouseY, context);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        final boolean clicked = super.mouseClicked(mouseX, mouseY, button);
        if (!clicked) for (Widget widget : this.buttons)
            if (widget.mouseClicked(mouseX, mouseY, button))
                return true;
        return clicked;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        final boolean released = super.mouseReleased(mouseX, mouseY, button);
        if (!released) for (Widget widget : this.buttons)
            if (widget.mouseReleased(mouseX, mouseY, button))
                return true;
        return released;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        final boolean dragged = super.mouseDragged(mouseX, mouseY, button, timeDragged);
        if (!dragged) for (Widget widget : this.buttons)
            if (widget.mouseDragged(mouseX, mouseY, button, timeDragged))
                return true;
        return dragged;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        final boolean wheeled = super.mouseWheelMove(mouseX, mouseY, wheelDelta);
        if (!wheeled) for (Widget widget : this.buttons)
            if (widget.mouseWheelMove(mouseX, mouseY, wheelDelta))
                return true;
        return wheeled;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.buttons.stream()
                .filter(widget -> !widget.isVisible())
                .forEach(Widget::detectAndSendChanges);
    }

    @Override
    protected void setPopupIndex(String buttonId) {
        try {
            final int i = buttonId.lastIndexOf(":");
            if (i != -1) {
                buttonId = buttonId.substring(i + 1);
                final int index = Integer.parseInt(buttonId) + 1;
                EnumActionResult actionResult = null;
                final boolean contains = this.buttonConditions.get(i) != null;
                if (contains && (actionResult = this.buttonConditions.get(index).test(buttonId)) == EnumActionResult.PASS)
                    return;
                buttonId = !contains || actionResult == EnumActionResult.SUCCESS ? buttonId : String.valueOf(index);
            }
            final int lastIndex = this.selectedIndex;
            this.selectedIndex = Integer.parseInt(buttonId);
            this.updateWidgets(lastIndex, this.selectedIndex);
            this.writeUpdateInfo(2, buffer -> {
                buffer.writeInt(lastIndex);
                buffer.writeInt(this.selectedIndex);
            });
        } catch (NumberFormatException e) {
            TJ.logger.info(e.getMessage());
        }
    }
}
