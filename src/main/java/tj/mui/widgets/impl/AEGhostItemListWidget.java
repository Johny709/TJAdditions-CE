package tj.mui.widgets.impl;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.function.BiFunction;

public class AEGhostItemListWidget<T> extends AEItemListWidget<T> {

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
}
