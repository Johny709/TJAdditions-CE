package tj.capability.impl.workable;

import gregicadditions.GAValues;
import gregicadditions.worldgen.PumpjackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.IMachineHandler;
import tj.capability.TJCapabilities;
import tj.capability.AbstractWorkableHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregicadditions.GAMaterials.DrillingMud;
import static gregicadditions.GAMaterials.UsedDrillingMud;


public class InfiniteFluidDrillWorkableHandler extends AbstractWorkableHandler<IMachineHandler> implements IItemFluidHandlerInfo {

    private Fluid veinFluid;
    private FluidStack[] veinFluidStack;
    private int[] outputVeinFluidAmount;
    private int outputIndex;
    private int drillingMudAmount;
    private final List<FluidStack> fluidInputsList = new ArrayList<>();
    private final List<FluidStack> fluidOutputsList = new ArrayList<>();

    public InfiniteFluidDrillWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    public void initialize(int tier) {
        super.initialize(tier);
        this.drillingMudAmount = (int) Math.pow(4, (tier - GAValues.EV)) * 10;

        World world = this.metaTileEntity.getWorld();
        BlockPos pos = this.metaTileEntity.getPos();
        this.veinFluid = PumpjackHandler.getFluid(world, world.getChunk(this.metaTileEntity.getPos()).x, world.getChunk(pos).z);
        this.maxProgress = 20;

        if (this.veinFluid == null) return;
        long totalAmount = (long) (Math.pow(4, (tier - GAValues.EV)) * 4000);
        long totalAmount2 = totalAmount;
        int size = 0;
        while (totalAmount > 0) {
            totalAmount -= Integer.MAX_VALUE;
            size++;
        }
        this.veinFluidStack = new FluidStack[size];
        this.outputVeinFluidAmount = new int[size];
        for (int i = 0; i < veinFluidStack.length; i++) {
            this.outputVeinFluidAmount[i] = (int) Math.min(totalAmount2, Integer.MAX_VALUE);
            this.veinFluidStack[i] = new FluidStack(this.veinFluid, this.outputVeinFluidAmount[i]);
            totalAmount2 -= Integer.MAX_VALUE;
        }
    }

    @Override
    protected boolean startRecipe() {
        FluidStack drillingMud = DrillingMud.getFluid(this.drillingMudAmount);
        FluidStack usedDrillingMud = UsedDrillingMud.getFluid(this.drillingMudAmount);
        if (drillingMud.isFluidStackIdentical(this.handler.getImportFluidTank().drain(drillingMud, false)) && this.canOutputFluid(usedDrillingMud, this.drillingMudAmount)) {
            this.fluidInputsList.add(this.handler.getImportFluidTank().drain(DrillingMud.getFluid(this.drillingMudAmount), true));
            int outputAmount = this.handler.getExportFluidTank().fill(UsedDrillingMud.getFluid(this.drillingMudAmount), true);
            this.fluidOutputsList.add(new FluidStack(UsedDrillingMud.getFluid(outputAmount), outputAmount));
            this.fluidOutputsList.addAll(Arrays.asList(this.veinFluidStack));
            this.energyPerTick = this.handler.getMaxVoltage();
            return true;
        }
        return false;
    }

    @Override
    protected boolean completeRecipe() {
        for (int i = this.outputIndex; i < this.veinFluidStack.length; i++) {
            if (!this.canOutputFluid(this.veinFluidStack[i], this.outputVeinFluidAmount[i]))
                return false;
            this.handler.getExportFluidTank().fill(this.veinFluidStack[i], true);
            this.outputIndex++;
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
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.outputIndex = compound.getInteger("outputIndex");
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

    @Override
    public List<FluidStack> getFluidInputs() {
        return this.fluidInputsList;
    }

    @Override
    public List<FluidStack> getFluidOutputs() {
        return this.fluidOutputsList;
    }
}
