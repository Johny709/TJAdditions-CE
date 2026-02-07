package tj.capability.impl.workable;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.IMachineHandler;
import tj.capability.TJCapabilities;
import tj.capability.AbstractWorkableHandler;
import tj.util.ItemStackHelper;

import java.util.ArrayList;
import java.util.List;


public class EnchanterWorkableHandler extends AbstractWorkableHandler<IMachineHandler> implements IItemFluidHandlerInfo {

    private ItemStack catalyst;
    private int outputIndex;
    private int experience;
    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private final List<FluidStack> fluidInputs = new ArrayList<>();

    public EnchanterWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected boolean startRecipe() {
        boolean canStart = false;
        IItemHandlerModifiable itemInputs = this.isDistinct ? this.handler.getInputBus(this.lastInputIndex) : this.handler.getImportItemInventory();
        int catalystSlotIndex = this.findCatalyst(itemInputs);
        if (catalystSlotIndex > -1 && this.findInputs(itemInputs, catalystSlotIndex, true)) {
            FluidStack fluid = FluidRegistry.getFluidStack("xpjuice", this.experience);
            if (this.hasEnoughFluid(fluid, this.experience)) {
                this.findInputs(itemInputs, catalystSlotIndex, false);
                this.handler.getImportFluidTank().drain(fluid, true);
                this.maxProgress = this.calculateOverclock(30, 2000, 2.8F);
                canStart = true;
            }
        }
        if (++this.lastInputIndex == this.busCount)
            this.lastInputIndex = 0;
        return canStart;
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
        this.experience = 0;
        this.itemInputs.clear();
        this.itemOutputs.clear();
        this.fluidInputs.clear();
        return true;
    }

    private int findCatalyst(IItemHandlerModifiable itemInputs) {
        for (int i = 0; i < itemInputs.getSlots(); i++) {
            ItemStack stack = itemInputs.getStackInSlot(i);
            if (!stack.isEmpty() && this.isEnchanted(stack.getTagCompound())) {
                this.catalyst = stack;
                return i;
            }
        }
        return -1;
    }

    private boolean findInputs(IItemHandlerModifiable itemInputs, int catalystSlotIndex, boolean simulate) {
        int applied = 0;
        for (int i = 0; i < itemInputs.getSlots(); i++) {
            ItemStack stack = itemInputs.getStackInSlot(i);
            ItemStack catalystStack = null;
            if (i != catalystSlotIndex && !stack.isEmpty()) {
                catalystStack = this.catalyst.copy();
                catalystStack.setCount(1);
                if (!simulate) {
                    this.itemInputs.add(stack.copy());
                    this.itemInputs.add(catalystStack.copy());
                    this.catalyst.shrink(1);
                }
                applied += this.applyEnchantments(catalystStack, i, simulate);
            }
            if (catalystStack != null && !simulate) {
                catalystStack = this.setBookOrEnchantedBook(catalystStack);
                this.itemOutputs.add(catalystStack);
            }
        }
        this.experience = applied * 1000;
        return this.experience > 0;
    }

    private int applyEnchantments(ItemStack catalyst, int slot, boolean simulate) {
        int applied = 0, parallelsUsed = 0;
        ItemStack stack = this.handler.getImportItemInventory().getStackInSlot(slot);
        NBTTagList catalystEnchants = this.getEnchantments(catalyst.getTagCompound()), newCatalystEnchants = new NBTTagList();
        NBTTagList stackEnchants = this.getEnchantments(stack.getTagCompound()), newStackEnchants = new NBTTagList();
        for (int i = 0; i < catalystEnchants.tagCount(); i++) {
            NBTTagCompound catalystCompound = catalystEnchants.getCompoundTagAt(i);
            boolean hasApplied = false;
            short catalystEnchant = catalystCompound.getShort("id");
            short catalystLevel = catalystCompound.getShort("lvl");
            for (int j = 0; j < stackEnchants.tagCount() && parallelsUsed < this.handler.getParallel(); j++) {
                NBTTagCompound stackCompound = stackEnchants.getCompoundTagAt(i);
                short stackEnchant = stackCompound.getShort("id");
                short stackLevel = stackCompound.getShort("lvl");
                if (stackEnchant == catalystEnchant) {
                    stackLevel = catalystLevel == stackLevel ? (short) (catalystLevel + 1)
                            : catalystLevel > stackLevel ? catalystLevel
                            : stackLevel;
                    if (stackLevel > this.handler.getTier())
                        continue;
                    hasApplied = true;
                    applied += stackLevel;
                    parallelsUsed++;
                }
                NBTTagCompound newStackCompound = new NBTTagCompound();
                newStackCompound.setShort("id", stackEnchant);
                newStackCompound.setShort("lvl", stackLevel);
                newStackEnchants.appendTag(newStackCompound);
            }
            if (!hasApplied) {
                NBTTagCompound newCatalystCompound = new NBTTagCompound();
                newCatalystCompound.setShort("id", catalystEnchant);
                newCatalystCompound.setShort("lvl", catalystLevel);
                if (parallelsUsed < this.handler.getParallel()) {
                    newStackEnchants.appendTag(newCatalystCompound);
                    applied += catalystLevel;
                    parallelsUsed++;
                } else newCatalystEnchants.appendTag(newCatalystCompound);
            }
        }
        ItemStack newStack = this.handler.getImportItemInventory().extractItem(slot, 1, simulate);
        this.setEnchantments(catalyst, newCatalystEnchants);
        this.setEnchantments(newStack, newStackEnchants);
        newStack = this.setBookOrEnchantedBook(newStack);
        if (!simulate)
            this.itemOutputs.add(newStack);
        return applied;
    }

    private ItemStack setBookOrEnchantedBook(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        if (stack.getItem() == Items.ENCHANTED_BOOK && !this.isEnchanted(compound))
            stack = new ItemStack(Items.BOOK);
        if (stack.getItem() == Items.BOOK && this.isEnchanted(compound)) {
            stack = new ItemStack(Items.ENCHANTED_BOOK);
            stack.setTagCompound(compound.copy());
        }
        return stack;
    }

    private boolean isEnchanted(NBTTagCompound compound) {
        return compound != null && (compound.hasKey("ench") || compound.hasKey("StoredEnchantments"));
    }

    private NBTTagList getEnchantments(NBTTagCompound compound) {
        if (compound == null)
            return new NBTTagList();
        NBTTagList enchantmentsList = compound.getTagList("ench", 10);
        if (enchantmentsList.tagCount() > 0)
            return enchantmentsList;
        else return compound.getTagList("StoredEnchantments", 10);
    }

    private void setEnchantments(ItemStack stack, NBTTagList enchantmentsList) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null) {
            stack.setTagCompound(new NBTTagCompound());
            compound = stack.getTagCompound();
        }
        if (stack.getItem() == Items.ENCHANTED_BOOK || stack.getItem() == Items.BOOK) {
            if (enchantmentsList.tagCount() > 0)
                compound.setTag("StoredEnchantments", enchantmentsList);
            else compound.removeTag("StoredEnchantments");
        } else {
            if (enchantmentsList.tagCount() > 0)
                compound.setTag("ench", enchantmentsList);
            else compound.removeTag("ench");
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        NBTTagList itemInputs = new NBTTagList(), itemOutputs = new NBTTagList(), fluidInputs = new NBTTagList();
        for (ItemStack stack : this.itemInputs)
            itemInputs.appendTag(stack.serializeNBT());
        for (ItemStack stack : this.itemOutputs)
            itemOutputs.appendTag(stack.serializeNBT());
        for (FluidStack stack : this.fluidInputs)
            fluidInputs.appendTag(stack.writeToNBT(new NBTTagCompound()));
        compound.setTag("itemInputs", itemInputs);
        compound.setTag("itemOutputs", itemOutputs);
        compound.setTag("fluidInputs", fluidInputs);
        compound.setInteger("outputIndex", this.outputIndex);
        compound.setInteger("experience", this.experience);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.outputIndex = compound.getInteger("outputIndex");
        this.experience = compound.getInteger("experience");
        NBTTagList itemInputs = compound.getTagList("itemInputs", 10), itemOutputs = compound.getTagList("itemOutputs", 10),
                fluidInputs = compound.getTagList("fluidInputs", 10);
        for (int i = 0; i < itemInputs.tagCount(); i++)
            this.itemInputs.add(new ItemStack(itemInputs.getCompoundTagAt(i)));
        for (int i = 0; i < itemOutputs.tagCount(); i++)
            this.itemOutputs.add(new ItemStack(itemOutputs.getCompoundTagAt(i)));
        for (int i = 0; i < fluidInputs.tagCount(); i++)
            this.fluidInputs.add(FluidStack.loadFluidStackFromNBT(fluidInputs.getCompoundTagAt(i)));
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

    @Override
    public List<FluidStack> getFluidInputs() {
        return this.fluidInputs;
    }
}
