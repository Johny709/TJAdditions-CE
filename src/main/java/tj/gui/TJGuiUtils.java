package tj.gui;

import appeng.api.util.AEPartLocation;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.core.AELog;
import appeng.core.sync.GuiWrapper;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import tj.TJ;
import tj.integration.appeng.core.sync.TJGuiBridge;
import tj.integration.appeng.core.sync.TJGuiHostType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static appeng.util.Platform.isClient;
import static gregtech.api.gui.resources.RenderUtil.setGlColorFromInt;

public class TJGuiUtils {

    /**
     *
     * @param widgetGroup widget group for slots to get added to
     * @param inventoryPlayer inventory of player
     * @param x X position
     * @param y Y position
     * @param stack the stack used to open this GUI. this prevents the item being taken out from slot while GUI is open.
     * @return widget
     */
    public static WidgetGroup bindPlayerInventory(WidgetGroup widgetGroup, InventoryPlayer inventoryPlayer, int x, int y, ItemStack stack) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                widgetGroup.addWidget((new SlotWidget(inventoryPlayer, col + (row + 1) * 9, x + col * 18, y + row * 18))
                        .setBackgroundTexture(GuiTextures.SLOT)
                        .setLocationInfo(true, false));
            }
        }
        return bindPlayerHotbar(widgetGroup, inventoryPlayer, x, y + 58, stack);
    }

    private static WidgetGroup bindPlayerHotbar(WidgetGroup widgetGroup, InventoryPlayer inventoryPlayer, int x, int y, ItemStack stack) {
        for (int slot = 0; slot < 9; ++slot) {
            boolean interact = inventoryPlayer.player.inventory.getStackInSlot(slot) != stack;
            widgetGroup.addWidget((new SlotWidget(inventoryPlayer, slot, x + slot * 18, y, interact, interact))
                    .setBackgroundTexture(GuiTextures.SLOT)
                    .setLocationInfo(true, true));
        }
        return widgetGroup;
    }

    public static void drawFluidForGui(FluidStack contents, long fluidAmount, long tankCapacity, long startX, long startY, long widthT, long heightT) {
        widthT--;
        Fluid fluid = contents.getFluid();
        ResourceLocation fluidStill = fluid.getStill();
        TextureAtlasSprite fluidStillSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluidStill.toString());
        int fluidColor = fluid.getColor(contents);
        long scaledAmount = fluidAmount * widthT / Math.max(1, tankCapacity);
        if (fluidAmount > 0 && scaledAmount < 1) {
            scaledAmount = 1;
        }
        if (scaledAmount > widthT) {
            scaledAmount = widthT;
        }
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        setGlColorFromInt(fluidColor, 200);

        final long xTileCount = scaledAmount / 16;
        final long xRemainder = scaledAmount - xTileCount * 16;
        final long yTileCount = heightT / 16;
        final long yRemainder = heightT - yTileCount * 16;

        final long yStart = startY + heightT;

        for (long xTile = 0; xTile <= xTileCount; xTile++) {
            for (long yTile = 0; yTile <= yTileCount; yTile++) {
                long width = xTile == xTileCount ? xRemainder : 16;
                long height = yTile == yTileCount ? yRemainder : 16;
                long x = startX + xTile * 16;
                long y = yStart - (yTile + 1) * 16;
                if (width > 0 && height > 0) {
                    long maskTop = 16 - height;
                    long maskRight = 16 - width;

                    drawFluidTexture(x, y, fluidStillSprite, maskTop, maskRight, 0.0);
                }
            }
        }
        GlStateManager.disableBlend();
    }

    private static void drawFluidTexture(double xCoord, double yCoord, TextureAtlasSprite textureSprite, long maskTop, long maskRight, double zLevel) {
        double uMin = textureSprite.getMinU();
        double uMax = textureSprite.getMaxU();
        double vMin = textureSprite.getMinV();
        double vMax = textureSprite.getMaxV();
        uMax = uMax - maskRight / 16.0 * (uMax - uMin);
        vMax = vMax - maskTop / 16.0 * (vMax - vMin);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(xCoord, yCoord + 16, zLevel).tex(uMin, vMax).endVertex();
        buffer.pos(xCoord + 16 - maskRight, yCoord + 16, zLevel).tex(uMax, vMax).endVertex();
        buffer.pos(xCoord + 16 - maskRight, yCoord + maskTop, zLevel).tex(uMax, vMin).endVertex();
        buffer.pos(xCoord, yCoord + maskTop, zLevel).tex(uMin, vMin).endVertex();
        tessellator.draw();
    }

    public static void openAEGui(@Nonnull final EntityPlayer p, @Nullable final TileEntity tile, @Nullable final AEPartLocation side, @Nonnull final TJGuiBridge type) {
        if (isClient()) {
            return;
        }

        if (type.getExternalGui() != null) {
            GuiWrapper.IExternalGui obj = type.getExternalGui();
            GuiWrapper.Opener opener = GuiWrapper.INSTANCE.getOpener(obj.getID());
            if (opener == null) {
                AELog.warn("External Gui with ID: %s is missing a opener.", obj.getID());
            } else {
                World world = tile == null ? p.world : tile.getWorld();
                BlockPos pos = tile == null ? null : tile.getPos();
                EnumFacing face = side == null ? null : side.getFacing();
                opener.open(obj, new GuiWrapper.GuiContext(world, p, pos, face, null));
            }
            return;
        }

        int x = 0;
        int y = 0;
        int z = Integer.MIN_VALUE;

        if (tile != null) {
            x = tile.getPos().getX();
            y = tile.getPos().getY();
            z = tile.getPos().getZ();
        } else {
            if (p.openContainer instanceof IInventorySlotAware) {
                x = ((IInventorySlotAware) p.openContainer).getInventorySlot();
                y = ((IInventorySlotAware) p.openContainer).isBaubleSlot() ? 1 : 0;
            } else {
                x = p.inventory.currentItem;
            }
        }

        if ((type.getType().isItem() && tile == null) || type.hasPermissions(tile, x, y, z, side, p)) {
            if (tile == null && type.getType() == TJGuiHostType.ITEM) {
                p.openGui(TJ.getInstance(), type.ordinal() << 4, p.getEntityWorld(), x, 0, 0);
            } else if (tile == null || type.getType() == TJGuiHostType.ITEM) {
                if (tile != null) {
                    p.openGui(TJ.getInstance(), type.ordinal() << 4 | (1 << 3), p.getEntityWorld(), x, y, z);
                } else {
                    p.openGui(TJ.getInstance(), type.ordinal() << 4, p.getEntityWorld(), x, y, z);
                }
            } else {
                p.openGui(TJ.getInstance(), type.ordinal() << 4 | (side.ordinal()), tile.getWorld(), x, y, z);
            }
        }
    }

    public static void openAEGui(@Nonnull final EntityPlayer p, int slot, @Nonnull final TJGuiBridge type, boolean isBauble) {
        if (isClient()) {
            return;
        }

        if (type.getExternalGui() != null) {
            GuiWrapper.IExternalGui obj = type.getExternalGui();
            GuiWrapper.Opener opener = GuiWrapper.INSTANCE.getOpener(obj.getID());
            if (opener == null) {
                AELog.warn("External Gui with ID: %s is missing a opener.", obj.getID());
            } else {
                NBTTagCompound extra = new NBTTagCompound();
                extra.setInteger("slot", slot);
                extra.setBoolean("isBauble", isBauble);
                opener.open(obj, new GuiWrapper.GuiContext(p.world, p, null, null, extra));
            }
            return;
        }

        if (type.getType().isItem()) {
            p.openGui(TJ.getInstance(), type.ordinal() << 4, p.getEntityWorld(), slot, isBauble ? 1 : 0, Integer.MIN_VALUE);
        }
    }
}
