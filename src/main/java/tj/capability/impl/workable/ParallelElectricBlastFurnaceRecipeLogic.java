package tj.capability.impl.workable;

import gregicadditions.GAValues;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.recipeproperties.BlastTemperatureProperty;
import tj.TJConfig;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;

import java.util.function.IntSupplier;

public class ParallelElectricBlastFurnaceRecipeLogic extends ParallelMultiblockRecipeLogic {

    private final IntSupplier temperature;

    public ParallelElectricBlastFurnaceRecipeLogic(ParallelRecipeMapMultiblockController tileEntity, IntSupplier temperature) {
        super(tileEntity, TJConfig.machines.recipeCacheCapacity);
        this.temperature = temperature;
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
}
