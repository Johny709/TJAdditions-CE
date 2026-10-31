package tj.capability.impl.workable;

import gregicadditions.GAValues;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.IItemHandlerModifiable;
import team.chisel.api.carving.CarvingUtils;
import team.chisel.api.carving.ICarvingGroup;
import team.chisel.api.carving.ICarvingVariation;
import tj.capability.IMachineHandler;
import tj.capability.AbstractWorkableHandler;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;


public class ChiselWorkbenchWorkableHandler extends AbstractWorkableHandler<IMachineHandler> {

    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private int circuitNumber;
    private int outputIndex;
    private boolean voiding;
    private boolean foundRecipe;

    public ChiselWorkbenchWorkableHandler(MetaTileEntity metaTileEntity) {
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
            this.foundRecipe = this.findCircuit(itemInputs) && this.findAndConsumeInputs(itemInputs, true);
            if (!this.foundRecipe) for (int i = 0; i < this.busCount; i++) {
                if (i == this.lastInputIndex) continue;
                itemInputs = this.handler.getInputBus(i);
                this.foundRecipe = this.findCircuit(itemInputs) && this.findAndConsumeInputs(itemInputs, true);
                if (this.foundRecipe) {
                    this.lastInputIndex = i;
                    break;
                }
            }
        } else {
            itemInputs = this.handler.getImportItemInventory();
            this.foundRecipe = this.findCircuit(itemInputs) && this.findAndConsumeInputs(itemInputs, true);
        }
        if (this.foundRecipe && this.handler.getInputEnergyContainer().getEnergyStored() >= GAValues.VA[this.handler.getTier()]) {
            this.findAndConsumeInputs(itemInputs, false);
            this.maxProgress = this.calculateOverclock(30, 200, 2.8F);
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

    private boolean findCircuit(IItemHandlerModifiable itemInputs) {
        this.circuitNumber = 0;
        boolean foundCircuit = false;
        for (int i = 0; i < itemInputs.getSlots(); i++) {
            final ItemStack stack = itemInputs.getStackInSlot(i);
            final NBTTagCompound compound = stack.getTagCompound();
            if (this.isCircuitStack(compound)) {
                this.circuitNumber += compound.getInteger("Configuration");
                foundCircuit = true;
            }
        }
        return foundCircuit;
    }

    private boolean findAndConsumeInputs(IItemHandlerModifiable itemInputs, boolean simulate) {
        int availableParallels = this.handler.getParallel();
        for (int i = 0; i < itemInputs.getSlots() && availableParallels > 0; i++) {
            final ItemStack stack = itemInputs.getStackInSlot(i);
            if (stack.isEmpty() || this.isCircuitStack(stack.getTagCompound())) continue;
            final ItemStack input = itemInputs.getStackInSlot(i);
            if (input.isEmpty()) continue;
            final ICarvingGroup carvingGroup = CarvingUtils.getChiselRegistry().getGroup(stack);
            if (carvingGroup != null) {
                final List<ICarvingVariation> carvingVariations = carvingGroup.getVariations();
                if (carvingVariations != null && !carvingVariations.isEmpty()) {
                    final int inputCount = itemInputs.extractItem(i, availableParallels, simulate).getCount();
                    availableParallels -= inputCount;
                    if (!simulate) {
                        final int variation = Math.min(this.circuitNumber, carvingVariations.size() - 1);
                        final ItemStack output = carvingVariations.get(variation).getStack().copy();
                        output.setCount(inputCount);
                        this.itemInputs.add(input);
                        this.itemOutputs.add(output);
                    }
                }
            }
        }
        return availableParallels != this.handler.getParallel();
    }

    private boolean isCircuitStack(NBTTagCompound compound) {
        return compound != null && compound.hasKey("Configuration");
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
        compound.setInteger("circuitNumber", this.circuitNumber);
        compound.setBoolean("voiding", this.voiding);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        final NBTTagList itemInputList = compound.getTagList("itemInputs", 10), itemOutputList = compound.getTagList("itemOutputs", 10);
        this.circuitNumber = compound.getInteger("circuitNumber");
        this.outputIndex = compound.getInteger("outputIndex");
        this.voiding = compound.getBoolean("voiding");
        for (int i = 0; i < itemInputList.tagCount(); i++)
            this.itemInputs.add(new ItemStack(itemInputList.getCompoundTagAt(i)));
        for (int i = 0; i < itemOutputList.tagCount(); i++)
            this.itemOutputs.add(new ItemStack(itemOutputList.getCompoundTagAt(i)));
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
