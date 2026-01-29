package tj.capability.impl.workable;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandlerModifiable;
import team.chisel.api.carving.CarvingUtils;
import team.chisel.api.carving.ICarvingGroup;
import team.chisel.api.carving.ICarvingVariation;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.IMachineHandler;
import tj.capability.TJCapabilities;
import tj.capability.AbstractWorkableHandler;
import tj.util.ItemStackHelper;

import java.util.Collections;
import java.util.List;


public class ChiselWorkbenchWorkableHandler extends AbstractWorkableHandler<IMachineHandler> implements IItemFluidHandlerInfo {

    private int circuitNumber;
    private ItemStack input;
    private ItemStack output;

    public ChiselWorkbenchWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected boolean startRecipe() {
        boolean canStart = false;
        IItemHandlerModifiable itemInputs = this.isDistinct ? this.handler.getInputBus(this.lastInputIndex) : this.handler.getImportItemInventory();
        if (this.findCircuit(itemInputs) && this.findInputs(itemInputs)) {
            List<ICarvingVariation> carvingGroups = CarvingUtils.getChiselRegistry().getGroup(this.input).getVariations();
            int variation = Math.min(this.circuitNumber, carvingGroups.size() - 1);
            this.output = carvingGroups.get(variation).getStack();
            this.output.setCount(this.input.getCount());
            this.maxProgress = this.calculateOverclock(30, 200, 2.8F);
            canStart = true;
        }
        if (++this.lastInputIndex == this.busCount)
            this.lastInputIndex = 0;
        return canStart;
    }

    @Override
    protected boolean completeRecipe() {
        if (ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), this.output, true).isEmpty()) {
            ItemStackHelper.insertIntoItemHandler(this.handler.getExportItemInventory(), this.output, false);
            this.input = null;
            this.output = null;
            return true;
        }
        return false;
    }

    private boolean findCircuit(IItemHandlerModifiable itemInputs) {
        this.circuitNumber = 0;
        for (int i = 0; i < itemInputs.getSlots(); i++) {
            ItemStack stack = itemInputs.getStackInSlot(i);
            NBTTagCompound compound = stack.getTagCompound();
            if (this.isCircuitStack(compound)) {
                this.circuitNumber += compound.getInteger("Configuration");
            }
        }
        return true;
    }

    private boolean findInputs(IItemHandlerModifiable itemInputs) {
        int availableParallels = this.handler.getParallel();
        int count = 0;
        for (int i = 0; i < itemInputs.getSlots(); i++) {
            ItemStack stack = itemInputs.getStackInSlot(i);
            if (stack.isEmpty() || this.isCircuitStack(stack.getTagCompound()))
                continue;
            if (this.input == null) {
                ICarvingGroup carvingGroup = CarvingUtils.getChiselRegistry().getGroup(stack);
                if (carvingGroup != null) {
                    List<ICarvingVariation> carvingVariations = carvingGroup.getVariations();
                    if (carvingVariations != null && !carvingVariations.isEmpty())
                        this.input = stack.copy();
                }
            }
            if (this.input == null || !stack.isItemEqual(this.input))
                continue;
            int reminder = Math.min(stack.getCount(), availableParallels);
            if (this.handler.getImportItemInventory().extractItem(i, reminder, true).getCount() == reminder) {
                this.handler.getImportItemInventory().extractItem(i, reminder, false);
                availableParallels -= reminder;
                count += reminder;
                this.input.setCount(count);
            }
        }
        return count > 0;
    }

    private boolean isCircuitStack(NBTTagCompound compound) {
        return compound != null && compound.hasKey("Configuration");
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        compound.setInteger("circuitNumber", this.circuitNumber);
        if (this.input != null)
            compound.setTag("itemInput", this.input.serializeNBT());
        if (this.output != null)
            compound.setTag("itemOutput", this.output.serializeNBT());
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.circuitNumber = compound.getInteger("circuitNumber");
        if (compound.hasKey("itemInput"))
            this.input = new ItemStack(compound.getCompoundTag("itemInput"));
        if (compound.hasKey("itemOutput"))
            this.output = new ItemStack(compound.getCompoundTag("itemOutput"));
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING)
            return TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING.cast(this);
        return super.getCapability(capability);
    }

    @Override
    public List<ItemStack> getItemInputs() {
        return this.input != null ? Collections.singletonList(this.input) : null;
    }

    @Override
    public List<ItemStack> getItemOutputs() {
        return this.output != null ? Collections.singletonList(this.output) : null;
    }
}
