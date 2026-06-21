package tj.mui.widgets.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.TextFormattingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import tj.mui.TJGuiUtils;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;

public class AEPatternSlotWidget extends TJSlotWidget<AEPatternSlotWidget> {

    @Nonnull
    private NBTTagCompound outputCompound = new NBTTagCompound();

    @Nonnull
    private ItemStack output = ItemStack.EMPTY;

    private FluidStack fluidOutput;
    private long count;

    public AEPatternSlotWidget(IItemHandler itemHandler, int slotIndex, int x, int y) {
        super(itemHandler, slotIndex, x, y);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        final Position pos = this.getPosition();
        final int stackX = pos.getX() + 1;
        final int stackY = pos.getY() + 1;
        if (this.isActive && this.activeBackgroundTexture != null) {
            for (TextureArea textureArea : this.activeBackgroundTexture) {
                textureArea.draw(pos.getX(), pos.getY(), 18, 18);
            }
        } else if (this.inactiveBackgroundTexture != null) for (TextureArea textureArea : this.inactiveBackgroundTexture) {
            textureArea.draw(pos.getX(), pos.getY(), 18, 18);
        }
        if (this.isActive) {
            if (!this.itemStack.isEmpty() || this.simulating) {
                final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
                final NBTTagCompound compound = TJItemUtils.getCompoundFromStack(this.itemStack);
                final NBTTagList outputList = compound.getTagList("out", 10);
                final NBTTagCompound outputCompound = outputList.getCompoundTagAt(0);
                if (!this.outputCompound.equals(outputCompound)) {
                    this.outputCompound = outputCompound;
                    final String id = this.outputCompound.getString("id");
                    if (id.equals("ae2fc:fluid_drop")) {
                        this.output = ItemStack.EMPTY;
                        this.fluidOutput = new FluidStack(FluidRegistry.getFluid(this.outputCompound.getCompoundTag("tag").getString("Fluid")), 1);
                    } else {
                        this.output = TJItemUtils.getItemStackFromName(id, 1, this.outputCompound.getShort("Damage"));
                        this.fluidOutput = null;
                    }
                    this.count = this.outputCompound.getLong("Cnt");
                    if (this.count < 1)
                        this.count = this.outputCompound.getInteger("Count");
                }
                GlStateManager.disableBlend();
                if (!this.output.isEmpty()) {
                    drawItemStack(this.output, stackX, stackY, null);
                } else if (this.fluidOutput != null) {
                    TJGuiUtils.drawFluidForGui(this.fluidOutput, this.count, this.count, pos.getX() + 1, pos.getY() + 1, 17, 17);
                } else drawItemStack(this.simulating ? this.getItemHandler().getStackInSlot(this.slotIndex) : this.itemStack, stackX, stackY, null);
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.5, 0.5, 1);
                final String s = TextFormattingUtil.formatLongToCompactString(this.count, 4) + (this.fluidOutput != null ? "L" : "");
                fontRenderer.drawStringWithShadow(s, (pos.getX() + 6) * 2 - fontRenderer.getStringWidth(s) + 21, (pos.getY() + 12) * 2, 0xFFFFFF);
                final int itemCount = this.simulating ? this.getItemHandler().getStackInSlot(this.slotIndex).getCount() : this.itemCount;
                if (itemCount > 1) {
                    final String s1 = TextFormattingUtil.formatLongToCompactString(itemCount, 4);
                    fontRenderer.drawStringWithShadow(s1, (pos.getX() + 6) * 2 - fontRenderer.getStringWidth(s1) + 21, (pos.getY() + 2) * 2, 0xFFFFFF);
                }
                GlStateManager.popMatrix();
                GlStateManager.enableBlend();
                GlStateManager.color(1.0f, 1.0f, 1.0f);
            }
            if (this.simulating || this.isMouseOverElement(mouseX, mouseY))
                drawSelectionOverlay(stackX, stackY, 16, 16);
        }
    }
}
