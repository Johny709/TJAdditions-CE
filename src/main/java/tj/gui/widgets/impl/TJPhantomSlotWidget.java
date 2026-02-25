package tj.gui.widgets.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.igredient.IGhostIngredientTarget;
import gregtech.api.util.GTLog;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.TJSlotWidget;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class TJPhantomSlotWidget extends TJSlotWidget<TJPhantomSlotWidget> implements IGhostIngredientTarget {

    private boolean[] areGhostItems;

    public TJPhantomSlotWidget(IItemHandler itemHandler, int slotIndex, int x, int y) {
        super(itemHandler, slotIndex, x, y);
    }

    public TJPhantomSlotWidget setAreGhostItems(boolean[] areGhostItems) {
        this.areGhostItems = areGhostItems;
        return this;
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object o) {
        if (o instanceof ItemStack) {
            return Collections.singletonList(new IGhostIngredientHandler.Target<Object>() {
                @Override
                public Rectangle getArea() {
                    return toRectangleBox();
                }

                @Override
                public void accept(Object item) {
                    if (!(item instanceof ItemStack)) return;
                    areGhostItems[slotIndex] = true;
                    writeClientAction(5, buffer -> {
                        buffer.writeItemStack((ItemStack) item);
                        buffer.writeBoolean(areGhostItems[slotIndex]);
                    });
                }
            });
        }
        return Collections.emptyList();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, context);
        if (this.getItemHandler().getStackInSlot(this.slotIndex).isEmpty()) return;
        if (this.areGhostItems[this.slotIndex])
            TJGuiTextures.SELECTION_BOX_2.draw(this.getPosition().getX(), this.getPosition().getY(), 18, 18);
        else TJGuiTextures.SELECTION_BOX_3.draw(this.getPosition().getX(), this.getPosition().getY(), 18, 18);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY) && this.areGhostItems[this.slotIndex]) {
            this.areGhostItems[this.slotIndex] = false;
            this.writeClientAction(6, buffer -> buffer.writeBoolean(this.areGhostItems[this.slotIndex]));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 5) {
            try {
                ItemStack stack = buffer.readItemStack();
                boolean isGhostItem = buffer.readBoolean();
                if (this.putItemsPredicate != null && !this.putItemsPredicate.test(stack)) return;
                if (this.getItemHandler().getStackInSlot(this.slotIndex).isEmpty() && this.getItemHandler().insertItem(this.slotIndex, stack, true).isEmpty()) {
                    this.getItemHandler().insertItem(this.slotIndex, stack, false);
                    this.areGhostItems[this.slotIndex] = isGhostItem;
                }
            } catch (IOException e) {
                GTLog.logger.info(e.getMessage());
            }
        } else if (id == 6) {
            boolean isGhostItem = buffer.readBoolean();
            if (this.areGhostItems[this.slotIndex]) {
                if (this.takeItemsPredicate != null && !this.takeItemsPredicate.test(this.getItemHandler().getStackInSlot(this.slotIndex))) return;
                this.areGhostItems[this.slotIndex] = isGhostItem;
                this.getItemHandler().extractItem(this.slotIndex, Integer.MAX_VALUE, false);
                this.insert(ItemStack.EMPTY, false);
            }
        }
    }
}
