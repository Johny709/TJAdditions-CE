package tj.mui.widgets.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
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
        if (this.isActive && this.activeBackgroundTexture != null) {
            for (TextureArea textureArea : this.activeBackgroundTexture) {
                textureArea.draw(pos.getX(), pos.getY(), 18, 18);
            }
        } else if (this.inactiveBackgroundTexture != null) for (TextureArea textureArea : this.inactiveBackgroundTexture) {
            textureArea.draw(pos.getX(), pos.getY(), 18, 18);
        }
        if (this.isActive) {
            if (!this.itemStack.isEmpty() || this.simulating) {
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
                    TJGuiUtils.drawItemStack(pos.x, pos.y, this.output, this.count, null);
                } else if (this.fluidOutput != null) {
                    TJGuiUtils.drawFluidStack(pos.x, pos.y, this.fluidOutput, this.count);
                } else TJGuiUtils.drawItemStack(pos.x, pos.y,
                        this.simulating ? this.getItemHandler().getStackInSlot(this.slotIndex) : this.itemStack,
                        this.simulating ? this.getItemHandler().getStackInSlot(this.slotIndex).getCount() : this.count,
                        null);
            }
            if (this.simulating || this.isMouseOverElement(mouseX, mouseY))
                drawSelectionOverlay(pos.x + 1, pos.y + 1, 16, 16);
        }
    }
}
