package tj.util.wrappers;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import tj.TJ;

import javax.annotation.Nonnull;

public final class GTItemStackWrapper {
    
    private final ItemStack itemStack;
    private long countLong;
    private int count;

    public GTItemStackWrapper(ItemStack itemStack) {
        this(itemStack, itemStack.getCount());
    }
    
    public GTItemStackWrapper(@Nonnull ItemStack itemStack, long count) {
        this.itemStack = itemStack;
        this.countLong = count;
        this.count = (int) Math.min(Integer.MAX_VALUE, this.countLong);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("item", this.itemStack.serializeNBT());
        compound.setLong("count", this.countLong);
        return compound;
    }

    public static GTItemStackWrapper readFromNBT(NBTTagCompound compound) {
        final ItemStack itemStack = new ItemStack(compound.getCompoundTag("item"));
        final long count = compound.getLong("count");
        return new GTItemStackWrapper(itemStack, count);
    }

    public PacketBuffer writeToBuffer(PacketBuffer buffer) {
        return buffer.writeCompoundTag(this.writeToNBT(new NBTTagCompound()));
    }

    public static GTItemStackWrapper readFromBuffer(PacketBuffer buffer) {
        GTItemStackWrapper itemStackWrapper = null;
        try {
            itemStackWrapper = readFromNBT(buffer.readCompoundTag());
        } catch (Exception e) {
            TJ.logger.info(e.getMessage());
        }
        return itemStackWrapper;
    }

    @Override
    public int hashCode() {
        return this.itemStack.getItem().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || (obj instanceof GTItemStackWrapper && this.itemStack.isItemEqual(((GTItemStackWrapper) obj).itemStack) &&
                this.itemStack.getMetadata() == ((GTItemStackWrapper) obj).itemStack.getMetadata());
    }

    public void increment(long count) {
        this.countLong += count;
        this.count = (int) Math.min(Integer.MAX_VALUE, this.countLong);
    }

    public void decrement(long count) {
        this.countLong -= count;
        this.count = (int) Math.min(Integer.MAX_VALUE, this.countLong);
    }

    public void setCount(long count) {
        this.countLong = count;
        this.count = (int) Math.min(Integer.MAX_VALUE, this.countLong);
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public long getCountLong() {
        return this.countLong;
    }

    public int getCount() {
        return this.count;
    }
}
