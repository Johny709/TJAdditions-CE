package tj.util.wrappers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import tj.TJ;

import javax.annotation.Nullable;

public final class GTFluidStackWrapper {
    
    private final FluidStack fluidStack;
    private long countLong;
    private int count;
    
    public GTFluidStackWrapper(FluidStack fluidStack, long count) {
        this.fluidStack = fluidStack;
        this.countLong = count;
        this.count = (int) Math.min(Integer.MAX_VALUE, this.countLong);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("fluid", this.fluidStack.writeToNBT(new NBTTagCompound()));
        compound.setLong("count", this.countLong);
        return compound;
    }

    public static GTFluidStackWrapper readFromNBT(NBTTagCompound compound) {
        final FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("fluid"));
        final long count = compound.getLong("count");
        return new GTFluidStackWrapper(fluidStack, count);
    }

    public PacketBuffer writeToBuffer(PacketBuffer buffer) {
        return buffer.writeCompoundTag(this.writeToNBT(new NBTTagCompound()));
    }

    @Nullable
    public static GTFluidStackWrapper readFromBuffer(PacketBuffer buffer) {
        GTFluidStackWrapper fluidStackWrapper = null;
        try {
            fluidStackWrapper = readFromNBT(buffer.readCompoundTag());
        } catch (Exception e) {
            TJ.logger.info(e.getMessage());
        }
        return fluidStackWrapper;
    }

    @Override
    public int hashCode() {
        return this.fluidStack.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || this.fluidStack.equals(obj);
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

    public FluidStack getFluidStack() {
        return this.fluidStack;
    }

    public long getCountLong() {
        return this.countLong;
    }

    public int getCount() {
        return this.count;
    }
}
