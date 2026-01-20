package tj.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.AdoptableTextureArea;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

public class PopUpWidget<R extends PopUpWidget<R>> extends AbstractWidgetGroup {

    protected final Int2ObjectMap<Pair<Boolean, WidgetGroup>> widgetMap = new Int2ObjectOpenHashMap<>();
    protected final List<Widget> pendingWidgets = new ArrayList<>();
    protected Rectangle clickArea;
    protected AdoptableTextureArea textureArea;
    protected IntSupplier indexSupplier;
    protected int selectedIndex;

    public PopUpWidget(int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
    }

    public R passPopup(Consumer<PopUpWidget<R>> widgetConsumer) {
        widgetConsumer.accept(this);
        return (R) this;
    }

    public R setClickArea(Rectangle clickArea) {
        this.clickArea = clickArea;
        return (R) this;
    }

    /**
     * Set resizable texture to render in the background.
     * @param textureArea resizable texture
     */
    public R setTexture(AdoptableTextureArea textureArea) {
        this.textureArea = textureArea;
        return (R) this;
    }

    /**
     * Supplier to update index.
     * @param indexSupplier index supplier
     */
    public R setIndexSupplier(IntSupplier indexSupplier) {
        this.indexSupplier = indexSupplier;
        return (R) this;
    }

    /**
     * return true in the predicate for non-selected widgets to be visible but still can not be interacted. Adds a new popup every time this method is called.
     * @param widgets widgets to add.
     */
    public R addPopup(Predicate<WidgetGroup> widgets) {
        WidgetGroup widgetGroup = new WidgetGroup();
        boolean visible = widgets.test(widgetGroup);
        this.addWidget(widgetGroup);
        this.pendingWidgets.clear();
        this.widgetMap.put(this.selectedIndex++ ,Pair.of(visible, widgetGroup));
        return (R) this;
    }

    @Override
    public void initWidget() {
        super.initWidget();
        this.selectedIndex = 0;
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        AbstractWidgetGroup widgetGroup = this.widgetMap.get(this.selectedIndex).getRight();
        return widgetGroup.getPhantomTargets(ingredient);
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        AbstractWidgetGroup widgetGroup = this.widgetMap.get(this.selectedIndex).getRight();
        return widgetGroup.getIngredientOverMouse(mouseX, mouseY);
    }

    @Override
    public void detectAndSendChanges() {
        if (this.indexSupplier != null) {
            this.selectedIndex = this.indexSupplier.getAsInt();
            this.writeUpdateInfo(2, buffer -> buffer.writeInt(this.selectedIndex));
        }
        this.widgetMap.get(this.selectedIndex).getRight().detectAndSendChanges();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateScreen() {
        for (Int2ObjectMap.Entry<Pair<Boolean, WidgetGroup>> widget : this.widgetMap.int2ObjectEntrySet())
            if (this.selectedIndex == widget.getIntKey() || widget.getValue().getLeft())
                widget.getValue().getRight().updateScreen();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        this.widgetMap.get(this.selectedIndex).getRight().drawInForeground(mouseX, mouseY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        if (this.textureArea != null)
            this.textureArea.draw(this.getPosition().getX(), this.getPosition().getY(), this.getSize().getWidth(), this.getSize().getHeight());
        for (Int2ObjectMap.Entry<Pair<Boolean, WidgetGroup>> widget : this.widgetMap.int2ObjectEntrySet())
            if (this.selectedIndex == widget.getIntKey() || widget.getValue().getLeft())
                widget.getValue().getRight().drawInBackground(mouseX, mouseY, context);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        return this.widgetMap.get(this.selectedIndex).getRight().mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.selectedIndex != 0 && !this.isMouseOverElements(this.widgetMap.get(this.selectedIndex).getValue(), mouseX, mouseY)) {
            this.selectedIndex = 0;
            this.writeClientAction(2, buffer -> buffer.writeInt(this.selectedIndex));
        }
        return this.widgetMap.get(this.selectedIndex).getRight().mouseClicked(mouseX, mouseY, button);
    }

    public boolean isMouseOverElements(AbstractWidgetGroup widgetGroup, int mouseX, int mouseY) {
        for (Widget widget : widgetGroup.getContainedWidgets(true)) {
            if (widget.isMouseOverElement(mouseX, mouseY))
                return true;
        }
        return this.isMouseOverElement(mouseX, mouseY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        return this.widgetMap.get(this.selectedIndex).getRight().mouseDragged(mouseX, mouseY, button, timeDragged);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        return this.widgetMap.get(this.selectedIndex).getRight().mouseReleased(mouseX, mouseY, button);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean keyTyped(char charTyped, int keyCode) {
        return this.widgetMap.get(this.selectedIndex).getRight().keyTyped(charTyped, keyCode);
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 2) {
            this.selectedIndex = buffer.readInt();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 2) {
            this.selectedIndex = buffer.readInt();
        }
    }
}
