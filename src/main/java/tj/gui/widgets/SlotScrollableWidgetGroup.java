package tj.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.util.GTLog;
import gregtech.api.util.Position;
import gregtech.api.util.RenderUtil;
import gregtech.api.util.Size;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.input.Keyboard;
import tj.mixin.gregtech.IAbstractWidgetGroupMixin;
import tj.util.ItemStackHelper;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SlotScrollableWidgetGroup extends AbstractWidgetGroup implements ISlotGroup {

    protected static final int SLOT_HEIGHT = 18;
    private final int rowLength;
    protected int totalListHeight;
    protected int scrollOffset;
    protected int scrollPaneWidth = 10;
    protected int lastMouseX;
    protected int lastMouseY;
    protected boolean draggedOnScrollBar;
    protected IItemHandler itemHandler;

    @SideOnly(Side.CLIENT)
    private final Map<ISlotHandler, ItemStack> dragWidgets = new HashMap<>();

    @SideOnly(Side.CLIENT)
    private boolean canAddWidgets = true;

    @SideOnly(Side.CLIENT)
    private ItemStack dragStack;

    @SideOnly(Side.CLIENT)
    private int timer;

    public SlotScrollableWidgetGroup(int x, int y, int width, int height, int rowLength) {
        super(new Position(x, y), new Size(width, height));
        this.rowLength = rowLength;
    }

    public SlotScrollableWidgetGroup setItemHandler(IItemHandler itemHandler) {
        this.itemHandler = itemHandler;
        return this;
    }

    public void clearWidgets() {
        if (((IAbstractWidgetGroupMixin) this).getInit()) {
            this.clearAllWidgets();
            ((IAbstractWidgetGroupMixin) this).setInit(false);
        }
    }

    @Override
    public void addWidget(Widget widget) {
        super.addWidget(widget);
    }

    @Override
    protected boolean recomputeSize() {
        this.updateElementPositions();
        return false;
    }

    private void addScrollOffset(int offset) {
        this.scrollOffset = MathHelper.clamp(this.scrollOffset + offset, 0, this.totalListHeight - this.getSize().height);
        this.updateElementPositions();
    }

    private boolean isOnScrollPane(int mouseX, int mouseY) {
        Position pos = this.getPosition();
        Size size = this.getSize();
        return isMouseOver(pos.x + size.width - this.scrollPaneWidth, pos.y, this.scrollPaneWidth, size.height, mouseX, mouseY);
    }

    @Override
    protected void onPositionUpdate() {
        this.updateElementPositions();
    }

    private void updateElementPositions() {
        Position position = this.getPosition();
        int currentPosY = position.y - this.scrollOffset;
        int totalListHeight = 0;
        for (int i = 0; i < this.widgets.size(); i++) {
            Widget widget = this.widgets.get(i);
            Position childPosition = new Position(position.x, currentPosY);
            widget.setParentPosition(childPosition);
            if (i % this.rowLength == 0) {
                totalListHeight += widget.getSize().getHeight();
            }
            final Size size = getSize();
            widget.applyScissor(position.x, position.y, size.width - this.scrollPaneWidth, size.height);
        }
        this.totalListHeight = totalListHeight;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        //make sure mouse is not hovered on any element when outside of bounds,
        //since foreground rendering is not scissored,
        //because cut tooltips don't really look nice
        if (!this.isPositionInsideScissor(mouseX, mouseY)) {
            mouseX = Integer.MAX_VALUE;
            mouseY = Integer.MAX_VALUE;
        }
        super.drawInForeground(mouseX, mouseY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        //make sure mouse is not hovered on any element when outside of bounds
        if (!this.isPositionInsideScissor(mouseX, mouseY)) {
            mouseX = Integer.MAX_VALUE;
            mouseY = Integer.MAX_VALUE;
        }
        int finalMouseX = mouseX;
        int finalMouseY = mouseY;
        Position position = this.getPosition();
        Size size = this.getSize();
        int paneSize = this.scrollPaneWidth;
        int scrollX = position.x + size.width - paneSize;
        drawSolidRect(scrollX, position.y, paneSize, size.height, 0xFF666666);
        drawSolidRect(scrollX + 1, position.y + 1, paneSize - 2, size.height - 2, 0xFF888888);

        int maxScrollOffset = this.totalListHeight - size.height;
        float scrollPercent = maxScrollOffset == 0 ? 0 : this.scrollOffset / (maxScrollOffset * 1.0f);
        int scrollSliderHeight = 14;
        int scrollSliderY = Math.round(position.y + (size.height - scrollSliderHeight) * scrollPercent);
        drawGradientRect(scrollX + 1, scrollSliderY, paneSize - 2, scrollSliderHeight, 0xFF555555, 0xFF454545);

        RenderUtil.useScissor(position.x, position.y, size.width - paneSize, size.height, () ->
                super.drawInBackground(finalMouseX, finalMouseY, context));
    }

    @Override
    public boolean isWidgetClickable(final Widget widget) {
        if (!super.isWidgetClickable(widget)) {
            return false;
        }
        return this.isWidgetOverlapsScissor(widget);
    }

    private boolean isPositionInsideScissor(int mouseX, int mouseY) {
        return this.isMouseOverElement(mouseX, mouseY) && !this.isOnScrollPane(mouseX, mouseY);
    }

    private boolean isWidgetOverlapsScissor(Widget widget) {
        final Position position = widget.getPosition();
        final Size size = widget.getSize();
        final int x0 = position.x;
        final int y0 = position.y;
        final int x1 = position.x + size.width - 1;
        final int y1 = position.y + size.height - 1;
        return this.isPositionInsideScissor(x0, y0) ||
                this.isPositionInsideScissor(x0, y1) ||
                this.isPositionInsideScissor(x1, y0) ||
                this.isPositionInsideScissor(x1, y1);
    }

    private boolean isBoxInsideScissor(Rectangle rectangle) {
        return this.isPositionInsideScissor(rectangle.x, rectangle.y) &&
                this.isPositionInsideScissor(rectangle.x + rectangle.width - 1, rectangle.y + rectangle.height - 1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (this.isMouseOverElement(mouseX, mouseY, true)) {
            int direction = -MathHelper.clamp(wheelDelta, -1, 1);
            int moveDelta = direction * SLOT_HEIGHT;
            this.addScrollOffset(moveDelta);
            this.writeClientAction(2, buffer -> buffer.writeInt(moveDelta));
            return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        boolean isShiftKeyPressed = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        if (this.isOnScrollPane(mouseX, mouseY)) {
            this.draggedOnScrollBar = true;
        }
        if (this.isPositionInsideScissor(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        } else if (isShiftKeyPressed && this.itemHandler != null) {
            this.writeClientAction(4, buffer -> buffer.writeBoolean(false));
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        int mouseDelta = (mouseY - this.lastMouseY);
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        if (this.draggedOnScrollBar) {
            this.addScrollOffset(mouseDelta);
            this.writeClientAction(2, buffer -> buffer.writeInt(mouseDelta));
            return true;
        }
        if (this.isPositionInsideScissor(mouseX, mouseY)) {
            return super.mouseDragged(mouseX, mouseY, button, timeDragged);
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        this.draggedOnScrollBar = false;
        if (!this.dragWidgets.isEmpty()) {
            this.writeClientAction(5, buffer -> {
                buffer.writeItemStack(this.dragStack);
                buffer.writeInt(this.dragWidgets.size());
                this.dragWidgets.forEach((slot, stack) -> {
                    buffer.writeInt(slot.index());
                    buffer.writeItemStack(this.dragStack);
                    slot.onRemove();
                });
            });
            this.dragWidgets.clear();
            this.canAddWidgets = true;
        }
        if (this.isPositionInsideScissor(mouseX, mouseY)) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            int widgetIndex = buffer.readVarInt();
            int widgetUpdateId = buffer.readVarInt();
            Widget widget = this.widgets.get(Math.min(this.widgets.size() - 1, widgetIndex));
            widget.readUpdateInfo(widgetUpdateId, buffer);
        } else if (id == 2) {
            int time = buffer.readInt();
            if (this.timer > 0 && time == 1)
                this.timer--;
        } else if (id == 3) {
            try {
                this.gui.entityPlayer.inventory.setItemStack(buffer.readItemStack());
            } catch (IOException e) {
                GTLog.logger.error(e);
            }
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 2) {
            this.addScrollOffset(buffer.readInt());
        } else if (id == 3) {
            try {
                this.gui.entityPlayer.inventory.setItemStack(buffer.readItemStack());
            } catch (IOException e) {
                GTLog.logger.error(e);
            }
        } else if (id == 4) {
            ItemStack stack = this.gui.entityPlayer.inventory.getItemStack();
            final ItemStack finalStack = ItemStackHelper.insertIntoItemHandler(this.itemHandler, stack, buffer.readBoolean());
            this.gui.entityPlayer.inventory.setItemStack(finalStack);
            this.writeUpdateInfo(3, buffer1 -> buffer1.writeItemStack(finalStack));
        } else if (id == 5) {
            try {
                ItemStack heldStack = buffer.readItemStack();
                int size = buffer.readInt();
                int remainder = heldStack.getCount() % size;
                int amountPerSlot = (heldStack.getCount() - remainder) / size;
                for (int i = 0; i < size; i++) {
                    int index = buffer.readInt();
                    ItemStack stack = buffer.readItemStack();
                    stack.setCount(amountPerSlot);
                    stack = this.itemHandler.insertItem(index, stack, false);
                    remainder += stack.getCount();
                }
                heldStack.setCount(remainder);
                this.gui.entityPlayer.inventory.setItemStack(heldStack);
                this.writeUpdateInfo(3, buffer1 -> buffer1.writeItemStack(heldStack));
            } catch (IOException e) {
                GTLog.logger.error(e);
            }
        }
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (this.isPositionInsideScissor(mouseX, mouseY)) {
            return super.getIngredientOverMouse(mouseX, mouseY);
        }
        return null;
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        //for phantom targets, show only ones who are fully inside scissor box to avoid visual glitches
        return super.getPhantomTargets(ingredient).stream()
                .filter(it -> this.isBoxInsideScissor(it.getArea()))
                .collect(Collectors.toList());
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.writeUpdateInfo(2, buffer -> buffer.writeInt(1));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addSlotToDrag(ISlotHandler widget, Runnable callback) {
        if (this.canAddWidgets && !this.dragWidgets.containsKey(widget)) {
            if (this.dragWidgets.isEmpty())
                this.dragStack = this.gui.entityPlayer.inventory.getItemStack().copy();
            ItemStack heldStack = this.dragStack.copy();
            this.dragWidgets.put(widget, this.dragStack.copy());
            callback.run();
            int size = this.dragWidgets.size();
            int remainder = heldStack.getCount() % size;
            int amountPerSlot = (heldStack.getCount() - remainder) / size;
            for (Map.Entry<ISlotHandler, ItemStack> dragWidgets : this.dragWidgets.entrySet()) {
                ISlotHandler slot = dragWidgets.getKey();
                ItemStack stack = dragWidgets.getValue();
                slot.extract(slot.getSimulatedAmount(), stack, false);
                stack.setCount(amountPerSlot);
                int count = stack.getCount();
                stack = slot.insert(stack, false);
                slot.setSimulatedAmount(stack.isEmpty() ? amountPerSlot : count - stack.getCount());
                remainder += stack.getCount();
            }
            heldStack.setCount(remainder);
            if (amountPerSlot < 2 && heldStack.getCount() < 1)
                this.canAddWidgets = false;
            this.gui.entityPlayer.inventory.setItemStack(heldStack);
            this.writeClientAction(3, buffer -> buffer.writeItemStack(heldStack));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getTimer() {
        return this.timer;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setTimer(int timer) {
        this.timer = timer;
    }
}
