package tj.mui.widgets.impl;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import gregtech.api.gui.igredient.IGhostIngredientTarget;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.TJ;
import tj.mui.TJGuiTextures;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class AEGhostItemListWidget<T> extends AEItemListWidget<T> implements IGhostIngredientTarget {

    private int selectedIndex = -1;

    @SafeVarargs
    public AEGhostItemListWidget(int x, int y, int width, int height, IGridNode gridNode, Class<? extends IGridHost>... gridHosts) {
        super(x, y, width, height, gridNode, gridHosts);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void drawSlot(int index, int x, int y, int mouseX, int mouseY, ItemStack itemStack) {
        super.drawSlot(index, x, y, mouseX, mouseY, itemStack);
        if (index == this.selectedIndex)
            TJGuiTextures.SELECTION_BOX.draw(x, y, 18, 18);
    }

    @Override
    protected ItemStack slotAction(IItemHandler itemHandler, int slotIndex, ItemStack playerStack, ElementData data, BiFunction<ItemStack, Boolean, ItemStack> itemStackTransfer) {
        if (playerStack.isEmpty()) {
            this.selectedIndex = data.index;
            this.writeUpdateInfo(5, buffer -> buffer.writeInt(this.selectedIndex));
            return playerStack;
        }
        if (data.button == 1) {
            itemHandler.extractItem(slotIndex, Integer.MAX_VALUE, false);
        } else if (itemHandler instanceof IItemHandlerModifiable) {
            ((IItemHandlerModifiable) itemHandler).setStackInSlot(slotIndex, playerStack.copy());
        } else itemHandler.insertItem(slotIndex, playerStack.copy(), false);
        return playerStack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 5)
            this.selectedIndex = buffer.readInt();
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            this.selectedIndex = -1;
            this.writeUpdateInfo(5, buffer1 -> buffer1.writeInt(this.selectedIndex));
        } else if (id == 4) {
            try {
                final int index = buffer.readInt();
                final ItemStack itemStack = buffer.readItemStack();
                if (!itemStack.isEmpty())
                    this.setItemAt(index, itemStack);
            } catch (IOException e) {
                TJ.logger.info(e.getMessage());
            }
        }
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object o) {
        final List<IGhostIngredientHandler.Target<?>> targetList = new ArrayList<>();
        int scrollOffset = 0;
        int slotColumn = 0;
        int slotXOffset = 0;
        for (Int2ObjectMap.Entry<Object> entry : this.elements.int2ObjectEntrySet()) {
            if (entry.getValue() instanceof ItemStack) {
                if (slotColumn > 8) {
                    slotColumn = 0;
                    scrollOffset += 18;
                    slotXOffset = 0;
                }
                final int x = this.getPosition().getX() + slotXOffset;
                final int y = this.getPosition().getY() + scrollOffset - (this.scrollOffset % 18);
                targetList.add(new IGhostIngredientHandler.Target<Object>() {

                    @Nonnull
                    @Override
                    public Rectangle getArea() {
                        return new Rectangle(x, y, 18, 18);
                    }

                    @Override
                    public void accept(@Nonnull Object o) {
                        if (o instanceof ItemStack) {
                            writeClientAction(4, buffer -> {
                                buffer.writeInt(entry.getIntKey());
                                buffer.writeItemStack((ItemStack) o);
                            });
                        }
                    }
                });
                slotXOffset += 18;
                slotColumn++;
            } else {
                if (entry.getIntKey() > 0)
                    scrollOffset += 18;
                scrollOffset += 18;
                slotXOffset = 0;
                slotColumn = 0;
            }
        }
        return targetList;
    }
}
