package tj.capability.impl.workable;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.IMachineHandler;
import tj.capability.TJCapabilities;
import tj.capability.AbstractWorkableHandler;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;


public class ArchitectWorkbenchWorkableHandler extends AbstractWorkableHandler<IMachineHandler> implements IItemFluidHandlerInfo {

    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();

    @Nonnull
    private ItemStack catalyst = ItemStack.EMPTY;
    private int outputIndex;

    public ArchitectWorkbenchWorkableHandler(MetaTileEntity metaTileEntity) {
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
            foundRecipe = this.findCatalyst(itemInputs) && this.findInputs(itemInputs);
            if (!foundRecipe) for (int i = 0; i < this.busCount; i++) {
                if (i == this.lastInputIndex) continue;
                itemInputs = this.handler.getInputBus(i);
                foundRecipe = this.findCatalyst(itemInputs) && this.findInputs(itemInputs);
                if (foundRecipe) {
                    this.lastInputIndex = i;
                    break;
                }
            }
        } else {
            itemInputs = this.handler.getImportItemInventory();
            foundRecipe = this.findCatalyst(itemInputs) && this.findInputs(itemInputs);
        }
        if (foundRecipe) {
            this.maxProgress = this.calculateOverclock(30, 200, 2.8F);
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

    private boolean findCatalyst(IItemHandlerModifiable itemInputs) {
        for (int i = 0; i < itemInputs.getSlots(); i++) {
            final ItemStack stack = itemInputs.getStackInSlot(i);
            if (this.isArchitectureStack(stack.getTagCompound())) {
                this.catalyst = stack;
                return true;
            }
        }
        return false;
    }

    private boolean findInputs(IItemHandlerModifiable itemInputs) {
        int availableParallels = this.handler.getParallel();
        for (int i = 0; i < itemInputs.getSlots() && availableParallels > 0; i++) {
            final ItemStack stack = itemInputs.getStackInSlot(i);
            if (stack.isEmpty() || this.isArchitectureStack(stack.getTagCompound()))
                continue;
            if (!this.handler.getImportItemInventory().extractItem(i, availableParallels, true).isEmpty()) {
                final ItemStack input = this.handler.getImportItemInventory().extractItem(i, availableParallels, false);
                final int inputCount = input.getCount();
                final ItemStack output = new ItemStack(Item.getByNameOrId("architecturecraft:shape"), inputCount);
                final NBTTagCompound compound = this.catalyst.getTagCompound().copy();
                compound.setString("BaseName", Item.REGISTRY.getNameForObject(input.getItem()).toString());
                compound.setInteger("BaseData", input.getMetadata());
                output.setTagCompound(compound);
                availableParallels -= inputCount;
                this.itemInputs.add(input);
                this.itemOutputs.add(output);
            }
        }
        return availableParallels != this.handler.getParallel();
    }

    private boolean isArchitectureStack(NBTTagCompound tagCompound) {
        return tagCompound != null && tagCompound.hasKey("Shape") && tagCompound.hasKey("BaseName") && tagCompound.hasKey("BaseData");
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound compound = super.serializeNBT();
        final NBTTagList itemInputList = new NBTTagList(), itemOutputList = new NBTTagList();
        if (!this.catalyst.isEmpty())
            compound.setTag("catalyst", this.catalyst.serializeNBT());
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
        final NBTTagList itemInputList = compound.getTagList("itemInputs", 10), itemOutputsList = compound.getTagList("itemOutputs", 10);
        if (compound.hasKey("catalyst"))
            this.catalyst = new ItemStack(compound.getCompoundTag("catalyst"));
        this.outputIndex = compound.getInteger("outputIndex");
        for (int i = 0; i < itemInputList.tagCount(); i++)
            this.itemInputs.add(new ItemStack(itemInputList.getCompoundTagAt(i)));
        for (int i = 0; i < itemOutputsList.tagCount(); i++)
            this.itemOutputs.add(new ItemStack(itemOutputsList.getCompoundTagAt(i)));
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
