package tj.builder.handlers;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.recipes.CountableIngredient;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Triple;;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.TJCapabilities;
import tj.capability.impl.AbstractWorkableHandler;
import tj.capability.impl.CraftingRecipeLRUCache;
import tj.multiblockpart.TJMultiblockAbility;
import tj.util.ItemStackHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class CrafterRecipeLogic extends AbstractWorkableHandler<CrafterRecipeLogic> implements IItemFluidHandlerInfo {

    private final CraftingRecipeLRUCache previousRecipe = new CraftingRecipeLRUCache(10);
    private final List<Int2ObjectMap<Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>>> recipeMapList = new ArrayList<>();
    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private ItemStack[] lastItemInputs;
    private boolean recipeRecheck;
    private boolean voidOutputs;

    public CrafterRecipeLogic(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    public CrafterRecipeLogic initialize(int busCount) {
        super.initialize(busCount);
        this.recipeMapList.clear();
        if (this.metaTileEntity instanceof MultiblockControllerBase) {
            List<IRecipeMapProvider> crafters = ((MultiblockControllerBase) this.metaTileEntity).getAbilities(TJMultiblockAbility.CRAFTER);
            for (IRecipeMapProvider provider : crafters)
                this.recipeMapList.add(provider.getRecipeMap());
        } else if (this.metaTileEntity instanceof IRecipeMapProvider) {
            this.recipeMapList.add(((IRecipeMapProvider) this.metaTileEntity).getRecipeMap());
        }
        return this;
    }

    public void clearCache() {
        this.previousRecipe.clear();
    }

    public CraftingRecipeLRUCache getPreviousRecipe() {
        return this.previousRecipe;
    }

    protected boolean checkRecipeInputsDirty(IItemHandler inputs) {
        boolean shouldRecheckRecipe = false;
        if (lastItemInputs == null || lastItemInputs.length != inputs.getSlots()) {
            this.lastItemInputs = new ItemStack[inputs.getSlots()];
            Arrays.fill(lastItemInputs, ItemStack.EMPTY);
        }
        for (int i = 0; i < lastItemInputs.length; i++) {
            ItemStack currentStack = inputs.getStackInSlot(i);
            ItemStack lastStack = lastItemInputs[i];
            if (!ItemStack.areItemStacksEqual(currentStack, lastStack)) {
                this.lastItemInputs[i] = currentStack.isEmpty() ? ItemStack.EMPTY : currentStack.copy();
                shouldRecheckRecipe = true;
            } else if (currentStack.getCount() != lastStack.getCount()) {
                lastStack.setCount(currentStack.getCount());
                shouldRecheckRecipe = true;
            }
        }
        return shouldRecheckRecipe;
    }

    private boolean trySearchForRecipe(IItemHandlerModifiable importItems) {
        int parallels = this.parallelSupplier.getAsInt();
        Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>> currentRecipe = this.previousRecipe.get(importItems);
        if (currentRecipe == null && (this.recipeRecheck || this.checkRecipeInputsDirty(importItems))) {
            this.recipeRecheck = false;
            recipeMapList:
            for (int i = 0; i < this.recipeMapList.size(); i++) {
                Map<Integer, Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> recipeMap = this.recipeMapList.get(i);
                recipeMap:
                for (int j = 0; j < recipeMap.size(); j++) {
                    Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>> recipe = recipeMap.get(j);
                    if (recipe == null)
                        continue;
                    List<CountableIngredient> countableIngredients = recipe.getMiddle();
                    for (int k = 0; k < countableIngredients.size(); k++) {
                        CountableIngredient ingredient = countableIngredients.get(k);
                        int size = ingredient.getCount();
                        int extracted = ItemStackHelper.extractFromItemHandlerByIngredient(importItems, ingredient.getIngredient(), size, true);
                        if (extracted < size)
                            continue recipeMap;
                    }
                    currentRecipe = recipe;
                    break recipeMapList;
                }
            }
        }
        if (currentRecipe != null) {
            int amountToExtract = parallels;
            for (CountableIngredient ingredient : currentRecipe.getMiddle()) { // calculate and simulate parallels
                int size = ingredient.getCount();
                int extracted = ItemStackHelper.extractFromItemHandlerByIngredient(importItems, ingredient.getIngredient(), parallels * size, true);
                amountToExtract = Math.min(amountToExtract, extracted / size);
            }
            for (CountableIngredient ingredient : currentRecipe.getMiddle()) { // consume recipe inputs
                int size = ingredient.getCount();
                ItemStackHelper.extractFromItemHandlerByIngredient(importItems, ingredient.getIngredient(), amountToExtract * size, false);
            }
            ItemStack output = currentRecipe.getLeft().getRecipeOutput().copy();
            output.setCount(output.getCount() * amountToExtract);
            this.itemOutputs.add(output);
            this.previousRecipe.put(currentRecipe);
            return true;
        }
        return false;
    }

    @Override
    protected boolean startRecipe() {
        boolean canStart = false;
        IItemHandlerModifiable itemInputs = this.isDistinct ? this.inputBus.apply(this.lastInputIndex) : this.importItemsSupplier.get();
        if (this.trySearchForRecipe(itemInputs)) {
            this.maxProgress = this.calculateOverclock(30, 50, 2.8F);
            canStart = true;
        }
        if (++this.lastInputIndex == this.busCount)
            this.lastInputIndex = 0;
        return canStart;
    }

    @Override
    protected boolean completeRecipe() {
        if (this.voidOutputs || ItemStackHelper.insertIntoItemHandler(this.exportItemsSupplier.get(), this.itemOutputs.get(0), true).isEmpty()) {
            ItemStackHelper.insertIntoItemHandler(this.exportItemsSupplier.get(), this.itemOutputs.get(0), false);
            this.itemInputs.clear();
            this.itemOutputs.clear();
            this.recipeRecheck = true;
            return true;
        }
        return false;
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
    public NBTTagCompound serializeNBT() {
        NBTTagCompound data = super.serializeNBT();
        NBTTagList inputList = new NBTTagList(), outputList = new NBTTagList();
        for (ItemStack stack : this.itemInputs)
            inputList.appendTag(stack.serializeNBT());
        for (ItemStack stack : this.itemOutputs)
            outputList.appendTag(stack.serializeNBT());
        data.setTag("inputList", inputList);
        data.setTag("outputList", outputList);
        data.setBoolean("voidOutputs", this.voidOutputs);
        return data;
    }

    @Override
    public void deserializeNBT(NBTTagCompound data) {
        super.deserializeNBT(data);
        NBTTagList inputList = data.getTagList("inputList", 10), outputList = data.getTagList("outputList", 10);
        for (int i = 0; i < inputList.tagCount(); i++)
            this.itemInputs.add(new ItemStack(inputList.getCompoundTagAt(i)));
        for (int i = 0; i < outputList.tagCount(); i++)
            this.itemOutputs.add(new ItemStack(outputList.getCompoundTagAt(i)));
        this.voidOutputs = data.getBoolean("voidOutputs");
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING)
            return TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING.cast(this);
        return super.getCapability(capability);
    }

    public void setVoidOutputs(boolean voidOutputs) {
        this.voidOutputs = voidOutputs;
        this.metaTileEntity.markDirty();
    }

    public boolean isVoidOutputs() {
        return this.voidOutputs;
    }
}
