package tj.mui.widgets.impl;

import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.input.Keyboard;
import tj.TJ;
import tj.mui.widgets.ISlotGroup;
import tj.mui.widgets.ISlotHandler;
import tj.mui.widgets.TJWidget;
import tj.util.TJItemUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SlotWidgetGroup extends TJWidget<SlotWidgetGroup> implements ISlotGroup {

    protected IItemHandler itemHandler;

    private final Map<ISlotHandler, ItemStack> dragWidgets = new HashMap<>();

    private boolean canAddWidgets = true;

    @SideOnly(Side.CLIENT)
    private ItemStack dragStack;

    @SideOnly(Side.CLIENT)
    private int timer;

    public SlotWidgetGroup(int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
    }

    public SlotWidgetGroup setItemHandler(IItemHandler itemHandler) {
        this.itemHandler = itemHandler;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateScreen() {
        if (this.timer > 0)
            this.timer--;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        final boolean isShiftKeyPressed = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        if (isShiftKeyPressed && this.itemHandler != null) {
            this.writeClientAction(4, buffer -> buffer.writeBoolean(false));
        }
        return false;
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
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
         if (id == 3) {
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
        try {
            if (id == 3) {
                this.gui.entityPlayer.inventory.setItemStack(buffer.readItemStack());
            } else if (id == 4) {
                final ItemStack stack = this.gui.entityPlayer.inventory.getItemStack();
                final ItemStack finalStack = TJItemUtils.insertIntoItemHandler(this.itemHandler, stack, buffer.readBoolean());
                this.gui.entityPlayer.inventory.setItemStack(finalStack);
                this.writeUpdateInfo(3, buffer1 -> buffer1.writeItemStack(finalStack));
            } else if (id == 5) {
                final ItemStack heldStack = buffer.readItemStack();
                final int size = buffer.readInt();
                int remainder = heldStack.getCount() % size;
                int amountPerSlot = (heldStack.getCount() - remainder) / size;
                for (int i = 0; i < size; i++) {
                    final int index = buffer.readInt();
                    ItemStack stack = buffer.readItemStack();
                    stack.setCount(amountPerSlot);
                    stack = this.itemHandler.insertItem(index, stack, false);
                    remainder += stack.getCount();
                }
                heldStack.setCount(remainder);
                this.gui.entityPlayer.inventory.setItemStack(heldStack);
                this.writeUpdateInfo(3, buffer1 -> buffer1.writeItemStack(heldStack));
            }
        } catch (IOException e) {
            TJ.logger.info(e.getMessage());
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