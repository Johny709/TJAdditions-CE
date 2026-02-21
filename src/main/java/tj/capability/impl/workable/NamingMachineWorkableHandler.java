package tj.capability.impl.workable;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import tj.capability.AbstractWorkableHandler;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.impl.handler.INameHandler;
import tj.util.ItemStackHelper;

import java.util.ArrayList;
import java.util.List;

public class NamingMachineWorkableHandler extends AbstractWorkableHandler<INameHandler> implements IItemFluidHandlerInfo {

    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private int outputIndex;

    public NamingMachineWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected boolean startRecipe() {
        int availableParallels = this.handler.getParallel();
        for (int i = 0; i < this.handler.getImportItemInventory().getSlots() && availableParallels > 0; i++) {
            ItemStack stack = this.handler.getImportItemInventory().extractItem(i, availableParallels, false);
            if (stack.isEmpty()) continue;
            this.itemInputs.add(stack.copy());
            stack.setStackDisplayName(this.handler.getName());
            availableParallels -= stack.getCount();
            this.itemOutputs.add(stack);
        }
        this.setMaxProgress(this.calculateOverclock(30L, 50, 2.8F));
        return availableParallels != this.handler.getParallel();
    }

    @Override
    protected boolean completeRecipe() {
        for (int i = this.outputIndex; i < this.itemOutputs.size(); i++) {
            ItemStack stack = this.itemOutputs.get(i);
            if (ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, true).isEmpty()) {
                ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, false);
                this.outputIndex++;
            } else return false;
        }
        this.outputIndex = 0;
        this.itemInputs.clear();
        this.itemOutputs.clear();
        return true;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        compound.setInteger("outputIndex", this.outputIndex);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.outputIndex = compound.getInteger("outputIndex");
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
