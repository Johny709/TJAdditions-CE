package tj.capability.impl.workable;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.capability.AbstractWorkableHandler;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.TJCapabilities;
import tj.capability.impl.handler.INameHandler;
import tj.util.ItemStackHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class NamingMachineWorkableHandler extends AbstractWorkableHandler<INameHandler> implements IItemFluidHandlerInfo {

    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    @Nonnull
    private ItemStack catalyst = ItemStack.EMPTY;
    private int outputIndex;

    public NamingMachineWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected boolean startRecipe() {
        this.itemInputs.clear();
        this.itemOutputs.clear();
        int catalystIndex = -1;
        int availableParallels = this.handler.getParallel();
        IItemHandlerModifiable itemHandlerModifiable = this.isDistinct ? this.handler.getInputBus(this.lastInputIndex) : this.handler.getImportItemInventory();
        for (int i = 0; i < itemHandlerModifiable.getSlots(); i++) {
            ItemStack stack = itemHandlerModifiable.getStackInSlot(i);
            if (this.catalyst.isEmpty() && stack.getItem() == Items.NAME_TAG) {
                this.itemInputs.add(this.catalyst = stack);
                catalystIndex = i;
                break;
            }
        }
        for (int i = 0; i < itemHandlerModifiable.getSlots() && availableParallels > 0; i++) {
            ItemStack stack = itemHandlerModifiable.extractItem(i, availableParallels, false);
            if (stack.isEmpty() || i == catalystIndex) continue;
            this.itemInputs.add(stack.copy());
            stack.setStackDisplayName(this.catalyst.isEmpty() ? this.handler.getName() : this.catalyst.getDisplayName());
            availableParallels -= stack.getCount();
            this.itemOutputs.add(stack);
        }
        if (++this.lastInputIndex == this.busCount)
            this.lastInputIndex = 0;
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
        this.catalyst = ItemStack.EMPTY;
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
