package tj.gui.widgets.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.igredient.IGhostIngredientTarget;
import gregtech.api.gui.igredient.IIngredientSlot;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.GTLog;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.TextFormattingUtil;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.gui.TJGuiUtils;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TJPhantomFluidSlotWidget extends Widget implements IGhostIngredientTarget, IIngredientSlot {

    private final Supplier<FluidStack> fluidStackSupplier;
    private final Consumer<FluidStack> fluidStackConsumer;
    private TextureArea backgroundTexture;
    private FluidStack fluidStack;

    public TJPhantomFluidSlotWidget(int x, int y, int width, int height, Supplier<FluidStack> fluidStackSupplier, Consumer<FluidStack> fluidStackConsumer) {
        super(new Position(x, y), new Size(width, height));
        this.fluidStackSupplier = fluidStackSupplier;
        this.fluidStackConsumer = fluidStackConsumer;
    }

    public TJPhantomFluidSlotWidget setBackgroundTexture(TextureArea backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        if (this.backgroundTexture != null)
            this.backgroundTexture.draw(this.getPosition().getX(), this.getPosition().getY(), this.getSize().getWidth(), this.getSize().getHeight());
        if (this.fluidStack == null) return;
        final Position pos = this.getPosition();
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        GlStateManager.disableBlend();
        TJGuiUtils.drawFluidForGui(this.fluidStack, this.fluidStack.amount, this.fluidStack.amount, pos.getX() + 1, pos.getY() + 1, 17, 17);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 1);
        final String s = TextFormattingUtil.formatLongToCompactString(this.fluidStack.amount, 4) + "L";
        fontRenderer.drawStringWithShadow(s, (pos.getX() + 6) * 2 - fontRenderer.getStringWidth(s) + 21, (pos.getY() + 12) * 2, 0xFFFFFF);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY) && button == 1) {
            this.writeClientAction(2, buffer -> {});
            this.fluidStack = null;
            return true;
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            try {
                this.fluidStack = FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag());
            } catch (IOException e) {
                GTLog.logger.info(e.getMessage());
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (this.fluidStackSupplier != null) {
            final FluidStack stack = this.fluidStackSupplier.get();
            if (stack != null && !stack.isFluidStackIdentical(this.fluidStack)) {
                this.fluidStack = stack;
                this.writeUpdateInfo(1, buffer -> buffer.writeCompoundTag(this.fluidStack.writeToNBT(new NBTTagCompound())));
            }
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            try {
                this.fluidStack = FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag());
            } catch (IOException e) {
                GTLog.logger.info(e.getMessage());
            }
        } else if (id == 2) {
            this.fluidStack = null;
            if (this.fluidStackConsumer != null)
                this.fluidStackConsumer.accept(null);
        } else if (id == 3) {
            try {
                this.fluidStack = FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag());
            } catch (IOException e) {
                GTLog.logger.info(e.getMessage());
            }
            if (this.fluidStackConsumer != null)
                this.fluidStackConsumer.accept(this.fluidStack);
        }
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object o) {
        return !(o instanceof FluidStack) ? Collections.emptyList() : Collections.singletonList(new IGhostIngredientHandler.Target<Object>() {
            @Override
            public Rectangle getArea() {
                return toRectangleBox();
            }

            @Override
            public void accept(Object o) {
                if (o instanceof FluidStack) {
                    fluidStack = ((FluidStack) o).copy();
                    writeClientAction(3, buffer -> buffer.writeCompoundTag(fluidStack.writeToNBT(new NBTTagCompound())));
                }
            }
        });
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        return this.isMouseOverElement(mouseX, mouseY) ? this.fluidStack : null;
    }
}
