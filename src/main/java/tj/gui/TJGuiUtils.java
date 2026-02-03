package tj.gui;

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
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import static gregtech.api.gui.resources.RenderUtil.setGlColorFromInt;


public final class TJGuiUtils {

    private TJGuiUtils() {}

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
}
