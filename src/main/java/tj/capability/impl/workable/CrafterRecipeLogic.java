package tj.capability.impl.workable;

import gregicadditions.GAValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.recipes.CountableIngredient;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.tuple.Triple;
import tj.capability.*;
import tj.capability.impl.handler.IRecipeMapProvider;
import tj.multiblockpart.TJMultiblockAbility;
import tj.util.TJItemUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class CrafterRecipeLogic extends AbstractWorkableHandler<IMachineHandler> {

    private final CraftingRecipeLRUCache previousRecipe = new CraftingRecipeLRUCache(10);
    private final List<Int2ObjectMap<Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>>> recipeMapList = new ArrayList<>();
    private final List<ItemStack> itemInputs = new ArrayList<>();
    private final List<ItemStack> itemOutputs = new ArrayList<>();
    private ItemStack[] lastItemInputs;
    private Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>> currentRecipe;
    private boolean recipeRecheck;
    private boolean voidOutputs;
    private int outputIndex;

    public CrafterRecipeLogic(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    public void initialize(int busCount) {
        super.initialize(busCount);
        this.recipeMapList.clear();
        if (this.metaTileEntity instanceof MultiblockControllerBase) {
            final List<IRecipeMapProvider> crafters = ((MultiblockControllerBase) this.metaTileEntity).getAbilities(TJMultiblockAbility.CRAFTER);
            for (IRecipeMapProvider provider : crafters)
                this.recipeMapList.add(provider.getRecipeMap());
        } else if (this.metaTileEntity instanceof IRecipeMapProvider) {
            this.recipeMapList.add(((IRecipeMapProvider) this.metaTileEntity).getRecipeMap());
        }
    }

    @Override
    public void invalidate() {
        this.lastInputIndex = 0;
        this.recipeMapList.clear();
    }

    @Override
    protected boolean startRecipe() {
        IItemHandlerModifiable itemInputs;
        if (this.isDistinct) {
            itemInputs = this.handler.getInputBus(this.lastInputIndex);
            this.currentRecipe = this.trySearchForRecipe(itemInputs);
            if (this.currentRecipe == null) for (int i = 0; i < this.busCount; i++) {
                if (i == this.lastInputIndex) continue;
                itemInputs = this.handler.getInputBus(i);
                this.currentRecipe = this.trySearchForRecipe(itemInputs);
                if (this.currentRecipe != null) {
                    this.lastInputIndex = i;
                    break;
                }
            }
        } else {
            itemInputs = this.handler.getImportItemInventory();
            this.currentRecipe = this.trySearchForRecipe(itemInputs);
        }
        if (this.currentRecipe != null && this.handler.getInputEnergyContainer().getEnergyStored() >= GAValues.VA[this.handler.getTier()]) {
            final int parallels = this.handler.getParallel();
            int parallelsPerformed = parallels;
            for (CountableIngredient ingredient : this.currentRecipe.getMiddle()) { // calculate and simulate parallels
                final int size = ingredient.getCount();
                final int extracted = TJItemUtils.extractFromItemHandlerByIngredient(itemInputs, ingredient.getIngredient(), parallels * size, true);
                parallelsPerformed = Math.min(parallelsPerformed, extracted / size);
            }
            for (CountableIngredient ingredient : this.currentRecipe.getMiddle()) { // consume recipe inputs
                final int size = ingredient.getCount();
                TJItemUtils.extractFromItemHandlerByIngredient(itemInputs, ingredient.getIngredient(), parallelsPerformed * size, false);
            }
            final ItemStack output = this.currentRecipe.getLeft().getRecipeOutput().copy();
            output.setCount(output.getCount() * parallelsPerformed);
            this.itemOutputs.add(output);
            this.previousRecipe.put(this.currentRecipe);
            this.maxProgress = this.calculateOverclock(30, 50, 2.8F);
            return true;
        } else return false;
    }

    @Override
    protected boolean completeRecipe() {
        for (int i = this.outputIndex; i < this.itemOutputs.size(); i++) {
            final ItemStack stack = this.itemOutputs.get(i);
            if (this.voidOutputs || TJItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, true).isEmpty()) {
                TJItemUtils.insertIntoItemHandler(this.handler.getExportItemInventory(), stack, false);
                this.outputIndex++;
            } else return false;
        }
        this.currentRecipe = null;
        this.outputIndex = 0;
        this.itemInputs.clear();
        this.itemOutputs.clear();
        this.recipeRecheck = true;
        return true;
    }

    private Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>> trySearchForRecipe(IItemHandlerModifiable importItems) {
        Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>> currentRecipe = this.previousRecipe.get(importItems);
        if (currentRecipe == null && (this.recipeRecheck || this.checkRecipeInputsDirty(importItems))) {
            this.recipeRecheck = false;
            recipeMapList:
            for (int i = 0; i < this.recipeMapList.size(); i++) {
                final Map<Integer, Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>>> recipeMap = this.recipeMapList.get(i);
                if (recipeMap == null) continue;
                recipeMap:
                for (int j = 0; j < recipeMap.size(); j++) {
                    final Triple<IRecipe, NonNullList<CountableIngredient>, NonNullList<ItemStack>> recipe = recipeMap.get(j);
                    if (recipe == null) continue;
                    final List<CountableIngredient> countableIngredients = recipe.getMiddle();
                    for (int k = 0; k < countableIngredients.size(); k++) {
                        final CountableIngredient ingredient = countableIngredients.get(k);
                        final int size = ingredient.getCount();
                        final int extracted = TJItemUtils.extractFromItemHandlerByIngredient(importItems, ingredient.getIngredient(), size, true);
                        if (extracted < size)
                            continue recipeMap;
                    }
                    currentRecipe = recipe;
                    break recipeMapList;
                }
            }
        }
        return currentRecipe;
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

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound data = super.serializeNBT();
        final NBTTagList inputList = new NBTTagList(), outputList = new NBTTagList();
        for (ItemStack stack : this.itemInputs)
            inputList.appendTag(stack.serializeNBT());
        for (ItemStack stack : this.itemOutputs)
            outputList.appendTag(stack.serializeNBT());
        data.setInteger("outputIndex", this.outputIndex);
        data.setTag("inputList", inputList);
        data.setTag("outputList", outputList);
        data.setBoolean("voidOutputs", this.voidOutputs);
        return data;
    }

    @Override
    public void deserializeNBT(NBTTagCompound data) {
        super.deserializeNBT(data);
        final NBTTagList inputList = data.getTagList("inputList", 10), outputList = data.getTagList("outputList", 10);
        for (int i = 0; i < inputList.tagCount(); i++)
            this.itemInputs.add(new ItemStack(inputList.getCompoundTagAt(i)));
        for (int i = 0; i < outputList.tagCount(); i++)
            this.itemOutputs.add(new ItemStack(outputList.getCompoundTagAt(i)));
        this.voidOutputs = data.getBoolean("voidOutputs");
        this.outputIndex = data.getInteger("outputIndex");
    }

    @Override
    protected boolean otherConditions() {
        return this.currentRecipe != null;
    }

    public void clearCache() {
        this.previousRecipe.clear();
    }

    public CraftingRecipeLRUCache getPreviousRecipe() {
        return this.previousRecipe;
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

    public void setVoidOutputs(boolean voidOutputs) {
        this.voidOutputs = voidOutputs;
        this.metaTileEntity.markDirty();
    }

    public boolean isVoidOutputs() {
        return this.voidOutputs;
    }
}
