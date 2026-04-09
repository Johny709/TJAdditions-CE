package tj.util.wrappers;

import net.minecraftforge.fluids.FluidStack;

public final class GTFluidStackWrapper {
    
    private final FluidStack fluidStack;
    private long countLong;
    private int count;
    
    public GTFluidStackWrapper(FluidStack fluidStack) {
        this(fluidStack, fluidStack.amount);
    }
    
    public GTFluidStackWrapper(FluidStack fluidStack, long count) {
        this.fluidStack = fluidStack;
        this.countLong = count;
        this.count = (int) Math.min(Integer.MAX_VALUE, this.countLong);
    }

    public FluidStack getFluidStack() {
        return this.fluidStack;
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
