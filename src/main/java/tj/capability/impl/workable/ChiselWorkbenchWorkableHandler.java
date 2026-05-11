package tj.capability.impl.workable;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandlerModifiable;
import team.chisel.api.carving.CarvingUtils;
import team.chisel.api.carving.ICarvingGroup;
import team.chisel.api.carving.ICarvingVariation;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.IMachineHandler;
import tj.capability.TJCapabilities;
import tj.capability.AbstractWorkableHandler;
import tj.util.TJItemUtils;

import java.util.ArrayList;
import java.util.List;


public class ChiselWorkbenchWorkableHandler extends AbstractWorkableHandler<IMachineHandler> implements IItemFluidHandlerInfo {

    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private int circuitNumber;
    private int outputIndex;

    public ChiselWorkbenchWorkableHandler(MetaTileEntity metaTileEntity) {
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
            foundRecipe = this.findCircuit(itemInputs) && this.findInputs(itemInputs);
            if (!foundRecipe) for (int i = 0; i < this.busCount; i++) {
                if (i == this.lastInputIndex) continue;
                itemInputs = this.handler.getInputBus(i);
                foundRecipe = this.findCircuit(itemInputs) && this.findInputs(itemInputs);
                if (foundRecipe) {
                    this.lastInputIndex = i;
                    break;
                }
            }
        } else {
            itemInputs = this.handler.getImportItemInventory();
            foundRecipe = this.findCircuit(itemInputs) && this.findInputs(itemInputs);
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

    private boolean findInputs(IItemHandlerModifiable itemInputs) {
        int availableParallels = this.handler.getParallel();
        for (int i = 0; i < itemInputs.getSlots() && availableParallels > 0; i++) {
            final ItemStack stack = itemInputs.getStackInSlot(i);
            if (stack.isEmpty() || this.isCircuitStack(stack.getTagCompound())) continue;
            final ItemStack input = this.handler.getImportItemInventory().extractItem(i, availableParallels, true);
            if (input.isEmpty()) continue;
            final ICarvingGroup carvingGroup = CarvingUtils.getChiselRegistry().getGroup(stack);
            if (carvingGroup != null) {
                final List<ICarvingVariation> carvingVariations = carvingGroup.getVariations();
                if (carvingVariations != null && !carvingVariations.isEmpty()) {
                    final int variation = Math.min(this.circuitNumber, carvingVariations.size() - 1);
                    final int inputCount = this.handler.getImportItemInventory().extractItem(i, availableParallels, false).getCount();
                    final ItemStack output = carvingVariations.get(variation).getStack().copy();
                    output.setCount(inputCount);
                    this.itemInputs.add(input);
                    this.itemOutputs.add(output);
                    availableParallels -= inputCount;
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
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        final NBTTagList itemInputList = compound.getTagList("itemInputs", 10), itemOutputList = compound.getTagList("itemOutputs", 10);
        this.circuitNumber = compound.getInteger("circuitNumber");
        this.outputIndex = compound.getInteger("outputIndex");
        for (int i = 0; i < itemInputList.tagCount(); i++)
            this.itemInputs.add(new ItemStack(itemInputList.getCompoundTagAt(i)));
        for (int i = 0; i < itemOutputList.tagCount(); i++)
            this.itemOutputs.add(new ItemStack(itemOutputList.getCompoundTagAt(i)));
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
