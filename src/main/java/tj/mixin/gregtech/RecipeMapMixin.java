package tj.mixin.gregtech;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.map.MapFluidIngredient;
import gregtech.api.recipes.map.MapItemStackIngredient;
import gregtech.api.util.GTUtility;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tj.builder.RecipeUtility;
import tj.capability.IRecipeMap;
import tj.gui.widgets.impl.RecipeOutputDisplayWidget;
import tj.gui.widgets.impl.RecipeOutputSlotWidget;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;

@Mixin(value = RecipeMap.class, remap = false)
public abstract class RecipeMapMixin implements IRecipeMap {

    @Shadow
    protected TextureArea progressBarTexture;

    @Shadow
    protected ProgressWidget.MoveType moveType;

    @Shadow
    protected static int[] determineSlotsGrid(int itemInputsCount) {
        return null;
    }

    @Shadow
    protected abstract void addSlot(ModularUI.Builder builder, int x, int y, int slotIndex, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, boolean isFluid, boolean isOutputs);

    @Shadow
    @Final
    private Map<MapItemStackIngredient, Collection<Recipe>> recipeItemMap;

    @Shadow
    @Final
    private Map<MapFluidIngredient, Collection<Recipe>> recipeFluidMap;

    @Shadow
    @Final
    private Collection<Recipe> recipeList;

    @Shadow
    @Final
    private Map<Recipe, Byte> recipeIngredientCountMap;

    @Shadow
    @Final
    private int minFluidInputs;

    @Shadow
    @Final
    private int minInputs;

    @Shadow
    @Final
    private int maxInputs;

    @Override
    public ModularUI.Builder createUITemplateAdvanced(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, RecipeOutputDisplayWidget displayWidget) {
        ModularUI.Builder builder = ModularUI.defaultBuilder();
        builder.widget(new ProgressWidget(progressSupplier, 77, 22, 21, 20, this.progressBarTexture, this.moveType));
        this.addInventorySlotGroupAdvanced(builder, importItems, importFluids, false, displayWidget);
        this.addInventorySlotGroupAdvanced(builder, exportItems, exportFluids, true, displayWidget);
        return builder;
    }

    @Override
    public void addInventorySlotGroupAdvanced(ModularUI.Builder builder, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, boolean isOutputs, RecipeOutputDisplayWidget displayWidget) {
        int itemInputsCount = itemHandler.getSlots();
        int fluidInputsCount = fluidHandler.getTanks();
        boolean invertFluids = false;
        if (itemInputsCount == 0) {
            int tmp = itemInputsCount;
            itemInputsCount = fluidInputsCount;
            fluidInputsCount = tmp;
            invertFluids = true;
        }
        int[] inputSlotGrid = determineSlotsGrid(itemInputsCount);
        int itemSlotsToLeft = inputSlotGrid[0];
        int itemSlotsToDown = inputSlotGrid[1];
        int startInputsX = isOutputs ? 106 : 69 - itemSlotsToLeft * 18;
        int startInputsY = 32 - (int) (itemSlotsToDown / 2.0 * 18);
        for (int i = 0; i < itemSlotsToDown; i++) {
            for (int j = 0; j < itemSlotsToLeft; j++) {
                int slotIndex = i * itemSlotsToLeft + j;
                int x = startInputsX + 18 * j;
                int y = startInputsY + 18 * i;
                addSlot(builder, x, y, slotIndex, itemHandler, fluidHandler, invertFluids, isOutputs);
                if (isOutputs)
                    builder.widget(new RecipeOutputSlotWidget(slotIndex, x, y, 18, 18, displayWidget::getItemOutputAt, null));
            }
        }
        if (fluidInputsCount > 0 || invertFluids) {
            if (itemSlotsToDown >= fluidInputsCount && itemSlotsToLeft < 3) {
                int startSpecX = isOutputs ? startInputsX + itemSlotsToLeft * 18 : startInputsX - 18;
                for (int i = 0; i < fluidInputsCount; i++) {
                    int y = startInputsY + 18 * i;
                    addSlot(builder, startSpecX, y, i, itemHandler, fluidHandler, !invertFluids, isOutputs);
                    if (isOutputs)
                        builder.widget(new RecipeOutputSlotWidget(i, startSpecX, y, 18, 18, null, displayWidget::getFluidOutputAt));
                }
            } else {
                int startSpecY = startInputsY + itemSlotsToDown * 18;
                for (int i = 0; i < fluidInputsCount; i++) {
                    int x = isOutputs ? startInputsX + 18 * (i % 3) : startInputsX + itemSlotsToLeft * 18 - 18 - 18 * (i % 3);
                    int y = startSpecY + (i / 3) * 18;
                    addSlot(builder, x, y, i, itemHandler, fluidHandler, !invertFluids, isOutputs);
                    if (isOutputs)
                        builder.widget(new RecipeOutputSlotWidget(i, x, y, 18, 18, null, displayWidget::getFluidOutputAt));
                }
            }
        }
    }

    @Override
    public Recipe findRecipeDistinct(long voltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, int outputFluidTankCapacity, boolean useOptimizedRecipeLookUp, List<Recipe> occupiedRecipes, boolean distinct) {
        return this.findRecipeDistinct(voltage, GTUtility.itemHandlerToList(inputs), GTUtility.fluidHandlerToList(fluidInputs), outputFluidTankCapacity, MatchingMode.DEFAULT, useOptimizedRecipeLookUp, occupiedRecipes, distinct);
    }

    @Unique
    public Recipe findRecipeDistinct(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, int outputFluidTankCapacity, MatchingMode matchingMode, boolean useOptimizedRecipeLookUp, List<Recipe> occupiedRecipes, boolean distinct) {
        if (this.recipeList.isEmpty())
            return null;
        if (this.minFluidInputs > 0 && GTUtility.amountOfNonNullElements(fluidInputs) < this.minFluidInputs)
            return null;
        if (this.minInputs > 0 && GTUtility.amountOfNonEmptyStacks(inputs) < this.minInputs)
            return null;
        if (useOptimizedRecipeLookUp)
            return this.findWithHashMapDistinct(voltage, inputs, fluidInputs, matchingMode, occupiedRecipes, distinct);
        if (this.maxInputs > 0) {
            return this.findByInputsDistinct(voltage, inputs, fluidInputs, matchingMode, occupiedRecipes, distinct);
        } else return this.findByFluidInputsDistinct(voltage, inputs, fluidInputs, matchingMode, occupiedRecipes, distinct);
    }

    @Unique
    public Recipe findByFluidInputsDistinct(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, MatchingMode matchingMode, List<Recipe> occupiedRecipes, boolean distinct) {
        for (FluidStack fluid : fluidInputs) {
            if (fluid == null) continue;
            Collection<Recipe> recipes = this.recipeFluidMap.get(new MapFluidIngredient(fluid));
            if (recipes == null) continue;
            for (Recipe tmpRecipe : recipes) {
                if (tmpRecipe.matches(false, inputs, fluidInputs, matchingMode)) {
                    if (distinct && occupiedRecipes.contains(tmpRecipe)) continue;
                    return voltage >= tmpRecipe.getEUt() ? tmpRecipe : null;
                }
            }
        }
        return null;
    }

    @Unique
    public Recipe findByInputsDistinct(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, MatchingMode matchingMode, List<Recipe> occupiedRecipes, boolean distinct) {
        for (Recipe recipe : this.recipeList) {
            if (recipe.matches(false, inputs, fluidInputs, matchingMode)) {
                if (distinct && occupiedRecipes.contains(recipe)) continue;
                return voltage >= recipe.getEUt() ? recipe : null;
            }
        }
        return null;
    }

    @Unique
    public Recipe findWithHashMapDistinct(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, MatchingMode matchingMode, List<Recipe> occupiedRecipes, boolean distinct) {
        HashSet<MapItemStackIngredient> uniqueItems = new HashSet<>();
        HashSet<MapFluidIngredient> uniqueFluids = new HashSet<>();

        for (ItemStack item : inputs) {
            uniqueItems.add(new MapItemStackIngredient(item));
        }
        for (FluidStack fluid : fluidInputs) {
            if (fluid == null) continue;
            uniqueFluids.add(new MapFluidIngredient(fluid));
        }

        Object2ByteMap<Recipe> recipeLeftoverIngredients = new Object2ByteOpenHashMap<>();
        for (MapItemStackIngredient item : uniqueItems) {
            boolean hasRecipes = recipeItemMap.containsKey(item);
            if (!hasRecipes) continue;
            Collection<Recipe> recipes = recipeItemMap.get(item);
            for (Recipe recipe : recipes) {
                Byte leftOverIngredients = recipeLeftoverIngredients.getOrDefault(recipe, this.recipeIngredientCountMap.getOrDefault(recipe, (byte) 0));
                leftOverIngredients--;
                recipeLeftoverIngredients.put(recipe, leftOverIngredients);
                if (leftOverIngredients > 0) {
                    continue;
                }
                int v = recipe.getEUt();
                if (voltage < v) {
                    continue;
                }
                boolean isMatch = recipe.matches(false, inputs, fluidInputs, matchingMode);
                if (isMatch) {
                    if (distinct && occupiedRecipes.contains(recipe)) continue;
                    return recipe;
                }
            }
        }
        for (MapFluidIngredient fluid : uniqueFluids) {
            boolean hasRecipes = recipeFluidMap.containsKey(fluid);
            if (!hasRecipes) continue;
            Collection<Recipe> recipes = recipeFluidMap.get(fluid);
            for (Recipe recipe : recipes) {
                Byte leftOverIngredients = recipeLeftoverIngredients.getOrDefault(recipe, this.recipeIngredientCountMap.getOrDefault(recipe, (byte) 0));
                leftOverIngredients--;
                recipeLeftoverIngredients.put(recipe, leftOverIngredients);
                if (leftOverIngredients > 0) {
                    continue;
                }
                int v = recipe.getEUt();
                if (voltage < v) {
                    continue;
                }
                boolean isMatch = recipe.matches(false, inputs, fluidInputs, matchingMode);
                if (isMatch) {
                    if (distinct && occupiedRecipes.contains(recipe)) continue;
                    return recipe;
                }
            }
        }
        return null;
    }

    @Override
    public Recipe findByInputsAndOutputs(long voltage, List<ItemStack> inputs, List<ItemStack> outputs, List<FluidStack> fluidInputs, List<FluidStack> fluidOutputs) {
        for (Recipe recipe : this.recipeList) {
            if (RecipeUtility.recipeMatches(recipe, inputs, outputs, fluidInputs, fluidOutputs)) {
                return voltage >= recipe.getEUt() ? recipe : null;
            }
        }
        return null;
    }

    @Override
    public Recipe findRecipe(Recipe recipe) {
        for (Map.Entry<MapItemStackIngredient, Collection<Recipe>> map : this.recipeItemMap.entrySet()) {
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
        for (Map.Entry<MapFluidIngredient, Collection<Recipe>> map : this.recipeFluidMap.entrySet()) {
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

    @Unique
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
