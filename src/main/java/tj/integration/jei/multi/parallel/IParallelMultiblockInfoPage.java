package tj.integration.jei.multi.parallel;

import tj.integration.jei.TJMultiblockShapeInfo;

import java.util.List;

public interface IParallelMultiblockInfoPage {

    List<TJMultiblockShapeInfo[]> getMatchingShapes(TJMultiblockShapeInfo[] shapes);
}
