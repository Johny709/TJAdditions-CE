package tj.integration.appeng;

import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IItems;

public interface IApiItems extends IItems {

    IItemDefinition getCell65m();

    IItemDefinition getCell262m();

    IItemDefinition getCell1048m();

    IItemDefinition getCellDigitalSingularity();

    IItemDefinition getFluidCell65m();

    IItemDefinition getFluidCell262m();

    IItemDefinition getFluidCell1048m();

    IItemDefinition getFluidCellDigitalSingularity();
}
