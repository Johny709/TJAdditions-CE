package tj.gui.widgets.impl;

import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.AEFluidStack;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.igredient.IIngredientSlot;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.gui.TJGuiUtils;
import tj.gui.widgets.TJWidget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AEFluidTankWidget extends TJWidget<AEFluidTankWidget> implements IIngredientSlot {

    private final AEFluidInventory fluidTank;
    private final int slotIndex;

    private TextureArea[] backgroundTextures;
    private FluidStack fluidStack;
    private int capacity;

    public AEFluidTankWidget(AEFluidInventory fluidTank, int slotIndex, int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        this.fluidTank = fluidTank;
        this.slotIndex = slotIndex;
    }

    public AEFluidTankWidget setBackgroundTextures(TextureArea... backgroundTextures) {
        this.backgroundTextures = backgroundTextures;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (!this.isActive || !this.isMouseOverElement(mouseX, mouseY)) return;
        final int amount;
        final List<String> tooltips = new ArrayList<>();
        if (this.fluidStack != null) {
            tooltips.add(fluidStack.getLocalizedName());
            // Add chemical formula tooltip
            final String formula = FluidTooltipUtil.getFluidTooltip(this.fluidStack);
            tooltips.add(formula == null || formula.isEmpty() ? "" : "§7" + formula);
            amount = this.fluidStack.amount;
        } else amount = 0;
        tooltips.add(I18n.format("gregtech.fluid.amount", amount, this.capacity));
        this.drawHoveringText(ItemStack.EMPTY, tooltips, 100, mouseX, mouseY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        final Size size = this.getSize();
        final Position pos = this.getPosition();
        if (this.backgroundTextures != null) for (TextureArea textureArea : this.backgroundTextures) {
            textureArea.draw(pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
        }
        if (this.fluidStack == null) return;
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        GlStateManager.disableBlend();
        TJGuiUtils.drawFluidForGui(this.fluidStack, Math.max(1, this.fluidStack.amount), Math.max(1, this.capacity), pos.getX() + 1, pos.getY() + 1, size.getWidth() - 1, size.getHeight() - 1);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 1);
        final String s = TextFormattingUtil.formatLongToCompactString(this.fluidStack.amount, 4) + "L";
        fontRenderer.drawStringWithShadow(s, (pos.getX() + 6) * 2 - fontRenderer.getStringWidth(s) + 21, (pos.getY() + size.getHeight() - 6) * 2, 0xFFFFFF);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!this.isActive || !this.isMouseOverElement(mouseX, mouseY))
            return false;
        final ItemStack itemStack = this.gui.entityPlayer.inventory.getItemStack();
        final IFluidHandlerItem fluidHandlerItem = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandlerItem == null)
            return false;
        this.writeClientAction(1, buffer -> buffer.writeItemStack(itemStack));
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        try {
            if (id == 1) {
                this.gui.entityPlayer.inventory.setItemStack(buffer.readItemStack());
            } else if (id == 2) {
                this.fluidStack = FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag());
            } else if (id == 3) {
                this.capacity = buffer.readInt();
            }
        } catch (IOException e) {
            GTLog.logger.info(e.getMessage());
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        try {
            if (id == 1) {
                final ItemStack itemStack = buffer.readItemStack();
                if (!this.isActive) return;
                final IFluidHandlerItem fluidHandlerItem = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (fluidHandlerItem == null) return;
                final FluidStack fluidStack = FluidUtil.getFluidContained(itemStack);
                if (fluidStack == null) return;
                final int toDrain = Math.min(fluidStack.amount, this.fluidTank.getTankProperties()[0].getCapacity() - this.getFluidAmount(this.slotIndex));
                final FluidActionResult fluidActionResult = FluidUtil.tryEmptyContainer(itemStack, new FluidTank(this.getFluidStack(this.slotIndex), this.fluidTank.getTankProperties()[0].getCapacity()), toDrain, this.gui.entityPlayer, true);
                if (fluidActionResult == FluidActionResult.FAILURE) return;
                FluidStack filled = this.getFluidStack(this.slotIndex);
                if (filled == null) {
                    filled = new FluidStack(fluidStack.getFluid(), toDrain);
                } else filled.amount += toDrain;
                this.fluidTank.setFluidInSlot(this.slotIndex, AEFluidStack.fromFluidStack(filled));
                this.gui.entityPlayer.inventory.setItemStack(fluidActionResult.getResult());
                this.writeUpdateInfo(1, buffer1 -> buffer1.writeItemStack(this.gui.entityPlayer.inventory.getItemStack()));
            }
        } catch (IOException e) {
            GTLog.logger.info(e.getMessage());
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        final FluidStack fluidStack = this.getFluidStack(this.slotIndex);
        if (fluidStack != null && !fluidStack.isFluidStackIdentical(this.fluidStack)) {
            this.fluidStack = fluidStack;
            this.writeUpdateInfo(2, buffer -> buffer.writeCompoundTag(this.fluidStack.writeToNBT(new NBTTagCompound())));
        }
        final int capacity = this.fluidTank.getTankProperties()[0].getCapacity();
        if (capacity != this.capacity) {
            this.capacity = capacity;
            this.writeUpdateInfo(3, buffer -> buffer.writeInt(this.capacity));
        }
    }

    private FluidStack getFluidStack(int slotIndex) {
        final IAEFluidStack iaeFluidStack = this.fluidTank.getFluidInSlot(slotIndex);
        return iaeFluidStack != null ? iaeFluidStack.getFluidStack() : null;
    }

    private int getFluidAmount(int slotIndex) {
        final IAEFluidStack iaeFluidStack = this.fluidTank.getFluidInSlot(slotIndex);
        if (iaeFluidStack == null)
            return 0;
        final FluidStack fluidStack = iaeFluidStack.getFluidStack();
        return fluidStack != null ? fluidStack.amount : 0;
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        return this.isMouseOverElement(mouseX, mouseY) ? this.getFluidStack(this.slotIndex) : null;
    }
}
