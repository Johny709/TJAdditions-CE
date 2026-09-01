package tj.integration.hwyla;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import tj.integration.hwyla.providers.ProgressInfoDataProvider;
import tj.integration.hwyla.providers.RecipeInfoDataProvider;
import tj.integration.hwyla.providers.StructureInfoDataProvider;

@WailaPlugin
public final class HWYLACompatibility implements IWailaPlugin {

    @Override
    public void register(IWailaRegistrar iWailaRegistrar) {
        ProgressInfoDataProvider.INSTANCE.register(iWailaRegistrar);
        RecipeInfoDataProvider.INSTANCE.register(iWailaRegistrar);
        StructureInfoDataProvider.INSTANCE.register(iWailaRegistrar);
    }
}
