package tj.capability.impl.workable;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.capabilities.Capability;
import tj.capability.AbstractWorkableHandler;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.TJCapabilities;
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
        this.itemInputs.clear();
        this.itemOutputs.clear();
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
        NBTTagList itemInputList = new NBTTagList(), itemOutputList = new NBTTagList();
        for (ItemStack stack : this.itemInputs)
            itemInputList.appendTag(stack.serializeNBT());
        for (ItemStack stack : this.itemOutputs)
            itemOutputList.appendTag(stack.serializeNBT());
        compound.setTag("itemInputs", itemInputList);
        compound.setTag("itemOutputs", itemOutputList);
        compound.setInteger("outputIndex", this.outputIndex);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        NBTTagList itemInputList = compound.getTagList("itemInputs", 10), itemOutputList = compound.getTagList("itemOutputs", 10);
        for (int i = 0; i < itemInputList.tagCount(); i++)
            this.itemInputs.add(new ItemStack(itemInputList.getCompoundTagAt(i)));
        for (int i = 0; i < itemOutputList.tagCount(); i++)
            this.itemOutputs.add(new ItemStack(itemOutputList.getCompoundTagAt(i)));
        this.outputIndex = compound.getInteger("outputIndex");
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING)
            return TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING.cast(this);
        return super.getCapability(capability);
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
