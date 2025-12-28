package tj.capability;

/**
 * For Machines which have multiple workable instances
 */
public interface IMultipleWorkable extends IMultiControllable {

    /**
     * @param i instance
     * @return Recipe EU/t of this instance
     */
    long getRecipeEUt(int i);

    /**
     * @param i instance
     * @return Parallels performed of this instance
     */
    int getParallel(int i);

    /**
     * @param i instance
     * @return current progress of this instance
     */
    int getProgress(int i);

    /**
     * @param i instance
     * @return gets the amount of ticks to complete operation of this instance
     */
    int getMaxProgress(int i);

    /**
     * @param i instance
     * @return if this instance is active
     */
    boolean isInstanceActive(int i);

    /**
     * @param i instance
     * @return if this instance has problems
     */
    boolean hasProblems(int i);

    /**
     * @return total amount of instances in this recipe workable
     */
    int getSize();
}
