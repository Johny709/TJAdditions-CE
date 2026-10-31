package tj.capability.impl.workable;

import gregicadditions.GAValues;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.capability.AbstractWorkableHandler;
import tj.capability.impl.handler.INameHandler;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class NamingMachineWorkableHandler extends AbstractWorkableHandler<INameHandler> {

    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    @Nonnull
    private ItemStack catalyst = ItemStack.EMPTY;
    private int catalystIndex;
    private int outputIndex;
    private boolean voiding;
    private boolean foundRecipe;

    public NamingMachineWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    public void invalidate() {
        this.lastInputIndex = 0;
    }

    @Override
    protected boolean startRecipe() {
        IItemHandlerModifiable itemInputs;
        if (this.isDistinct) {
            itemInputs = this.handler.getInputBus(this.lastInputIndex);
            this.foundRecipe = this.findNameTag(itemInputs) && this.findAndConsumeInputs(itemInputs, true);
            if (!this.foundRecipe) for (int i = 0; i < this.busCount; i++) {
                if (i == this.lastInputIndex) continue;
                itemInputs = this.handler.getInputBus(i);
                this.foundRecipe = this.findNameTag(itemInputs) && this.findAndConsumeInputs(itemInputs, true);
                if (this.foundRecipe) {
                    this.lastInputIndex = i;
                    break;
                }
            }
        } else {
            itemInputs = this.handler.getImportItemInventory();
            this.foundRecipe = this.findNameTag(itemInputs) && this.findAndConsumeInputs(itemInputs, true);
        }
        if (this.foundRecipe && this.handler.getInputEnergyContainer().getEnergyStored() >= GAValues.VA[this.handler.getTier()]) {
            this.findAndConsumeInputs(itemInputs, false);
            this.setMaxProgress(this.calculateOverclock(30L, 50, 2.8F));
            return true;
        } else return false;
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
        this.foundRecipe = false;
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

    private boolean findAndConsumeInputs(IItemHandlerModifiable itemInputs, boolean simulate) {
        int availableParallels = this.handler.getParallel();
        for (int i = 0; i < itemInputs.getSlots() && availableParallels > 0; i++) {
            if (i == this.catalystIndex) continue;
            final ItemStack stack = itemInputs.extractItem(i, availableParallels, simulate);
            if (stack.isEmpty()) continue;
            availableParallels -= stack.getCount();
            if (!simulate) {
                final ItemStack output = stack.copy();
                this.itemInputs.add(output);
                output.setStackDisplayName(this.catalyst.isEmpty() ? this.handler.getName() : this.catalyst.getDisplayName());
                this.itemOutputs.add(output);
            }
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
        compound.setBoolean("voiding", this.voiding);
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
        this.voiding = compound.getBoolean("voiding");
    }

    @Override
    protected boolean otherConditions() {
        return this.foundRecipe;
    }

    @Nonnull
    @Override
    public List<ItemStack> getItemInputs() {
        return this.itemInputs;
    }

    @Nonnull
    @Override
    public List<ItemStack> getItemOutputs() {
        return this.itemOutputs;
    }

    public void setVoiding(boolean voiding) {
        this.voiding = voiding;
        this.metaTileEntity.markDirty();
    }

    public boolean isVoiding() {
        return this.voiding;
    }
}
