package tj.capability.impl.workable;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.IMachineHandler;
import tj.capability.TJCapabilities;
import tj.capability.AbstractWorkableHandler;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;


public class ArchitectWorkbenchWorkableHandler extends AbstractWorkableHandler<IMachineHandler> implements IItemFluidHandlerInfo {

    @Nonnull
    private ItemStack catalyst = ItemStack.EMPTY;

    @Nonnull
    private ItemStack input = ItemStack.EMPTY;

    @Nonnull
    private ItemStack output = ItemStack.EMPTY;

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
            this.output = new ItemStack(Item.getByNameOrId("architecturecraft:shape"), this.input.getCount());
            NBTTagCompound compound = this.catalyst.getTagCompound().copy();
            compound.setString("BaseName", Item.REGISTRY.getNameForObject(this.input.getItem()).toString());
            compound.setInteger("BaseData", this.input.getMetadata());
            this.output.setTagCompound(compound);
            this.maxProgress = this.calculateOverclock(30, 200, 2.8F);
            return true;
        }
        return false;
    }

    @Override
    protected boolean completeRecipe() {
        if (TJItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), this.output, true).isEmpty()) {
            TJItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), this.output, false);
            this.catalyst = ItemStack.EMPTY;
            this.input = ItemStack.EMPTY;
            this.output = ItemStack.EMPTY;
            return true;
        }
        return false;
    }

    private boolean findCatalyst(IItemHandlerModifiable itemInputs) {
        for (int i = 0; i < itemInputs.getSlots(); i++) {
            ItemStack stack = itemInputs.getStackInSlot(i);
            if (this.isArchitectureStack(stack.getTagCompound())) {
                this.catalyst = stack;
                return true;
            }
        }
        return false;
    }

    private boolean findInputs(IItemHandlerModifiable itemInputs) {
        int availableParallels = this.handler.getParallel();
        int count = 0;
        for (int i = 0; i < itemInputs.getSlots() && availableParallels != 0; i++) {
            ItemStack stack = itemInputs.getStackInSlot(i);
            if (stack.isEmpty() || this.isArchitectureStack(stack.getTagCompound()))
                continue;
            if (this.input.isEmpty())
                this.input = stack.copy();
            if (!stack.isItemEqual(this.input))
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

    private boolean isArchitectureStack(NBTTagCompound tagCompound) {
        return tagCompound != null && tagCompound.hasKey("Shape") && tagCompound.hasKey("BaseName") && tagCompound.hasKey("BaseData");
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        if (!this.catalyst.isEmpty())
            compound.setTag("catalyst", this.catalyst.serializeNBT());
        if (!this.input.isEmpty())
            compound.setTag("input", this.input.serializeNBT());
        if (!this.output.isEmpty())
            compound.setTag("output", this.output.serializeNBT());
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        if (compound.hasKey("catalyst"))
            this.catalyst = new ItemStack(compound.getCompoundTag("catalyst"));
        if (compound.hasKey("input"))
            this.input = new ItemStack(compound.getCompoundTag("input"));
        if (compound.hasKey("output"))
            this.output = new ItemStack(compound.getCompoundTag("output"));
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING)
            return TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING.cast(this);
        return super.getCapability(capability);
    }

    @Override
    public List<ItemStack> getItemInputs() {
        return !this.input.isEmpty() ? Collections.singletonList(this.input) : null;
    }

    @Override
    public List<ItemStack> getItemOutputs() {
        return !this.output.isEmpty() ? Collections.singletonList(this.output) : null;
    }
}
