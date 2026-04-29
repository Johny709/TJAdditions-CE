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
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class NamingMachineWorkableHandler extends AbstractWorkableHandler<INameHandler> implements IItemFluidHandlerInfo {

    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    @Nonnull
    private ItemStack catalyst = ItemStack.EMPTY;
    private int catalystIndex;
    private int outputIndex;

    public NamingMachineWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    public void invalidate() {
        this.lastInputIndex = 0;
    }

    @Override
    protected boolean startRecipe() {
        boolean foundRecipe;
        IItemHandlerModifiable itemInputs;
        if (this.isDistinct) {
            itemInputs = this.handler.getInputBus(this.lastInputIndex);
            foundRecipe = this.findNameTag(itemInputs) && this.findInputs(itemInputs);
            if (!foundRecipe) for (int i = 0; i < this.busCount; i++) {
                if (i == this.lastInputIndex) continue;
                itemInputs = this.handler.getInputBus(i);
                foundRecipe = this.findNameTag(itemInputs) && this.findInputs(itemInputs);
                if (foundRecipe) {
                    this.lastInputIndex = i;
                    break;
                }
            }
        } else {
            itemInputs = this.handler.getImportItemInventory();
            foundRecipe = this.findNameTag(itemInputs) && this.findInputs(itemInputs);
        }
        if (foundRecipe) {
            this.setMaxProgress(this.calculateOverclock(30L, 50, 2.8F));
            return true;
        }
        return false;
    }

    @Override
    protected boolean completeRecipe() {
        for (int i = this.outputIndex; i < this.itemOutputs.size(); i++) {
            final ItemStack stack = this.itemOutputs.get(i);
            if (TJItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, true).isEmpty()) {
                TJItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, false);
                this.outputIndex++;
            } else return false;
        }
        this.outputIndex = 0;
        this.itemInputs.clear();
        this.itemOutputs.clear();
        this.catalyst = ItemStack.EMPTY;
        return true;
    }

    private boolean findNameTag(IItemHandlerModifiable itemInputs) {
        for (int i = 0; i < itemInputs.getSlots(); i++) {
            final ItemStack stack = itemInputs.getStackInSlot(i);
            if (stack.getItem() == Items.NAME_TAG) {
                this.catalystIndex = i;
                this.catalyst = stack.copy();
                return true;
            }
        }
        this.catalystIndex = -1;
        return !this.handler.getName().isEmpty();
    }

    private boolean findInputs(IItemHandlerModifiable itemInputs) {
        int availableParallels = this.handler.getParallel();
        for (int i = 0; i < itemInputs.getSlots() && availableParallels > 0; i++) {
            if (i == this.catalystIndex) continue;
            final ItemStack stack = itemInputs.extractItem(i, availableParallels, false).copy();
            if (stack.isEmpty()) continue;
            this.itemInputs.add(stack);
            stack.setStackDisplayName(this.catalyst.isEmpty() ? this.handler.getName() : this.catalyst.getDisplayName());
            availableParallels -= stack.getCount();
            this.itemOutputs.add(stack);
        }
        return availableParallels != this.handler.getParallel();
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
