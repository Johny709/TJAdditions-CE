package tj.util.map;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.item.ItemStack;

public final class Strategies {
    public static final ItemStackStrategy ITEMSTACK_STRATEGY = new ItemStackStrategy();

    public static class ItemStackStrategy implements Hash.Strategy<ItemStack> {
        @Override
        public int hashCode(ItemStack o) {
            return o.getItem().hashCode();
        }

        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            return b != null && a.getItem() == b.getItem() && a.getMetadata() == b.getMetadata();
        }
    }
}
