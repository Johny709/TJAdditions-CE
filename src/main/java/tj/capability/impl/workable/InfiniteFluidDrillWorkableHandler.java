package tj.capability.impl.workable;

import gregicadditions.GAValues;
import gregicadditions.worldgen.PumpjackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.IMachineHandler;
import tj.capability.TJCapabilities;
import tj.capability.AbstractWorkableHandler;
import tj.util.TJFluidUtils;

import java.util.ArrayList;
import java.util.List;

import static gregicadditions.GAMaterials.DrillingMud;
import static gregicadditions.GAMaterials.UsedDrillingMud;
import static tj.machines.multi.electric.MetaTileEntityVoidMOreMiner.DRILLING_MUD;


public class InfiniteFluidDrillWorkableHandler extends AbstractWorkableHandler<IMachineHandler> implements IItemFluidHandlerInfo {

    private final List<FluidStack> fluidInputsList = new ArrayList<>();
    private final List<FluidStack> fluidOutputsList = new ArrayList<>();
    private Fluid veinFluid;
    private int outputIndex;
    private long drillingMudAmount;
    private long outputFluidAmount;
    private boolean voidingFluids;

    public InfiniteFluidDrillWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    public void initialize(int tier) {
        super.initialize(tier);
        final World world = this.metaTileEntity.getWorld();
        this.veinFluid = PumpjackHandler.getFluid(world, world.getChunk(this.metaTileEntity.getPos()).x, world.getChunk(this.metaTileEntity.getPos()).z);
        if (this.veinFluid == null) return;
        this.drillingMudAmount = (long) (Math.pow(4, (tier - GAValues.EV)) * 10);
        this.outputFluidAmount = (long) (Math.pow(4, (tier - GAValues.EV)) * 4000);
    }

    @Override
    protected boolean startRecipe() {
        if (TJFluidUtils.drainFromTanksLong(this.handler.getImportFluidTank(), DRILLING_MUD, this.drillingMudAmount, false) == this.drillingMudAmount) {
            TJFluidUtils.drainFromTanksLong(this.handler.getImportFluidTank(), DRILLING_MUD, this.drillingMudAmount, true);
            long amount = this.drillingMudAmount;
            for (; amount > 0; amount -= Integer.MAX_VALUE)
                this.fluidInputsList.add(DrillingMud.getFluid((int) Math.min(Integer.MAX_VALUE, amount)));
            for (amount = this.drillingMudAmount; amount > 0; amount -= Integer.MAX_VALUE)
                this.fluidOutputsList.add(UsedDrillingMud.getFluid((int) Math.min(Integer.MAX_VALUE, amount)));
            amount = this.outputFluidAmount /= (long) (1.00 + 0.05 * this.handler.getMaintenanceProblems());
            for (; amount > 0; amount -= Integer.MAX_VALUE)
                this.fluidOutputsList.add(new FluidStack(this.veinFluid, (int) Math.min(Integer.MAX_VALUE, amount)));
            this.maxProgress = 20;
            return true;
        } else return false;
    }

    @Override
    protected boolean completeRecipe() {
        for (int i = this.outputIndex; i < this.fluidOutputsList.size(); i++) {
            FluidStack stack = this.fluidOutputsList.get(i);
            if (this.voidingFluids || this.handler.getExportFluidTank().fill(stack, false) == stack.amount) {
                this.handler.getExportFluidTank().fill(stack, true);
                this.outputIndex++;
            } else return false;
        }
        this.fluidInputsList.clear();
        this.fluidOutputsList.clear();
        this.outputIndex = 0;
        return true;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        NBTTagList fluidInputsList = new NBTTagList(), fluidOutputsList = new NBTTagList();
        for (FluidStack fluid : this.fluidInputsList)
            fluidInputsList.appendTag(fluid.writeToNBT(new NBTTagCompound()));
        for (FluidStack fluid : this.fluidOutputsList)
            fluidOutputsList.appendTag(fluid.writeToNBT(new NBTTagCompound()));
        compound.setTag("fluidInputsList", fluidInputsList);
        compound.setTag("fluidOutputsList", fluidOutputsList);
        compound.setInteger("outputIndex", this.outputIndex);
        compound.setBoolean("voidFluids", this.voidingFluids);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.outputIndex = compound.getInteger("outputIndex");
        this.voidingFluids = compound.getBoolean("voidFluids");
        NBTTagList fluidInputsList = compound.getTagList("fluidInputsList", 10), fluidOutputsList = compound.getTagList("fluidOutputsList", 10);
        for (int i = 0; i < fluidInputsList.tagCount(); i++)
            this.fluidInputsList.add(FluidStack.loadFluidStackFromNBT(fluidInputsList.getCompoundTagAt(i)));
        for (int i = 0; i < fluidOutputsList.tagCount(); i++)
            this.fluidOutputsList.add(FluidStack.loadFluidStackFromNBT(fluidOutputsList.getCompoundTagAt(i)));
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING)
            return TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING.cast(this);
        return super.getCapability(capability);
    }

    public Fluid getVeinFluid() {
        return this.veinFluid;
    }

    public long getDrillingMudAmount() {
        return this.drillingMudAmount;
    }

    public boolean isVoidingFluids() {
        return this.voidingFluids;
    }

    public void setVoidingFluids(boolean voidingFluids) {
        this.voidingFluids = voidingFluids;
        this.metaTileEntity.markDirty();
    }

    @Override
    public List<FluidStack> getFluidInputs() {
        return this.fluidInputsList;
    }

    @Override
    public List<FluidStack> getFluidOutputs() {
        return this.fluidOutputsList;
    }
}
