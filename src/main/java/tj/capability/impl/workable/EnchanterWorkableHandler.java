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
import tj.util.TJItemUtils;

import java.util.ArrayList;
import java.util.List;


public class EnchanterWorkableHandler extends AbstractWorkableHandler<IMachineHandler> implements IItemFluidHandlerInfo {

    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private final List<FluidStack> fluidInputs = new ArrayList<>();
    private ItemStack catalyst;
    private int catalystIndex;
    private int outputIndex;
    private int experience;

    public EnchanterWorkableHandler(MetaTileEntity metaTileEntity) {
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
            foundRecipe = this.findCatalyst(itemInputs) && this.findInputs(itemInputs, true);
            if (!foundRecipe) for (int i = 0; i < this.busCount; i++) {
                if (i == this.lastInputIndex) continue;
                itemInputs = this.handler.getInputBus(i);
                foundRecipe = this.findCatalyst(itemInputs) && this.findInputs(itemInputs, true);
                if (foundRecipe) {
                    this.lastInputIndex = i;
                    break;
                }
            }
        } else {
            itemInputs = this.handler.getImportItemInventory();
            foundRecipe = this.findCatalyst(itemInputs) && this.findInputs(itemInputs, true);
        }
        if (foundRecipe) {
            FluidStack fluid = FluidRegistry.getFluidStack("xpjuice", this.experience);
            if (this.hasEnoughFluid(fluid, this.experience)) {
                this.findInputs(itemInputs, false);
                this.handler.getImportFluidTank().drain(fluid, true);
                this.maxProgress = this.calculateOverclock(30, 2000, 2.8F);
                return true;
            }
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
        this.experience = 0;
        this.outputIndex = 0;
        this.catalystIndex = 0;
        this.itemInputs.clear();
        this.itemOutputs.clear();
        this.fluidInputs.clear();
        return true;
    }

    private boolean findCatalyst(IItemHandlerModifiable itemInputs) {
        for (int i = 0; i < itemInputs.getSlots(); i++) {
            final ItemStack stack = itemInputs.getStackInSlot(i);
            if (!stack.isEmpty() && this.isEnchanted(stack.getTagCompound())) {
                this.catalystIndex = i;
                this.catalyst = stack;
                return true;
            }
        }
        return false;
    }

    private boolean findInputs(IItemHandlerModifiable itemInputs, boolean simulate) {
        int applied = 0;
        for (int i = 0; i < itemInputs.getSlots(); i++) {
            final ItemStack stack = itemInputs.getStackInSlot(i);
            ItemStack catalystStack = null;
            if (i != this.catalystIndex && !stack.isEmpty()) {
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
        final ItemStack stack = this.handler.getImportItemInventory().getStackInSlot(slot);
        final NBTTagList catalystEnchants = this.getEnchantments(catalyst.getTagCompound()), newCatalystEnchants = new NBTTagList();
        final NBTTagList stackEnchants = this.getEnchantments(stack.getTagCompound()), newStackEnchants = new NBTTagList();
        for (int i = 0; i < catalystEnchants.tagCount(); i++) {
            final NBTTagCompound catalystCompound = catalystEnchants.getCompoundTagAt(i);
            boolean hasApplied = false;
            final short catalystEnchant = catalystCompound.getShort("id");
            final short catalystLevel = catalystCompound.getShort("lvl");
            for (int j = 0; j < stackEnchants.tagCount() && parallelsUsed < this.handler.getParallel(); j++) {
                final NBTTagCompound stackCompound = stackEnchants.getCompoundTagAt(i);
                final short stackEnchant = stackCompound.getShort("id");
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
                final NBTTagCompound newStackCompound = new NBTTagCompound();
                newStackCompound.setShort("id", stackEnchant);
                newStackCompound.setShort("lvl", stackLevel);
                newStackEnchants.appendTag(newStackCompound);
            }
            if (!hasApplied) {
                final NBTTagCompound newCatalystCompound = new NBTTagCompound();
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
         final NBTTagCompound compound = stack.getTagCompound();
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
        final NBTTagList enchantmentsList = compound.getTagList("ench", 10);
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
        final NBTTagCompound compound = super.serializeNBT();
        final NBTTagList itemInputs = new NBTTagList(), itemOutputs = new NBTTagList(), fluidInputs = new NBTTagList();
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
        final NBTTagList itemInputs = compound.getTagList("itemInputs", 10), itemOutputs = compound.getTagList("itemOutputs", 10),
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
