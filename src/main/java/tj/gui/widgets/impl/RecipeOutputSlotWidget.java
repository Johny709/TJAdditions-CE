package tj.gui.widgets.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.igredient.IIngredientSlot;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.gui.TJGuiTextures;
import tj.gui.TJGuiUtils;

import java.util.Collections;
import java.util.function.IntFunction;

public class RecipeOutputSlotWidget extends Widget implements IIngredientSlot {

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
    public void drawInForeground(int mouseX, int mouseY) {
        if (!this.isMouseOverElement(mouseX, mouseY)) return;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int screenWidth = Minecraft.getMinecraft().displayWidth;
        int screenHeight = Minecraft.getMinecraft().displayHeight;
        ItemStack itemStack = this.itemOutputs != null ? this.itemOutputs.apply(this.slotIndex) : null;
        if (itemStack != null && !itemStack.isEmpty()) {
            GuiUtils.drawHoveringText(getItemToolTip(itemStack), mouseX, mouseY, screenWidth, screenHeight, 100, fontRenderer);
        }
        FluidStack fluidStack = this.fluidOutputs != null ? this.fluidOutputs.apply(this.slotIndex) : null;
        if (fluidStack != null) {
            String formula = FluidTooltipUtil.getFluidTooltip(fluidStack);
            formula = formula == null || formula.isEmpty() ? "" : fluidStack.getLocalizedName();
            GuiUtils.drawHoveringText(Collections.singletonList(formula), mouseX, mouseY, screenWidth, screenHeight, 100, fontRenderer);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        Position pos = this.getPosition();
        ItemStack itemStack = this.itemOutputs != null ? this.itemOutputs.apply(this.slotIndex) : null;
        if (itemStack != null) {
            Widget.drawItemStack(itemStack, pos.getX() + 1, pos.getY() + 1, null);
            if (itemStack.isEmpty()) {
                TJGuiTextures.SELECTION_BOX.draw(pos.getX(), pos.getY(), 18, 18);
            } else TJGuiTextures.SELECTION_BOX_2.draw(pos.getX(), pos.getY(), 18, 18);
        }
        FluidStack fluidStack = this.fluidOutputs != null ? this.fluidOutputs.apply(this.slotIndex) : null;
        if (fluidStack != null) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            GlStateManager.disableBlend();
            TJGuiUtils.drawFluidForGui(fluidStack, fluidStack.amount, fluidStack.amount, pos.getX() + 1, pos.getY() + 1, 17, 17);
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5, 0.5, 1);
            String s = TextFormattingUtil.formatLongToCompactString(fluidStack.amount, 4) + "L";
            fontRenderer.drawStringWithShadow(s, (pos.getX() + 6) * 2 - fontRenderer.getStringWidth(s) + 21, (pos.getY() + 12) * 2, 0xFFFFFF);
            GlStateManager.popMatrix();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f);
            if (fluidStack.amount < 1) {
                TJGuiTextures.SELECTION_BOX.draw(pos.getX(), pos.getY(), 18, 18);
            } else TJGuiTextures.SELECTION_BOX_2.draw(pos.getX(), pos.getY(), 18, 18);
        }
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (!this.isMouseOverElement(mouseX, mouseY))
            return null;
        if (this.itemOutputs != null)
            return this.itemOutputs.apply(this.slotIndex);
        if (this.fluidOutputs != null)
            return this.fluidOutputs.apply(this.slotIndex);
        return null;
    }
}
