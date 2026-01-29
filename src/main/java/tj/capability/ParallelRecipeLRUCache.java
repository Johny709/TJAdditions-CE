package tj.capability;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.Recipe;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.LinkedList;
import java.util.List;

public final class ParallelRecipeLRUCache {

    private final int capacity;
    private long cacheHit;
    private long cacheMiss;
    private final LinkedList<Recipe> recipeList = new LinkedList<>();

    public ParallelRecipeLRUCache(int capacity) {
        this.capacity = capacity;
    }

    public void clear() {
        this.recipeList.clear();
        this.cacheHit = 0;
        this.cacheMiss = 0;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public long getCacheHit() {
        return this.cacheHit;
    }

    public long getCacheMiss() {
        return this.cacheMiss;
    }

    public void put(Recipe recipe) {
        if (this.recipeList.size() >= this.capacity) {
            this.recipeList.removeLast();
        }
        this.recipeList.addFirst(recipe);
    }

    public Recipe get(IItemHandlerModifiable importInventory, IMultipleTankHandler importFluids) {
        for (Recipe recipe : this.recipeList) {
            if (recipe == null)
                continue;
            if (recipe.matches(false, importInventory, importFluids)) {
                this.recipeList.remove(recipe);
                this.recipeList.addFirst(recipe);
                this.cacheHit++;
                return recipe;
            }
        }
        this.cacheMiss++;
        return null;
    }

    public Recipe get(IItemHandlerModifiable importInventory, IMultipleTankHandler importFluids, int i, List<Recipe> occupiedRecipes) {
        for (Recipe recipe : recipeList) {
            if (recipe == null)
                continue;
            if (recipe.matches(false, importInventory, importFluids)) {
                if (occupiedRecipes.contains(recipe) && recipe != occupiedRecipes.get(i))
                    continue;
                this.recipeList.remove(recipe);
                this.recipeList.addFirst(recipe);
                this.cacheHit++;
                return recipe;
            }
        }
        this.cacheMiss++;
        return null;
    }
}
