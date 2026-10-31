package tj.capability.impl.workable;

import gregicadditions.GAValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.ModHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.capability.AbstractWorkableHandler;
import tj.capability.impl.handler.ICoilHandler;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MultiSmelterWorkableHandler extends AbstractWorkableHandler<ICoilHandler> {

    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private boolean voiding;
    private boolean foundRecipe;
    private int parallelsPerformed;
    private int outputIndex;

    public MultiSmelterWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected boolean startRecipe() {
        IItemHandlerModifiable itemInputs;
        if (this.isDistinct) {
            itemInputs = this.handler.getInputBus(this.lastInputIndex);
            this.foundRecipe = this.findAndConsumeInputs(itemInputs, true);
            if (!this.foundRecipe) for (int i = 0; i < this.busCount; i++) {
                if (i == this.lastInputIndex) continue;
                itemInputs = this.handler.getInputBus(i);
                this.foundRecipe = this.findAndConsumeInputs(itemInputs, true);
                if (this.foundRecipe) {
                    this.lastInputIndex = i;
                    break;
                }
            }
        } else {
            itemInputs = this.handler.getImportItemInventory();
            this.foundRecipe = this.findAndConsumeInputs(itemInputs, true);
        }
        if (this.foundRecipe && this.handler.getInputEnergyContainer().getEnergyStored() >= GAValues.V[this.handler.getTier()] / 2) {
            this.findAndConsumeInputs(itemInputs, false);
            this.setMaxProgress(this.calculateOverclock(16, this.getDuration(), 2.8F));
            this.energyPerTick /= this.handler.getCoilEnergyDiscount();
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
        return true;
    }

    private boolean findAndConsumeInputs(IItemHandlerModifiable itemHandler, boolean simulate) {
        int availableParallels = this.handler.getParallel();
        this.parallelsPerformed = 0;
        for (int i = 0; i < itemHandler.getSlots() && availableParallels > 0; i++) {
            final ItemStack stack = itemHandler.extractItem(i, availableParallels, simulate);
            if (stack.isEmpty()) continue;
            availableParallels -= stack.getCount();
            if (!simulate) {
                final ItemStack output = ModHandler.getSmeltingOutput(stack).copy();
                output.setCount(stack.getCount() * output.getCount());
                this.parallelsPerformed += stack.getCount();
                this.itemInputs.add(stack);
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

    @Override
    protected boolean otherConditions() {
        return this.foundRecipe;
    }

    public int getDuration() {
        return (int) Math.max(1, 256 * (this.parallelsPerformed / (this.handler.getParallel() * 1.0)));
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

    public int getParallelsPerformed() {
        return this.parallelsPerformed;
    }
}
