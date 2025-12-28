package tj.capability.impl;

public class OverclockManager<T> {

    private long EUt;
    private int duration;
    private int parallel;
    private T recipeProperty;

    public void setEUtAndDuration(long EUt, int duration) {
        this.EUt = EUt;
        this.duration = duration;
    }

    public void setRecipeProperty(T recipeProperty) {
        this.recipeProperty = recipeProperty;
    }

    public void setEUt(long EUt) {
        this.EUt = EUt;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setParallel(int parallel) {
        this.parallel = parallel;
    }

    public T getRecipeProperty() {
        return this.recipeProperty;
    }

    public long getEUt() {
        return this.EUt;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getParallel() {
        return this.parallel;
    }
}
