package tj.mixin.gregtech;

import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tj.capability.IGTRecipe;
import tj.util.Counter;
import tj.util.wrappers.GTFluidStackWrapper;
import tj.util.wrappers.GTIngredientWrapper;

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
    private List<GTIngredientWrapper> mergedItemInputs = new ArrayList<>();

    @Final
    @Unique
    private List<GTFluidStackWrapper> mergedFluidInputs = new ArrayList<>();

    @Unique
    private boolean hasMergedRecipes;

    @Override
    public void mergeRecipeInputs() {
        if (this.hasMergedRecipes) return;
        this.hasMergedRecipes = true;
        final Object2ObjectMap<String, GTIngredientWrapper> ingredientMap = new Object2ObjectLinkedOpenHashMap<>();
        for (CountableIngredient countableIngredient : this.getInputs()) {
            final Ingredient ingredient = countableIngredient.getIngredient();
            final String key = Arrays.toString(ingredient.getMatchingStacks());
            ingredientMap.computeIfAbsent(key, k -> new GTIngredientWrapper(ingredient, 0))
                    .increment(countableIngredient.getCount());
        }
        this.mergedItemInputs.addAll(ingredientMap.values());
        final Object2ObjectMap<FluidStack, GTFluidStackWrapper> fluidStackMap = new Object2ObjectLinkedOpenHashMap<>();
        for (FluidStack stack : this.getFluidInputs()) {
            fluidStackMap.computeIfAbsent(stack, k -> new GTFluidStackWrapper(stack, 0))
                    .increment(stack.amount);
        }
        this.mergedFluidInputs.addAll(fluidStackMap.values());
    }

    @Override
    public List<GTIngredientWrapper> getMergedItemInputs() {
        return this.mergedItemInputs;
    }

    @Override
    public List<GTFluidStackWrapper> getMergedFluidInputs() {
        return this.mergedFluidInputs;
    }
}
