package tj.integration.hwyla.renderers;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.TextureArea;
import mcp.mobius.waila.api.IWailaCommonAccessor;
import mcp.mobius.waila.api.IWailaTooltipRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import tj.TJValues;
import tj.mui.TJGuiTextures;

import javax.annotation.Nonnull;
import java.awt.*;

public class ProgressInfoRenderer implements IWailaTooltipRenderer {

    @Nonnull
    @Override
    public Dimension getSize(@Nonnull String[] strings, @Nonnull IWailaCommonAccessor iWailaCommonAccessor) {
        final int text = Minecraft.getMinecraft().fontRenderer.getStringWidth(strings[0] + " ");
        final int progress = Minecraft.getMinecraft().fontRenderer.getStringWidth(strings[1]);
        final int slash = Minecraft.getMinecraft().fontRenderer.getStringWidth(" / ");
        final int maxProgress = Minecraft.getMinecraft().fontRenderer.getStringWidth(strings[2]);
        final int colon = Minecraft.getMinecraft().fontRenderer.getStringWidth(" : ");
        final int progressSuffix = Minecraft.getMinecraft().fontRenderer.getStringWidth(strings[3]);
        final int maxProgressSuffix = Minecraft.getMinecraft().fontRenderer.getStringWidth(strings[4]);
        return new Dimension(text + Math.max(50, progress + progressSuffix + slash + maxProgress + maxProgressSuffix + colon), 12);
    }

    @Override
    public void draw(@Nonnull String[] strings, @Nonnull IWailaCommonAccessor iWailaCommonAccessor) {
        final int offsetX = Minecraft.getMinecraft().fontRenderer.getStringWidth(strings[0] + " ");
        final int progress = Integer.parseInt(strings[1]);
        final int maxProgress = Integer.parseInt(strings[2]);
        final String percentage = String.format("%s%%", TJValues.thousandFormat.format(1.0 * progress / maxProgress * 100));
        final int progressLength = Minecraft.getMinecraft().fontRenderer.getStringWidth(strings[1]);
        final int slashLength = Minecraft.getMinecraft().fontRenderer.getStringWidth(" / ");
        final int maxProgressLength = Minecraft.getMinecraft().fontRenderer.getStringWidth(strings[2]);
        final int colonLength = Minecraft.getMinecraft().fontRenderer.getStringWidth(" : ");
        final int percentageLength = Minecraft.getMinecraft().fontRenderer.getStringWidth(percentage);
        final int progressSuffixLength = Minecraft.getMinecraft().fontRenderer.getStringWidth(strings[3]);
        final int maxProgressSuffixLength = Minecraft.getMinecraft().fontRenderer.getStringWidth(strings[4]);
        final int totalLength = Math.max(50,
                progressLength + progressSuffixLength + slashLength + maxProgressLength + maxProgressSuffixLength + colonLength + percentageLength + 6);
        final int barWidth = maxProgress == 0 ? 0 : (int) (totalLength * (1.0 * progress / maxProgress));

        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        GuiTextures.DISPLAY.draw(offsetX, 0, totalLength, 12);
        this.getBarByColor(strings[5]).draw(offsetX + 1, 1, barWidth - 1, 10);
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(strings[0], 0, 2, 0xAAAAAA);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(strings[1], offsetX + 3, 2, 0xFFFFFF);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(strings[3], offsetX + 3 + progressLength, 2, 0xFFFFFF);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(" / ", offsetX + 3 + progressLength + progressSuffixLength, 2, 0xFFFFFF);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(strings[2],
                offsetX + 3 + progressLength + progressSuffixLength + slashLength, 2, 0xFFFFFF);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(strings[4],
                offsetX + 3 + progressLength + progressSuffixLength + slashLength + maxProgressLength, 2, 0xFFFFFF);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(" : ",
                offsetX + 3 + progressLength + progressSuffixLength + slashLength + maxProgressLength + maxProgressSuffixLength, 2, 0xFFFFFF);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(percentage,
                offsetX + 3 + progressLength + progressSuffixLength + slashLength + maxProgressLength + maxProgressSuffixLength + colonLength,
                2, 0xFFFFFF);
    }

    private TextureArea getBarByColor(String color) {
        switch (color) {
            case "YELLOW": return TJGuiTextures.BAR_YELLOW;
            case "GREEN": return TJGuiTextures.BAR_GREEN;
            default: return TJGuiTextures.BAR_RED;
        }
    }
}
