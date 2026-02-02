package tj.integration.appeng;

import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;

public interface IApiMaterials extends IMaterials {

    IItemDefinition getCell65mPart();

    IItemDefinition getCell262mPart();

    IItemDefinition getCell1048mPart();

    IItemDefinition getCellDigitalSingularityPart();

    IItemDefinition getFluidCell65mPart();

    IItemDefinition getFluidCell262mPart();

    IItemDefinition getFluidCell1048mPart();

    IItemDefinition getFluidCellDigitalSingularityPart();
}
