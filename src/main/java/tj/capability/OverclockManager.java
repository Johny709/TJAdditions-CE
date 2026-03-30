package tj.capability;

import gregtech.api.recipes.Recipe;

public final class OverclockManager<T> {

    private long EUt;
    private int duration;
    private int parallel;
    private int chanceMultiplier;
    private float durationMultiplier;
    private T recipeProperty;

    public void setRecipeProperty(T recipeProperty) {
        this.recipeProperty = recipeProperty;
    }

    /**
     * Can be set in {@link tj.capability.impl.handler.IRecipeHandler#preOverclock(OverclockManager, Recipe) preOverlock} and {@link tj.capability.impl.handler.IRecipeHandler#postOverclock(OverclockManager, Recipe) postOverclock}.
     * {@link tj.capability.impl.handler.IRecipeHandler#preOverclock(OverclockManager, Recipe) preOverclock} can affect overclocking tiers, e.g. setting 120 EU/t to 30 EU/t will have the recipe be treated as LV recipe instead of MV for overclocking.
     * Can be safely changed in {@link tj.capability.impl.handler.IRecipeHandler#postOverclock(OverclockManager, Recipe) postOverclock} and determines the final recipe EU/t.
     * Initial value is set from recipe EU/t.
     * @param EUt energy consumed per tick.
     */
    public void setEUt(long EUt) {
        this.EUt = EUt;
    }

    /**
     * Can be set in {@link tj.capability.impl.handler.IRecipeHandler#preOverclock(OverclockManager, Recipe) preOverclock}. Has no effect in {@link tj.capability.impl.handler.IRecipeHandler#postOverclock(OverclockManager, Recipe) postOverclock}.
     * Initial value is set to 2.8
     * @param durationMultiplier multiplier applied per overclock. e.g. setting to 2 will cut the recipe time in half for every overclock.
     */
    public void setDurationMultiplier(float durationMultiplier) {
        this.durationMultiplier = durationMultiplier;
    }

    /**
     * Can be set in {@link tj.capability.impl.handler.IRecipeHandler#preOverclock(OverclockManager, Recipe) preOverclock} and {@link tj.capability.impl.handler.IRecipeHandler#postOverclock(OverclockManager, Recipe) postOverclock}.
     * In {@link tj.capability.impl.handler.IRecipeHandler#preOverclock(OverclockManager, Recipe) preOverclock}, overclock can end early upon 1 tick if duration is set too low.
     * Can be safely changed in {@link tj.capability.impl.handler.IRecipeHandler#postOverclock(OverclockManager, Recipe) postOverclock} and determines the final recipe time.
     * Initial value is set with from recipe duration.
     * @param duration duration in ticks.
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Set both {@link OverclockManager#setEUt(long)} and {@link OverclockManager#setDuration(int)} for convenience.
     * @param EUt energy consumed per tick.
     * @param duration duration in ticks.
     */
    public void setEUtAndDuration(long EUt, int duration) {
        this.EUt = EUt;
        this.duration = duration;
    }

    /**
     * Can be set in {@link tj.capability.impl.handler.IRecipeHandler#preOverclock(OverclockManager, Recipe) preOverclock}. Has no effect in {@link tj.capability.impl.handler.IRecipeHandler#postOverclock(OverclockManager, Recipe) postOverclock}.
     * This determines the maximum amount of recipes the logic will try to do simultaneously. If parallel is less than 1, then the recipe is invalid and cannot start.
     * Initial value is set from {@link IMachineHandler#getParallel()}.
     * @param parallel amount of recipes performed simultaneously.
     */
    public void setParallel(int parallel) {
        this.parallel = parallel;
    }

    /**
     * Can be set in {@link tj.capability.impl.handler.IRecipeHandler#preOverclock(OverclockManager, Recipe) preOverclock}. Has no effect in {@link tj.capability.impl.handler.IRecipeHandler#postOverclock(OverclockManager, Recipe) postOverclock}.
     * Multiplier to calculate chance outputs with the output's chance value. If the roll fails, then the output is voided.
     * @param chanceMultiplier multiplier amount. e.g. 10000 == 100.00%.
     */
    public void setChanceMultiplier(int chanceMultiplier) {
        this.chanceMultiplier = chanceMultiplier;
    }

    public T getRecipeProperty() {
        return this.recipeProperty;
    }

    public long getEUt() {
        return this.EUt;
    }

    public float getDurationMultiplier() {
        return this.durationMultiplier;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getParallel() {
        return this.parallel;
    }

    public int getChanceMultiplier() {
        return this.chanceMultiplier;
    }
}
