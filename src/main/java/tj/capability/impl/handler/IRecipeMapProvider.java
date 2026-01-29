package tj.capability.impl.handler;

import gregtech.api.recipes.CountableIngredient;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import org.apache.commons.lang3.tuple.Triple;


public interface IRecipeMapProvider {

    Int2ObjectMap<Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> getRecipeMap();

    default void clearRecipeCache() {}
}
