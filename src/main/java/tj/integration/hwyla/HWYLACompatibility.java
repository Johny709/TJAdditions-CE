package tj.integration.hwyla;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import tj.integration.hwyla.providers.*;

@WailaPlugin
public final class HWYLACompatibility implements IWailaPlugin {

    @Override
    public void register(IWailaRegistrar iWailaRegistrar) {
        FuelableInfoDataProvider.INSTANCE.register(iWailaRegistrar);
        EnergyInfoDataProvider.INSTANCE.register(iWailaRegistrar);
        HeatInfoDataProvider.INSTANCE.register(iWailaRegistrar);
        ProgressInfoDataProvider.INSTANCE.register(iWailaRegistrar);
        ParallelControllerInfoDataProvider.INSTANCE.register(iWailaRegistrar);
        ParallelWorkableInfoDataProvider.INSTANCE.register(iWailaRegistrar);
        ParallelItemFluidHandlerInfoDataProvider.INSTANCE.register(iWailaRegistrar);
        LinkedPosInfoDataProvider.INSTANCE.register(iWailaRegistrar);
        LinkedEntityInfoDataProvider.INSTANCE.register(iWailaRegistrar);
        GeneratorInfoDataProvider.INSTANCE.register(iWailaRegistrar);
        RecipeInfoDataProvider.INSTANCE.register(iWailaRegistrar);
        StructureInfoDataProvider.INSTANCE.register(iWailaRegistrar);
    }
}
