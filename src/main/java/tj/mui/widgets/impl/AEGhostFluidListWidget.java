package tj.mui.widgets.impl;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.AEFluidStack;
import gregtech.api.gui.igredient.IGhostIngredientTarget;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import tj.TJ;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AEGhostFluidListWidget<T> extends AEFluidListWidget<T> implements IGhostIngredientTarget {

    @SafeVarargs
    public AEGhostFluidListWidget(int x, int y, int width, int height, IGridNode gridNode, Class<? extends IGridHost>... gridHosts) {
        super(x, y, width, height, gridNode, gridHosts);
    }

    @Override
    protected ItemStack slotAction(IFluidHandler fluidHandler, int slotIndex, ItemStack playerStack, ElementData data) {
        final AEFluidInventory fluidTank = (AEFluidInventory) fluidHandler;
        final int tankCapacity = fluidTank.getTankProperties()[0].getCapacity();
        final int size = data.shiftClick ? playerStack.getCount() : 1;
        IAEFluidStack iaeFluidStack;
        if (data.button == 0) { // Left-click
            final IFluidHandlerItem fluidHandlerItem = playerStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandlerItem == null)
                return playerStack;
            for (int i = 0; i < size; i++) {
                final FluidStack fluidContained = FluidUtil.getFluidContained(playerStack);
                if (fluidContained == null)
                    return playerStack;
                final int toDrain = (int) Math.min(fluidContained.amount, tankCapacity - this.getFluidAmount(slotIndex, fluidTank));
                final FluidStack bucketFluid = this.getFluidStack(slotIndex, fluidTank);
                final FluidActionResult fluidActionResult = FluidUtil.tryEmptyContainer(playerStack, new FluidTank(bucketFluid, tankCapacity), toDrain, this.gui.entityPlayer, false);
                if (fluidActionResult == FluidActionResult.FAILURE) break;
                final ItemStack fillStack = fluidActionResult.getResult();
                FluidUtil.tryEmptyContainer(fillStack, new FluidTank(bucketFluid, tankCapacity), toDrain, this.gui.entityPlayer, true);
                FluidStack fluidStack = this.getFluidStack(slotIndex, fluidTank);
                if (fluidStack == null) {
                    fluidStack = new FluidStack(fluidContained.getFluid(), toDrain);
                } else fluidStack.amount += toDrain;
                iaeFluidStack = AEFluidStack.fromFluidStack(fluidStack);
                fluidTank.setFluidInSlot(slotIndex, iaeFluidStack.copy());
            }
        } else if (data.button == 1) { // Right-click
            fluidTank.setFluidInSlot(slotIndex, null);
        }
        return playerStack;
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object o) {
        final List<IGhostIngredientHandler.Target<?>> targetList = new ArrayList<>();
        int scrollOffset = 0;
        int slotColumn = 0;
        int slotXOffset = 0;
        for (Int2ObjectMap.Entry<Object> entry : this.elements.int2ObjectEntrySet()) {
            if (entry.getValue() instanceof FluidStack) {
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
                        if (o instanceof FluidStack) {
                            writeClientAction(3, buffer -> {
                                buffer.writeInt(entry.getIntKey());
                                buffer.writeCompoundTag((((FluidStack) o).writeToNBT(new NBTTagCompound())));
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

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 3) {
            try {
                this.setFluidAt(buffer.readInt(), FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag()));
            } catch (IOException e) {
                TJ.logger.info(e.getMessage());
            }
        }
    }
}
