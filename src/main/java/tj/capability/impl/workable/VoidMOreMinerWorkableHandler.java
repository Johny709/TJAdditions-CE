package tj.capability.impl.workable;

import gregicadditions.machines.multi.IMaintenance;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import tj.capability.*;
import tj.util.ItemStackHelper;
import tj.util.TJFluidUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static gregicadditions.GAMaterials.*;
import static gregicadditions.GAMaterials.UsedDrillingMud;
import static gregicadditions.recipes.categories.handlers.VoidMinerHandler.ORES_3;
import static tj.machines.multi.electric.MetaTileEntityVoidMOreMiner.*;

public class VoidMOreMinerWorkableHandler extends AbstractWorkableHandler<IMachineHandler> implements IHeatInfo, IItemFluidHandlerInfo {

    private static final int CONSUME_START = 100;

    private final List<ItemStack> oreOutputs = new ArrayList<>();
    private final List<FluidStack> fluidInputsList = new ArrayList<>();
    private final List<FluidStack> fluidOutputsList = new ArrayList<>();

    private boolean overheat;
    private boolean voidingFluids;
    private long maxTemperature;
    private long temperature;
    private double currentDrillingFluid = CONSUME_START;

    public VoidMOreMinerWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    public void initialize(int tier) {
        super.initialize(tier);
        final int startTier = tier - GTValues.ZPM;
        final int multiplier = (startTier + 2) * 100;
        final int multiplier2 = Math.min((startTier + 2) * 10, 40);
        final int multiplier3 = startTier > 2 ? (int) Math.pow(2.8, startTier - 2) : 1;
        this.maxTemperature = multiplier * ((long) multiplier2 * multiplier3);
        this.maxProgress = 20;
    }

    @Override
    protected boolean startRecipe() {
        if (this.overheat) {
            if (this.temperature < 1) {
                this.overheat = false;
                this.temperature = 0;
            }
            return false;
        }

        boolean canMineOres = false;
        final long consumeAmount = (long) this.currentDrillingFluid;
        final boolean hasEnoughPyrotheum = TJFluidUtils.drainFromTanksLong(this.handler.getImportFluidTank(), PYROTHEUM, consumeAmount, false) == consumeAmount;
        final boolean hasEnoughCryotheum = TJFluidUtils.drainFromTanksLong(this.handler.getImportFluidTank(), CRYOTHEUM, consumeAmount, false) == consumeAmount;
        if (hasEnoughPyrotheum && hasEnoughCryotheum) {
            TJFluidUtils.drainFromTanksLong(this.handler.getImportFluidTank(), PYROTHEUM, consumeAmount, true);
            TJFluidUtils.drainFromTanksLong(this.handler.getImportFluidTank(), CRYOTHEUM, consumeAmount, true);
            for (long amount = consumeAmount; amount > 0; amount -= Integer.MAX_VALUE)
                this.fluidInputsList.add(Pyrotheum.getFluid((int) Math.min(Integer.MAX_VALUE, amount)));
            for (long amount = consumeAmount; amount > 0; amount =- Integer.MAX_VALUE)
                this.fluidInputsList.add(Cryotheum.getFluid((int) Math.min(Integer.MAX_VALUE, amount)));
            canMineOres = true;
        } else if (hasEnoughPyrotheum) {
            TJFluidUtils.drainFromTanksLong(this.handler.getImportFluidTank(), PYROTHEUM, consumeAmount, true);
            for (long amount = consumeAmount; amount > 0; amount -= Integer.MAX_VALUE)
                this.fluidInputsList.add(Pyrotheum.getFluid((int) Math.min(Integer.MAX_VALUE, amount)));
            this.temperature += (long) (this.currentDrillingFluid / 100.0);
            this.currentDrillingFluid *= 1.02;
            canMineOres = true;
        } else if (hasEnoughCryotheum) {
            TJFluidUtils.drainFromTanksLong(this.handler.getImportFluidTank(), CRYOTHEUM, consumeAmount, true);
            for (long amount = consumeAmount; amount > 0; amount -= Integer.MAX_VALUE)
                this.fluidInputsList.add(Cryotheum.getFluid((int) Math.min(Integer.MAX_VALUE, amount)));
            this.currentDrillingFluid /= 1.02;
            this.temperature -= (long) (this.currentDrillingFluid / 100.0);
        } else {
            return false; // prevent energy consumption if either fluids are not consumed
        }

        if (this.currentDrillingFluid < CONSUME_START) {
            this.currentDrillingFluid = CONSUME_START;
        }
        if (this.temperature > this.maxTemperature) {
            this.overheat = true;
            this.currentDrillingFluid = CONSUME_START;
            return false;
        }

        if (this.metaTileEntity instanceof IMaintenance)
            this.currentDrillingFluid += ((IMaintenance) this.metaTileEntity).getNumProblems();

        if ((this.voidingFluids || TJFluidUtils.fillIntoTanksLong(this.handler.getExportFluidTank(), USED_DRILLING_MUD, consumeAmount, false) == consumeAmount) && TJFluidUtils.drainFromTanksLong(this.handler.getImportFluidTank(), DRILLING_MUD, consumeAmount, false) == consumeAmount) {
            TJFluidUtils.drainFromTanksLong(this.handler.getImportFluidTank(), DRILLING_MUD, consumeAmount, true);
            for (long amount = consumeAmount; amount > 0; amount -= Integer.MAX_VALUE)
                this.fluidInputsList.add(DrillingMud.getFluid((int) Math.min(Integer.MAX_VALUE, amount)));
            for (long amount = consumeAmount; amount > 0; amount -= Integer.MAX_VALUE)
                this.fluidOutputsList.add(UsedDrillingMud.getFluid((int) Math.min(Integer.MAX_VALUE, amount)));

            final long nbOres = this.temperature / 1000;
            if (nbOres != 0 && canMineOres) {
                final List<ItemStack> ores = getOres();
                Collections.shuffle(ores);
                this.oreOutputs.addAll(ores.stream()
                        .limit(10)
                        .flatMap(itemStack -> {
                            final List<ItemStack> stackList = new ArrayList<>();
                            long amount = ThreadLocalRandom.current().nextLong(nbOres * nbOres) + 1;
                            for (; amount > 0; amount -= Integer.MAX_VALUE) {
                                itemStack = itemStack.copy();
                                itemStack.setCount((int) Math.min(Integer.MAX_VALUE, amount));
                                stackList.add(itemStack);
                            }
                            return stackList.stream();
                        }).collect(Collectors.toCollection(ArrayList::new)));
            }
        } else return false;
        this.energyPerTick = this.handler.getMaxVoltage();
        return true;
    }

    @Override
    protected void sleepRecipe() {
        super.sleepRecipe();
        this.stopRecipe();
    }

    @Override
    protected void stopRecipe() {
        if (this.temperature > 0)
            this.temperature--;
    }

    @Override
    protected boolean completeRecipe() {
        this.oreOutputs.forEach(ore -> ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), ore, false));
        this.fluidOutputsList.forEach(fluid -> this.handler.getExportFluidTank().fill(fluid, true));
        this.fluidInputsList.clear();
        this.fluidOutputsList.clear();
        this.oreOutputs.clear();
        return true;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        NBTTagList oreList = new NBTTagList(), fluidInputList = new NBTTagList(), fluidOutputList = new NBTTagList();
        for (ItemStack item : this.oreOutputs)
            oreList.appendTag(item.serializeNBT());
        for (FluidStack fluid : this.fluidInputsList)
            fluidInputList.appendTag(fluid.writeToNBT(new NBTTagCompound()));
        for (FluidStack fluid : this.fluidOutputsList)
            fluidOutputList.appendTag(fluid.writeToNBT(new NBTTagCompound()));
        compound.setLong("temperature", this.temperature);
        compound.setDouble("currentDrillingFluid", this.currentDrillingFluid);
        compound.setBoolean("overheat", this.overheat);
        compound.setBoolean("voidFluids", this.voidingFluids);
        compound.setTag("oreList", oreList);
        compound.setTag("fluidInputList", fluidOutputList);
        compound.setTag("fluidOutputList", fluidOutputList);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        NBTTagList oreList = compound.getTagList("oreList", 10), fluidInputList = compound.getTagList("fluidInputList", 10),
                fluidOutputList = compound.getTagList("fluidOutputList", 10);
        for (int i = 0; i < oreList.tagCount(); i++)
            this.oreOutputs.add(new ItemStack(oreList.getCompoundTagAt(i)));
        for (int i = 0; i < fluidInputList.tagCount(); i++)
            this.fluidInputsList.add(FluidStack.loadFluidStackFromNBT(fluidInputList.getCompoundTagAt(i)));
        for (int i = 0; i < fluidOutputList.tagCount(); i++)
            this.fluidOutputsList.add(FluidStack.loadFluidStackFromNBT(fluidOutputList.getCompoundTagAt(i)));
        this.temperature = compound.getLong("temperature");
        this.currentDrillingFluid = compound.getDouble("currentDrillingFluid");
        this.overheat = compound.getBoolean("overheat");
        this.voidingFluids = compound.getBoolean("voidFluids");
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == TJCapabilities.CAPABILITY_HEAT)
            return TJCapabilities.CAPABILITY_HEAT.cast(this);
        if (capability == TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING)
            return TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING.cast(this);
        return super.getCapability(capability);
    }

    public boolean isOverheat() {
        return this.overheat;
    }

    public void setVoidingFluids(boolean voidingFluids) {
        this.voidingFluids = voidingFluids;
        this.metaTileEntity.markDirty();
    }

    public boolean isVoidingFluids() {
        return this.voidingFluids;
    }

    public double getCurrentDrillingFluid() {
        return this.currentDrillingFluid;
    }

    @Override
    public long heat() {
        return this.temperature;
    }

    @Override
    public long maxHeat() {
        return this.maxTemperature;
    }

    @Override
    public List<ItemStack> getItemOutputs() {
        return this.oreOutputs;
    }

    @Override
    public List<FluidStack> getFluidInputs() {
        return this.fluidInputsList;
    }

    @Override
    public List<FluidStack> getFluidOutputs() {
        return this.fluidOutputsList;
    }

    private static List<ItemStack> getOres() {
        return ORES_3;
    }
}
