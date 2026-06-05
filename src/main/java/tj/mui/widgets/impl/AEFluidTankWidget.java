package tj.mui.widgets.impl;

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
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import tj.TJ;
import tj.mui.TJGuiUtils;
import tj.mui.widgets.TJWidget;
import tj.util.TJItemUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AEFluidTankWidget extends TJWidget<AEFluidTankWidget> implements IIngredientSlot {

    private final AEFluidInventory fluidTank;
    private final int slotIndex;

    private TextureArea[] backgroundTextures;
    private IAEFluidStack iaeFluidStack;
    private int capacity;
    private int amount;

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
        final List<String> tooltips = new ArrayList<>();
        if (this.amount > 0 && this.iaeFluidStack != null) {
            tooltips.add(this.iaeFluidStack.getFluidStack().getLocalizedName());
            // Add chemical formula tooltip
            final String formula = FluidTooltipUtil.getFluidTooltip(this.iaeFluidStack.getFluidStack());
            tooltips.add(formula == null || formula.isEmpty() ? "" : "§7" + formula);
        }
        tooltips.add(I18n.format("gregtech.fluid.amount", this.amount, this.capacity));
        this.drawHoveringText(ItemStack.EMPTY, tooltips, 200, mouseX, mouseY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        final Size size = this.getSize();
        final Position pos = this.getPosition();
        if (this.backgroundTextures != null) for (TextureArea textureArea : this.backgroundTextures) {
            textureArea.draw(pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
        }
        if (this.amount < 1 || this.iaeFluidStack == null) return;
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        GlStateManager.disableBlend();
        TJGuiUtils.drawFluidForGui(this.iaeFluidStack.getFluidStack(), this.amount, this.capacity, pos.getX() + 1, pos.getY() + 1, size.getWidth() - 1, size.getHeight() - 2);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 1);
        final String s = TextFormattingUtil.formatLongToCompactString(this.amount, 4) + "L";
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
        this.playButtonClickSound();
        final boolean shiftClick = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        if (button == 0) { // Left-Click
            this.writeClientAction(1, buffer -> {
                buffer.writeItemStack(itemStack);
                buffer.writeBoolean(shiftClick);
            });
        } else if (button == 1) { // Right-Click
            this.writeClientAction(2, buffer -> {
                buffer.writeItemStack(itemStack);
                buffer.writeBoolean(shiftClick);
            });
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        try {
            switch (id) {
                case 1: this.gui.entityPlayer.inventory.setItemStack(buffer.readItemStack());
                    break;
                case 2: this.iaeFluidStack = AEFluidStack.fromPacket(buffer);
                    break;
                case 3: this.capacity = buffer.readInt();
                    break;
                case 4: this.iaeFluidStack = null;
                    break;
                case 5: this.amount = buffer.readInt();
            }
        } catch (IOException e) {
            TJ.logger.info(e.getMessage());
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        try {
            if (id > 2) return;
            ItemStack itemStack = buffer.readItemStack();
            final int size = buffer.readBoolean() ? itemStack.getCount() : 1;
            final int tankCapacity = this.fluidTank.getTankProperties()[0].getCapacity();
            if (!this.isActive) return;
            final IFluidHandlerItem fluidHandlerItem = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandlerItem == null) return;
            if (id == 1) {
                for (int i = 0; i < size; i++) {
                    final FluidStack fluidContained = FluidUtil.getFluidContained(itemStack);
                    if (fluidContained == null) return;
                    final int toDrain = (int) Math.min(fluidContained.amount, tankCapacity - this.getFluidAmount(this.slotIndex));
                    final FluidStack bucketFluid = this.getFluidStack(this.slotIndex);
                    final FluidActionResult fluidActionResult = FluidUtil.tryEmptyContainer(itemStack, new FluidTank(bucketFluid, tankCapacity), toDrain, this.gui.entityPlayer, false);
                    if (fluidActionResult == FluidActionResult.FAILURE) break;
                    if (itemStack.getCount() > 1) {
                        if (!TJItemUtils.insertInMainInventory(this.gui.entityPlayer.inventory, fluidActionResult.getResult()).isEmpty()) break;
                        itemStack.shrink(1);
                    } else itemStack = fluidActionResult.getResult();
                    FluidUtil.tryEmptyContainer(itemStack, new FluidTank(bucketFluid, tankCapacity), toDrain, this.gui.entityPlayer, true);
                    FluidStack fluidStack = this.getFluidStack(this.slotIndex);
                    if (fluidStack == null) {
                        fluidStack = new FluidStack(fluidContained.getFluid(), toDrain);
                    } else fluidStack.amount += toDrain;
                    this.iaeFluidStack = AEFluidStack.fromFluidStack(fluidStack);
                    this.fluidTank.setFluidInSlot(this.slotIndex, this.iaeFluidStack.copy());
                }
            } else if (id == 2) {
                this.iaeFluidStack = this.fluidTank.getFluidInSlot(this.slotIndex);
                if (this.iaeFluidStack == null) return;
                for (int i = 0; i < size; i++) {
                    final FluidStack fluidContained = FluidUtil.getFluidContained(itemStack);
                    final int bucketCapacity = fluidHandlerItem.getTankProperties()[0].getCapacity();
                    final int toFill = (int) Math.min(this.getFluidAmount(this.slotIndex), bucketCapacity - (fluidContained != null ? fluidContained.amount : 0));
                    final FluidStack tankFluid = this.getFluidStack(this.slotIndex);
                    final IFluidHandler tank = new FluidTank(tankFluid, tankCapacity);
                    final FluidActionResult fluidActionResult = FluidUtil.tryFillContainer(itemStack, tank, toFill, this.gui.entityPlayer, false);
                    if (fluidActionResult == FluidActionResult.FAILURE) break;
                    if (itemStack.getCount() > 1) {
                        if (!TJItemUtils.insertInMainInventory(this.gui.entityPlayer.inventory, fluidActionResult.getResult()).isEmpty()) break;
                        itemStack.shrink(1);
                    } else itemStack = fluidActionResult.getResult();
                    FluidUtil.tryFillContainer(itemStack, tank, toFill, this.gui.entityPlayer, true);
                    if (tank.getTankProperties()[0].getContents() == null || (this.iaeFluidStack.getStackSize() - toFill) < 1) {
                        this.iaeFluidStack = null;
                    } else this.iaeFluidStack.setStackSize(this.iaeFluidStack.getStackSize() - toFill);
                    this.fluidTank.setFluidInSlot(this.slotIndex, this.iaeFluidStack != null ? this.iaeFluidStack.copy() : null);
                }
            }
            this.gui.entityPlayer.inventory.setItemStack(itemStack);
            this.writeUpdateInfo(1, buffer1 -> buffer1.writeItemStack(this.gui.entityPlayer.inventory.getItemStack()));
        } catch (IOException e) {
            TJ.logger.info(e.getMessage());
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        final IAEFluidStack iaeFluidStack = this.fluidTank.getFluidInSlot(this.slotIndex);
        if (iaeFluidStack != null) {
            if (iaeFluidStack != this.iaeFluidStack) {
                this.iaeFluidStack = iaeFluidStack;
                this.writeUpdateInfo(2, buffer -> {
                    try {
                        this.iaeFluidStack.writeToPacket(buffer);
                    } catch (IOException e) {
                        TJ.logger.info(e.getMessage());
                    }
                });
            }
        } else if (this.iaeFluidStack != null) {
            this.iaeFluidStack = null;
            this.writeUpdateInfo(4, buffer -> {});
        }
        final int amount = (int) (iaeFluidStack != null ? iaeFluidStack.getStackSize() : 0);
        if (amount != this.amount) {
            this.amount = amount;
            this.writeUpdateInfo(5, buffer -> buffer.writeInt(this.amount));
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

    private long getFluidAmount(int slotIndex) {
        final IAEFluidStack iaeFluidStack = this.fluidTank.getFluidInSlot(slotIndex);
        return iaeFluidStack != null ? iaeFluidStack.getStackSize() : 0;
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        return this.isMouseOverElement(mouseX, mouseY) ? this.getFluidStack(this.slotIndex) : null;
    }
}
