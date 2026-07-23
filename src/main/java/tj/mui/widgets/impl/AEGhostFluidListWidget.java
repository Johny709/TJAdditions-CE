package tj.mui.widgets.impl;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.AEFluidInventory;
import appeng.fluids.util.AEFluidStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class AEGhostFluidListWidget<T> extends AEFluidListWidget<T> {

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
}
