package tj.capability;

import java.util.Queue;

public interface IProgressBar {

    int[][] getBarMatrix();

    void getProgressBars(Queue<ProgressBar> bars);
}
