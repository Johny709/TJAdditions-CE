package tj.integration.theoneprobe.impl;

import gregtech.api.util.TextFormattingUtil;
import io.netty.buffer.ByteBuf;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.network.NetworkTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import tj.integration.theoneprobe.TheOneProbeCompatibility;
import tj.mui.TJGuiUtils;

public class ElementFluidStack implements IElement {

    private FluidStack fluidStack;

    public ElementFluidStack(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
    }

    public ElementFluidStack(ByteBuf byteBuf) {
        if (byteBuf.readBoolean())
            this.fluidStack = FluidStack.loadFluidStackFromNBT(NetworkTools.readNBT(byteBuf));
    }

    @Override
    public void render(int x, int y) {
        if (this.fluidStack == null || this.fluidStack.amount < 1) return;
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        GlStateManager.disableBlend();
        TJGuiUtils.drawFluidForGui(this.fluidStack, this.fluidStack.amount, this.fluidStack.amount, x + 1, y + 1, this.getWidth() - 1, this.getHeight() - 2);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 1);
        final String s = TextFormattingUtil.formatLongToCompactString(this.fluidStack.amount, 4) + "L";
        fontRenderer.drawStringWithShadow(s, (x + 6) * 2 - fontRenderer.getStringWidth(s) + 21, (y + this.getHeight() - 6) * 2, 0xFFFFFF);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
    }

    @Override
    public int getWidth() {
        return 18;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeBoolean(this.fluidStack != null);
        if (this.fluidStack != null)
            NetworkTools.writeNBT(byteBuf, this.fluidStack.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public int getID() {
        return TheOneProbeCompatibility.ELEMENT_FLUIDSTACK;
    }
}
