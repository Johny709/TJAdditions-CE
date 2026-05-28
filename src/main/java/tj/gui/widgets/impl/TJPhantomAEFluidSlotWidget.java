package tj.gui.widgets.impl;

import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidTank;
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

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TJPhantomAEFluidSlotWidget extends Widget implements IGhostIngredientTarget, IIngredientSlot {

    private final Consumer<FluidStack> onUpdate;
    private final Consumer<FluidStack> onExtracted;
    private final IAEFluidTank tanks;
    private final int slotIndex;
    private TextureArea backgroundTexture;
    private FluidStack fluidStack;
    private Predicate<FluidStack> putFluidsPredicate;
    private Predicate<FluidStack> takeFluidsPredicate;

    public TJPhantomAEFluidSlotWidget(int x, int y, int width, int height, int slotIndex, IAEFluidTank tanks, Consumer<FluidStack> onUpdate) {
        this(x, y, width, height, slotIndex, tanks, onUpdate, null);
    }

    public TJPhantomAEFluidSlotWidget(int x, int y, int width, int height, int slotIndex, IAEFluidTank tanks, Consumer<FluidStack> onUpdate, Consumer<FluidStack> onExtracted) {
        super(new Position(x, y), new Size(width, height));
        this.tanks = tanks;
        this.slotIndex = slotIndex;
        this.onUpdate = onUpdate;
        this.onExtracted = onExtracted;
    }

    public TJPhantomAEFluidSlotWidget setBackgroundTexture(TextureArea backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    /**
     * Set condition for fluid to be placed into slot either by dragging from JEI or placing fluid held by mouse into slot.
     * Will always be true if supplier not set or null.
     */
    public TJPhantomAEFluidSlotWidget setPutFluidsPredicate(Predicate<FluidStack> putFluidsPredicate) {
        this.putFluidsPredicate = putFluidsPredicate;
        return this;
    }

    /**
     * Set condition for fluid to be removed from slot. Will always be true if supplier not set or null.
     */
    public TJPhantomAEFluidSlotWidget setTakeFluidsPredicate(Predicate<FluidStack> takeItemsPredicate) {
        this.takeFluidsPredicate = takeItemsPredicate;
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
        } else if (id == 2) {
            this.fluidStack = null;
        }
    }

    @Override
    public void detectAndSendChanges() {
        final IAEFluidStack iaeFluidStack = this.tanks.getFluidInSlot(this.slotIndex);
        if (iaeFluidStack != null) {
            final FluidStack stack = iaeFluidStack.getFluidStack();
            if (stack != null) {
                this.fluidStack = stack;
                this.writeUpdateInfo(1, buffer -> buffer.writeCompoundTag(this.fluidStack.writeToNBT(new NBTTagCompound())));
            }
        } else this.writeUpdateInfo(2, buffer -> {});
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            try {
                final FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag());
                if (this.putFluidsPredicate == null || this.putFluidsPredicate.test(fluidStack)) {
                    this.fluidStack = fluidStack;
                    this.tanks.setFluidInSlot(this.slotIndex, AEFluidStack.fromFluidStack(this.fluidStack));
                    if (this.onUpdate != null)
                        this.onUpdate.accept(this.fluidStack);
                } else if (this.fluidStack != null) {
                    this.writeUpdateInfo(1, buffer1 -> buffer1.writeCompoundTag(this.fluidStack.writeToNBT(new NBTTagCompound())));
                } else this.writeUpdateInfo(2, buffer1 -> {});
            } catch (IOException e) {
                GTLog.logger.info(e.getMessage());
            }
        } else if (id == 2) {
            final FluidStack extracted = this.tanks.getFluidInSlot(this.slotIndex).getFluidStack();
            if (extracted == null) return;
            if (this.takeFluidsPredicate == null || this.takeFluidsPredicate.test(extracted)) {
                final FluidStack fluidStack = this.tanks.getFluidInSlot(this.slotIndex).getFluidStack();
                this.tanks.setFluidInSlot(this.slotIndex, null);
                if (this.onUpdate != null)
                    this.onUpdate.accept(null);
                if (this.onExtracted != null && fluidStack != null)
                    this.onExtracted.accept(fluidStack);
            } else this.writeUpdateInfo(1, buffer1 -> buffer1.writeCompoundTag(extracted.writeToNBT(new NBTTagCompound())));
        }
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object o) {
        return !(o instanceof FluidStack) ? Collections.emptyList() : Collections.singletonList(new IGhostIngredientHandler.Target<Object>() {

            @Nonnull
            @Override
            public Rectangle getArea() {
                return toRectangleBox();
            }

            @Override
            public void accept(@Nonnull Object o) {
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
