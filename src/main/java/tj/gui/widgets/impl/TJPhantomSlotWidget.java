package tj.gui.widgets.impl;

import gregtech.api.gui.igredient.IGhostIngredientTarget;
import gregtech.api.util.GTLog;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import tj.gui.widgets.TJSlotWidget;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class TJPhantomSlotWidget extends TJSlotWidget<TJPhantomSlotWidget> implements IGhostIngredientTarget {

    public TJPhantomSlotWidget(IItemHandler itemHandler, int slotIndex, int x, int y) {
        super(itemHandler, slotIndex, x, y);
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
                public void accept(Object o) {
                    if (!(o instanceof ItemStack)) return;
                    writeClientAction(5, buffer -> buffer.writeItemStack((ItemStack) o));
                }
            });
        }
        return Collections.emptyList();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        this.writeClientAction(6, buffer -> buffer.writeInt(this.slotIndex));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 5) {
            try {
                ItemStack stack = buffer.readItemStack();
                if (this.putItemsPredicate.getAsBoolean()) {
                    this.getItemHandler().extractItem(this.slotIndex, Integer.MAX_VALUE, false);
                    this.insert(stack, false);
                }
            } catch (IOException e) {
                GTLog.logger.info(e.getMessage());
            }
        } else if (id == 6) {
            this.getItemHandler().extractItem(buffer.readInt(), Integer.MAX_VALUE, false);
            this.insert(ItemStack.EMPTY, false);
        }
    }
}
