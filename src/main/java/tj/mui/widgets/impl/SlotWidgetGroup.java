package tj.mui.widgets.impl;

import gregtech.api.gui.INativeWidget;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import tj.TJ;
import tj.mui.widgets.ISlotGroup;
import tj.mui.widgets.ISlotHandler;
import tj.mixin.gregtech.IMixinAbstractWidgetGroup;
import tj.util.TJItemUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class SlotWidgetGroup extends ScrollingWidgetGroup<SlotWidgetGroup> implements ISlotGroup {

    private final int rowLength;
    protected IItemHandler itemHandler;
    protected UnaryOperator<ItemStack> itemStackTransfer;

    private final Map<ISlotHandler, ItemStack> dragWidgets = new HashMap<>();

    private boolean canAddWidgets = true;

    @SideOnly(Side.CLIENT)
    private ItemStack dragStack;

    @SideOnly(Side.CLIENT)
    private int timer;

    public SlotWidgetGroup(int x, int y, int width, int height, int rowLength) {
        super(x, y, width, height);
        this.rowLength = rowLength;
    }

    public SlotWidgetGroup(int x, int y, int width, int height, int rowLength, boolean horizontal) {
        super(x, y, width, height, horizontal);
        this.rowLength = rowLength;
    }

    public SlotWidgetGroup setItemHandler(IItemHandler itemHandler) {
        this.itemHandler = itemHandler;
        return this;
    }

    public SlotWidgetGroup setItemStackTransfer(UnaryOperator<ItemStack> itemStackTransfer) {
        this.itemStackTransfer = itemStackTransfer;
        return this;
    }

    public void clearWidgets() {
        if (((IMixinAbstractWidgetGroup) this).getInit()) {
            this.clearAllWidgets();
            ((IMixinAbstractWidgetGroup) this).setInit(false);
        }
    }

    @Override
    protected boolean recomputeSize() {
        this.updateElementPositions();
        return super.recomputeSize();
    }

    @Override
    protected void onPositionUpdate() {
        super.onPositionUpdate();
        this.updateElementPositions();
    }

    private void updateElementPositions() {
        final Position position = this.getPosition();
        final int currentPosY = position.y - this.scrollVerticalOffset;
        int totalListHeight = 0;
        int totalListWidth = 0;
        int slotIndex = 0;
        for (Widget widget : this.widgets) {
            if (!(widget instanceof INativeWidget)) continue;
            final Position childPosition = new Position(position.x, currentPosY);
            widget.setParentPosition(childPosition);
            if (slotIndex % this.rowLength == 0) {
                totalListHeight += widget.getSize().height;
                totalListWidth = 0;
            }
            totalListWidth = Math.max(totalListWidth, totalListWidth + widget.getSize().width);
            final Size size = getSize();
            widget.applyScissor(position.x, position.y, size.width, size.height);
            slotIndex++;
        }
        this.scrollHeight = totalListHeight;
        this.scrollWidth = totalListWidth;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        if (this.timer > 0)
            this.timer--;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
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
        return super.mouseReleased(mouseX, mouseY, button);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            final int widgetIndex = buffer.readVarInt();
            final int widgetUpdateId = buffer.readVarInt();
            final Widget widget = this.widgets.get(Math.min(this.widgets.size() - 1, widgetIndex));
            widget.readUpdateInfo(widgetUpdateId, buffer);
        } else if (id == 3) {
            try {
                this.gui.entityPlayer.inventory.setItemStack(buffer.readItemStack());
            } catch (IOException e) {
                TJ.logger.info(e.getMessage());
            }
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 4) {
            try {
                this.gui.entityPlayer.inventory.setItemStack(buffer.readItemStack());
            } catch (IOException e) {
                TJ.logger.info(e.getMessage());
            }
        } else if (id == 5) {
            try {
                ItemStack heldStack = buffer.readItemStack();
                final int size = buffer.readInt();
                int remainder = heldStack.getCount() % size;
                int amountPerSlot = (heldStack.getCount() - remainder) / size;
                ItemStack stack = heldStack;
                for (int i = 0; i < size; i++) {
                    final int index = buffer.readInt();
                    stack = buffer.readItemStack();
                    stack.setCount(amountPerSlot);
                    if (size == 1) {
                        stack = TJItemUtils.insertOrSwap(this.itemHandler, index, stack);
                    } else stack = this.itemHandler.insertItem(index, stack, false);
                    remainder += stack.getCount();
                }
                if (size == 1) {
                    heldStack = stack;
                } else heldStack.setCount(remainder);
                final ItemStack finalHeldStack = heldStack;
                this.gui.entityPlayer.inventory.setItemStack(finalHeldStack);
                this.writeUpdateInfo(3, buffer1 -> buffer1.writeItemStack(finalHeldStack));
            } catch (IOException e) {
                TJ.logger.info(e.getMessage());
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addSlotToDrag(ISlotHandler widget, Runnable callback) {
        if (this.canAddWidgets && !this.dragWidgets.containsKey(widget)) {
            if (this.dragWidgets.isEmpty())
                this.dragStack = this.gui.entityPlayer.inventory.getItemStack().copy();
            final ItemStack heldStack = this.dragStack.copy();
            this.dragWidgets.put(widget, this.dragStack.copy());
            callback.run();
            final int size = this.dragWidgets.size();
            int remainder = heldStack.getCount() % size;
            final int amountPerSlot = (heldStack.getCount() - remainder) / size;
            for (Map.Entry<ISlotHandler, ItemStack> dragWidgets : this.dragWidgets.entrySet()) {
                final ISlotHandler slot = dragWidgets.getKey();
                ItemStack stack = dragWidgets.getValue();
                slot.extract(slot.getSimulatedAmount(), stack, false);
                stack.setCount(amountPerSlot);
                final int count = stack.getCount();
                stack = slot.insert(stack, false);
                slot.setSimulatedAmount(stack.isEmpty() ? amountPerSlot : count - stack.getCount());
                remainder += stack.getCount();
            }
            heldStack.setCount(remainder);
            if (amountPerSlot < 2 && heldStack.getCount() < 1)
                this.canAddWidgets = false;
            this.gui.entityPlayer.inventory.setItemStack(heldStack);
            this.writeClientAction(4, buffer -> buffer.writeItemStack(heldStack));
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

    @Override
    public UnaryOperator<ItemStack> getItemStackTransfer() {
        return this.itemStackTransfer;
    }
}
