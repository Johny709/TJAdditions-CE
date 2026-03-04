package tj.capability.impl.workable;

import gregicadditions.GAUtility;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.capability.*;
import tj.capability.impl.handler.IRecipeHandler;
import tj.util.ItemStackHelper;
import tj.util.TJFluidUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.minecraft.item.ItemStack.areItemStacksEqual;

public class BasicRecipeLogic<R extends IRecipeHandler> extends AbstractWorkableHandler<R> implements IItemFluidHandlerInfo {

    private final ParallelRecipeLRUCache recipeLRUCache = new ParallelRecipeLRUCache(10);
    private final OverclockManager<?> overclockManager = new OverclockManager<>();
    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private final List<FluidStack> fluidInputs = new ArrayList<>();
    private final List<FluidStack> fluidOutputs = new ArrayList<>();
    protected ItemStack[] lastItemInputs;
    protected FluidStack[] lastFluidInputs;
    private boolean allowOverclocking = true;
    private boolean recipeRecheck = true;
    private int itemOutputIndex;
    private int fluidOutputIndex;

    public BasicRecipeLogic(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    public void setAllowOverclocking(boolean allowOverclocking) {
        this.allowOverclocking = allowOverclocking;
    }

    @Override
    public void invalidate() {
        this.recipeRecheck = true;
    }

    @Override
    protected boolean startRecipe() {
        IItemHandlerModifiable itemHandlerModifiable = this.isDistinct ? this.handler.getInputBus(this.lastInputIndex) : this.handler.getImportItemInventory();
        Recipe recipe = this.recipeLRUCache.get(itemHandlerModifiable, this.handler.getImportFluidTank());
        if (recipe == null && (this.recipeRecheck || this.checkRecipeInputsDirty(itemHandlerModifiable, this.handler.getImportFluidTank()))) {
            this.recipeRecheck = false;
            recipe = this.handler.getRecipeMap().findRecipe(this.handler.getMaxVoltage(), itemHandlerModifiable, this.handler.getImportFluidTank(), this.getMinTankCapacity(this.handler.getExportFluidTank()), true);
            if (recipe != null) {
                ((IGTRecipe) recipe).mergeRecipeInputs();
                this.recipeLRUCache.put(recipe);
            }
        }
        if (recipe != null && (recipe = this.handler.createRecipe(recipe)) != null) {
            this.overclockManager.setEuMultiplier(2.8F);
            this.overclockManager.setEUt(recipe.getEUt());
            this.overclockManager.setDuration(recipe.getDuration());
            this.overclockManager.setParallel(this.handler.getParallel());
            this.handler.preOverclock(this.overclockManager, recipe);
            if (this.handler.checkRecipe(recipe) && this.consumeRecipe(recipe, itemHandlerModifiable)) {
                this.calculateOverclock(this.overclockManager.getEUt(), this.overclockManager.getDuration(), this.overclockManager.getEuMultiplier());
                this.handler.postOverclock(this.overclockManager, recipe);
                this.energyPerTick = this.overclockManager.getEUt();
                this.setMaxProgress(this.overclockManager.getDuration());
                return true;
            }
        }
        return false;
    }

    protected boolean consumeRecipe(Recipe recipe, IItemHandlerModifiable itemHandlerModifiable) {
        int parallels = this.overclockManager.getParallel();
        // check for parallel count and if there's enough inputs to be consumed.
        if ((parallels = this.checkItemInputsAmount(parallels, recipe, itemHandlerModifiable)) < 1)
            return false;
        if ((parallels = this.checkFluidInputsAmount(parallels, recipe)) < 1)
            return false;
        // consume item and fluid inputs then add to input list
        this.consumeItemInputs(parallels, recipe, itemHandlerModifiable);
        this.consumeFluidInputs(parallels, recipe);
        // add item and fluid outputs to output list
        for (ItemStack stack : recipe.getOutputs()) {
            ItemStack item = stack.copy();
            item.setCount(stack.getCount() * parallels);
            this.itemOutputs.add(item);
        }
        int tier = this.handler.getTier() - GAUtility.getTierByVoltage(this.overclockManager.getEUt());
        for (Recipe.ChanceEntry entry : recipe.getChancedOutputs()) {
            int chance = entry.getChance() + (entry.getBoostPerTier() * tier) / this.overclockManager.getChanceMultiplier() * 100;
            if (Math.random() * 10000 < chance) {
                ItemStack stack = entry.getItemStack().copy();
                stack.setCount(stack.getCount() * parallels);
                this.itemOutputs.add(stack);
            }
        }
        for (FluidStack stack : recipe.getFluidOutputs()) {
            FluidStack fluid = stack.copy();
            fluid.amount *= parallels;
            this.fluidOutputs.add(fluid);
        }
        this.overclockManager.setParallel(parallels);
        return true;
    }

    protected int checkItemInputsAmount(int parallels, Recipe recipe, IItemHandlerModifiable itemHandlerModifiable) {
        for (CountableIngredient ingredient : ((IGTRecipe) recipe).getMergedItemInputs()) {
            if (ingredient.getCount() > 0) {
                parallels = Math.min(parallels, ItemStackHelper.extractFromItemHandlerByIngredient(itemHandlerModifiable, ingredient.getIngredient(), ingredient.getCount() * parallels, true) / ingredient.getCount());
                if (parallels < 1) return 0;
            } else if (!ItemStackHelper.checkItemHandlerForIngredient(itemHandlerModifiable, ingredient.getIngredient()))
                return 0;
        }
        return parallels;
    }

    protected void consumeItemInputs(int parallels, Recipe recipe, IItemHandlerModifiable itemHandlerModifiable) {
        for (CountableIngredient ingredient : ((IGTRecipe) recipe).getMergedItemInputs()) {
            ItemStackHelper.extractFromItemHandlerByIngredientToList(itemHandlerModifiable, ingredient.getIngredient(), ingredient.getCount() * parallels, false, this.itemInputs);
        }
    }

    protected int checkFluidInputsAmount(int parallels, Recipe recipe) {
        for (FluidStack stack : ((IGTRecipe) recipe).getMergedFluidInputs()) {
            if (stack.amount > 0) {
                parallels = Math.min(parallels, TJFluidUtils.drainFromTanks(this.handler.getImportFluidTank(), stack, stack.amount * parallels, false) / stack.amount);
                if (parallels < 1) return 0;
            } else if (!TJFluidUtils.findFluidFromTanks(this.handler.getImportFluidTank(), stack))
                return 0;
        }
        return parallels;
    }

    protected void consumeFluidInputs(int parallels, Recipe recipe) {
        for (FluidStack stack : ((IGTRecipe) recipe).getMergedFluidInputs()) {
            FluidStack fluid = stack.copy();
            fluid.amount *= parallels;
            TJFluidUtils.drainFromTanks(this.handler.getImportFluidTank(), stack, stack.amount * parallels, true);
            this.fluidInputs.add(fluid);
        }
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
        if (!this.allowOverclocking) {
            this.overclockManager.setEUtAndDuration(baseEnergy, duration);
            return 0;
        }
        long voltage = this.handler.getMaxVoltage();
        baseEnergy *= 4;
        while (duration > 1 && baseEnergy <= voltage) {
            duration /= (int) multiplier;
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
        NBTTagList itemInputList = new NBTTagList(), itemOutputList = new NBTTagList();
        NBTTagList fluidInputList = new NBTTagList(), fluidOutputList = new NBTTagList();
        for (ItemStack stack : this.itemInputs)
            itemInputList.appendTag(stack.serializeNBT());
        for (ItemStack stack : this.itemOutputs)
            itemOutputList.appendTag(stack.serializeNBT());
        for (FluidStack stack : this.fluidInputs)
            fluidInputList.appendTag(stack.writeToNBT(new NBTTagCompound()));
        for (FluidStack stack : this.fluidOutputs)
            fluidOutputList.appendTag(stack.writeToNBT(new NBTTagCompound()));
        compound.setTag("itemInputs", itemInputList);
        compound.setTag("itemOutputs", itemOutputList);
        compound.setTag("fluidInputs", fluidInputList);
        compound.setTag("fluidOutputs", fluidOutputList);
        compound.setInteger("itemOutputIndex", this.itemOutputIndex);
        compound.setInteger("fluidOutputIndex", this.fluidOutputIndex);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        NBTTagList itemInputList = compound.getTagList("itemInputs", 10), itemOutputList = compound.getTagList("itemOutputs", 10);
        NBTTagList fluidInputList = compound.getTagList("fluidInputs",10), fluidOutputList = compound.getTagList("fluidOutputs", 10);
        for (int i = 0; i < itemInputList.tagCount(); i++)
            this.itemInputs.add(new ItemStack(itemInputList.getCompoundTagAt(i)));
        for (int i = 0; i < itemOutputList.tagCount(); i++)
            this.itemOutputs.add(new ItemStack(itemOutputList.getCompoundTagAt(i)));
        for (int i = 0; i < fluidInputList.tagCount(); i++)
            this.fluidInputs.add(FluidStack.loadFluidStackFromNBT(fluidInputList.getCompoundTagAt(i)));
        for (int i = 0; i < fluidOutputList.tagCount(); i++)
            this.fluidOutputs.add(FluidStack.loadFluidStackFromNBT(fluidOutputList.getCompoundTagAt(i)));
        this.itemOutputIndex = compound.getInteger("itemOutputIndex");
        this.fluidOutputIndex = compound.getInteger("fluidOutputIndex");
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING)
            return TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING.cast(this);
        return super.getCapability(capability);
    }

    public ParallelRecipeLRUCache getRecipeLRUCache() {
        return this.recipeLRUCache;
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
