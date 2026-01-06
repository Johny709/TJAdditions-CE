package tj.capability;

import java.util.Queue;

public interface IProgressBar {

    /**
     * Outer array length determines amount of bars in the column.
     * Inner array length determines amount of bars in a row. The values in the inner array won't have any effect.
     */
    int[][] getBarMatrix();

    void getProgressBars(Queue<ProgressBar> bars, ProgressBar.ProgressBarBuilder barBuilder);
}
