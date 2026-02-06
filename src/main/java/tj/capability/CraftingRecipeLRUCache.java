package tj.capability;

import gregtech.api.recipes.CountableIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Triple;
import tj.util.ItemStackHelper;

import java.util.LinkedList;


public final class CraftingRecipeLRUCache {

    private final int capacity;
    private long cacheHit;
    private long cacheMiss;
    private final LinkedList<Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> recipeList = new LinkedList<>();

    public CraftingRecipeLRUCache(int capacity) {
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

    public void put(Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>> recipe) {
        if (this.recipeList.size() >= this.capacity) {
            this.recipeList.removeLast();
        }
        this.recipeList.addFirst(recipe);
    }

    public Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>> get(IItemHandlerModifiable importItems) {
        recipe:
        for (Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>> recipe : this.recipeList) {
            if (recipe == null)
                continue;
            for (int i = 0; i < recipe.getMiddle().size(); i++) {
                CountableIngredient ingredient = recipe.getMiddle().get(i);
                int size = ingredient.getCount();
                int extracted = ItemStackHelper.extractFromItemHandlerByIngredient(importItems, ingredient.getIngredient(), size, true);
                if (extracted < size)
                    continue recipe;
            }
            this.recipeList.remove(recipe);
            this.recipeList.addFirst(recipe);
            this.cacheHit++;
            return recipe;
        }
        this.cacheMiss++;
        return null;
    }
}
