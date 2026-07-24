package tj.mui.widgets.impl;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import gregtech.api.gui.igredient.IGhostIngredientTarget;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class AEGhostItemListWidget<T> extends AEItemListWidget<T> implements IGhostIngredientTarget {

    @SafeVarargs
    public AEGhostItemListWidget(int x, int y, int width, int height, IGridNode gridNode, Class<? extends IGridHost>... gridHosts) {
        super(x, y, width, height, gridNode, gridHosts);
    }

    @Override
    protected ItemStack slotAction(IItemHandler itemHandler, int slotIndex, ItemStack playerStack, ElementData data, BiFunction<ItemStack, Boolean, ItemStack> itemStackTransfer) {
        if (data.button == 1) {
            itemHandler.extractItem(slotIndex, Integer.MAX_VALUE, false);
        } else if (itemHandler instanceof IItemHandlerModifiable)
            ((IItemHandlerModifiable) itemHandler).setStackInSlot(slotIndex, playerStack.copy());
        return playerStack;
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object o) {
        final List<IGhostIngredientHandler.Target<?>> targetList = new ArrayList<>();
        int scrollOffset = 0;
        int slotColumn = 0;
        int slotXOffset = 0;
        for (Int2ObjectMap.Entry<Object> entry : this.elements.int2ObjectEntrySet()) {
            if (entry.getValue() instanceof ItemStack) {
                if (slotColumn > 8) {
                    slotColumn = 0;
                    scrollOffset += 18;
                    slotXOffset = 0;
                }
                final int x = this.getPosition().getX() + slotXOffset;
                final int y = this.getPosition().getY() + scrollOffset - (this.scrollOffset % 18);
                targetList.add(new IGhostIngredientHandler.Target<Object>() {

                    @Nonnull
                    @Override
                    public Rectangle getArea() {
                        return new Rectangle(x, y, 18, 18);
                    }

                    @Override
                    public void accept(@Nonnull Object o) {
                        if (o instanceof ItemStack) {
                            writeClientAction(2, buffer -> {
                                buffer.writeBoolean(true); // isSlot is true.
                                buffer.writeInt(entry.getIntKey());
                                buffer.writeInt(0);
                            });
                        }
                    }
                });
                slotXOffset += 18;
                slotColumn++;
            } else {
                if (entry.getIntKey() > 0)
                    scrollOffset += 18;
                scrollOffset += 18;
                slotXOffset = 0;
                slotColumn = 0;
            }
        }
        return targetList;
    }
}
