package tj.capability.impl.workable;

import gregicadditions.GAUtility;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.capability.*;
import tj.capability.impl.handler.IMultiRecipeHandler;
import tj.util.ItemStackHelper;
import tj.util.TJFluidUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.minecraft.item.ItemStack.areItemStacksEqual;

public class ParallelRecipeLogic<R extends IMultiRecipeHandler> extends AbstractParallelWorkableHandler<R> {

    private final ParallelRecipeLRUCache recipeLRUCache = new ParallelRecipeLRUCache(10);
    private final OverclockManager<?> overclockManager = new OverclockManager<>();
    private final Int2ObjectMap<List<ItemStack>> itemInputs = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<List<ItemStack>> itemOutputs = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<List<FluidStack>> fluidInputs = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<List<FluidStack>> fluidOutputs = new Int2ObjectOpenHashMap<>();
    private final List<Recipe> occupiedRecipes = new ArrayList<>();
    private int[] itemOutputIndex = new int[1];
    private int[] fluidOutputIndex = new int[1];
    private boolean[] recipeLock = new boolean[1];
    private boolean[] recipeRecheck = new boolean[1];
    private ItemStack[] lastItemInputs;
    private FluidStack[] lastFluidInputs;
    private boolean distinctRecipes;
    private boolean voidingItems;
    private boolean voidingFluids;

    public ParallelRecipeLogic(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
        Arrays.fill(this.recipeRecheck, true);
        this.occupiedRecipes.add(0, null);
        this.itemInputs.put(0, new ArrayList<>());
        this.itemOutputs.put(0, new ArrayList<>());
        this.fluidInputs.put(0, new ArrayList<>());
        this.fluidOutputs.put(0, new ArrayList<>());
    }

    @Override
    public void setLayer(int i, boolean remove) {
        super.setLayer(i, remove);
        this.itemOutputIndex = Arrays.copyOf(this.itemOutputIndex, this.size);
        this.fluidOutputIndex = Arrays.copyOf(this.fluidOutputIndex, this.size);
        this.recipeLock = Arrays.copyOf(this.recipeLock, this.size);
        this.recipeRecheck = Arrays.copyOf(this.recipeRecheck, this.size);
        if (remove) {
            this.itemInputs.remove(i);
            this.itemOutputs.remove(i);
            this.fluidInputs.remove(i);
            this.fluidOutputs.remove(i);
            this.occupiedRecipes.remove(i);
        } else {
            this.occupiedRecipes.add(this.size - 1, null);
            this.itemInputs.put(this.size - 1, new ArrayList<>());
            this.itemOutputs.put(this.size - 1, new ArrayList<>());
            this.fluidInputs.put(this.size - 1, new ArrayList<>());
            this.fluidOutputs.put(this.size - 1, new ArrayList<>());
        }
    }

    @Override
    public void invalidate() {
        Arrays.fill(this.recipeRecheck, true);
    }

    @Override
    protected boolean startRecipe(int i) {
        Recipe recipe;
        IItemHandlerModifiable itemHandlerModifiable = this.isDistinct ? this.handler.getInputBus(this.lastInputIndex[i]) : this.handler.getImportItemInventory();
        if (this.recipeLock[i]) {
            recipe = this.occupiedRecipes.get(i);
        } else if (this.distinctRecipes) {
            recipe = this.recipeLRUCache.get(itemHandlerModifiable, this.handler.getImportFluidTank(), i, this.occupiedRecipes);
        } else recipe = this.recipeLRUCache.get(itemHandlerModifiable, this.handler.getImportFluidTank());
        if (recipe == null && (this.recipeRecheck[i] || this.checkRecipeInputsDirty(itemHandlerModifiable, this.handler.getImportFluidTank()))) {
            this.recipeRecheck[i] = false;
            if (this.distinctRecipes) {
                recipe = ((IRecipeMap) this.handler.getRecipeMap()).findRecipeDistinct(this.handler.getMaxVoltage(), itemHandlerModifiable, this.handler.getImportFluidTank(), this.getMinTankCapacity(this.handler.getExportFluidTank()), true, this.occupiedRecipes, this.distinctRecipes);
            } else recipe = this.handler.getRecipeMap().findRecipe(this.handler.getMaxVoltage(), itemHandlerModifiable, this.handler.getImportFluidTank(), this.getMinTankCapacity(this.handler.getExportFluidTank()), true);
            if (recipe != null) {
                ((IGTRecipe) recipe).mergeRecipeInputs();
                this.recipeLRUCache.put(recipe);
            }
        }
        if (recipe != null && (recipe = this.handler.createRecipe(recipe, i)) != null) {
            this.overclockManager.setEuMultiplier(2.8F);
            this.overclockManager.setEUt(recipe.getEUt());
            this.overclockManager.setDuration(recipe.getDuration());
            this.overclockManager.setParallel(this.handler.getParallel());
            this.handler.preOverclock(this.overclockManager, recipe, i);
            if (this.handler.checkRecipe(recipe, i) && this.consumeRecipe(recipe, itemHandlerModifiable, i)) {
                this.calculateOverclock(this.overclockManager.getEUt(), this.overclockManager.getDuration(), this.overclockManager.getEuMultiplier(), i);
                this.handler.postOverclock(this.overclockManager, recipe, i);
                this.energyPerTick[i] = this.overclockManager.getEUt();
                this.setMaxProgress(this.overclockManager.getDuration(), i);
                this.occupiedRecipes.set(i, recipe);
                return true;
            }
        }
        return false;
    }

    protected boolean consumeRecipe(Recipe recipe, IItemHandlerModifiable itemHandlerModifiable, int i) {
        int parallels = this.overclockManager.getParallel();
        // check for parallel count and if there's enough inputs to be consumed.
        if ((parallels = this.checkItemInputsAmount(parallels, recipe, itemHandlerModifiable)) < 1)
            return false;
        if ((parallels = this.checkFluidInputsAmount(parallels, recipe)) < 1)
            return false;
        // consume item and fluid inputs then add to input list
        this.consumeItemInputs(parallels, recipe, itemHandlerModifiable, i);
        this.consumeFluidInputs(parallels, recipe, i);
        // add item and fluid outputs to output list
        for (ItemStack stack : recipe.getOutputs()) {
            ItemStack item = stack.copy();
            item.setCount(stack.getCount() * parallels);
            this.itemOutputs.get(i).add(item);
        }
        int tier = this.handler.getTier() - GAUtility.getTierByVoltage(this.overclockManager.getEUt());
        for (Recipe.ChanceEntry entry : recipe.getChancedOutputs()) {
            int chance = entry.getChance() + (entry.getBoostPerTier() * tier) / this.overclockManager.getChanceMultiplier() * 100;
            if (Math.random() * 10000 < chance) {
                ItemStack stack = entry.getItemStack().copy();
                stack.setCount(stack.getCount() * parallels);
                this.itemOutputs.get(i).add(stack);
            }
        }
        for (FluidStack stack : recipe.getFluidOutputs()) {
            FluidStack fluid = stack.copy();
            fluid.amount *= parallels;
            this.fluidOutputs.get(i).add(fluid);
        }
        this.overclockManager.setParallel(this.parallel[i] = parallels);
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

    protected void consumeItemInputs(int parallels, Recipe recipe, IItemHandlerModifiable itemHandlerModifiable, int i) {
        for (CountableIngredient ingredient : ((IGTRecipe) recipe).getMergedItemInputs()) {
            ItemStackHelper.extractFromItemHandlerByIngredientToList(itemHandlerModifiable, ingredient.getIngredient(), ingredient.getCount() * parallels, false, this.itemInputs.get(i));
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

    protected void consumeFluidInputs(int parallels, Recipe recipe, int i) {
        for (FluidStack stack : ((IGTRecipe) recipe).getMergedFluidInputs()) {
            FluidStack fluid = stack.copy();
            fluid.amount *= parallels;
            TJFluidUtils.drainFromTanks(this.handler.getImportFluidTank(), stack, stack.amount * parallels, true);
            this.fluidInputs.get(i).add(fluid);
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
    protected int calculateOverclock(long baseEnergy, int duration, float multiplier, int i) {
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
    protected boolean completeRecipe(int i) {
        List<ItemStack> itemStackList = this.itemOutputs.get(i);
        for (int j = this.itemOutputIndex[i]; j < itemStackList.size(); j++) {
            ItemStack stack = itemStackList.get(j);
            if (this.voidingItems || ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, true).isEmpty()) {
                ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, false);
                this.itemOutputIndex[i]++;
            } else return false;
        }
        List<FluidStack> fluidStackList = this.fluidOutputs.get(i);
        for (int j = this.fluidOutputIndex[i]; j < fluidStackList.size(); j++) {
            FluidStack stack = fluidStackList.get(j);
            if (this.voidingFluids || this.handler.getExportFluidTank().fill(stack, false) == stack.amount) {
                this.handler.getExportFluidTank().fill(stack, true);
                this.fluidOutputIndex[i]++;
            } else return false;
        }
        this.itemOutputIndex[i] = 0;
        this.fluidOutputIndex[i] = 0;
        this.itemInputs.get(i).clear();
        this.itemOutputs.get(i).clear();
        this.fluidInputs.get(i).clear();
        this.fluidOutputs.get(i).clear();
        if (!this.recipeLock[i])
            this.occupiedRecipes.set(i, null);
        return true;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        NBTTagList occupiedRecipeList = new NBTTagList();
        NBTTagList itemInputIndexList = new NBTTagList(), fluidInputIndexList = new NBTTagList(), recipeLockList = new NBTTagList();
        for (int itemInputIndex : this.itemOutputIndex)
            itemInputIndexList.appendTag(new NBTTagInt(itemInputIndex));
        for (int fluidInputIndex : this.fluidOutputIndex)
            fluidInputIndexList.appendTag(new NBTTagInt(fluidInputIndex));
        for (boolean recipeLock : this.recipeLock)
            recipeLockList.appendTag(new NBTTagByte((byte) (recipeLock ? 1 : 0)));
        for (int i = 0; i < this.occupiedRecipes.size(); i++) {
            Recipe recipe = this.occupiedRecipes.get(i);
            if (!this.recipeLock[i] || recipe == null) continue;
            NBTTagCompound recipeCompound = new NBTTagCompound();
            NBTTagList recipeItemInputList = new NBTTagList(), recipeChancedOutputList = new NBTTagList(), recipeItemOutputList = new NBTTagList();
            NBTTagList recipeFluidInputList = new NBTTagList(), recipeFluidOutputList = new NBTTagList();
            for (CountableIngredient ingredient : recipe.getInputs()) {
                NBTTagCompound ingredientCompound = new NBTTagCompound();
                NBTTagList oreDictStackList = new NBTTagList();
                for (ItemStack stack : ingredient.getIngredient().getMatchingStacks())
                    oreDictStackList.appendTag(stack.serializeNBT());
                ingredientCompound.setTag("oreDictStacks", oreDictStackList);
                ingredientCompound.setInteger("count", ingredient.getCount());
                ingredientCompound.setInteger("size", oreDictStackList.tagCount());
                recipeItemInputList.appendTag(ingredientCompound);
            }
            for (Recipe.ChanceEntry entry : recipe.getChancedOutputs()) {
                NBTTagCompound chanceCompound = new NBTTagCompound();
                chanceCompound.setTag("stack", entry.getItemStack().serializeNBT());
                chanceCompound.setInteger("chance", entry.getChance());
                chanceCompound.setInteger("boost", entry.getBoostPerTier());
                recipeChancedOutputList.appendTag(chanceCompound);
            }
            for (ItemStack stack : recipe.getOutputs())
                recipeItemOutputList.appendTag(stack.serializeNBT());
            for (FluidStack stack : recipe.getFluidInputs())
                recipeFluidInputList.appendTag(stack.writeToNBT(new NBTTagCompound()));
            for (FluidStack stack : recipe.getFluidOutputs())
                recipeFluidOutputList.appendTag(stack.writeToNBT(new NBTTagCompound()));
            recipeCompound.setTag("recipeItemInputs", recipeItemInputList);
            recipeCompound.setTag("recipeChancedOutputs", recipeChancedOutputList);
            recipeCompound.setTag("recipeItemOutputs", recipeItemOutputList);
            recipeCompound.setTag("recipeFluidInputs", recipeFluidInputList);
            recipeCompound.setTag("recipeFluidOutputs", recipeFluidOutputList);
            recipeCompound.setInteger("duration", recipe.getDuration());
            recipeCompound.setInteger("EUt", recipe.getEUt());
            recipeCompound.setBoolean("hidden", recipe.isHidden());
            recipeCompound.setInteger("index", i);
            occupiedRecipeList.appendTag(recipeCompound);
        }
        compound.setTag("itemInputIndex", itemInputIndexList);
        compound.setTag("fluidInputIndex", fluidInputIndexList);
        compound.setTag("recipeLock", recipeLockList);
        compound.setTag("occupiedRecipes", occupiedRecipeList);
        compound.setTag("itemInputs", this.writeItemStacksMapNBT(this.itemInputs));
        compound.setTag("itemOutputs", this.writeItemStacksMapNBT(this.itemOutputs));
        compound.setTag("fluidInputs", this.writeFluidStacksMapNBT(this.fluidInputs));
        compound.setTag("fluidOutputs", this.writeFluidStacksMapNBT(this.fluidOutputs));
        compound.setBoolean("distinctRecipes", this.distinctRecipes);
        compound.setBoolean("voidingItems", this.voidingItems);
        compound.setBoolean("voidingFluids", this.voidingFluids);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        NBTTagList occupiedRecipeList = compound.getTagList("occupiedRecipes", 10);
        NBTTagList itemInputIndexList = compound.getTagList("itemInputIndex", 3), fluidInputIndexList = compound.getTagList("fluidInputIndex", 3), recipeLockList = compound.getTagList("recipeLock", 1);
        this.itemOutputIndex = new int[this.size];
        this.fluidOutputIndex = new int[this.size];
        this.recipeLock = new boolean[this.size];
        this.recipeRecheck = new boolean[this.size];
        this.occupiedRecipes.clear();
        for (int i = 0; i < itemInputIndexList.tagCount(); i++)
            this.itemOutputIndex[i] = itemInputIndexList.getIntAt(i);
        for (int i = 0; i < fluidInputIndexList.tagCount(); i++)
            this.fluidOutputIndex[i] = fluidInputIndexList.getIntAt(i);
        for (int i = 0; i < recipeLockList.tagCount(); i++)
            this.recipeLock[i] = ((NBTTagByte) recipeLockList.get(i)).getByte() == 1;
        for (int i = 0; i < this.size; i++) {
            this.occupiedRecipes.add(null);
            this.itemInputs.put(i, new ArrayList<>());
            this.itemOutputs.put(i, new ArrayList<>());
            this.fluidInputs.put(i, new ArrayList<>());
            this.fluidOutputs.put(i, new ArrayList<>());
        }
        for (int i = 0; i < occupiedRecipeList.tagCount(); i++) {
            NBTTagCompound recipeCompound = occupiedRecipeList.getCompoundTagAt(i);
            NBTTagList recipeItemInputList = recipeCompound.getTagList("recipeItemInputs", 10), recipeChancedOutputList = recipeCompound.getTagList("recipeChancedOutputs", 10), recipeItemOutputList = recipeCompound.getTagList("recipeItemOutputs", 10);
            NBTTagList recipeFluidInputList = recipeCompound.getTagList("recipeFluidInputs", 10), recipeFluidOutputList = recipeCompound.getTagList("recipeFluidOutputs", 10);
            List<CountableIngredient> itemInputs = new ArrayList<>();
            for (int j = 0; j < recipeItemInputList.tagCount(); j++) {
                NBTTagCompound ingredientCompound = recipeItemOutputList.getCompoundTagAt(j);
                NBTTagList oreDictStackList = ingredientCompound.getTagList("oreDictStacks", 10);
                ItemStack[] stacks = new ItemStack[ingredientCompound.getInteger("size")];
                for (int k = 0; k < oreDictStackList.tagCount(); k++)
                    stacks[k] = new ItemStack(oreDictStackList.getCompoundTagAt(k));
                itemInputs.add(new CountableIngredient(Ingredient.fromStacks(stacks), ingredientCompound.getInteger("count")));
            }
            List<Recipe.ChanceEntry> chancedOutputs = new ArrayList<>();
            for (int j = 0; j < recipeChancedOutputList.tagCount(); j++) {
                NBTTagCompound chanceCompound = recipeChancedOutputList.getCompoundTagAt(j);
                chancedOutputs.add(new Recipe.ChanceEntry(new ItemStack(chanceCompound.getCompoundTag("stack")), chanceCompound.getInteger("chance"), chanceCompound.getInteger("boost")));
            }
            List<ItemStack> itemOutputs = new ArrayList<>();
            for (int j = 0; j < recipeItemOutputList.tagCount(); j++)
                itemOutputs.add(new ItemStack(recipeItemOutputList.getCompoundTagAt(j)));
            List<FluidStack> fluidInputs = new ArrayList<>();
            for (int j = 0; j < recipeFluidInputList.tagCount(); j++)
                fluidInputs.add(FluidStack.loadFluidStackFromNBT(recipeFluidInputList.getCompoundTagAt(j)));
            List<FluidStack> fluidOutputs = new ArrayList<>();
            for (int j = 0; j < recipeFluidOutputList.tagCount(); j++)
                fluidOutputs.add(FluidStack.loadFluidStackFromNBT(recipeFluidOutputList.getCompoundTagAt(j)));
            Recipe recipe = new Recipe(itemInputs, itemOutputs, chancedOutputs, fluidInputs, fluidOutputs, recipeCompound.getInteger("duration"), recipeCompound.getInteger("EUt"), recipeCompound.getBoolean("hidden"));
            recipe = ((IRecipeMap) this.handler.getRecipeMap()).findRecipe(recipe);
            this.occupiedRecipes.set(recipeCompound.getInteger("index"), recipe);
        }
        this.itemInputs.putAll(this.readItemStackMapNBT(compound.getTagList("itemInputs", 10)));
        this.itemOutputs.putAll(this.readItemStackMapNBT(compound.getTagList("itemOutputs", 10)));
        this.fluidInputs.putAll(this.readFluidStackMapNBT(compound.getTagList("fluidInputs", 10)));
        this.fluidOutputs.putAll(this.readFluidStackMapNBT(compound.getTagList("fluidOutputs", 10)));
        this.distinctRecipes = compound.getBoolean("distinctRecipes");
        this.voidingItems = compound.getBoolean("voidingItems");
        this.voidingFluids = compound.getBoolean("voidingFluids");
    }

    private NBTTagList writeItemStacksMapNBT(Int2ObjectMap<List<ItemStack>> itemStackMap) {
        NBTTagList tagList = new NBTTagList();
        for (Int2ObjectMap.Entry<List<ItemStack>> entry : itemStackMap.int2ObjectEntrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagList itemStackList = new NBTTagList();
            for (ItemStack stack : entry.getValue())
                itemStackList.appendTag(stack.serializeNBT());
            compound.setTag("itemStacks", itemStackList);
            compound.setInteger("index", entry.getIntKey());
            tagList.appendTag(compound);
        }
        return tagList;
    }

    private Int2ObjectMap<List<ItemStack>> readItemStackMapNBT(NBTTagList tagList) {
        Int2ObjectMap<List<ItemStack>> itemStackMap = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound compound = tagList.getCompoundTagAt(i);
            NBTTagList itemStackList = compound.getTagList("itemStacks", 10);
            List<ItemStack> itemStacks = new ArrayList<>();
            for (int j = 0; j < itemStackList.tagCount(); j++) {
                itemStacks.add(new ItemStack(itemStackList.getCompoundTagAt(j)));
            }
            itemStackMap.put(compound.getInteger("index"), itemStacks);
        }
        return itemStackMap;
    }

    private NBTTagList writeFluidStacksMapNBT(Int2ObjectMap<List<FluidStack>> fluidStackMap) {
        NBTTagList tagList = new NBTTagList();
        for (Int2ObjectMap.Entry<List<FluidStack>> entry : fluidStackMap.int2ObjectEntrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagList fluidStackList = new NBTTagList();
            for (FluidStack stack : entry.getValue())
                fluidStackList.appendTag(stack.writeToNBT(new NBTTagCompound()));
            compound.setTag("fluidStacks", fluidStackList);
            compound.setInteger("index", entry.getIntKey());
            tagList.appendTag(compound);
        }
        return tagList;
    }

    private Int2ObjectMap<List<FluidStack>> readFluidStackMapNBT(NBTTagList tagList) {
        Int2ObjectMap<List<FluidStack>> fluidStackMap = new Int2ObjectOpenHashMap<>();
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound compound = tagList.getCompoundTagAt(i);
            NBTTagList fluidStackList = compound.getTagList("fluidStacks", 10);
            List<FluidStack> fluidStacks = new ArrayList<>();
            for (int j = 0; j < fluidStackList.tagCount(); j++) {
                fluidStacks.add(FluidStack.loadFluidStackFromNBT(fluidStackList.getCompoundTagAt(j)));
            }
            fluidStackMap.put(compound.getInteger("index"), fluidStacks);
        }
        return fluidStackMap;
    }

    public List<ItemStack> getItemInputsAt(int i) {
        return this.itemInputs.get(i);
    }

    public List<ItemStack> getItemOutputsAt(int i) {
        return this.itemOutputs.get(i);
    }

    public List<FluidStack> getFluidInputsAt(int i) {
        return this.fluidInputs.get(i);
    }

    public List<FluidStack> getFluidOutputsAt(int i) {
        return this.fluidOutputs.get(i);
    }

    public ParallelRecipeLRUCache getRecipeLRUCache() {
        return this.recipeLRUCache;
    }

    public boolean isRecipeLocked(int i) {
        return this.recipeLock[i];
    }

    public boolean isDistinctRecipes() {
        return this.distinctRecipes;
    }

    public void setDistinctRecipes(boolean distinctRecipes) {
        this.distinctRecipes = distinctRecipes;
        this.metaTileEntity.markDirty();
    }

    public void setRecipeLock(boolean recipeLock, int i) {
        this.recipeLock[i] = recipeLock;
        this.metaTileEntity.markDirty();
    }

    public void setRecipe(Recipe recipe, int i) {
        this.occupiedRecipes.set(i, recipe);
        this.metaTileEntity.markDirty();
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

    public Recipe getRecipe(int i) {
        return this.occupiedRecipes.get(i);
    }
}
