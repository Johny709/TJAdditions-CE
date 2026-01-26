package tj.gui.widgets.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.gui.TJGuiUtils;

import java.util.function.IntFunction;

public class RecipeOutputSlotWidget extends Widget {

    private final IntFunction<ItemStack> itemOutputs;
    private final IntFunction<FluidStack> fluidOutputs;
    private final int slotIndex;

    public RecipeOutputSlotWidget(int slotIndex, int x, int y, int width, int height, IntFunction<ItemStack> itemOutputs, IntFunction<FluidStack> fluidOutputs) {
        super(new Position(x, y), new Size(width, height));
        this.itemOutputs = itemOutputs;
        this.fluidOutputs = fluidOutputs;
        this.slotIndex = slotIndex;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        Position pos = this.getPosition();
        ItemStack itemStack = this.itemOutputs != null ? this.itemOutputs.apply(this.slotIndex) : null;
        if (itemStack != null) {
            Widget.drawSelectionOverlay(pos.getX(), pos.getY(), 18, 18);
            Widget.drawItemStack(itemStack, pos.getX() + 1, pos.getY() + 1, null);
        }
        FluidStack fluidStack = this.fluidOutputs != null ? this.fluidOutputs.apply(this.slotIndex) : null;
        if (fluidStack != null) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            Widget.drawSelectionOverlay(pos.getX(), pos.getY(), 18, 18);
            GlStateManager.disableBlend();
            TJGuiUtils.drawFluidForGui(fluidStack, fluidStack.amount, fluidStack.amount, pos.getX() + 1, pos.getY() + 1, 17, 17);
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5, 0.5, 1);
            String s = TextFormattingUtil.formatLongToCompactString(fluidStack.amount, 4) + "L";
            fontRenderer.drawStringWithShadow(s, (pos.getX() + 6) * 2 - fontRenderer.getStringWidth(s) + 21, (pos.getY() + 14) * 2, 0xFFFFFF);
            GlStateManager.popMatrix();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f);
        }
    }
}
