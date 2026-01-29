package tj.capability.impl.workable;

import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.TJConfig;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;
import tj.capability.IMultipleWorkable;
import tj.capability.IRecipeMap;
import tj.capability.OverclockManager;
import tj.capability.TJCapabilities;
import tj.capability.ParallelRecipeLRUCache;
import tj.util.ItemStackHelper;

import java.util.*;
import java.util.function.LongSupplier;

public abstract class ParallelAbstractRecipeLogic extends MTETrait implements IMultipleWorkable {

    private static final String ALLOW_OVERCLOCKING = "AllowOverclocking";
    private static final String OVERCLOCK_VOLTAGE = "OverclockVoltage";

    protected final ParallelRecipeMapMultiblockController controller;
    protected final OverclockManager overclockManager = new OverclockManager();
    private int size = 1;

    protected boolean[] forceRecipeRecheck = new boolean[1];
    protected ItemStack[] lastItemInputs;
    protected FluidStack[] lastFluidInputs;
    public ParallelRecipeLRUCache previousRecipe;
    public int recipeCacheSize;
    protected boolean useOptimizedRecipeLookUp = true;
    protected boolean allowOverclocking = true;
    private long overclockVoltage;
    protected LongSupplier maxVoltage = this::getMaxVoltage;

    protected int[] progressTime = new int[1];
    protected int[] maxProgressTime = new int[1];
    protected long[] recipeEUt = new long[1];
    protected int[] parallel = new int[1];
    protected int[] itemOutputIndex = new int[1];
    protected int[] fluidOutputIndex = new int[1];
    protected List<Recipe> occupiedRecipes = new ArrayList<>();
    protected Int2ObjectMap<List<FluidStack>> fluidOutputs = new Int2ObjectOpenHashMap<>();
    protected Int2ObjectMap<NonNullList<ItemStack>> itemOutputs = new Int2ObjectOpenHashMap<>();
    protected final Random random = new Random();

    protected boolean isActive;
    protected boolean distinct;
    protected boolean voidItems;
    protected boolean voidFluids;
    protected boolean[] isInstanceActive = new boolean[1];
    protected boolean[] workingEnabled = new boolean[1];
    protected boolean[] hasNotEnoughEnergy = new boolean[1];
    protected boolean[] wasActiveAndNeedsUpdate = new boolean[1];
    protected boolean[] lockRecipe = new boolean[1];
    protected boolean[] hasProblems = new boolean[1];
    private final long[] V;
    private final String[] VN;

    private int[] sleepTimer = new int[1];
    private int[] sleepTime = new int[1];
    private int[] failCount = new int[1];

    public ParallelAbstractRecipeLogic(MetaTileEntity metaTileEntity, int recipeCacheSize) {
        super(metaTileEntity);
        this.controller = (ParallelRecipeMapMultiblockController) metaTileEntity;
        this.recipeCacheSize = recipeCacheSize;
        this.previousRecipe = new ParallelRecipeLRUCache(this.recipeCacheSize);
        this.occupiedRecipes.add(0, null);
        this.sleepTime[0] = 1;
        this.workingEnabled[0] = true;
        this.parallel[0] = 1;

        if (ConfigHolder.gregicalityOverclocking) {
            this.V = GTValues.V2;
            this.VN = GTValues.VN2;
        } else {
           this. V = GTValues.V;
            this.VN = GTValues.VN;
        }
    }

    public void setLayer(int i, boolean remove) {
        this.size = i;
        this.forceRecipeRecheck = Arrays.copyOf(this.forceRecipeRecheck, this.size);
        this.progressTime = Arrays.copyOf(this.progressTime, this.size);
        this.maxProgressTime = Arrays.copyOf(this.maxProgressTime, this.size);
        this.recipeEUt = Arrays.copyOf(this.recipeEUt, this.size);
        this.parallel = Arrays.copyOf(this.parallel, this.size);
        this.itemOutputIndex = Arrays.copyOf(this.itemOutputIndex, this.size);
        this.fluidOutputIndex = Arrays.copyOf(this.fluidOutputIndex, this.size);
        this.isInstanceActive = Arrays.copyOf(this.isInstanceActive, this.size);
        this.workingEnabled = Arrays.copyOf(this.workingEnabled, this.size);
        this.wasActiveAndNeedsUpdate = Arrays.copyOf(this.wasActiveAndNeedsUpdate, this.size);
        this.hasNotEnoughEnergy = Arrays.copyOf(this.hasNotEnoughEnergy, this.size);
        this.lockRecipe = Arrays.copyOf(this.lockRecipe, this.size);
        this.hasProblems = Arrays.copyOf(this.hasProblems, this.size);
        this.sleepTimer = Arrays.copyOf(this.sleepTimer, this.size);
        this.sleepTime = Arrays.copyOf(this.sleepTime, this.size);
        this.failCount = Arrays.copyOf(this.failCount, this.size);
        if (remove) {
            this.fluidOutputs.remove(i);
            this.itemOutputs.remove(i);
            this.occupiedRecipes.remove(i);
        } else {
            this.occupiedRecipes.add(this.size - 1, null);
            this.sleepTime[this.size -1] = 1;
            this.workingEnabled[this.size -1] = true;
            this.parallel[this.size -1] = 1;
        }
    }

    public void setMaxVoltage(LongSupplier maxVoltage) {
        this.maxVoltage = maxVoltage;
    }

    protected abstract long getEnergyStored();

    protected abstract long getEnergyCapacity();

    protected abstract boolean drawEnergy(long recipeEUt);

    protected abstract long getMaxVoltage();

    protected IItemHandlerModifiable getInputInventory() {
        return this.metaTileEntity.getImportItems();
    }

    protected IItemHandlerModifiable getOutputInventory() {
        return this.metaTileEntity.getExportItems();
    }

    protected IMultipleTankHandler getInputTank() {
        return this.metaTileEntity.getImportFluids();
    }

    protected IMultipleTankHandler getOutputTank() {
        return this.metaTileEntity.getExportFluids();
    }

    public List<FluidStack> getFluidOutputs(int i) {
        return this.fluidOutputs.get(i);
    }

    public NonNullList<ItemStack> getItemOutputs(int i) {
        return this.itemOutputs.get(i);
    }

    @Override
    public int getSize() {
        return this.size;
    }

    public void setRecipe(Recipe recipe, int i) {
        this.occupiedRecipes.set(i, recipe);
    }

    public Recipe getRecipe(int i) {
        return this.occupiedRecipes.get(i);
    }

    @Override
    public int getPageIndex() {
        return this.controller.getPageIndex();
    }

    @Override
    public int getPageSize() {
        return this.controller.getPageSize();
    }

    @Override
    public boolean hasProblems(int i) {
        return this.hasProblems[i];
    }

    public boolean isVoidingItems() {
        return this.voidItems;
    }

    public void setVoidItems(boolean voidItems) {
        this.voidItems = voidItems;
        this.metaTileEntity.markDirty();
    }

    public boolean isVoidingFluids() {
        return this.voidFluids;
    }

    public void setVoidFluids(boolean voidFluids) {
        this.voidFluids = voidFluids;
        this.metaTileEntity.markDirty();
    }

    public void setLockingMode(boolean setLockingMode, int i) {
        this.lockRecipe[i] = setLockingMode;
        this.metaTileEntity.markDirty();
    }

    public boolean getLockingMode(int i) {
        return this.lockRecipe[i];
    }

    public void update(int i) {
        if (!this.getMetaTileEntity().getWorld().isRemote) {
            if (this.workingEnabled[i]) {
                if (this.progressTime[i] > 0) {
                    this.updateRecipeProgress(i);
                }
                if (this.progressTime[i] == 0 && this.sleepTimer[i] == 0) {
                    boolean result = this.trySearchNewRecipe(i);
                    if (!result) {
                        this.failCount[i]++;
                        if (this.failCount[i] > 4) {

                            this.sleepTime[i] = Math.min(this.sleepTime[i] * 2, (ConfigHolder.maxSleepTime >= 0 && ConfigHolder.maxSleepTime <= 400) ? ConfigHolder.maxSleepTime : 20);
                            this.failCount[i] = 0;

                        }
                        this.sleepTimer[i] = this.sleepTime[i];
                    } else {
                        this.sleepTime[i] = 1;
                        this.failCount[i] = 0;
                    }
                }
                if (this.sleepTimer[i] > 0) {
                    this.sleepTimer[i]--;
                }
            }
            if (this.wasActiveAndNeedsUpdate[i]) {
                this.wasActiveAndNeedsUpdate[i] = false;
                this.setActive(false, i);
            }
        }
    }

    protected void updateRecipeProgress(int i) {
        if (this.recipeEUt[i] < 1 || this.drawEnergy(this.recipeEUt[i])) {
            //as recipe starts with progress on 1 this has to be > only not => to compensate for it
            if (++this.progressTime[i] > this.maxProgressTime[i]) {
                if (!this.completeRecipe(i)) {
                    this.progressTime[i] = 1;
                    this.setMaxProgress(TJConfig.machines.recipeCooldown, i);
                    this.recipeEUt[i] = 0;
                    this.hasProblems[i] = true;
                } else this.hasProblems[i] = false;
            }
        } else if (this.recipeEUt[i] > 0) {
            //only set hasNotEnoughEnergy if this recipe is consuming recipe
            //generators always have enough energy
            this.hasNotEnoughEnergy[i] = true;
            //if current progress value is greater than 2, decrement it by 2
            if (this.progressTime[i] >= 2) {
                if (ConfigHolder.insufficientEnergySupplyWipesRecipeProgress) {
                    this.progressTime[i] = 1;
                } else {
                    this.progressTime[i] = Math.max(1, this.progressTime[i] - 2);
                }
            }
        }
    }

    protected boolean trySearchNewRecipe(int i) {
        long maxVoltage = this.maxVoltage.getAsLong();
        Recipe currentRecipe = null;
        IItemHandlerModifiable importInventory = this.getInputInventory();
        IMultipleTankHandler importFluids = this.getInputTank();
        Recipe foundRecipe;
        if (this.lockRecipe[i] && this.occupiedRecipes.get(i) != null) {
            if (!this.occupiedRecipes.get(i).matches(false, importInventory, importFluids)) {
                return false;
            }
            foundRecipe = this.occupiedRecipes.get(i);
        } else {
            foundRecipe = this.distinct ? this.previousRecipe.get(importInventory, importFluids, i, this.occupiedRecipes) : this.previousRecipe.get(importInventory, importFluids);
        }
        if (foundRecipe != null) {
            //if previous recipe still matches inputs, try to use it
            currentRecipe = foundRecipe;
        } else {
            boolean dirty = this.checkRecipeInputsDirty(importInventory, importFluids);
            if (dirty || this.forceRecipeRecheck[i]) {
                this.forceRecipeRecheck[i] = false;
                //else, try searching new recipe for given inputs
                currentRecipe = findRecipe(maxVoltage, importInventory, importFluids, this.useOptimizedRecipeLookUp);
                if (currentRecipe != null) {
                    this.previousRecipe.put(currentRecipe);
                }
            }
        }
        if (currentRecipe != null && this.setupAndConsumeRecipeInputs(currentRecipe)) {
            this.occupiedRecipes.set(i, currentRecipe);
            this.setupRecipe(currentRecipe, i);
            return true;
        }
        return false;
    }

    public void forceRecipeRecheck(int i) {
        this.forceRecipeRecheck[i] = true;
    }

    public boolean getUseOptimizedRecipeLookUp() {
        return this.useOptimizedRecipeLookUp;
    }

    public void setUseOptimizedRecipeLookUp(boolean use) {
        this.useOptimizedRecipeLookUp = use;
    }

    public boolean toggleUseOptimizedRecipeLookUp() {
        this.setUseOptimizedRecipeLookUp(!this.useOptimizedRecipeLookUp);
        return this.useOptimizedRecipeLookUp;
    }

    protected int getMinTankCapacity(IMultipleTankHandler tanks) {
        if (tanks.getTanks() == 0) {
            return 0;
        }
        int result = Integer.MAX_VALUE;
        for (IFluidTank fluidTank : tanks.getFluidTanks()) {
            result = Math.min(fluidTank.getCapacity(), result);
        }
        return result;
    }

    public Recipe findRecipe(long maxVoltage, IItemHandlerModifiable itemInputs, IMultipleTankHandler fluidInputs) {
        return this.findRecipe(maxVoltage, itemInputs, fluidInputs, this.useOptimizedRecipeLookUp);
    }

    protected Recipe findRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, boolean useOptimizedRecipeLookUp) {
        return ((IRecipeMap) this.controller.recipeMaps[this.controller.getRecipeMapIndex()]).findRecipeDistinct(maxVoltage, inputs, fluidInputs, getMinTankCapacity(getOutputTank()), useOptimizedRecipeLookUp, this.occupiedRecipes, distinct);
    }

    protected boolean checkRecipeInputsDirty(IItemHandler inputs, IMultipleTankHandler fluidInputs) {
        boolean shouldRecheckRecipe = false;
        if (this.lastItemInputs == null || this.lastItemInputs.length != inputs.getSlots()) {
            this.lastItemInputs = new ItemStack[inputs.getSlots()];
            Arrays.fill(this.lastItemInputs, ItemStack.EMPTY);
        }
        if (this.lastFluidInputs == null || this.lastFluidInputs.length != fluidInputs.getTanks()) {
            this.lastFluidInputs = new FluidStack[fluidInputs.getTanks()];
        }
        for (int j = 0; j < this.lastItemInputs.length; j++) {
            ItemStack currentStack = inputs.getStackInSlot(j);
            ItemStack lastStack = this.lastItemInputs[j];
            if (!areItemStacksEqual(currentStack, lastStack)) {
                this.lastItemInputs[j] = currentStack.isEmpty() ? ItemStack.EMPTY : currentStack.copy();
                shouldRecheckRecipe = true;
            } else if (currentStack.getCount() != lastStack.getCount()) {
                lastStack.setCount(currentStack.getCount());
                shouldRecheckRecipe = true;
            }
        }
        for (int j = 0; j < this.lastFluidInputs.length; j++) {
            FluidStack currentStack = fluidInputs.getTankAt(j).getFluid();
            FluidStack lastStack = this.lastFluidInputs[j];
            if ((currentStack == null && lastStack != null) ||
                    (currentStack != null && !currentStack.isFluidEqual(lastStack))) {
                this.lastFluidInputs[j] = currentStack == null ? null : currentStack.copy();
                shouldRecheckRecipe = true;
            } else if (currentStack != null && lastStack != null &&
                    currentStack.amount != lastStack.amount) {
                lastStack.amount = currentStack.amount;
                shouldRecheckRecipe = true;
            }
        }
        return shouldRecheckRecipe;
    }

    protected static boolean areItemStacksEqual(ItemStack stackA, ItemStack stackB) {
        return (stackA.isEmpty() && stackB.isEmpty()) ||
                (ItemStack.areItemsEqual(stackA, stackB) &&
                        ItemStack.areItemStackTagsEqual(stackA, stackB));
    }

    protected boolean setupAndConsumeRecipeInputs(Recipe recipe) {
        if (!this.calculateOverclock(recipe.getEUt(), recipe.getDuration()))
            return false;
        long resultEU = this.overclockManager.getEUt();
        long totalEUt = resultEU * this.overclockManager.getDuration();
        IItemHandlerModifiable importInventory = this.getInputInventory();
        IMultipleTankHandler importFluids = this.getInputTank();
        return (totalEUt >= 0 ? this.getEnergyStored() >= (totalEUt > this.getEnergyCapacity() / 2 ? resultEU : totalEUt) :
                (this.getEnergyStored() - resultEU <= this.getEnergyCapacity())) &&
                recipe.matches(true, importInventory, importFluids);
    }

    public double getDurationOverclock() {
        return 2.8;
    }

    /**
     *
     * @param EUt recipe EU/t
     * @param duration recipe duration ticks
     * @return true if the overclock can be successfully performed
     */
    protected boolean calculateOverclock(long EUt, int duration) {
        if (!this.allowOverclocking) {
            this.overclockManager.setEUtAndDuration(EUt, duration);
            return true;
        }
        boolean negativeEU = EUt < 0;
        int tier = this.getOverclockingTier(this.maxVoltage.getAsLong());
        if (this.V[tier] <= EUt || tier == 0) {
            this.overclockManager.setEUtAndDuration(EUt, duration);
            return true;
        }
        if (negativeEU)
            EUt = -EUt;
        long resultEUt = EUt;
        double resultDuration = duration;
        double durationModifier = this.getDurationOverclock();
        //do not overclock further if duration is already too small
        while (resultDuration >= 1 && resultEUt <= this.V[tier - 1]) {
            resultEUt *= 4;
            resultDuration /= durationModifier;
        }
        this.overclockManager.setEUtAndDuration(resultEUt, (int) Math.round(resultDuration));
        return true;
    }

    protected int getOverclockingTier(long voltage) {
        if (ConfigHolder.gregicalityOverclocking) {
            return GTUtility.getGATierByVoltage(voltage);
        } else {
            return GTUtility.getTierByVoltage(voltage);
        }
    }

    protected long getVoltageByTier(final int tier) {
        return this.V[tier];
    }

    public String[] getAvailableOverclockingTiers() {
        final int maxTier = this.getOverclockingTier(this.maxVoltage.getAsLong());
        final String[] result = new String[maxTier + 2];
        result[0] = "gregtech.gui.overclock.off";
        for (int i = 0; i < maxTier + 1; ++i) {
            result[i + 1] = VN[i];
        }
        return result;
    }

    protected void setupRecipe(Recipe recipe, int i) {
        this.progressTime[i] = 1;
        this.setMaxProgress(this.overclockManager.getDuration(), i);
        this.recipeEUt[i] = this.overclockManager.getEUt();
        this.fluidOutputs.put(i, GTUtility.copyFluidList(recipe.getFluidOutputs()));
        int tier = this.getMachineTierForRecipe(recipe);
        this.itemOutputs.put(i, GTUtility.copyStackList(recipe.getResultItemOutputs(this.getOutputInventory().getSlots(), this.random, tier)));
        if (this.wasActiveAndNeedsUpdate[i]) {
            this.wasActiveAndNeedsUpdate[i] = false;
        } else {
            this.setActive(true, i);
        }
    }

    protected int getMachineTierForRecipe(Recipe recipe) {
        return GTUtility.getGATierByVoltage(this.maxVoltage.getAsLong());
    }

    protected boolean completeRecipe(int i) {
        List<ItemStack> itemOutputs = this.itemOutputs.get(i);
        for (int j = this.itemOutputIndex[i]; j < itemOutputs.size(); j++) {
            ItemStack stack = itemOutputs.get(j);
            if (this.voidItems || ItemStackHelper.insertIntoItemHandler(this.getOutputInventory(), stack, true).isEmpty()) {
                ItemStackHelper.insertIntoItemHandler(this.getOutputInventory(), stack, false);
                this.itemOutputIndex[i]++;
            } else return false;
        }
        List<FluidStack> fluidOutputs = this.fluidOutputs.get(i);
        for (int j = this.fluidOutputIndex[i]; j < fluidOutputs.size(); j++) {
            FluidStack stack = fluidOutputs.get(j);
            if (this.voidFluids || this.getOutputTank().fill(stack, false) == stack.amount) {
                this.getOutputTank().fill(stack, true);
                this.fluidOutputIndex[i]++;
            } else return false;
        }
        this.itemOutputIndex[i] = 0;
        this.fluidOutputIndex[i] = 0;
        this.progressTime[i] = 0;
        this.setMaxProgress(0, i);
        this.recipeEUt[i] = 0;
        this.hasNotEnoughEnergy[i] = false;
        this.wasActiveAndNeedsUpdate[i] = true;
        //force recipe recheck because inputs could have changed since last time
        //we checked them before starting our recipe, especially if recipe took long time
        this.forceRecipeRecheck[i] = true;
        return true;
    }

    public double getProgressPercent(int i) {
        return this.getMaxProgress(i) == 0 ? 0.0 : this.getProgress(i) / (this.getMaxProgress(i) * 1.0);
    }

    public int getTicksTimeLeft(int i) {
        return this.maxProgressTime[i] == 0 ? 0 : (this.maxProgressTime[i] - this.progressTime[i]);
    }

    @Override
    public int getProgress(int i) {
        return this.progressTime[i];
    }

    @Override
    public int getMaxProgress(int i) {
       return this.maxProgressTime[i];
    }

    @Override
    public long getRecipeEUt(int i) {
        return this.recipeEUt[i];
    }

    @Override
    public int getParallel(int i) {
        return this.parallel[i];
    }

    public void setMaxProgress(int maxProgress, int i) {
        this.maxProgressTime[i] = maxProgress;
    }

    protected void setActive(boolean active, int i) {
        this.isInstanceActive[i] = active;
        this.isActive = active;
        if (!this.metaTileEntity.getWorld().isRemote) {
            this.writeCustomData(1, buf -> buf.writeBoolean(active));
            this.metaTileEntity.markDirty();
        }
    }

    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public boolean isInstanceActive(int i) {
        return this.isInstanceActive[i];
    }

    @Override
    public boolean isWorkingEnabled(int i) {
        return this.workingEnabled[i];
    }

    public boolean isDistinct() {
        return this.distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
        this.previousRecipe.clear();
        Collections.fill(this.occupiedRecipes, null);
        this.metaTileEntity.markDirty();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == 1) {
            this.isActive = buf.readBoolean();
        }
        this.getMetaTileEntity().getHolder().scheduleChunkForRenderUpdate();
    }

    @Override
    public void writeInitialData(PacketBuffer buf) {
        buf.writeBoolean(this.isActive);
    }

    @Override
    public void receiveInitialData(PacketBuffer buf) {
        this.isActive = buf.readBoolean();
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled, int i) {
        this.workingEnabled[i] = workingEnabled;
        this.metaTileEntity.markDirty();
    }

    @Override
    public String getName() {
        return "RecipeMapWorkable";
    }

    @Override
    public int getNetworkID() {
        return TraitNetworkIds.TRAIT_ID_WORKABLE;
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == TJCapabilities.CAPABILITY_MULTIPLE_WORKABLE) {
            return TJCapabilities.CAPABILITY_MULTIPLE_WORKABLE.cast(this);
        } else if (capability == TJCapabilities.CAPABILITY_MULTI_CONTROLLABLE) {
            return TJCapabilities.CAPABILITY_MULTI_CONTROLLABLE.cast(this);
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound mainCompound = new NBTTagCompound();

        NBTTagList occupiedRecipeList = new NBTTagList();
        for (int i = 0; i < this.occupiedRecipes.size(); i++) {
            Recipe recipe = this.occupiedRecipes.get(i);
            if (recipe != null && this.lockRecipe[i]) {
                NBTTagCompound workableInstanceCompound = new NBTTagCompound();

                NBTTagList itemInputsList = new NBTTagList();
                for (int j = 0; j < recipe.getInputs().size(); j++) {
                    NBTTagCompound itemInputsCompound = new NBTTagCompound();
                    NBTTagList itemInputsOreDictList = new NBTTagList();
                    int count = recipe.getInputs().get(j).getCount();

                    for (ItemStack itemStack : recipe.getInputs().get(j).getIngredient().getMatchingStacks()) {
                        itemInputsOreDictList.appendTag(itemStack.writeToNBT(new NBTTagCompound()));
                    }
                    itemInputsCompound.setInteger("Count", count);
                    itemInputsCompound.setTag("ItemInputsOreDict", itemInputsOreDictList);
                    itemInputsList.appendTag(itemInputsCompound);
                }

                NBTTagList itemChancedOutputsList = new NBTTagList();
                for (int j = 0; j < recipe.getChancedOutputs().size(); j++) {
                    NBTTagCompound chanceEntryCompound = new NBTTagCompound();
                    Recipe.ChanceEntry chanceEntry = recipe.getChancedOutputs().get(j);
                    chanceEntryCompound.setTag("ItemStack", chanceEntry.getItemStack().writeToNBT(new NBTTagCompound()));
                    chanceEntryCompound.setInteger("Chance", chanceEntry.getChance());
                    chanceEntryCompound.setInteger("BoostPerTier", chanceEntry.getBoostPerTier());
                    itemChancedOutputsList.appendTag(chanceEntryCompound);
                }

                NBTTagList itemOutputsList = new NBTTagList();
                for (ItemStack itemStack : recipe.getOutputs()) {
                    itemOutputsList.appendTag(itemStack.writeToNBT(new NBTTagCompound()));
                }
                NBTTagList fluidInputsList = new NBTTagList();
                for (FluidStack fluidStack : recipe.getFluidInputs()) {
                    fluidInputsList.appendTag(fluidStack.writeToNBT(new NBTTagCompound()));
                }
                NBTTagList fluidOutputsList = new NBTTagList();
                for (FluidStack fluidStack : recipe.getFluidOutputs()) {
                    fluidOutputsList.appendTag(fluidStack.writeToNBT(new NBTTagCompound()));
                }

                workableInstanceCompound.setTag("ItemInputs", itemInputsList);
                workableInstanceCompound.setTag("ItemChancedOutputs", itemChancedOutputsList);
                workableInstanceCompound.setTag("ItemOutputs", itemOutputsList);
                workableInstanceCompound.setTag("FluidInputs", fluidInputsList);
                workableInstanceCompound.setTag("FluidOutputs", fluidOutputsList);
                workableInstanceCompound.setInteger("Energy", recipe.getEUt());
                workableInstanceCompound.setInteger("Duration", recipe.getDuration());
                workableInstanceCompound.setInteger("Parallel", this.parallel[i]);
                workableInstanceCompound.setInteger("Index", i);
                occupiedRecipeList.appendTag(workableInstanceCompound);
            }
        }

        NBTTagList workableInstanceList = new NBTTagList();
        for (int i = 0; i < this.size; i++) {
            NBTTagCompound workableInstanceCompound = new NBTTagCompound();
            workableInstanceCompound.setBoolean("Enabled", this.workingEnabled[i]);
            workableInstanceCompound.setBoolean("Lock", this.lockRecipe[i]);
            workableInstanceCompound.setBoolean("HasProblems", this.hasProblems[i]);
            workableInstanceCompound.setBoolean("Active", this.isInstanceActive[i]);
            workableInstanceCompound.setInteger("MaxProgress", this.maxProgressTime[i]);
            workableInstanceCompound.setInteger("Progress", this.progressTime[i]);
            workableInstanceCompound.setLong("EUt", this.recipeEUt[i]);
            workableInstanceCompound.setInteger("ItemIndex", this.itemOutputIndex[i]);
            workableInstanceCompound.setInteger("FluidIndex", this.fluidOutputIndex[i]);

            if (this.progressTime[i] > 0) {
                NBTTagList itemOutputsList = new NBTTagList();
                for (ItemStack itemOutput : this.itemOutputs.get(i)) {
                    itemOutputsList.appendTag(itemOutput.writeToNBT(new NBTTagCompound()));
                }
                NBTTagList fluidOutputsList = new NBTTagList();
                for (FluidStack fluidOutput : this.fluidOutputs.get(i)) {
                    fluidOutputsList.appendTag(fluidOutput.writeToNBT(new NBTTagCompound()));
                }

                workableInstanceCompound.setTag("ItemOutputs", itemOutputsList);
                workableInstanceCompound.setTag("FluidOutputs", fluidOutputsList);
            }
            workableInstanceList.appendTag(workableInstanceCompound); // lol this was supposed to be outside this if block
        }
        mainCompound.setBoolean(ALLOW_OVERCLOCKING, this.allowOverclocking);
        mainCompound.setLong(OVERCLOCK_VOLTAGE, this.overclockVoltage);
        mainCompound.setBoolean("IsActive", this.isActive);
        mainCompound.setBoolean("Distinct", this.distinct);
        mainCompound.setBoolean("VoidItems", this.voidItems);
        mainCompound.setBoolean("VoidFluids", this.voidFluids);
        mainCompound.setInteger("Size", this.size);
        mainCompound.setTag("OccupiedRecipes", occupiedRecipeList);
        mainCompound.setTag("WorkableInstances", workableInstanceList);
        return mainCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        NBTTagList workableInstanceList = compound.getTagList("WorkableInstances", Constants.NBT.TAG_COMPOUND);
        NBTTagList occupiedRecipeList = compound.getTagList("OccupiedRecipes", Constants.NBT.TAG_COMPOUND);

        this.voidItems = compound.getBoolean("VoidItems");
        this.voidFluids = compound.getBoolean("VoidFluids");
        this.distinct = compound.getBoolean("Distinct");
        this.isActive = compound.getBoolean("IsActive");
        if (compound.hasKey(ALLOW_OVERCLOCKING)) {
            this.allowOverclocking = compound.getBoolean(ALLOW_OVERCLOCKING);
        }
        if (compound.hasKey(OVERCLOCK_VOLTAGE)) {
            this.overclockVoltage = compound.getLong(OVERCLOCK_VOLTAGE);
        } else {
            // Calculate overclock voltage based on old allow flag
            this.overclockVoltage = this.allowOverclocking ? this.maxVoltage.getAsLong() : 0;
        }
        if (!compound.hasKey("Size")) {
            return;
        }

        this.size = compound.getInteger("Size");
        this.forceRecipeRecheck = new boolean[this.size];
        this.progressTime = new int[this.size];
        this.maxProgressTime = new int[this.size];
        this.recipeEUt = new long[this.size];
        this.parallel = new int[this.size];
        this.itemOutputIndex = new int[this.size];
        this.fluidOutputIndex = new int[this.size];
        this.isInstanceActive = new boolean[this.size];
        this.workingEnabled = new boolean[this.size];
        this.wasActiveAndNeedsUpdate = new boolean[this.size];
        this.hasNotEnoughEnergy = new boolean[this.size];
        this.lockRecipe = new boolean[this.size];
        this.hasProblems = new boolean[this.size];
        this.sleepTimer = new int[this.size];
        this.sleepTime = new int[this.size];
        this.failCount = new int[this.size];
        this.occupiedRecipes.clear();
        Arrays.fill(this.sleepTime, 1);
        Arrays.fill(this.parallel, 1);

        for (int i = 0; i < workableInstanceList.tagCount(); i++) {
            NBTTagCompound workableInstanceCompound = workableInstanceList.getCompoundTagAt(i);
            this.workingEnabled[i] = workableInstanceCompound.getBoolean("Enabled");
            this.lockRecipe[i] = workableInstanceCompound.getBoolean("Lock");
            this.hasProblems[i] = workableInstanceCompound.getBoolean("HasProblems");
            this.isInstanceActive[i] = workableInstanceCompound.getBoolean("Active");
            this.maxProgressTime[i] = workableInstanceCompound.getInteger("MaxProgress");
            this.progressTime[i] = workableInstanceCompound.getInteger("Progress");
            this.recipeEUt[i] = workableInstanceCompound.getLong("EUt");
            this.itemOutputIndex[i] = workableInstanceCompound.getInteger("ItemIndex");
            this.fluidOutputIndex[i] = workableInstanceCompound.getInteger("FluidIndex");
            this.occupiedRecipes.add(i, null);
            if (this.progressTime[i] > 0) {
                NBTTagList itemOutputsList = workableInstanceCompound.getTagList("ItemOutputs", Constants.NBT.TAG_COMPOUND);
                this.itemOutputs.put(i, NonNullList.create());
                for (int j = 0; j < itemOutputsList.tagCount(); j++) {
                    this.itemOutputs.get(i).add(new ItemStack(itemOutputsList.getCompoundTagAt(j)));
                }
                NBTTagList fluidOutputsList = workableInstanceCompound.getTagList("FluidOutputs", Constants.NBT.TAG_COMPOUND);
                this.fluidOutputs.put(i, new ArrayList<>());
                for (int j = 0; j < fluidOutputsList.tagCount(); j++) {
                    this.fluidOutputs.get(i).add(FluidStack.loadFluidStackFromNBT(fluidOutputsList.getCompoundTagAt(j)));
                }
            }
        }

        for (int i = 0; i < occupiedRecipeList.tagCount(); i++) {
            NBTTagCompound occupiedRecipeCompound = occupiedRecipeList.getCompoundTagAt(i);
            int index = occupiedRecipeCompound.getInteger("Index");
            int parallel = occupiedRecipeCompound.getInteger("Parallel");
            int duration = occupiedRecipeCompound.getInteger("Duration");
            int energy = occupiedRecipeCompound.getInteger("Energy");
            NBTTagList itemInputsList = occupiedRecipeCompound.getTagList("ItemInputs", Constants.NBT.TAG_COMPOUND);
            NBTTagList itemChancedOutputsList = occupiedRecipeCompound.getTagList("ItemChancedOutputs", Constants.NBT.TAG_COMPOUND);
            NBTTagList itemOutputsList = occupiedRecipeCompound.getTagList("ItemOutputs", Constants.NBT.TAG_COMPOUND);
            NBTTagList fluidInputsList = occupiedRecipeCompound.getTagList("FluidInputs", Constants.NBT.TAG_COMPOUND);
            NBTTagList fluidOutputsList = occupiedRecipeCompound.getTagList("FluidOutputs", Constants.NBT.TAG_COMPOUND);

            List<CountableIngredient> inputIngredients = NonNullList.create();
            for (int j = 0; j < itemInputsList.tagCount(); j++) {
                NBTTagCompound itemInputsCompound = itemInputsList.getCompoundTagAt(j);
                NBTTagList itemInputsOreDictList = itemInputsCompound.getTagList("ItemInputsOreDict", Constants.NBT.TAG_COMPOUND);
                int count = itemInputsCompound.getInteger("Count");

                ItemStack[] oreStacks = new ItemStack[itemInputsOreDictList.tagCount()];
                for (int k = 0; k < itemInputsOreDictList.tagCount(); k++) {
                    oreStacks[k] = new ItemStack(itemInputsOreDictList.getCompoundTagAt(k));
                }
                inputIngredients.add(new CountableIngredient(Ingredient.fromStacks(oreStacks), count));
            }

            List<Recipe.ChanceEntry> chanceOutputs = new ArrayList<>();
            for (int j = 0; j < itemChancedOutputsList.tagCount(); j++) {
                NBTTagCompound chanceEntryCompound = itemChancedOutputsList.getCompoundTagAt(j);
                ItemStack itemStack = new ItemStack(chanceEntryCompound.getCompoundTag("ItemStack"));
                int chance = chanceEntryCompound.getInteger("Chance");
                int boost = chanceEntryCompound.getInteger("BoostPerTier");
                chanceOutputs.add(new Recipe.ChanceEntry(itemStack, chance, boost));
            }

            List<ItemStack> itemOutputs = NonNullList.create();
            for (int j = 0; j < itemOutputsList.tagCount(); j++) {
                itemOutputs.add(new ItemStack(itemOutputsList.getCompoundTagAt(j)));
            }

            List<FluidStack> fluidInputs = new ArrayList<>();
            for (int j = 0; j < fluidInputsList.tagCount(); j++) {
                fluidInputs.add(FluidStack.loadFluidStackFromNBT(fluidInputsList.getCompoundTagAt(j)));
            }

            List<FluidStack> fluidOutputs = new ArrayList<>();
            for (int j = 0; j < fluidOutputsList.tagCount(); j++) {
                fluidOutputs.add(FluidStack.loadFluidStackFromNBT(fluidOutputsList.getCompoundTagAt(j)));
            }

            Recipe recipe = new Recipe(inputIngredients, itemOutputs, chanceOutputs, fluidInputs, fluidOutputs, duration, energy, false);
            this.occupiedRecipes.set(index, ((IRecipeMap) this.controller.recipeMaps[this.controller.getRecipeMapIndex()]).findRecipe(recipe));
            this.parallel[index] = parallel;
        }
    }
}
