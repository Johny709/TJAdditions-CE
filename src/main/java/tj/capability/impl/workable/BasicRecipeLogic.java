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

    protected final ParallelRecipeLRUCache recipeLRUCache = new ParallelRecipeLRUCache(10);
    protected final OverclockManager<?> overclockManager = new OverclockManager<>();
    protected final List<ItemStack> itemInputs = new ArrayList<>();
    protected final List<ItemStack> itemOutputs = new ArrayList<>();
    protected final List<FluidStack> fluidInputs = new ArrayList<>();
    protected final List<FluidStack> fluidOutputs = new ArrayList<>();
    protected ItemStack[] lastItemInputs;
    protected ItemStack[][] lastItemInputsMatrix;
    protected FluidStack[] lastFluidInputs;
    protected boolean allowOverclocking = true;
    protected boolean recipeRecheck = true;
    protected boolean voidingItems;
    protected boolean voidingFluids;
    protected int itemOutputIndex;
    protected int fluidOutputIndex;

    public BasicRecipeLogic(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    public void setAllowOverclocking(boolean allowOverclocking) {
        this.allowOverclocking = allowOverclocking;
    }

    @Override
    public void invalidate() {
        this.lastInputIndex = 0;
        this.recipeRecheck = true;
    }

    @Override
    protected boolean startRecipe() {
        Recipe recipe;
        IItemHandlerModifiable itemInputs;
        if (this.isDistinct) {
            itemInputs = this.handler.getInputBus(this.lastInputIndex);
            recipe = this.trySearchForRecipeDistinct(itemInputs, this.lastInputIndex);
            if (recipe == null) for (int i = 0; i < this.busCount; i++) {
                if (i == this.lastInputIndex) continue;
                itemInputs = this.handler.getInputBus(i);
                if ((recipe = this.trySearchForRecipeDistinct(itemInputs, i)) != null) {
                    this.lastInputIndex = i;
                    break;
                }
            }
        } else {
            itemInputs = this.handler.getImportItemInventory();
            recipe = this.recipeLRUCache.get(itemInputs, this.handler.getImportFluidTank());
            if (recipe == null && (this.recipeRecheck || this.checkRecipeInputsDirty(itemInputs, this.handler.getImportFluidTank()))) {
                this.recipeRecheck = false;
                recipe = this.handler.getRecipeMap().findRecipe(this.handler.getMaxVoltage(), itemInputs, this.handler.getImportFluidTank(), this.getMinTankCapacity(this.handler.getExportFluidTank()), true);
                if (recipe != null) {
                    ((IGTRecipe) recipe).mergeRecipeInputs();
                    this.recipeLRUCache.put(recipe);
                }
            }
        }
        if (recipe != null) {
            this.overclockManager.setEuMultiplier(2.8F);
            this.overclockManager.setEUt(recipe.getEUt());
            this.overclockManager.setDuration(recipe.getDuration());
            this.overclockManager.setParallel(this.handler.getParallel());
            this.handler.preOverclock(this.overclockManager, recipe);
            if (this.handler.checkRecipe(recipe) && this.consumeRecipe(recipe, itemInputs)) {
                this.calculateOverclock(this.overclockManager.getEUt(), this.overclockManager.getDuration(), this.overclockManager.getEuMultiplier());
                this.handler.postOverclock(this.overclockManager, recipe);
                this.energyPerTick = this.overclockManager.getEUt();
                this.setMaxProgress(this.overclockManager.getDuration());
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean completeRecipe() {
        for (int i = this.itemOutputIndex; i < this.itemOutputs.size(); i++) {
            ItemStack stack = this.itemOutputs.get(i);
            if (this.voidingItems || ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, true).isEmpty()) {
                ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, false);
                this.itemOutputIndex++;
            } else return false;
        }
        for (int i = this.fluidOutputIndex; i < this.fluidOutputs.size(); i++) {
            FluidStack stack = this.fluidOutputs.get(i);
            if (this.voidingFluids || this.handler.getExportFluidTank().fill(stack, false) == stack.amount) {
                this.handler.getExportFluidTank().fill(stack, true);
                this.fluidOutputIndex++;
            } else return false;
        }
        this.recipeRecheck = true;
        this.itemOutputIndex = 0;
        this.fluidOutputIndex = 0;
        this.itemInputs.clear();
        this.itemOutputs.clear();
        this.fluidInputs.clear();
        this.fluidOutputs.clear();
        return true;
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
            duration /= multiplier;
            baseEnergy *= 4;
        }
        this.overclockManager.setEUtAndDuration(baseEnergy / 4, duration);
        return 0;
    }

    private Recipe trySearchForRecipeDistinct(IItemHandlerModifiable itemInputs, int index) {
        Recipe recipe;
        recipe = this.recipeLRUCache.get(itemInputs, this.handler.getImportFluidTank());
        if (recipe == null && (this.recipeRecheck || this.checkRecipeInputsDirty(itemInputs, this.handler.getImportFluidTank(), index))) {
            this.recipeRecheck = false;
            recipe = this.handler.getRecipeMap().findRecipe(this.handler.getMaxVoltage(), itemInputs, this.handler.getImportFluidTank(), this.getMinTankCapacity(this.handler.getExportFluidTank()), true);
            if (recipe != null) {
                ((IGTRecipe) recipe).mergeRecipeInputs();
                this.recipeLRUCache.put(recipe);
            }
        }
        return recipe;
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
        this.addItemOutputs(parallels, recipe);
        this.addChancedOutputs(parallels, recipe);
        this.addFluidOutputs(parallels, recipe);
        this.overclockManager.setParallel(parallels);
        return true;
    }

    protected int checkItemInputsAmount(int parallels, Recipe recipe, IItemHandlerModifiable itemInputs) {
        for (CountableIngredient ingredient : ((IGTRecipe) recipe).getMergedItemInputs()) {
            if (ingredient.getCount() > 0) {
                parallels = Math.min(parallels, ItemStackHelper.extractFromItemHandlerByIngredient(itemInputs, ingredient.getIngredient(), ingredient.getCount() * parallels, true) / ingredient.getCount());
                if (parallels < 1) return 0;
            } else if (!ItemStackHelper.checkItemHandlerForIngredient(itemInputs, ingredient.getIngredient()))
                return 0;
        }
        return parallels;
    }

    protected void consumeItemInputs(int parallels, Recipe recipe, IItemHandlerModifiable itemInputs) {
        for (CountableIngredient ingredient : ((IGTRecipe) recipe).getMergedItemInputs()) {
            ItemStackHelper.extractFromItemHandlerByIngredientToList(itemInputs, ingredient.getIngredient(), ingredient.getCount() * parallels, false, this.itemInputs);
        }
    }

    protected void addItemOutputs(int parallels, Recipe recipe) {
        for (ItemStack stack : recipe.getOutputs()) {
            ItemStack item = stack.copy();
            item.setCount(stack.getCount() * parallels);
            this.itemOutputs.add(item);
        }
    }

    protected void addChancedOutputs(int parallels, Recipe recipe) {
        int tier = this.handler.getTier() - GAUtility.getTierByVoltage(this.overclockManager.getEUt());
        for (Recipe.ChanceEntry entry : recipe.getChancedOutputs()) {
            int chance = entry.getChance() + (entry.getBoostPerTier() * tier) / this.overclockManager.getChanceMultiplier() * 100;
            if (Math.random() * 10000 < chance) {
                ItemStack stack = entry.getItemStack().copy();
                stack.setCount(stack.getCount() * parallels);
                this.itemOutputs.add(stack);
            }
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
            TJFluidUtils.drainFromTanks(this.handler.getImportFluidTank(), stack, fluid.amount, true);
            this.fluidInputs.add(fluid);
        }
    }

    protected void addFluidOutputs(int parallels, Recipe recipe) {
        for (FluidStack stack : recipe.getFluidOutputs()) {
            FluidStack fluid = stack.copy();
            fluid.amount *= parallels;
            this.fluidOutputs.add(fluid);
        }
    }

    private boolean checkRecipeInputsDirty(IItemHandler itemInputs, IMultipleTankHandler fluidInputs) {
        boolean shouldRecheckRecipe = false;
        if (this.lastItemInputs == null || this.lastItemInputs.length != itemInputs.getSlots()) {
            this.lastItemInputs = new ItemStack[itemInputs.getSlots()];
            Arrays.fill(this.lastItemInputs, ItemStack.EMPTY);
        }
        if (this.lastFluidInputs == null || this.lastFluidInputs.length != fluidInputs.getTanks()) {
            this.lastFluidInputs = new FluidStack[fluidInputs.getTanks()];
        }
        for (int i = 0; i < this.lastItemInputs.length; i++) {
            ItemStack currentStack = itemInputs.getStackInSlot(i);
            ItemStack lastStack = lastItemInputs[i];
            if (!areItemStacksEqual(currentStack, lastStack)) {
                this.lastItemInputs[i] = currentStack.isEmpty() ? ItemStack.EMPTY : currentStack.copy();
                shouldRecheckRecipe = true;
            } else if (currentStack.getCount() != lastStack.getCount()) {
                lastStack.setCount(currentStack.getCount());
                shouldRecheckRecipe = true;
            }
        }
        for (int i = 0; i < this.lastFluidInputs.length; i++) {
            FluidStack currentStack = fluidInputs.getTankAt(i).getFluid();
            FluidStack lastStack = this.lastFluidInputs[i];
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

    private boolean checkRecipeInputsDirty(IItemHandler itemInputs, IMultipleTankHandler fluidInputs, int index) {
        boolean shouldRecheckRecipe = false;

        if (this.lastItemInputsMatrix == null || this.lastItemInputsMatrix.length != this.busCount) {
            this.lastItemInputsMatrix = new ItemStack[this.busCount][];
        }
        if (this.lastItemInputsMatrix[index] == null || lastItemInputsMatrix[index].length != itemInputs.getSlots()) {
            this.lastItemInputsMatrix[index] = new ItemStack[itemInputs.getSlots()];
            Arrays.fill(this.lastItemInputsMatrix[index], ItemStack.EMPTY);
        }
        if (this.lastFluidInputs == null || this.lastFluidInputs.length != fluidInputs.getTanks()) {
            this.lastFluidInputs = new FluidStack[fluidInputs.getTanks()];
        }
        for (int i = 0; i < this.lastItemInputsMatrix[index].length; i++) {
            ItemStack currentStack = itemInputs.getStackInSlot(i);
            ItemStack lastStack = this.lastItemInputsMatrix[index][i];
            if (!areItemStacksEqual(currentStack, lastStack)) {
                this.lastItemInputsMatrix[index][i] = currentStack.isEmpty() ? ItemStack.EMPTY : currentStack.copy();
                shouldRecheckRecipe = true;
            } else if (currentStack.getCount() != lastStack.getCount()) {
                lastStack.setCount(currentStack.getCount());
                shouldRecheckRecipe = true;
            }
        }
        for (int i = 0; i < this.lastFluidInputs.length; i++) {
            FluidStack currentStack = fluidInputs.getTankAt(i).getFluid();
            FluidStack lastStack = this.lastFluidInputs[i];
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
        compound.setBoolean("voidingItems", this.voidingItems);
        compound.setBoolean("voidingFluids", this.voidingFluids);
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
        this.voidingItems = compound.getBoolean("voidingItems");
        this.voidingFluids = compound.getBoolean("voidingFluids");
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

    public int getParallel() {
        return this.overclockManager.getParallel();
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

    public void setVoidingItems(boolean voidingItems) {
        this.voidingItems = voidingItems;
        this.metaTileEntity.markDirty();
    }

    public boolean isVoidingItems() {
        return this.voidingItems;
    }

    public void setVoidingFluids(boolean voidingFluids) {
        this.voidingFluids = voidingFluids;
        this.metaTileEntity.markDirty();
    }

    public boolean isVoidingFluids() {
        return this.voidingFluids;
    }
}
