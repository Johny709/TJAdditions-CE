package tj.capability.impl.workable;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.util.RecipeLRUCache;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.capability.AbstractWorkableHandler;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.OverclockManager;
import tj.capability.TJCapabilities;
import tj.capability.impl.handler.IRecipeHandler;
import tj.util.ItemStackHelper;
import tj.util.TJFluidUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.minecraft.item.ItemStack.areItemStacksEqual;

public class BasicRecipeLogic extends AbstractWorkableHandler<IRecipeHandler> implements IItemFluidHandlerInfo {

    private final RecipeLRUCache previousRecipe = new RecipeLRUCache(10);
    private final OverclockManager<?> overclockManager = new OverclockManager<>();
    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private final List<FluidStack> fluidInputs = new ArrayList<>();
    private final List<FluidStack> fluidOutputs = new ArrayList<>();
    protected ItemStack[] lastItemInputs;
    protected FluidStack[] lastFluidInputs;
    private boolean recipeRecheck = true;
    private int itemOutputIndex;
    private int fluidOutputIndex;

    public BasicRecipeLogic(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    public void invalidate() {
        this.recipeRecheck = true;
    }

    @Override
    protected boolean startRecipe() {
        boolean start = false;
        IItemHandlerModifiable itemHandlerModifiable = this.isDistinct ? this.handler.getInputBus(this.lastInputIndex) : this.handler.getImportItemInventory();
        Recipe recipe = this.previousRecipe.get(itemHandlerModifiable, this.handler.getImportFluidTank());
        if (recipe == null && this.recipeRecheck && this.checkRecipeInputsDirty(itemHandlerModifiable, this.handler.getImportFluidTank())) {
            this.recipeRecheck = false;
            recipe = this.handler.getRecipeMap().findRecipe(this.handler.getMaxVoltage(), itemHandlerModifiable, this.handler.getImportFluidTank(), this.getMinTankCapacity(this.handler.getExportFluidTank()), true);
            if (recipe != null) {
                this.previousRecipe.put(recipe);
                this.previousRecipe.cacheUnutilized();
            }
        }
        if (recipe != null) {
            this.overclockManager.setEUt(recipe.getEUt());
            this.overclockManager.setDuration(recipe.getDuration());
            this.overclockManager.setParallel(this.handler.getParallel());
            this.handler.preOverclock(this.overclockManager, recipe);
            if (this.handler.checkRecipe(recipe) && this.consumeRecipe(recipe)) {
                this.calculateOverclock(this.overclockManager.getEUt(), this.overclockManager.getDuration(), 2.8F);
                this.handler.postOverclock(this.overclockManager, recipe);
                this.energyPerTick = this.overclockManager.getEUt();
                this.setMaxProgress(this.overclockManager.getDuration());
                this.previousRecipe.cacheUtilized();
                start = true;
            }
        }
        if (++this.lastInputIndex == this.busCount)
            this.lastInputIndex = 0;
        return start;
    }

    protected boolean consumeRecipe(Recipe recipe) {
        int parallels = this.overclockManager.getParallel();
        // check for parallel count and if there's enough inputs to be consumed.
        for (CountableIngredient ingredient : recipe.getInputs()) {
            if (ingredient.getCount() > 0) {
                parallels = Math.min(parallels, ItemStackHelper.extractFromItemHandlerByIngredient(this.handler.getImportItemInventory(), ingredient.getIngredient(), ingredient.getCount() * parallels, true) / ingredient.getCount());
                if (parallels < 1)
                    return false;
            } else if (!ItemStackHelper.checkItemHandlerForIngredient(this.handler.getImportItemInventory(), ingredient.getIngredient()))
                return false;
        }
        for (FluidStack stack : recipe.getFluidInputs()) {
            if (stack.amount > 0) {
                parallels = Math.min(parallels, TJFluidUtils.drainFromTanks(this.handler.getImportFluidTank(), stack, stack.amount * parallels, false) / stack.amount);
                if (parallels < 1)
                    return false;
            } else if (!TJFluidUtils.findFluidFromTanks(this.handler.getImportFluidTank(), stack))
                return false;
        }
        // consume item and fluid inputs then add to input list
        for (CountableIngredient ingredient : recipe.getInputs()) {
            ItemStackHelper.extractFromItemHandlerByIngredientToList(this.handler.getImportItemInventory(), ingredient.getIngredient(), ingredient.getCount() * parallels, false, this.itemInputs);
        }
        for (FluidStack stack : recipe.getFluidInputs()) {
            FluidStack fluid = stack.copy();
            fluid.amount = fluid.amount * parallels;
            TJFluidUtils.drainFromTanks(this.handler.getImportFluidTank(), stack, stack.amount * parallels, true);
            this.fluidInputs.add(fluid);
        }
        // add item and fluid outputs to output list
        for (ItemStack stack : recipe.getOutputs()) {
            ItemStack item = stack.copy();
            item.setCount(stack.getCount() * parallels);
            this.itemOutputs.add(item);
        }
        for (FluidStack stack : recipe.getFluidOutputs()) {
            FluidStack fluid = stack.copy();
            fluid.amount = stack.amount * parallels;
            this.fluidOutputs.add(fluid);
        }
        this.overclockManager.setParallel(parallels);
        return true;
    }

    protected boolean checkRecipeInputsDirty(IItemHandler inputs, IMultipleTankHandler fluidInputs) {
        boolean shouldRecheckRecipe = false;
        if (lastItemInputs == null || lastItemInputs.length != inputs.getSlots()) {
            this.lastItemInputs = new ItemStack[inputs.getSlots()];
            Arrays.fill(lastItemInputs, ItemStack.EMPTY);
        }
        if (lastFluidInputs == null || lastFluidInputs.length != fluidInputs.getTanks()) {
            this.lastFluidInputs = new FluidStack[fluidInputs.getTanks()];
        }
        for (int i = 0; i < lastItemInputs.length; i++) {
            ItemStack currentStack = inputs.getStackInSlot(i);
            ItemStack lastStack = lastItemInputs[i];
            if (!areItemStacksEqual(currentStack, lastStack)) {
                this.lastItemInputs[i] = currentStack.isEmpty() ? ItemStack.EMPTY : currentStack.copy();
                shouldRecheckRecipe = true;
            } else if (currentStack.getCount() != lastStack.getCount()) {
                lastStack.setCount(currentStack.getCount());
                shouldRecheckRecipe = true;
            }
        }
        for (int i = 0; i < lastFluidInputs.length; i++) {
            FluidStack currentStack = fluidInputs.getTankAt(i).getFluid();
            FluidStack lastStack = lastFluidInputs[i];
            if ((currentStack == null && lastStack != null) ||
                    (currentStack != null && !currentStack.isFluidEqual(lastStack))) {
                this.lastFluidInputs[i] = currentStack == null ? null : currentStack.copy();
                shouldRecheckRecipe = true;
            } else if (currentStack != null && lastStack != null &&
                    currentStack.amount != lastStack.amount) {
                lastStack.amount = currentStack.amount;
                shouldRecheckRecipe = true;
            }
        }
        return shouldRecheckRecipe;
    }

    protected int getMinTankCapacity(IMultipleTankHandler tanks) {
        if (tanks.getTanks() == 0) {
            return 0;
        }
        int result = Integer.MAX_VALUE;
        for (IFluidTank fluidTank : tanks.getFluidTanks()) {
            result = Math.min(fluidTank.getCapacity(), result);
        }
        return result;
    }

    @Override
    protected int calculateOverclock(long baseEnergy, int duration, float multiplier) {
        long voltage = this.handler.getMaxVoltage();
        baseEnergy *= 4;
        while (duration > 1 && baseEnergy <= voltage) {
            duration /= multiplier;
            baseEnergy *= 4;
        }
        this.overclockManager.setEUtAndDuration(baseEnergy / 4, duration);
        return 0;
    }

    @Override
    protected boolean completeRecipe() {
        for (int i = this.itemOutputIndex; i < this.itemOutputs.size(); i++) {
            ItemStack stack = this.itemOutputs.get(i);
            if (ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, true).isEmpty()) {
                ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, false);
                this.itemOutputIndex++;
            } else return false;
        }
        for (int i = this.fluidOutputIndex; i < this.fluidOutputs.size(); i++) {
            FluidStack stack = this.fluidOutputs.get(i);
            if (this.handler.getExportFluidTank().fill(stack, false) == stack.amount) {
                this.handler.getExportFluidTank().fill(stack, true);
                this.fluidOutputIndex++;
            } else return false;
        }
        this.recipeRecheck = true;
        this.itemInputs.clear();
        this.itemOutputs.clear();
        this.fluidInputs.clear();
        this.fluidOutputs.clear();
        this.itemOutputIndex = 0;
        this.fluidOutputIndex = 0;
        return true;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        compound.setInteger("itemOutputIndex", this.itemOutputIndex);
        compound.setInteger("fluidOutputIndex", this.fluidOutputIndex);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.itemOutputIndex = compound.getInteger("itemOutputIndex");
        this.fluidOutputIndex = compound.getInteger("fluidOutputIndex");
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING)
            return TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING.cast(this);
        return super.getCapability(capability);
    }

    @Override
    public List<ItemStack> getItemInputs() {
        return this.itemInputs;
    }

    @Override
    public List<ItemStack> getItemOutputs() {
        return this.itemOutputs;
    }

    @Override
    public List<FluidStack> getFluidInputs() {
        return this.fluidInputs;
    }

    @Override
    public List<FluidStack> getFluidOutputs() {
        return this.fluidOutputs;
    }
}
