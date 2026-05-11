package tj.gui.widgets.impl;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.igredient.IGhostIngredientTarget;
import gregtech.api.gui.igredient.IIngredientSlot;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.*;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.gui.TJGuiUtils;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class TJPhantomFluidSlotWidget extends Widget implements IGhostIngredientTarget, IIngredientSlot {

    private final Consumer<FluidStack> fluidStackConsumer;
    private final IMultipleTankHandler tanks;
    private final int slotIndex;
    private TextureArea backgroundTexture;
    private FluidStack fluidStack;

    public TJPhantomFluidSlotWidget(int x, int y, int width, int height, int slotIndex, IMultipleTankHandler tanks, Consumer<FluidStack> fluidStackConsumer) {
        super(new Position(x, y), new Size(width, height));
        this.fluidStackConsumer = fluidStackConsumer;
        this.tanks = tanks;
        this.slotIndex = slotIndex;
    }

    public TJPhantomFluidSlotWidget setBackgroundTexture(TextureArea backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (!this.isMouseOverElement(mouseX, mouseY)) return;
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        final int screenWidth = Minecraft.getMinecraft().displayWidth;
        final int screenHeight = Minecraft.getMinecraft().displayHeight;
        if (this.fluidStack != null) {
            String formula = FluidTooltipUtil.getFluidTooltip(this.fluidStack);
            formula = formula == null || formula.isEmpty() ? "" : this.fluidStack.getLocalizedName();
            GuiUtils.drawHoveringText(Collections.singletonList(formula), mouseX, mouseY, screenWidth, screenHeight, 100, fontRenderer);
        }
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
        if (this.isMouseOverElement(mouseX, mouseY)) {
            if (button == 0) { // Left-Click
                final FluidStack fluidStack = FluidUtil.getFluidContained(this.gui.entityPlayer.inventory.getItemStack());
                if (fluidStack != null) {
                    this.fluidStack = fluidStack.copy();
                    this.writeClientAction(1, buffer -> buffer.writeCompoundTag(this.fluidStack.writeToNBT(new NBTTagCompound())));
                    return true;
                }
            } else if (button == 1) { // Right-Click
                this.writeClientAction(2, buffer -> {});
                this.fluidStack = null;
                return true;
            }
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
        final FluidStack stack = this.tanks.getTankAt(this.slotIndex).getFluid();
        if (stack != null) {
            this.fluidStack = stack;
            this.writeUpdateInfo(1, buffer -> buffer.writeCompoundTag(this.fluidStack.writeToNBT(new NBTTagCompound())));
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            try {
                this.fluidStack = FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag());
                this.tanks.getTankAt(this.slotIndex).drain(Integer.MIN_VALUE, true);
                this.tanks.getTankAt(this.slotIndex).fill(this.fluidStack, true);
                if (this.fluidStackConsumer != null)
                    this.fluidStackConsumer.accept(this.fluidStack);
            } catch (IOException e) {
                GTLog.logger.info(e.getMessage());
            }
        } else if (id == 2) {
            this.fluidStack = null;
            this.tanks.getTankAt(this.slotIndex).drain(Integer.MIN_VALUE, true);
            if (this.fluidStackConsumer != null)
                this.fluidStackConsumer.accept(null);
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
                    writeClientAction(1, buffer -> buffer.writeCompoundTag(fluidStack.writeToNBT(new NBTTagCompound())));
                }
            }
        });
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        return this.isMouseOverElement(mouseX, mouseY) ? this.fluidStack : null;
    }
}
