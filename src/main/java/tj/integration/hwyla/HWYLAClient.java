package tj.integration.hwyla;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.integration.hwyla.renderers.ProgressInfoRenderer;
import tj.integration.hwyla.renderers.RecipeInfoRenderer;

@SideOnly(Side.CLIENT)
@WailaPlugin
public final class HWYLAClient implements IWailaPlugin {

    @Override
    public void register(IWailaRegistrar iWailaRegistrar) {
        iWailaRegistrar.registerTooltipRenderer("tj.progressinfo", new ProgressInfoRenderer());
        iWailaRegistrar.registerTooltipRenderer("tj.recipeinfo", new RecipeInfoRenderer());
    }
}
