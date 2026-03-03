package tj.capability;

public final class OverclockManager<T> {

    private long EUt;
    private int duration;
    private int parallel;
    private int chanceMultiplier;
    private float euMultiplier;
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

    public void setEuMultiplier(float euMultiplier) {
        this.euMultiplier = euMultiplier;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setParallel(int parallel) {
        this.parallel = parallel;
    }

    public void setChanceMultiplier(int chanceMultiplier) {
        this.chanceMultiplier = chanceMultiplier;
    }

    public T getRecipeProperty() {
        return this.recipeProperty;
    }

    public long getEUt() {
        return this.EUt;
    }

    public float getEuMultiplier() {
        return this.euMultiplier;
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
