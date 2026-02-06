package tj.capability.impl.workable;

import gregtech.api.GTValues;
import gregtech.api.capability.IFuelInfo;
import gregtech.api.capability.IFuelable;
import gregtech.api.capability.impl.FluidFuelInfo;
import gregtech.api.capability.impl.FuelRecipeLogic;
import gregtech.api.capability.impl.ItemFuelInfo;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipes.FuelRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.apache.commons.lang3.ArrayUtils;
import tj.builder.multicontrollers.TJMultiblockControllerBase;
import tj.capability.IGeneratorInfo;
import tj.capability.IHeatInfo;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.AbstractWorkableHandler;
import tj.capability.impl.handler.IBoilerHandler;
import tj.util.ItemStackHelper;

import java.util.*;
import java.util.function.*;

import static gregtech.api.capability.GregtechCapabilities.CAPABILITY_FUELABLE;
import static gregtech.api.unification.material.Materials.*;
import static tj.capability.TJCapabilities.*;
import static tj.capability.TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING;

public class MegaBoilerRecipeLogic extends AbstractWorkableHandler<IBoilerHandler> implements IFuelable, IHeatInfo, IGeneratorInfo, IItemFluidHandlerInfo {

    private static final int CONSUMPTION_MULTIPLIER = 100;
    private static final int BOILING_TEMPERATURE = 100;
    private static final double COAL_BURNTIME = 1600;
    private final List<FluidStack> fluidInput = new ArrayList<>();
    private final List<FluidStack> fluidOutput = new ArrayList<>();
    private final List<ItemStack> itemInput = new ArrayList<>();
    private final List<ItemStack> itemOutput = new ArrayList<>();
    private final Set<FluidStack> lastSearchedFluid = new HashSet<>();

    private boolean hasNoWater;
    private int currentTemperature;
    private int waterConsumption;
    private int steamProduction;
    private int searchCount;
    private int throttlePercentage = 100;

    public MegaBoilerRecipeLogic(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected boolean startRecipe() {
        int fuelMaxBurnTime = this.findFluidInputs();
        if (fuelMaxBurnTime == 0)
            fuelMaxBurnTime = this.findItemInputs();
        if (fuelMaxBurnTime > 0) {
            this.maxProgress = (int) (fuelMaxBurnTime * this.handler.getHeatEfficiencyMultiplier());
        } else return false;
        return true;
    }

    @Override
    protected void progressRecipe(int progress) {
        this.progress++;
        if (this.metaTileEntity.getOffsetTimer() % 20 == 0) {
            double outputMultiplier = this.currentTemperature / (this.maxHeat() * 1.0) * this.getThrottleMultiplier() * this.getThrottleEfficiency();
            this.steamProduction = (int) (this.handler.getBaseSteamOutput() * this.handler.getParallel() * outputMultiplier);
            if (this.currentTemperature < this.maxHeat())
                this.currentTemperature++;
        }

        if (this.currentTemperature < BOILING_TEMPERATURE) {
            this.hasNoWater = false;
            return;
        }
        this.waterConsumption = Math.round((float) this.steamProduction / 160);
        boolean hasEnoughWater = this.hasEnoughFluid(Water.getFluid(this.waterConsumption), this.waterConsumption);
        if (hasEnoughWater && this.hasNoWater) {
            this.metaTileEntity.getWorld().setBlockToAir(this.metaTileEntity.getPos());
            this.metaTileEntity.getWorld().createExplosion(null,
                    this.metaTileEntity.getPos().getX() + 0.5, this.metaTileEntity.getPos().getY() + 0.5, this.metaTileEntity.getPos().getZ() + 0.5,
                    2.0f, true);
        } else {
            if (hasEnoughWater) {
                this.handler.getExportFluidTank().fill(Steam.getFluid(this.steamProduction), true);
                this.handler.getImportFluidTank().drain(Water.getFluid(this.waterConsumption), true);
            } else {
                this.hasNoWater = true;
            }
        }
    }

    @Override
    protected void sleepRecipe() {
        super.sleepRecipe();
        this.stopRecipe();
    }

    @Override
    protected void stopRecipe() {
        if (this.metaTileEntity.getOffsetTimer() % 20 == 0)
            if (this.currentTemperature > 0)
                this.currentTemperature--;
    }

    @Override
    protected boolean completeRecipe() {
        if (!this.itemOutput.isEmpty())
            ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), this.itemOutput.remove(0), false);
        if (!this.fluidOutput.isEmpty())
            this.handler.getExportFluidTank().fill(this.fluidOutput.remove(0), true);
        if (this.metaTileEntity instanceof TJMultiblockControllerBase)
            ((TJMultiblockControllerBase) this.metaTileEntity).calculateMaintenance(this.maxProgress);
        this.itemInput.clear();
        this.fluidInput.clear();
        return true;
    }

    private int findFluidInputs() {
        FluidStack fuelStack = null;
        for (int i = 0; i < this.handler.getImportFluidTank().getTanks(); i++) {
            IFluidTank tank = this.handler.getImportFluidTank().getTankAt(i);
            FluidStack stack = tank.getFluid();
            if (stack == null) continue;
            if (fuelStack == null) {
                if (this.lastSearchedFluid.contains(stack)) continue;
                fuelStack = stack.copy();
                this.lastSearchedFluid.add(fuelStack);
            } else if (fuelStack.isFluidEqual(stack)) {
                long amount = fuelStack.amount + stack.amount;
                fuelStack.amount = (int) Math.min(Integer.MAX_VALUE, amount);
            }
        }
        FuelRecipe dieselRecipe = RecipeMaps.DIESEL_GENERATOR_FUELS.findRecipe(GTValues.V[9], fuelStack);
        if (dieselRecipe != null) {
            fuelStack.amount = (int) Math.ceil(dieselRecipe.getRecipeFluid().amount * CONSUMPTION_MULTIPLIER * this.handler.getParallel() * this.handler.getFuelConsumptionMultiplier() * getThrottleMultiplier());
            if (fuelStack.isFluidStackIdentical(this.handler.getImportFluidTank().drain(fuelStack, false))) {
                this.fluidInput.add(this.handler.getImportFluidTank().drain(fuelStack, true));
                long recipeVoltage = FuelRecipeLogic.getTieredVoltage(dieselRecipe.getMinVoltage());
                int voltageMultiplier = (int) Math.max(1L, recipeVoltage / GTValues.V[GTValues.LV]);
                int burnTime = (int) Math.ceil(dieselRecipe.getDuration() * CONSUMPTION_MULTIPLIER / 2.0 * voltageMultiplier * this.getThrottleMultiplier());
                this.getCarbonDioxideByproduct(burnTime, fuelStack.amount);
                this.lastSearchedFluid.clear();
                return burnTime;
            }
        }
        FuelRecipe denseFuelRecipe = RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.findRecipe(GTValues.V[9], fuelStack);
        if (denseFuelRecipe != null) {
            fuelStack.amount = (int) Math.ceil(denseFuelRecipe.getRecipeFluid().amount * CONSUMPTION_MULTIPLIER * this.handler.getParallel() * this.handler.getFuelConsumptionMultiplier() * getThrottleMultiplier());
            if (fuelStack.isFluidStackIdentical(this.handler.getImportFluidTank().drain(fuelStack, false))) {
                this.fluidInput.add(this.handler.getImportFluidTank().drain(fuelStack, true));
                long recipeVoltage = FuelRecipeLogic.getTieredVoltage(denseFuelRecipe.getMinVoltage());
                int voltageMultiplier = (int) Math.max(1L, recipeVoltage / GTValues.V[GTValues.LV]);
                int burnTime = (int) Math.ceil(denseFuelRecipe.getDuration() * CONSUMPTION_MULTIPLIER * 2 * voltageMultiplier * this.getThrottleMultiplier());
                this.getCarbonDioxideByproduct(burnTime, fuelStack.amount);
                this.lastSearchedFluid.clear();
                return burnTime;
            }
        }
        if (++this.searchCount >= this.handler.getImportFluidTank().getTanks()) {
            this.lastSearchedFluid.clear();
            this.searchCount = 0;
        }
        return 0;
    }

    private void getCarbonDioxideByproduct(int burnTime, int fuelAmount) {
        double carbonBurnTime = COAL_BURNTIME / this.handler.getParallel();
        if (burnTime >= carbonBurnTime) {
            int amount = (int) (fuelAmount * Math.max(0.4, Math.random()));
            this.fluidOutput.add(CarbonDioxide.getFluid(amount));
        }
    }

    private int findItemInputs() {
        int burnTime = 0, count = 0;
        ItemStack fuelStack = null;
        int availableParallels = this.handler.getParallel();
        for (int i = 0; i < this.handler.getImportItemInventory().getSlots(); i++) {
            ItemStack stack = this.handler.getImportItemInventory().getStackInSlot(i);
            if (fuelStack == null) {
                int fuelBurnValue = (int) (this.getBurnValue(stack) * stack.getCount());
                if (fuelBurnValue > 0) {
                    fuelStack = stack.copy();
                    this.itemInput.add(fuelStack);
                }
            }
            if (fuelStack != null && fuelStack.isItemEqual(stack)) {
                int extracted = Math.min(availableParallels, stack.getCount());
                int fuelBurnValue = (int) (this.getBurnValue(stack) * extracted);
                burnTime += fuelBurnValue;
                availableParallels -= extracted;
                count += extracted;
                fuelStack.setCount(count);
                if (this.handler.getImportItemInventory().extractItem(i, extracted, true).getCount() == extracted)
                    this.handler.getImportItemInventory().extractItem(i, extracted, false);
                else return 0;
            }
            if (availableParallels < 1)
                break;
        }
        double ashBurnTime = COAL_BURNTIME / this.handler.getParallel();
        if (burnTime >= ashBurnTime) {
            int amount = (int) ((burnTime / ashBurnTime) * Math.max(0.4, Math.random()));
            this.itemOutput.add(new ItemStack(Item.getByNameOrId("gregtech:meta_item_1"), amount, 2110)); // dark ashes
        }
        return burnTime;
    }

    private double getBurnValue(ItemStack stack) {
        return Math.ceil(TileEntityFurnace.getItemBurnTime(stack) / (50.0 * this.handler.getFuelConsumptionMultiplier() * this.getThrottleMultiplier())) / this.handler.getParallel();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        compound.setInteger("CurrentTemperature", this.currentTemperature);
        compound.setBoolean("HasNoWater", this.hasNoWater);
        compound.setInteger("ThrottlePercentage", this.throttlePercentage);
        if (!this.itemInput.isEmpty())
            compound.setTag("itemInput", this.itemInput.get(0).serializeNBT());
        if (!this.itemOutput.isEmpty())
            compound.setTag("itemOutput", this.itemOutput.get(0).serializeNBT());
        if (!this.fluidInput.isEmpty())
            compound.setTag("fluidInput", this.fluidInput.get(0).writeToNBT(new NBTTagCompound()));
        if (!this.fluidOutput.isEmpty())
            compound.setTag("fluidOutput", this.fluidOutput.get(0).writeToNBT(new NBTTagCompound()));
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.currentTemperature = compound.getInteger("CurrentTemperature");
        this.hasNoWater = compound.getBoolean("HasNoWater");
        if (compound.hasKey("ThrottlePercentage"))
            this.throttlePercentage = compound.getInteger("ThrottlePercentage");
        if (compound.hasKey("itemInput"))
            this.itemInput.add(new ItemStack(compound.getCompoundTag("itemInput")));
        if (compound.hasKey("itemOutput"))
            this.itemOutput.add(new ItemStack(compound.getCompoundTag("itemOutput")));
        if (compound.hasKey("fluidInput"))
            this.fluidInput.add(FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("fluidInput")));
        if (compound.hasKey("fluidOutput"))
            this.fluidOutput.add(FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("fluidOutput")));
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == CAPABILITY_FUELABLE)
            return CAPABILITY_FUELABLE.cast(this);
        if (capability == CAPABILITY_HEAT)
            return CAPABILITY_HEAT.cast(this);
        if (capability == CAPABILITY_GENERATOR)
            return CAPABILITY_GENERATOR.cast(this);
        if (capability == CAPABILITY_ITEM_FLUID_HANDLING)
            return CAPABILITY_ITEM_FLUID_HANDLING.cast(this);
        return super.getCapability(capability);
    }

    @Override
    public Collection<IFuelInfo> getFuels() {
        final LinkedHashMap<Object, IFuelInfo> fuels = new LinkedHashMap<>();
        int fluidCapacity = 0; // fluid capacity is all non water tanks
        FluidStack fluidFuelStack = null;
        for (int i = 0; i < this.handler.getImportFluidTank().getTanks(); i++) {
            IFluidTank tank = this.handler.getImportFluidTank().getTankAt(i);
            FluidStack stack = tank.getFluid();
            if (stack == null || ModHandler.isWater(stack))
                continue;
            long capacity = fluidCapacity + tank.getCapacity();
            fluidCapacity = (int) Math.min(Integer.MAX_VALUE, capacity);
            if (fluidFuelStack == null)
                fluidFuelStack = stack.copy();
            else if (fluidFuelStack.isFluidEqual(stack)) {
                long amount = fluidFuelStack.amount + stack.amount;
                fluidFuelStack.amount = (int) Math.min(Integer.MAX_VALUE, amount);
            }
        }
        FuelRecipe dieselRecipe = RecipeMaps.DIESEL_GENERATOR_FUELS.findRecipe(GTValues.V[9], fluidFuelStack);
        if (dieselRecipe != null) {
            long recipeVoltage = FuelRecipeLogic.getTieredVoltage(dieselRecipe.getMinVoltage());
            int voltageMultiplier = (int) Math.max(1L, recipeVoltage / GTValues.V[GTValues.LV]);
            int burnTime = (int) Math.ceil(dieselRecipe.getDuration() * CONSUMPTION_MULTIPLIER  / 2.0 * voltageMultiplier * getThrottleMultiplier());
            int fuelAmountToConsume = (int) Math.ceil(dieselRecipe.getRecipeFluid().amount * this.handler.getParallel() * CONSUMPTION_MULTIPLIER * this.handler.getFuelConsumptionMultiplier() * getThrottleMultiplier());
            final long fuelBurnTime = ((long) fluidFuelStack.amount * burnTime) / fuelAmountToConsume;
            FluidFuelInfo fluidFuelInfo = new FluidFuelInfo(fluidFuelStack, fluidFuelStack.amount, fluidCapacity, fuelAmountToConsume, fuelBurnTime);
            fuels.put(fluidFuelStack.getUnlocalizedName(), fluidFuelInfo);
        }
        FuelRecipe denseFuelRecipe = RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.findRecipe(GTValues.V[9], fluidFuelStack);
        if (denseFuelRecipe != null) {
            long recipeVoltage = FuelRecipeLogic.getTieredVoltage(denseFuelRecipe.getMinVoltage());
            int voltageMultiplier = (int) Math.max(1L, recipeVoltage / GTValues.V[GTValues.LV]);
            int burnTime = (int) Math.ceil(denseFuelRecipe.getDuration() * CONSUMPTION_MULTIPLIER * 2 * voltageMultiplier * getThrottleMultiplier());
            int fuelAmountToConsume = (int) Math.ceil(denseFuelRecipe.getRecipeFluid().amount * this.handler.getParallel() * CONSUMPTION_MULTIPLIER * this.handler.getFuelConsumptionMultiplier() * getThrottleMultiplier());
            final long fuelBurnTime = ((long) fluidFuelStack.amount * burnTime) / fuelAmountToConsume;
            FluidFuelInfo fluidFuelInfo = new FluidFuelInfo(fluidFuelStack, fluidFuelStack.amount, fluidCapacity, fuelAmountToConsume, fuelBurnTime);
            fuels.put(fluidFuelStack.getUnlocalizedName(), fluidFuelInfo);
        }
        int itemCapacity = 0; // item capacity is all slots
        for (int slotIndex = 0; slotIndex < this.handler.getImportItemInventory().getSlots(); slotIndex++) {
            long capacity = itemCapacity + this.handler.getImportItemInventory().getSlotLimit(slotIndex);
            itemCapacity = (int) Math.min(Integer.MAX_VALUE, capacity);
        }
        int burnTime = 0, count = 0;
        ItemStack itemFuelStack = null;
        for (int i = 0; i < this.handler.getImportItemInventory().getSlots(); i++) {
            ItemStack stack = this.handler.getImportItemInventory().getStackInSlot(i);
            if (itemFuelStack == null) {
                int fuelBurnValue = (int) (this.getBurnValue(stack) * stack.getCount());
                if (fuelBurnValue > 0)
                    itemFuelStack = stack.copy();
            }
            if (itemFuelStack != null && itemFuelStack.isItemEqual(stack)) {
                int extracted = stack.getCount();
                int fuelBurnValue = (int) (this.getBurnValue(stack) * extracted);
                burnTime += fuelBurnValue;
                long amount = count + extracted;
                count = (int) Math.min(Integer.MAX_VALUE, amount);
                itemFuelStack.setCount(count);
            }
        }
        if (itemFuelStack != null) {
            ItemFuelInfo itemFuelInfo = new ItemFuelInfo(itemFuelStack, itemFuelStack.getCount(), itemCapacity, 1, burnTime);
            fuels.put(itemFuelStack.getDisplayName(), itemFuelInfo);
        }
        return fuels.values();
    }

    public double getThrottleMultiplier() {
        return this.throttlePercentage / 100.0;
    }

    public double getThrottleEfficiency() {
        return MathHelper.clamp(1.0 + 0.3*Math.log(this.getThrottleMultiplier()), 0.4, 1.0);
    }

    public void setThrottlePercentage(int throttlePercentage) {
        this.throttlePercentage = throttlePercentage;
    }

    public int getThrottlePercentage() {
        return this.throttlePercentage;
    }

    @Override
    public long heat() {
        return this.currentTemperature;
    }

    @Override
    public long maxHeat() {
        return this.handler.getMaxTemperature();
    }

    @Override
    public List<ItemStack> getItemInputs() {
        return this.itemInput;
    }

    @Override
    public List<ItemStack> getItemOutputs() {
        return this.itemOutput;
    }

    @Override
    public List<FluidStack> getFluidInputs() {
        return this.fluidInput;
    }

    @Override
    public List<FluidStack> getFluidOutputs() {
        return this.fluidOutput;
    }

    @Override
    public long getConsumption() {
        return this.waterConsumption;
    }

    @Override
    public long getProduction() {
        return this.steamProduction;
    }

    @Override
    public String[] consumptionInfo() {
        return ArrayUtils.toArray("machine.universal.consumption", "§9 ", "suffix", "machine.universal.liters.short",  "§r ", Water.getUnlocalizedName(), "machine.universal.tick");
    }

    @Override
    public String[] productionInfo() {
        return ArrayUtils.toArray("machine.universal.producing", "§7 ", "suffix", "machine.universal.liters.short", "§r ", Steam.getUnlocalizedName(), "machine.universal.tick");
    }
}
