package tj.mixin.gregtech;

import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tj.capability.IGTRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(value = Recipe.class, remap = false)
public abstract class RecipeMixin implements IGTRecipe {

    @Shadow
    public abstract List<CountableIngredient> getInputs();

    @Shadow
    public abstract List<FluidStack> getFluidInputs();

    @Final
    @Unique
    private List<CountableIngredient> mergedItemInputs = new ArrayList<>();

    @Final
    @Unique
    private List<FluidStack> mergedFluidInputs = new ArrayList<>();

    @Unique
    private boolean hasMergedRecipes;

    @Override
    public void mergeRecipeInputs() {
        if (this.hasMergedRecipes) return;
        Object2ObjectMap<String, CountableIngredient> ingredientMap = new Object2ObjectLinkedOpenHashMap<>();
        for (CountableIngredient countableIngredient : this.getInputs()) {
            Ingredient ingredient = countableIngredient.getIngredient();
            String key = Arrays.toString(ingredient.getMatchingStacks());
            CountableIngredient mergedIngredient = ingredientMap.get(key);
            if (mergedIngredient == null) {
                ingredientMap.put(key, new CountableIngredient(ingredient, countableIngredient.getCount()));
            } else ingredientMap.replace(key, new CountableIngredient(mergedIngredient.getIngredient(), mergedIngredient.getCount() + countableIngredient.getCount()));
        }
        this.mergedItemInputs.addAll(ingredientMap.values());
        Object2IntMap<FluidStack> fluidStackMap = new Object2IntLinkedOpenHashMap<>();
        fluidStackMap.defaultReturnValue(Integer.MIN_VALUE);
        for (FluidStack stack : this.getFluidInputs()) {
            int mergedAmount = fluidStackMap.getInt(stack);
            if (mergedAmount == Integer.MIN_VALUE) {
                fluidStackMap.put(stack, stack.amount);
            } else fluidStackMap.replace(stack, stack.amount + mergedAmount);
        }
        for (Object2IntMap.Entry<FluidStack> entry : fluidStackMap.object2IntEntrySet()) {
            FluidStack stack = entry.getKey().copy();
            stack.amount = entry.getIntValue();
            this.mergedFluidInputs.add(stack);
        }
        this.hasMergedRecipes = true;
    }

    @Override
    public List<CountableIngredient> getMergedItemInputs() {
        return this.mergedItemInputs;
    }

    @Override
    public List<FluidStack> getMergedFluidInputs() {
        return this.mergedFluidInputs;
    }
}
