package tj.gui.widgets.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.igredient.IGhostIngredientTarget;
import gregtech.api.gui.igredient.IIngredientSlot;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.gui.TJGuiTextures;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SelectionWidgetGroup extends WidgetGroup {

    private final Int2ObjectMap<List<Widget>> widgetMap = new Int2ObjectOpenHashMap<>();
    private final Object2IntMap<Widget> selectionBoxes = new Object2IntOpenHashMap<>();
    private int activeIndex = -1; // don't run any sub widgets until the player selected a rectangle box.

    public SelectionWidgetGroup(int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
    }

    /**
     *
     * @param index set widget list id for the sub widget to belong to.
     * @param widget widget to add to this selection widget group.
     */
    public void addSubWidget(int index, Widget widget) {
        final List<Widget> widgetList = this.widgetMap.computeIfAbsent(index, list -> new ArrayList<>());
        widgetList.add(widget);
        this.addWidget(widget);
    }

    /**
     *
     * @param index choose widget list id for this box to belong to.
     * @param x X relative to the position of this selection widget group.
     * @param y Y relative to the position of this selection widget group.
     * @param width width of box
     * @param height height of box
     */
    public void addSelectionBox(int index, int x, int y, int width, int height) {
        final Widget widget = new SelectionSlotWidget(new Position(x, y), new Size(width, height));
        this.selectionBoxes.put(widget, index);
        this.addWidget(widget);
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        return this.widgetMap.getOrDefault(this.activeIndex, Collections.emptyList()).stream()
                .filter(widget -> widget instanceof IGhostIngredientTarget)
                .flatMap(widget -> ((IGhostIngredientTarget) widget).getPhantomTargets(ingredient).stream())
                .collect(Collectors.toList());
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        return this.widgetMap.getOrDefault(this.activeIndex, Collections.emptyList()).stream()
                .filter(widget -> widget instanceof IIngredientSlot)
                .map(widget -> ((IIngredientSlot) widget).getIngredientOverMouse(mouseX, mouseY))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void detectAndSendChanges() {
        this.widgetMap.getOrDefault(this.activeIndex, Collections.emptyList())
                .forEach(Widget::detectAndSendChanges);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateScreen() {
        this.widgetMap.getOrDefault(this.activeIndex, Collections.emptyList())
                .forEach(Widget::updateScreen);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        this.widgetMap.getOrDefault(this.activeIndex, Collections.emptyList())
                .forEach(widget -> widget.drawInForeground(mouseX, mouseY));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        for (Object2IntMap.Entry<Widget> entry : this.selectionBoxes.object2IntEntrySet()) {
            if (entry.getIntValue() == this.activeIndex) {
                final Size size = entry.getKey().getSize();
                final Position pos = entry.getKey().getPosition();
                TJGuiTextures.SELECTION_BOX.draw(pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
            }
        }
        this.widgetMap.getOrDefault(this.activeIndex, Collections.emptyList())
                .forEach(widget -> widget.drawInBackground(mouseX, mouseY, context));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        return this.widgetMap.getOrDefault(this.activeIndex, Collections.emptyList()).stream()
                .anyMatch(widget -> widget.mouseWheelMove(mouseX, mouseY, wheelDelta));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        for (Object2IntMap.Entry<Widget> entry : this.selectionBoxes.object2IntEntrySet()) {
            if (entry.getKey().toRectangleBox().contains(mouseX, mouseY)) {
                this.activeIndex = entry.getIntValue();
                this.writeClientAction(2, buffer -> buffer.writeInt(this.activeIndex));
                return true;
            }
        }
        if (!this.isMouseOverElement(mouseX, mouseY) && this.widgetMap.getOrDefault(this.activeIndex, Collections.emptyList()).stream()
                .noneMatch(widget -> widget.toRectangleBox().contains(mouseX, mouseY))) {
            this.activeIndex = -1;
            this.writeClientAction(2, buffer -> buffer.writeInt(-1));
        }
        return this.widgetMap.getOrDefault(this.activeIndex, Collections.emptyList()).stream()
                .anyMatch(widget -> widget.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        return this.widgetMap.getOrDefault(this.activeIndex, Collections.emptyList()).stream()
                .anyMatch(widget -> widget.mouseDragged(mouseX, mouseY, button, timeDragged));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        return this.widgetMap.getOrDefault(this.activeIndex, Collections.emptyList()).stream()
                .anyMatch(widget -> widget.mouseReleased(mouseX, mouseY, button));
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 2) {
            this.activeIndex = buffer.readInt();
        }
    }

    private static class SelectionSlotWidget extends Widget {

        public SelectionSlotWidget(Position selfPosition, Size size) {
            super(selfPosition, size);
        }
    }
}
