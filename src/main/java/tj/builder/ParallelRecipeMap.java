package tj.builder;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.map.MapFluidIngredient;
import gregtech.api.recipes.map.MapItemStackIngredient;
import gregtech.api.util.GTUtility;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.*;


public final class ParallelRecipeMap {

    private final int minInputs, maxInputs;
    private final int minOutputs, maxOutputs;
    private final int minFluidInputs, maxFluidInputs;
    private final int minFluidOutputs, maxFluidOutputs;
    private final Collection<Recipe> recipeList;
    private final Object2ObjectMap<MapFluidIngredient, Collection<Recipe>> recipeFluidMap = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<MapItemStackIngredient, Collection<Recipe>> recipeItemMap = new Object2ObjectOpenHashMap<>();
    private final Object2ByteMap<Recipe> recipeIngredientCountMap = new Object2ByteOpenHashMap<>();
    private final RecipeMap<?> recipeMap;

    public ParallelRecipeMap(RecipeMap<?> recipeMap) {
        this.recipeMap = recipeMap;
        this.minInputs = recipeMap.getMinInputs();
        this.minFluidInputs = recipeMap.getMinFluidInputs();
        this.minOutputs = recipeMap.getMinOutputs();
        this.minFluidOutputs = recipeMap.getMinFluidOutputs();

        this.maxInputs = recipeMap.getMaxInputs();
        this.maxFluidInputs = recipeMap.getMaxFluidInputs();
        this.maxOutputs = recipeMap.getMaxOutputs();
        this.maxFluidOutputs = recipeMap.getMaxFluidOutputs();

        this.recipeList = Collections.unmodifiableCollection(recipeMap.getRecipeList());
        this.recipeList.forEach(recipe -> {

            HashSet<MapFluidIngredient> uniqueFluidIngredients = new HashSet<>();
            for (int i = 0; i < recipe.getFluidInputs().size(); i++) {
                FluidStack fluid = recipe.getFluidInputs().get(i);
                MapFluidIngredient fluidIngredient = new MapFluidIngredient(fluid);
                uniqueFluidIngredients.add(fluidIngredient);
                this.recipeFluidMap.computeIfAbsent(fluidIngredient, k -> new HashSet<>(1)).add(recipe);
            }

            HashSet<MapItemStackIngredient> uniqueItemIngredients = new HashSet<>();
            for (int i = 0; i < recipe.getInputs().size(); i++) {
                CountableIngredient item = recipe.getInputs().get(i);
                Ingredient ingredient = item.getIngredient();
                ItemStack[] itemStacks = ingredient.getMatchingStacks();
                if (itemStacks.length == 0) continue;
                uniqueItemIngredients.add(new MapItemStackIngredient(itemStacks[0].copy()));
                for (int j = 0; j < itemStacks.length; j++) {
                    ItemStack newItemStack = itemStacks[j].copy();
                    this.recipeItemMap.computeIfAbsent(new MapItemStackIngredient(newItemStack), k -> new HashSet<>(1)).add(recipe);
                }
            }
            byte uniqueIngredients = 0;
            uniqueIngredients += (byte) (uniqueFluidIngredients.size() + uniqueItemIngredients.size());
            this.recipeIngredientCountMap.defaultReturnValue((byte) -127);
            this.recipeIngredientCountMap.put(recipe, uniqueIngredients);
        });
    }

    public RecipeMap<?> getRecipeMap() {
        return recipeMap;
    }

    public int getMinFluidOutputs() {
        return minFluidOutputs;
    }

    public int getMinFluidInputs() {
        return minFluidInputs;
    }

    public int getMinInputs() {
        return minInputs;
    }

    public int getMinOutputs() {
        return minOutputs;
    }

    @Nullable
    public Recipe findRecipe(long voltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, int outputFluidTankCapacity, boolean useOptimizedRecipeLookUp, List<Recipe> occupiedRecipes, boolean distinct) {
        return this.findRecipe(voltage, inputs, fluidInputs, outputFluidTankCapacity, MatchingMode.DEFAULT, useOptimizedRecipeLookUp, occupiedRecipes, distinct);
    }

    @Nullable
    public Recipe findRecipe(long voltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, int outputFluidTankCapacity, MatchingMode matchingMode, boolean useOptimizedRecipeLookUp, List<Recipe> occupiedRecipes, boolean distinct) {

        if (recipeList.isEmpty())
            return null;
        if (minFluidInputs > 0 && GTUtility.amountOfNonNullElements(GTUtility.fluidHandlerToList(fluidInputs)) < minFluidInputs) {
            return null;
        }
        if (minInputs > 0 && GTUtility.amountOfNonEmptyStacks(GTUtility.itemHandlerToList(inputs)) < minInputs) {
            return null;
        }

        if (useOptimizedRecipeLookUp) {
            return findWithHashMap(voltage, inputs, fluidInputs, matchingMode, occupiedRecipes, distinct);
        }

        if (maxInputs > 0) {
            return findByInputs(voltage, inputs, fluidInputs, matchingMode, occupiedRecipes, distinct);
        } else {
            return findByFluidInputs(voltage, inputs, fluidInputs, matchingMode, occupiedRecipes, distinct);
        }
    }

    @Nullable
    private Recipe findByFluidInputs(long voltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, MatchingMode matchingMode, List<Recipe> occupiedRecipes, boolean distinct) {
        for (FluidStack fluid : GTUtility.fluidHandlerToList(fluidInputs)) {
            if (fluid == null) continue;
            Collection<Recipe> recipes = recipeFluidMap.get(new MapFluidIngredient(fluid));
            if (recipes == null) continue;
            for (Recipe tmpRecipe : recipes) {
                if (tmpRecipe.matchesFound(false, inputs, fluidInputs)) {
                    if (distinct) {
                        if (occupiedRecipes.contains(tmpRecipe))
                            continue;
                    }
                    return voltage >= tmpRecipe.getEUt() ? tmpRecipe : null;
                }
            }
        }
        return null;
    }

    @Nullable
    private Recipe findByInputs(long voltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, MatchingMode matchingMode, List<Recipe> occupiedRecipes, boolean distinct) {
        for (Recipe recipe : recipeList) {
            if (recipe.matchesFound(false, inputs, fluidInputs)) {
                if (distinct) {
                    if (occupiedRecipes.contains(recipe))
                        continue;
                }
                return voltage >= recipe.getEUt() ? recipe : null;
            }
        }
        return null;
    }

    @Nullable
    public Recipe findByInputsAndOutputs(long voltage, List<ItemStack> inputs, List<ItemStack> outputs, List<FluidStack> fluidInputs, List<FluidStack> fluidOutputs) {
        for (Recipe recipe : recipeList) {
            if (RecipeUtility.recipeMatches(recipe, inputs, outputs, fluidInputs, fluidOutputs)) {
                return voltage >= recipe.getEUt() ? recipe : null;
            }
        }
        return null;
    }

    @Nullable
    private Recipe findWithHashMap(long voltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, MatchingMode matchingMode, List<Recipe> occupiedRecipes, boolean distinct) {
        HashSet<MapItemStackIngredient> uniqueItems = new HashSet<>();
        HashSet<MapFluidIngredient> uniqueFluids = new HashSet<>();

        for (ItemStack item : GTUtility.itemHandlerToList(inputs)) {
            uniqueItems.add(new MapItemStackIngredient(item));
        }
        for (FluidStack fluid : GTUtility.fluidHandlerToList(fluidInputs)) {
            if (fluid == null) continue;
            uniqueFluids.add(new MapFluidIngredient(fluid));
        }

        Object2ByteMap<Recipe> recipeLeftoverIngredients = new Object2ByteOpenHashMap<>();
        recipeLeftoverIngredients.defaultReturnValue((byte) -127);
        for (MapItemStackIngredient item : uniqueItems) {
            boolean hasRecipes = recipeItemMap.containsKey(item);
            if (!hasRecipes) continue;
            Collection<Recipe> recipes = recipeItemMap.get(item);
            for (Recipe recipe : recipes) {
                byte leftOverIngredients;
                if ((leftOverIngredients = recipeLeftoverIngredients.getByte(recipe)) == -127)
                    if ((leftOverIngredients = this.recipeIngredientCountMap.getByte(recipe)) == -127)
                        leftOverIngredients = 0;
                leftOverIngredients--;
                recipeLeftoverIngredients.put(recipe, leftOverIngredients);
                if (leftOverIngredients > 0) {
                    continue;
                }
                int v = recipe.getEUt();
                if (voltage < v) {
                    continue;
                }
                boolean isMatch = recipe.matchesFound(false, inputs, fluidInputs);
                if (isMatch) {
                    if (distinct) {
                        if (occupiedRecipes.contains(recipe))
                            continue;
                    }
                    return recipe;
                }
            }
        }
        for (MapFluidIngredient fluid : uniqueFluids) {
            boolean hasRecipes = recipeFluidMap.containsKey(fluid);
            if (!hasRecipes) continue;
            Collection<Recipe> recipes = recipeFluidMap.get(fluid);
            for (Recipe recipe : recipes) {
                byte leftOverIngredients;
                if ((leftOverIngredients = recipeLeftoverIngredients.getByte(recipe)) == -127)
                    if ((leftOverIngredients = this.recipeIngredientCountMap.getByte(recipe)) == -127)
                        leftOverIngredients = 0;
                leftOverIngredients--;
                recipeLeftoverIngredients.put(recipe, leftOverIngredients);
                if (leftOverIngredients > 0) {
                    continue;
                }
                int v = recipe.getEUt();
                if (voltage < v) {
                    continue;
                }
                boolean isMatch = recipe.matchesFound(false, inputs, fluidInputs);
                if (isMatch) {
                    if (distinct) {
                        if (occupiedRecipes.contains(recipe))
                            continue;
                    }
                    return recipe;
                }
            }
        }
        return null;
    }

    public Recipe findRecipe(Recipe recipe) {
        for (Map.Entry<MapItemStackIngredient, Collection<Recipe>> map : recipeItemMap.entrySet()) {
            if (map == null) continue;
            for (Recipe foundRecipe : map.getValue()) {
                if (foundRecipe == null) continue;
                if (foundRecipe.getInputs().toString().equals(recipe.getInputs().toString()) &&
                        getOutputCountMatches(recipe, foundRecipe) &&
                        foundRecipe.getFluidInputs().equals(recipe.getFluidInputs()) &&
                        foundRecipe.getFluidOutputs().equals(recipe.getFluidOutputs()) &&
                        foundRecipe.getChancedOutputs().equals(recipe.getChancedOutputs()) &&
                        foundRecipe.getEUt() == recipe.getEUt() &&
                        foundRecipe.getDuration() == recipe.getDuration()) {
                    return foundRecipe;
                }
            }
        }
        for (Map.Entry<MapFluidIngredient, Collection<Recipe>> map : recipeFluidMap.entrySet()) {
            if (map == null) continue;
            for (Recipe foundRecipe : map.getValue()) {
                if (foundRecipe == null) continue;
                if (foundRecipe.getInputs().toString().equals(recipe.getInputs().toString()) &&
                        getOutputCountMatches(recipe, foundRecipe) &&
                        foundRecipe.getFluidInputs().equals(recipe.getFluidInputs()) &&
                        foundRecipe.getFluidOutputs().equals(recipe.getFluidOutputs()) &&
                        foundRecipe.getChancedOutputs().equals(recipe.getChancedOutputs()) &&
                        foundRecipe.getEUt() == recipe.getEUt() &&
                        foundRecipe.getDuration() == recipe.getDuration()) {
                    return foundRecipe;
                }
            }
        }
        return null;
    }

    private static boolean getOutputCountMatches(Recipe recipe, Recipe foundRecipe) {
        int outputCountMatches = 0;
        for (int i = 0; i < recipe.getOutputs().size(); i++) {
            if (foundRecipe.getOutputs().isEmpty()) continue;
            ItemStack itemInput = foundRecipe.getOutputs().get(i);
            ItemStack newItemInput = recipe.getOutputs().get(i);
            if (itemInput.getTranslationKey().equals(newItemInput.getTranslationKey()) &&
                    itemInput.getCount() == newItemInput.getCount() &&
                    itemInput.getMetadata() == newItemInput.getMetadata()) outputCountMatches++;
        }
        return outputCountMatches >= foundRecipe.getOutputs().size();
    }

}
