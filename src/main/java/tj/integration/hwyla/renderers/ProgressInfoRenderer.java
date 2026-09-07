package tj.integration.hwyla.renderers;

import mcp.mobius.waila.api.IWailaCommonAccessor;
import mcp.mobius.waila.api.IWailaTooltipRenderer;
import tj.mui.TJGuiUtils;

import javax.annotation.Nonnull;
import java.awt.*;


public class ProgressInfoRenderer implements IWailaTooltipRenderer {

    @Nonnull
    @Override
    public Dimension getSize(@Nonnull String[] strings, @Nonnull IWailaCommonAccessor iWailaCommonAccessor) {
        return TJGuiUtils.getBarSize(strings[0], strings[1], strings[2], strings[3], strings[4], strings[6]);
    }

    @Override
    public void draw(@Nonnull String[] strings, @Nonnull IWailaCommonAccessor iWailaCommonAccessor) {
        TJGuiUtils.drawBar(0, 0, strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], 0xAAAAAA);
    }
}
