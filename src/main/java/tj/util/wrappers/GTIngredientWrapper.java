package tj.util.wrappers;

import net.minecraft.item.crafting.Ingredient;

public final class GTIngredientWrapper {

    private final Ingredient ingredient;
    private long countLong;
    private int count;

    public GTIngredientWrapper(Ingredient ingredient) {
        this(ingredient, 1);
    }

    public GTIngredientWrapper(Ingredient ingredient, long count) {
        this.ingredient = ingredient;
        this.countLong = count;
        this.count = (int) Math.min(Integer.MAX_VALUE, this.countLong);
    }

    public Ingredient getIngredient() {
        return this.ingredient;
    }

    public void increment(long count) {
        this.countLong += count;
        this.count = (int) Math.min(Integer.MAX_VALUE, this.countLong);
    }

    public void setCount(long count) {
        this.countLong = count;
        this.count = (int) Math.min(Integer.MAX_VALUE, this.countLong);
    }

    public long getCountLong() {
        return this.countLong;
    }

    public int getCount() {
        return this.count;
    }
}
