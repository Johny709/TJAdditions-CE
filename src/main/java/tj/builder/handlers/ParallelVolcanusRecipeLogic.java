package tj.builder.handlers;

import gregicadditions.GAValues;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.api.recipes.recipeproperties.BlastTemperatureProperty;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.capability.impl.ParallelGAMultiblockRecipeLogic;

import java.util.Collection;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class ParallelVolcanusRecipeLogic extends ParallelGAMultiblockRecipeLogic {

    private final IntSupplier temperature;
    private final Supplier<FluidStack> pyrotheum;

    public ParallelVolcanusRecipeLogic(ParallelRecipeMapMultiblockController tileEntity, IntSupplier temperature, Supplier<FluidStack> pyrotheum, IntSupplier EUtPercentage, IntSupplier durationPercentage, IntSupplier chancePercentage, IntSupplier stack) {
        super(tileEntity, EUtPercentage, durationPercentage, chancePercentage, stack);
        this.temperature = temperature;
        this.pyrotheum = pyrotheum;
    }

    @Override
    protected boolean drawEnergy(long recipeEUt) {
        FluidStack drained = this.getInputTank().drain(this.pyrotheum.get(), true);
        if (drained == null || drained.amount != this.pyrotheum.get().amount)
            return false;
        return super.drawEnergy(recipeEUt);
    }

    @Override
    protected boolean setupAndConsumeRecipeInputs(Recipe recipe) {
        this.overclockManager.setRecipeProperty(recipe.getRecipePropertyStorage().getRecipePropertyValue(BlastTemperatureProperty.getInstance(), 0));
        return super.setupAndConsumeRecipeInputs(recipe);
    }

    @Override
    protected boolean calculateOverclock(long EUt, int duration) {
        if (!this.allowOverclocking) {
            this.overclockManager.setEUtAndDuration(EUt, duration);
            return true;
        }

        boolean negativeEU = EUt < 0;

        Integer heatProperty = (Integer) this.overclockManager.getRecipeProperty();
        if (heatProperty == null || heatProperty > this.temperature.getAsInt())
            return false;

        int bonusAmount = Math.max(0, this.temperature.getAsInt() - heatProperty) / 900;

        // Apply EUt discount for every 900K above the base recipe temperature
        EUt *= Math.pow(0.95, bonusAmount);

        int tier = getOverclockingTier(this.maxVoltage.getAsLong());
        if (GAValues.V[tier] <= EUt || tier == 0) {
            this.overclockManager.setEUtAndDuration(EUt, duration);
            return true;
        }
        if (negativeEU)
            EUt = -EUt;
        if (EUt <= 16) {
            int multiplier = EUt <= 8 ? tier : tier - 1;
            long resultEUt = EUt * (1 << multiplier) * (1 << multiplier);
            int resultDuration = duration / (1 << multiplier);
            this.overclockManager.setEUtAndDuration(negativeEU ? -resultEUt : resultEUt, resultDuration);
            return true;
        } else {
            long resultEUt = EUt;
            double resultDuration = duration;

            // Do not overclock further if duration is already too small
            // Apply Super Overclocks for every 1800k above the base recipe temperature
            for (int i = bonusAmount; resultEUt <= GAValues.V[tier - 1] && resultDuration >= 3 && i > 0; i--) {
                if (i % 2 == 0) {
                    resultEUt *= 4;
                    resultDuration *= 0.25;
                }
            }

            // Do not overclock further if duration is already too small
            // Apply Regular Overclocking
            while (resultDuration >= 3 && resultEUt <= GAValues.V[tier - 1]) {
                resultEUt *= 4;
                resultDuration /= 2.8;
            }

            this.overclockManager.setEUtAndDuration(resultEUt, (int) Math.round(resultDuration));
            return true;
        }
    }

    @Override
    protected Recipe buildRecipe(RecipeBuilder<?> recipeBuilder, Recipe recipe, Collection<CountableIngredient> inputs, Collection<FluidStack> fluidInputs, Collection<ItemStack> outputs, Collection<FluidStack> fluidOutputs) {
        BlastRecipeBuilder blastRecipeBuilder = (BlastRecipeBuilder) recipeBuilder;
        return blastRecipeBuilder.inputsIngredients(inputs)
                .fluidInputs(fluidInputs)
                .outputs(outputs)
                .fluidOutputs(fluidOutputs)
                .EUt((int) Math.min(Integer.MAX_VALUE, Math.max(1, (long) recipe.getEUt() * this.EUtPercentage.getAsInt() / 100)))
                .duration(Math.max(1, recipe.getDuration() * this.controller.getBatchMode().getAmount() * this.durationPercentage.getAsInt() / 100))
                .blastFurnaceTemp(recipe.getRecipePropertyStorage().getRecipePropertyValue(BlastTemperatureProperty.getInstance(), 0))
                .build()
                .getResult();
    }
}
