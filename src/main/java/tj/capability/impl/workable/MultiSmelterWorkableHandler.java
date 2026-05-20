package tj.capability.impl.workable;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.ModHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.capability.AbstractWorkableHandler;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.TJCapabilities;
import tj.capability.impl.handler.ICoilHandler;
import tj.capability.impl.handler.IRecipeHandler;

import java.util.ArrayList;
import java.util.List;

public class MultiSmelterWorkableHandler extends AbstractWorkableHandler<ICoilHandler> implements IItemFluidHandlerInfo {

    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();

    public MultiSmelterWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected boolean startRecipe() {
        boolean canStart = false;
        if (this.isDistinct) {
            if (!this.findInputs(this.handler.getInputBus(this.lastInputIndex))) {
                for (int i = 0; i < this.busCount; i++) {
                    if (i == this.lastInputIndex) continue;
                    canStart = this.findInputs(this.handler.getInputBus(i));
                    if (canStart) break;
                }
            }
        } else canStart = this.findInputs(this.handler.getImportItemInventory());
        this.setMaxProgress(this.calculateOverclock(16, 256, 2.8F));
        this.energyPerTick /= this.handler.getCoilEnergyDiscount();
        return canStart;
    }

    private boolean findInputs(IItemHandlerModifiable itemHandler) {
        int parallels = this.handler.getParallel();
        for (int i = 0; i < itemHandler.getSlots() && parallels > 0; i++) {
            ItemStack slotStack = itemHandler.extractItem(i, parallels, true);
            if (slotStack.isEmpty()) continue;
            final ItemStack output = ModHandler.getSmeltingOutput(slotStack).copy();
            output.setCount(slotStack.getCount() * output.getCount());
            slotStack = itemHandler.extractItem(i, parallels, false);
            parallels -= slotStack.getCount();
            this.itemInputs.add(slotStack);
            this.itemOutputs.add(output);
        }
        return parallels != this.handler.getParallel();
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        return capability == TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING ? TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING.cast(this) : super.getCapability(capability);
    }

    @Override
    public List<ItemStack> getItemInputs() {
        return this.itemInputs;
    }

    @Override
    public List<ItemStack> getItemOutputs() {
        return this.itemOutputs;
    }
}
