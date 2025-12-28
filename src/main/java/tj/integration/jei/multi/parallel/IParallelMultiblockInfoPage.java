package tj.integration.jei.multi.parallel;

import gregtech.integration.jei.multiblock.MultiblockShapeInfo;

import java.util.List;

public interface IParallelMultiblockInfoPage {

    List<MultiblockShapeInfo[]> getMatchingShapes(MultiblockShapeInfo[] shapes);
}
