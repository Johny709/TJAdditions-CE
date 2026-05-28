package tj.gui.widgets.impl;

import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.IAEFluidTank;
import com.mojang.realmsclient.gui.ChatFormatting;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.igredient.IIngredientSlot;
import gregtech.api.gui.resources.RenderUtil;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import tj.util.TJItemUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AEFluidTankWidget extends Widget implements IIngredientSlot {

    private final IAEFluidTank fluidTank;
    private final int slotIndex;

    public int fluidRenderOffset = 1;
    private boolean hideTooltip;
    private boolean alwaysShowFull;

    private boolean allowClickFilling;
    private boolean allowClickEmptying;

    private TextureArea[] backgroundTexture;
    private TextureArea overlayTexture;

    private FluidStack lastFluidInTank;
    private int lastTankCapacity;

    public AEFluidTankWidget(IAEFluidTank fluidTank, int slotIndex, int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        this.fluidTank = fluidTank;
        this.slotIndex = slotIndex;
    }

    public AEFluidTankWidget setHideTooltip(boolean hideTooltip) {
        this.hideTooltip = hideTooltip;
        return this;
    }

    public AEFluidTankWidget setAlwaysShowFull(boolean alwaysShowFull) {
        this.alwaysShowFull = alwaysShowFull;
        return this;
    }

    public AEFluidTankWidget setBackgroundTexture(TextureArea... backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    public AEFluidTankWidget setOverlayTexture(TextureArea overlayTexture) {
        this.overlayTexture = overlayTexture;
        return this;
    }

    public AEFluidTankWidget setFluidRenderOffset(int fluidRenderOffset) {
        this.fluidRenderOffset = fluidRenderOffset;
        return this;
    }

    public AEFluidTankWidget setContainerClicking(boolean allowClickContainerFilling, boolean allowClickContainerEmptying) {
        if (!(fluidTank instanceof IFluidHandler))
            throw new IllegalStateException("Container IO is only supported for fluid tanks that implement IFluidHandler");
        this.allowClickFilling = allowClickContainerFilling;
        this.allowClickEmptying = allowClickContainerEmptying;
        return this;
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            return lastFluidInTank;
        }
        return null;
    }

    public String getFormattedFluidAmount() {
        return String.format("%,d", lastFluidInTank == null ? 0 : lastFluidInTank.amount);
    }

    public String getFluidLocalizedName() {
        return lastFluidInTank == null ? "" : lastFluidInTank.getLocalizedName();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (backgroundTexture != null) {
            for (TextureArea textureArea : backgroundTexture) {
                textureArea.draw(pos.x, pos.y, size.width, size.height);
            }
        }
        //do not draw fluids if they are handled by JEI - it draws them itself
        if (lastFluidInTank != null && lastFluidInTank.amount > 0 && !gui.isJEIHandled) {
            GlStateManager.disableBlend();
            RenderUtil.drawFluidForGui(lastFluidInTank, alwaysShowFull ? lastFluidInTank.amount : lastTankCapacity,
                    pos.x + fluidRenderOffset, pos.y + fluidRenderOffset,
                    size.width - fluidRenderOffset, size.height - fluidRenderOffset);

            if (alwaysShowFull && !hideTooltip) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.5, 0.5, 1);

                String s = TextFormattingUtil.formatLongToCompactString(lastFluidInTank.amount, 4) + "L";

                FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
                fontRenderer.drawStringWithShadow(s, (pos.x + (size.width / 3)) * 2 - fontRenderer.getStringWidth(s) + 21, (pos.y + (size.height / 3) + 6) * 2, 0xFFFFFF);
                GlStateManager.popMatrix();
            }
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f);
        }
        if (overlayTexture != null) {
            overlayTexture.draw(pos.x, pos.y, size.width, size.height);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (!hideTooltip && !gui.isJEIHandled && isMouseOverElement(mouseX, mouseY)) {
            List<String> tooltips = new ArrayList<>();
            if (lastFluidInTank != null) {
                Fluid fluid = lastFluidInTank.getFluid();
                tooltips.add(fluid.getLocalizedName(lastFluidInTank));

                // Add chemical formula tooltip
                String formula = FluidTooltipUtil.getFluidTooltip(lastFluidInTank);
                if (formula != null && !formula.isEmpty())
                    tooltips.add(ChatFormatting.GRAY + formula);

                tooltips.add(I18n.format("gregtech.fluid.amount", lastFluidInTank.amount, lastTankCapacity));
                tooltips.add(I18n.format("gregtech.fluid.temperature", fluid.getTemperature(lastFluidInTank)));
                tooltips.add(I18n.format(fluid.isGaseous(lastFluidInTank) ? "gregtech.fluid.state_gas" : "gregtech.fluid.state_liquid"));
            } else {
                tooltips.add(I18n.format("gregtech.fluid.empty"));
                tooltips.add(I18n.format("gregtech.fluid.amount", 0, lastTankCapacity));
            }
            if (allowClickFilling) {
                tooltips.add(""); //add empty line to separate things
                tooltips.add(I18n.format("gregtech.fluid.click_to_fill"));
                tooltips.add(I18n.format("gregtech.fluid.click_to_fill.shift"));
            }
            if (allowClickEmptying) {
                tooltips.add(""); //add empty line to separate things
                tooltips.add(I18n.format("gregtech.fluid.click_to_empty"));
                tooltips.add(I18n.format("gregtech.fluid.click_to_empty.shift"));
            }
            drawHoveringText(ItemStack.EMPTY, tooltips, 300, mouseX, mouseY);
            GlStateManager.color(1.0f, 1.0f, 1.0f);
        }
    }

    @Override
    public void detectAndSendChanges() {
        final int capacity = this.fluidTank.getTankProperties()[this.slotIndex].getCapacity();
        if (capacity != this.lastTankCapacity) {
            this.lastTankCapacity = capacity;
            this.writeUpdateInfo(0, buffer -> buffer.writeVarInt(this.lastTankCapacity));
        }
        final FluidStack fluidStack = this.getFluidStack(this.slotIndex);
        if (fluidStack == null && lastFluidInTank != null) {
            this.lastFluidInTank = null;
            this.writeUpdateInfo(1, buffer -> {
            });
        } else if (fluidStack != null) {
            if (!fluidStack.isFluidEqual(lastFluidInTank)) {
                this.lastFluidInTank = fluidStack.copy();
                NBTTagCompound fluidStackTag = fluidStack.writeToNBT(new NBTTagCompound());
                this.writeUpdateInfo(2, buffer -> buffer.writeCompoundTag(fluidStackTag));
            } else if (fluidStack.amount != lastFluidInTank.amount) {
                this.lastFluidInTank.amount = fluidStack.amount;
                this.writeUpdateInfo(3, buffer -> buffer.writeVarInt(lastFluidInTank.amount));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 0) {
            this.lastTankCapacity = buffer.readVarInt();
        } else if (id == 1) {
            this.lastFluidInTank = null;
        } else if (id == 2) {
            NBTTagCompound fluidStackTag;
            try {
                fluidStackTag = buffer.readCompoundTag();
            } catch (IOException ignored) {
                return;
            }
            this.lastFluidInTank = FluidStack.loadFluidStackFromNBT(fluidStackTag);
        } else if (id == 3 && lastFluidInTank != null) {
            this.lastFluidInTank.amount = buffer.readVarInt();
        } else if (id == 4) {
            try {
                ItemStack stack = buffer.readItemStack();
                this.gui.entityPlayer.inventory.setItemStack(stack);
            } catch (IOException e) {
                GTLog.logger.info(e.getMessage());
            }
        } else if (id == 5) {
            ItemStack currentStack = gui.entityPlayer.inventory.getItemStack();
            int newStackSize = buffer.readVarInt();
            currentStack.setCount(newStackSize);
            gui.entityPlayer.inventory.setItemStack(currentStack);
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            boolean isShiftKeyDown = buffer.readBoolean();
            int button = buffer.readInt();
            if (buffer.readBoolean()) {
                ItemStack clickStack = this.tryClickContainer(isShiftKeyDown, button);
                if (!clickStack.isEmpty()) {
                    this.writeUpdateInfo(4, buf -> buf.writeItemStack(clickStack));
                }
            } else {
                int clickResult = tryClickContainer(isShiftKeyDown);
                if (clickResult >= 0) {
                    this.writeUpdateInfo(5, buf -> buf.writeVarInt(clickResult));
                }
            }
        }
    }

    private ItemStack tryClickContainer(boolean isShiftKeyDown, int button) {
        EntityPlayer player = this.gui.entityPlayer;
        ItemStack currentStack = player.inventory.getItemStack();
        if (!currentStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
            return ItemStack.EMPTY;
        int maxAttempts = isShiftKeyDown ? currentStack.getCount() : 1;

        if (button == 1 && this.allowClickFilling && this.getFluidAmount(this.slotIndex) > 0) {
            boolean performedFill = false;
            FluidStack initialFluid = this.getFluidStack(this.slotIndex);
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryFillContainer(currentStack,
                        this.fluidTank, Integer.MAX_VALUE, null, false);
                if (!result.isSuccess()) break;
                ItemStack remainingStack = result.getResult();
                if (!remainingStack.isEmpty() && currentStack.getCount() > 1 && !TJItemUtils.insertInPlayerInventory(player.inventory, remainingStack, true, false, false).isEmpty())
                    break; //do not continue if we can't add resulting container into inventory
                FluidActionResult fillResult = FluidUtil.tryFillContainer(currentStack, this.fluidTank, Integer.MAX_VALUE, null, true);
                ItemStack fillStack = fillResult.getResult();
                if (currentStack.getCount() > 1) {
                    currentStack.shrink(1);
                } else currentStack = fillStack;
                performedFill = true;
            }
            if (performedFill) {
                SoundEvent soundevent = initialFluid.getFluid().getFillSound(initialFluid);
                player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ,
                        soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                player.inventory.setItemStack(currentStack);
                return currentStack;
            }
        }

        if (button == 0 && this.allowClickEmptying) {
            boolean performedEmptying = false;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryEmptyContainer(currentStack,
                        this.fluidTank, Integer.MAX_VALUE, null, false);
                if (!result.isSuccess()) break;
                ItemStack remainingStack = result.getResult();
                if (!remainingStack.isEmpty() && currentStack.getCount() > 1 && !TJItemUtils.insertInPlayerInventory(player.inventory, remainingStack, true, false, false).isEmpty())
                    break; //do not continue if we can't add resulting container into inventory
                FluidActionResult emptyResult = FluidUtil.tryEmptyContainer(currentStack, this.fluidTank, Integer.MAX_VALUE, null, true);
                ItemStack emptyStack = emptyResult.getResult();
                if (currentStack.getCount() > 1)
                    currentStack.shrink(1);
                else currentStack = emptyStack;
                performedEmptying = true;
            }
            FluidStack filledFluid = this.getFluidStack(this.slotIndex);
            if (performedEmptying) {
                SoundEvent soundevent = filledFluid.getFluid().getEmptySound(filledFluid);
                player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ,
                        soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                player.inventory.setItemStack(currentStack);
                return currentStack;
            }
        }
        return ItemStack.EMPTY;
    }

    private int tryClickContainer(boolean isShiftKeyDown) {
        EntityPlayer player = gui.entityPlayer;
        ItemStack currentStack = player.inventory.getItemStack();
        if (!currentStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
            return -1;
        int maxAttempts = isShiftKeyDown ? currentStack.getCount() : 1;

        if (allowClickFilling && this.getFluidAmount(this.slotIndex) > 0) {
            boolean performedFill = false;
            FluidStack initialFluid = this.getFluidStack(this.slotIndex);
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryFillContainer(currentStack,
                        fluidTank, Integer.MAX_VALUE, null, false);
                if (!result.isSuccess()) break;
                ItemStack remainingStack = result.getResult();
                if (!remainingStack.isEmpty() && !TJItemUtils.insertInPlayerInventory(player.inventory, remainingStack, true, false, false).isEmpty())
                    break; //do not continue if we can't add resulting container into inventory
                FluidUtil.tryFillContainer(currentStack, fluidTank, Integer.MAX_VALUE, null, true);
                currentStack.shrink(1);
                performedFill = true;
            }
            if (performedFill) {
                SoundEvent soundevent = initialFluid.getFluid().getFillSound(initialFluid);
                player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ,
                        soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                gui.entityPlayer.inventory.setItemStack(currentStack);
                return currentStack.getCount();
            }
        }

        if (allowClickEmptying) {
            boolean performedEmptying = false;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryEmptyContainer(currentStack,
                        fluidTank, Integer.MAX_VALUE, null, false);
                if (!result.isSuccess()) break;
                ItemStack remainingStack = result.getResult();
                if (!remainingStack.isEmpty() && !player.inventory.addItemStackToInventory(remainingStack))
                    break; //do not continue if we can't add resulting container into inventory
                FluidUtil.tryEmptyContainer(currentStack, fluidTank, Integer.MAX_VALUE, null, true);
                currentStack.shrink(1);
                performedEmptying = true;
            }
            FluidStack filledFluid = this.getFluidStack(this.slotIndex);
            if (performedEmptying) {
                SoundEvent soundevent = filledFluid.getFluid().getEmptySound(filledFluid);
                player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ,
                        soundevent, SoundCategory.BLOCKS, 1.0F, 1.0F);
                gui.entityPlayer.inventory.setItemStack(currentStack);
                return currentStack.getCount();
            }
        }

        return -1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            ItemStack stack = this.gui.entityPlayer.inventory.getItemStack();
            if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                boolean isShiftKeyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                this.writeClientAction(1, buffer -> {
                    buffer.writeBoolean(isShiftKeyDown);
                    buffer.writeInt(button);
                    buffer.writeBoolean(true);
                });
                this.playButtonClickSound();
                return true;
            }
        }
        return false;
    }

    private FluidStack getFluidStack(int slotIndex) {
        final IAEFluidStack iaeFluidStack = this.fluidTank.getFluidInSlot(slotIndex);
        if (iaeFluidStack == null)
            return null;
        return iaeFluidStack.getFluidStack();
    }

    private int getFluidAmount(int slotIndex) {
        final IAEFluidStack iaeFluidStack = this.fluidTank.getFluidInSlot(slotIndex);
        if (iaeFluidStack == null)
            return 0;
        final FluidStack fluidStack = iaeFluidStack.getFluidStack();
        return fluidStack != null ? fluidStack.amount : 0;
    }
}
