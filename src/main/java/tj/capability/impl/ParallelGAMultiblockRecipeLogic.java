package tj.capability.impl;

import gregicadditions.GAUtility;
import gregicadditions.utils.GALog;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.TJConfig;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;

import java.util.*;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

/**
 * Parallel Recipe Logic for parallel multiblocks that implement more advanced features like recipe multiplication/parallels and attributes.
 */
public class ParallelGAMultiblockRecipeLogic extends ParallelMultiblockRecipeLogic {

    protected final IntSupplier EUtPercentage;
    protected final IntSupplier durationPercentage;
    protected final IntSupplier chancePercentage;
    protected final IntSupplier stack;

    public ParallelGAMultiblockRecipeLogic(ParallelRecipeMapMultiblockController tileEntity, IntSupplier EUtPercentage, IntSupplier durationPercentage, IntSupplier chancePercentage, IntSupplier stack) {
        super(tileEntity, TJConfig.machines.recipeCacheCapacity);
        this.EUtPercentage = EUtPercentage;
        this.durationPercentage = durationPercentage;
        this.chancePercentage = chancePercentage;
        this.stack = stack;
    }

    public boolean isBatching() {
        return true;
    }

    public RecipeMap<?> getRecipeMap() {
        return this.controller.getMultiblockRecipe();
    }

    @Override
    protected boolean trySearchNewRecipeCombined(int i) {
        long maxVoltage = this.maxVoltage.getAsLong();
        Recipe currentRecipe = null;
        IItemHandlerModifiable importInventory = this.getInputInventory();
        IMultipleTankHandler importFluids = this.getInputTank();
        Recipe foundRecipe;
        if (this.lockRecipe[i] && this.occupiedRecipes.get(i) != null) {
            if (!this.occupiedRecipes.get(i).matches(false, importInventory, importFluids))
                return false;
            foundRecipe = this.occupiedRecipes.get(i);
        } else {
            foundRecipe = this.distinct ? this.previousRecipe.get(importInventory, importFluids, i, this.occupiedRecipes) : this.previousRecipe.get(importInventory, importFluids);
        }
        if (foundRecipe != null) {
            //if previous recipe still matches inputs, try to use it
            currentRecipe = foundRecipe;
        } else {
            boolean dirty = this.checkRecipeInputsDirty(importInventory, importFluids);
            if (dirty || this.forceRecipeRecheck[i]) {
                this.forceRecipeRecheck[i] = false;
                //else, try searching new recipe for given inputs
                currentRecipe = this.findRecipe(maxVoltage, importInventory, importFluids, this.useOptimizedRecipeLookUp);
                if (currentRecipe != null) {
                    this.previousRecipe.put(currentRecipe);
                }
            }
        }
        if (currentRecipe == null) {
            return false;
        }
        this.occupiedRecipes.set(i, currentRecipe);
        if (this.isBatching()) {
            currentRecipe = this.createRecipe(maxVoltage, importInventory, importFluids, currentRecipe, i);
        }
        if (!this.setupAndConsumeRecipeInputs(currentRecipe)) {
            return false;
        }
        this.setupRecipe(currentRecipe, i);
        return true;
    }

    @Override
    protected boolean trySearchNewRecipeDistinct(int i) {
        long maxVoltage = this.maxVoltage.getAsLong();
        Recipe currentRecipe;
        List<IItemHandlerModifiable> importInventory = this.getInputBuses();
        IMultipleTankHandler importFluids = this.getInputTank();

        // Our caching implementation
        // This guarantees that if we get a recipe cache hit, our efficiency is no different from other machines
        Recipe foundRecipe = this.distinct ? this.previousRecipe.get(importInventory.get(this.lastRecipeIndex[i]), importFluids, i, this.occupiedRecipes) : this.previousRecipe.get(importInventory.get(this.lastRecipeIndex[i]), importFluids);
        HashSet<Integer> foundRecipeIndex = new HashSet<>();
        if (foundRecipe != null) {
            currentRecipe = foundRecipe;
            this.occupiedRecipes.set(i, currentRecipe);
            currentRecipe = this.createRecipe(maxVoltage, importInventory.get(this.lastRecipeIndex[i]), importFluids, currentRecipe, i);
            if (this.setupAndConsumeRecipeInputs(currentRecipe, this.lastRecipeIndex[i])) {
                this.setupRecipe(currentRecipe, i);
                return true;
            }
            foundRecipeIndex.add(this.lastRecipeIndex[i]);
        }

        for (int j = 0; j < importInventory.size(); j++) {
            if (j == this.lastRecipeIndex[i]) {
                continue;
            }
            foundRecipe = this.distinct ? this.previousRecipe.get(importInventory.get(this.lastRecipeIndex[i]), importFluids, i, this.occupiedRecipes) : this.previousRecipe.get(importInventory.get(this.lastRecipeIndex[i]), importFluids);
            if (foundRecipe != null) {
                currentRecipe = foundRecipe;
                this.occupiedRecipes.set(i, currentRecipe);
                currentRecipe = this.createRecipe(maxVoltage, importInventory.get(j), importFluids, currentRecipe, i);
                if (this.setupAndConsumeRecipeInputs(currentRecipe, j)) {
                    this.setupRecipe(currentRecipe, i);
                    return true;
                }
                foundRecipeIndex.add(j);
            }
        }

        // On a cache miss, our efficiency is much worse, as it will check
        // each bus individually instead of the combined inventory all at once.
        for (int j = 0; j < importInventory.size(); j++) {
            if (foundRecipeIndex.contains(j)) {
                continue;
            }
            IItemHandlerModifiable bus = importInventory.get(j);
            boolean dirty = this.checkRecipeInputsDirty(bus, importFluids, j);
            if (!dirty && !forceRecipeRecheck[i]) {
                continue;
            }
            this.forceRecipeRecheck[i] = false;
            currentRecipe = this.findRecipe(maxVoltage, bus, importFluids, this.useOptimizedRecipeLookUp);
            if (currentRecipe == null) {
                continue;
            }
            this.previousRecipe.put(currentRecipe);
            this.occupiedRecipes.set(i, currentRecipe);
            if (this.isBatching()) {
                currentRecipe = this.createRecipe(maxVoltage, bus, importFluids, currentRecipe, i);
            }
            if (!this.setupAndConsumeRecipeInputs(currentRecipe, j)) {
                continue;
            }
            this.lastRecipeIndex[i] = j;
            this.setupRecipe(currentRecipe, i);
            return true;
        }
        return false;
    }

    protected Recipe createRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, Recipe matchingRecipe, int j) {
        int maxItemsLimit = this.stack.getAsInt() * this.controller.getBatchMode().getAmount();
        int EUt = matchingRecipe.getEUt();
        int currentTier = this.getOverclockingTier(maxVoltage);
        int tierNeeded;
        int minMultiplier = Integer.MAX_VALUE;

        tierNeeded = Math.max(1, GAUtility.getTierByVoltage(EUt));
        maxItemsLimit *= currentTier - tierNeeded;
        maxItemsLimit = Math.max(1, maxItemsLimit);
        if (maxItemsLimit == 1) {
            return matchingRecipe;
        }

        Set<ItemStack> countIngredients = new HashSet<>();
        if (!matchingRecipe.getInputs().isEmpty()) {
            this.findIngredients(countIngredients, inputs);
            minMultiplier = Math.min(maxItemsLimit, this.getMinRatioItem(countIngredients, matchingRecipe, maxItemsLimit));
        }

        Object2IntMap<String> countFluid = new Object2IntOpenHashMap<>();
        if (!matchingRecipe.getFluidInputs().isEmpty()) {

            this.findFluid(countFluid, fluidInputs);
            minMultiplier = Math.min(minMultiplier, this.getMinRatioFluid(countFluid, matchingRecipe, maxItemsLimit));
        }

        if (minMultiplier == Integer.MAX_VALUE) {
            GALog.logger.error("Cannot calculate ratio of items for large multiblocks");
            return null;
        }

        int tierDiff = currentTier - tierNeeded;
        for (int i = 0; i < tierDiff; i++) {
            int attemptItemsLimit = this.stack.getAsInt() * this.controller.getBatchMode().getAmount();
            attemptItemsLimit *= tierDiff - i;
            attemptItemsLimit = Math.max(1, attemptItemsLimit);
            attemptItemsLimit = Math.min(minMultiplier, attemptItemsLimit);
            List<CountableIngredient> newRecipeInputs = new ArrayList<>();
            List<FluidStack> newFluidInputs = new ArrayList<>();
            List<ItemStack> outputI = new ArrayList<>();
            List<FluidStack> outputF = new ArrayList<>();
            this.multiplyInputsAndOutputs(newRecipeInputs, newFluidInputs, outputI, outputF, matchingRecipe, attemptItemsLimit);

            RecipeBuilder<?> newRecipe = getRecipeMap().recipeBuilder();
            this.copyChancedItemOutputs(newRecipe, matchingRecipe, attemptItemsLimit);

            // determine if there is enough room in the output to fit all of this
            // if there isn't, we can't process this recipe.
            List<ItemStack> totalOutputs = newRecipe.getChancedOutputs().stream().map(Recipe.ChanceEntry::getItemStack).collect(Collectors.toList());
            totalOutputs.addAll(outputI);
            this.parallel[j] = attemptItemsLimit;
            return this.buildRecipe(newRecipe, matchingRecipe, newRecipeInputs, newFluidInputs, outputI, outputF);
        }
        this.parallel[j] = 1;
        return matchingRecipe;
    }

    protected Recipe buildRecipe(RecipeBuilder<?> recipeBuilder, Recipe recipe, Collection<CountableIngredient> inputs, Collection<FluidStack> fluidInputs, Collection<ItemStack> outputs, Collection<FluidStack> fluidOutputs) {
        return recipeBuilder.inputsIngredients(inputs)
                .fluidInputs(fluidInputs)
                .outputs(outputs)
                .fluidOutputs(fluidOutputs)
                .EUt((int) Math.min(Integer.MAX_VALUE, Math.max(1, (long) recipe.getEUt() * this.EUtPercentage.getAsInt() / 100)))
                .duration(Math.max(1, recipe.getDuration() * this.controller.getBatchMode().getAmount() * this.durationPercentage.getAsInt() / 100))
                .build()
                .getResult();
    }

    protected void copyChancedItemOutputs(RecipeBuilder<?> newRecipe, Recipe oldRecipe, int multiplier) {
        for (Recipe.ChanceEntry s : oldRecipe.getChancedOutputs()) {
            int chance = Math.min(10000, s.getChance() * this.chancePercentage.getAsInt() / 100);
            int boost = s.getBoostPerTier() * this.chancePercentage.getAsInt() / 100;
            ItemStack stack = s.getItemStack().copy();
            int count = stack.getCount();
            stack.setCount(count * multiplier);
            newRecipe.chancedOutput(stack, chance, boost);
        }
    }


    protected void findIngredients(Set<ItemStack> countIngredients, IItemHandlerModifiable inputs) {
        for (int slot = 0; slot < inputs.getSlots(); slot++) {
            ItemStack wholeItemStack = inputs.getStackInSlot(slot);
            // skip empty slots
            String name = wholeItemStack.getItem().getUnlocalizedNameInefficiently(wholeItemStack);
            if (name.equals("tile.air"))
                continue;
            boolean found = false;
            for (ItemStack i : countIngredients) {
                if (i.isItemEqual(wholeItemStack)) {
                    i.setCount(i.getCount() + wholeItemStack.getCount());
                    found = true;
                    break;
                }
            }
            if (!found) {
                countIngredients.add(wholeItemStack.copy());
            }
        }
    }

    protected int getMinRatioItem(Set<ItemStack> countIngredients, Recipe r, int maxItemsLimit) {
        int minMultiplier = Integer.MAX_VALUE;
        for (CountableIngredient ci : r.getInputs()) {
            if (ci.getCount() == 0) {
                continue;
            }
            for (ItemStack wholeItemStack : countIngredients) {
                if (ci.getIngredient().apply(wholeItemStack)) {
                    int ratio = Math.min(maxItemsLimit, wholeItemStack.getCount() / ci.getCount());
                    if (ratio < minMultiplier) {
                        minMultiplier = ratio;
                    }
                    break;
                }
            }
        }
        return minMultiplier;
    }

    protected int getMinRatioFluid(Object2IntMap<String> countFluid, Recipe r, int maxItemsLimit) {
        int minMultiplier = Integer.MAX_VALUE;
        for (FluidStack fs : r.getFluidInputs()) {
            if (fs.amount != 0) { // skip notConsumable fluids
                String name = fs.getFluid().getUnlocalizedName();
                int ratio = Math.min(maxItemsLimit, countFluid.get(name) / fs.amount);
                if (ratio < minMultiplier) {
                    minMultiplier = ratio;
                }
            }
        }
        return minMultiplier;
    }

    protected void findFluid(Object2IntMap<String> countFluid, IMultipleTankHandler fluidInputs) {
        for (IFluidTank tank : fluidInputs) {
            if (tank.getFluid() != null) {
                String name = tank.getFluid().getUnlocalizedName();
                if (countFluid.containsKey(name)) {
                    int existingValue = countFluid.get(name);
                    countFluid.put(name, existingValue + tank.getFluidAmount());
                } else {
                    countFluid.put(name, tank.getFluidAmount());
                }
            }
        }
    }

    protected void multiplyInputsAndOutputs(List<CountableIngredient> newRecipeInputs, List<FluidStack> newFluidInputs, List<ItemStack> outputI, List<FluidStack> outputF, Recipe r, int multiplier) {
        for (CountableIngredient ci : r.getInputs()) {
            CountableIngredient newIngredient = new CountableIngredient(ci.getIngredient(), ci.getCount() * multiplier);
            newRecipeInputs.add(newIngredient);
        }
        for (FluidStack fs : r.getFluidInputs()) {
            FluidStack newFluid = new FluidStack(fs.getFluid(), fs.amount * multiplier);
            newFluidInputs.add(newFluid);
        }
        for (ItemStack s : r.getOutputs()) {
            int num = s.getCount() * multiplier;
            ItemStack itemCopy = s.copy();
            itemCopy.setCount(num);
            outputI.add(itemCopy);
        }
        for (FluidStack f : r.getFluidOutputs()) {
            int fluidNum = f.amount * multiplier;
            FluidStack fluidCopy = f.copy();
            fluidCopy.amount = fluidNum;
            outputF.add(fluidCopy);
        }
    }
}
