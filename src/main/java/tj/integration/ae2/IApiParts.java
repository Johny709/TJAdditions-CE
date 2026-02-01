package tj.integration.ae2;

import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IParts;

public interface IApiParts extends IParts {

    IItemDefinition getSuperInterface();

    IItemDefinition getSuperFluidInterface();
}
