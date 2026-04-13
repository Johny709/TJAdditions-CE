package tj.util.wrappers;

import net.minecraft.item.ItemStack;

public final class GTItemStackWrapper {
    
    private final ItemStack itemStack;
    private long countLong;
    private int count;
    
    public GTItemStackWrapper(ItemStack itemStack, long count) {
        this.itemStack = itemStack;
        this.countLong = count;
        this.count = (int) Math.min(Integer.MAX_VALUE, this.countLong);
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public void increment(long count) {
        this.countLong += count;
        this.count = (int) Math.min(Integer.MAX_VALUE, this.countLong);
    }

    public void setCount(long count) {
        this.countLong = count;
        this.count = (int) Math.min(Integer.MAX_VALUE, this.countLong);
    }

    public long getCountLong() {
        return this.countLong;
    }

    public int getCount() {
        return this.count;
    }
}
