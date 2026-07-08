package tj.integration.jei.multi.parallel;

import gregtech.integration.jei.multiblock.MultiblockShapeInfo;

public interface IParallelMultiblockInfoPage {

    MultiblockShapeInfo getMatchingShapes(int extent);
}
