package tj.integration.ae2.helpers;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.fluids.util.IAEFluidTank;
import appeng.util.inv.InvOperation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

public interface IDualitySuperFluidInterface {

    TickRateModulation tickingTheRequest(IGridNode node, int tickSinceLastCall);

    void readTheConfig();

    void updateThePlan(int slot);

    boolean useThePlan(int slot);

    boolean updateTheStorage();

    void onFluidInventoryHasChanged(IAEFluidTank inventory, FluidStack added, FluidStack removed);

    void onFluidInventoryHasChanged(IAEFluidTank inventory, int slot, InvOperation operation, FluidStack added, FluidStack removed);

    void onChangeTheInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack);

    void serializeToNBT(NBTTagCompound data);

    void deserializeFromNBT(NBTTagCompound data);
}
