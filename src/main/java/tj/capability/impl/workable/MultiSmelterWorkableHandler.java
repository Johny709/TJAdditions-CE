package tj.capability.impl.workable;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.ModHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.capability.AbstractWorkableHandler;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.TJCapabilities;
import tj.capability.impl.handler.ICoilHandler;
import tj.util.TJItemUtils;

import java.util.ArrayList;
import java.util.List;

public class MultiSmelterWorkableHandler extends AbstractWorkableHandler<ICoilHandler> implements IItemFluidHandlerInfo {

    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private boolean voiding;
    private int parallelsPerformed;
    private int outputIndex;

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
        if (!canStart)
            return false;
        this.setMaxProgress(this.calculateOverclock(16, this.getDuration(), 2.8F));
        this.energyPerTick /= this.handler.getCoilEnergyDiscount();
        return true;
    }

    @Override
    protected boolean completeRecipe() {
        for (int i = this.outputIndex; i < this.itemOutputs.size(); i++) {
            final ItemStack stack = this.itemOutputs.get(i);
            if (this.voiding || TJItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, true).isEmpty()) {
                TJItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, false);
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
        final NBTTagCompound compound = super.serializeNBT();
        final NBTTagList itemInputList = new NBTTagList(), itemOutputList = new NBTTagList();
        for (ItemStack stack : this.itemInputs)
            itemInputList.appendTag(stack.serializeNBT());
        for (ItemStack stack : this.itemOutputs)
            itemOutputList.appendTag(stack.serializeNBT());
        compound.setTag("itemInputs", itemInputList);
        compound.setTag("itemOutputs", itemOutputList);
        compound.setBoolean("voiding", this.voiding);
        compound.setInteger("outputIndex", this.outputIndex);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        final NBTTagList itemInputList = compound.getTagList("itemInputs", 10), itemOutputList = compound.getTagList("itemOutputs", 10);
        for (int i = 0; i < itemInputList.tagCount(); i++)
            this.itemInputs.add(new ItemStack(itemInputList.getCompoundTagAt(i)));
        for (int i = 0; i < itemOutputList.tagCount(); i++)
            this.itemOutputs.add(new ItemStack(itemOutputList.getCompoundTagAt(i)));
        this.voiding = compound.getBoolean("voiding");
        this.outputIndex = compound.getInteger("outputIndex");
    }

    private boolean findInputs(IItemHandlerModifiable itemHandler) {
        int parallels = this.handler.getParallel();
        this.parallelsPerformed = 0;
        for (int i = 0; i < itemHandler.getSlots() && parallels > 0; i++) {
            ItemStack slotStack = itemHandler.extractItem(i, parallels, true);
            if (slotStack.isEmpty()) continue;
            final ItemStack output = ModHandler.getSmeltingOutput(slotStack).copy();
            output.setCount(slotStack.getCount() * output.getCount());
            slotStack = itemHandler.extractItem(i, parallels, false);
            parallels -= slotStack.getCount();
            this.parallelsPerformed += slotStack.getCount();
            this.itemInputs.add(slotStack);
            this.itemOutputs.add(output);
        }
        return parallels != this.handler.getParallel();
    }

    public int getDuration() {
        return (int) Math.max(1, 256 * (this.parallelsPerformed / (this.handler.getParallel() * 1.0)));
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

    public int getParallelsPerformed() {
        return this.parallelsPerformed;
    }
}
