package tj.capability.impl;

import gregicadditions.GAUtility;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.recipes.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.builder.multicontrollers.ParallelRecipeMapMultiblockController;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Parallel Recipe Logic for Parallel Multiblocks. Implements distinct bus feature and uses containers from multiblock parts in the form of buses and hatches
 */
public class ParallelMultiblockRecipeLogic extends ParallelAbstractRecipeLogic {

    // Field used for maintenance
    protected int previousRecipeDuration;

    // Fields used for distinct mode
    protected int[] lastRecipeIndex;
    protected ItemStack[][] lastItemInputsMatrix;

    public ParallelMultiblockRecipeLogic(ParallelRecipeMapMultiblockController tileEntity, int recipeCacheSize) {
        super(tileEntity, recipeCacheSize);
        this.lastRecipeIndex = new int[1];
    }

    @Override
    public void setLayer(int i, boolean remove) {
        super.setLayer(i, remove);
        this.lastRecipeIndex = Arrays.copyOf(this.lastRecipeIndex, this.getSize());
    }

    public IEnergyContainer getEnergyContainer() {
        ParallelRecipeMapMultiblockController controller = (ParallelRecipeMapMultiblockController) this.metaTileEntity;
        return controller.getEnergyContainer();
    }

    @Override
    protected IItemHandlerModifiable getInputInventory() {
        ParallelRecipeMapMultiblockController controller = (ParallelRecipeMapMultiblockController) this.metaTileEntity;
        return controller.getInputInventory();
    }

    @Override
    protected IItemHandlerModifiable getOutputInventory() {
        ParallelRecipeMapMultiblockController controller = (ParallelRecipeMapMultiblockController) this.metaTileEntity;
        return controller.getOutputInventory();
    }

    @Override
    protected IMultipleTankHandler getInputTank() {
        ParallelRecipeMapMultiblockController controller = (ParallelRecipeMapMultiblockController) this.metaTileEntity;
        return controller.getInputFluidInventory();
    }

    @Override
    protected IMultipleTankHandler getOutputTank() {
        ParallelRecipeMapMultiblockController controller = (ParallelRecipeMapMultiblockController) this.metaTileEntity;
        return controller.getOutputFluidInventory();
    }

    @Override
    protected boolean setupAndConsumeRecipeInputs(Recipe recipe) {
        ParallelRecipeMapMultiblockController controller = (ParallelRecipeMapMultiblockController) this.metaTileEntity;
        if (controller.checkRecipe(recipe, false) &&
                super.setupAndConsumeRecipeInputs(recipe)) {
            controller.checkRecipe(recipe, true);
            return true;
        } else return false;
    }

    @Override
    protected long getEnergyStored() {
        return this.getEnergyContainer().getEnergyStored();
    }

    @Override
    protected long getEnergyCapacity() {
        return this.getEnergyContainer().getEnergyCapacity();
    }

    @Override
    protected boolean drawEnergy(long recipeEUt) {
        long resultEnergy = this.getEnergyStored() - recipeEUt;
        if (resultEnergy >= 0L && resultEnergy <= this.getEnergyCapacity()) {
            this.getEnergyContainer().changeEnergy(-recipeEUt);
            return true;
        } else return false;
    }

    @Override
    protected long getMaxVoltage() {
        return Math.max(this.getEnergyContainer().getInputVoltage(), this.getEnergyContainer().getOutputVoltage());
    }

    /**
     * Used to reset cached values after multiblock structure deforms
     */
    public void invalidate() {
        Arrays.fill(this.lastRecipeIndex, 0);
    }

    protected List<IItemHandlerModifiable> getInputBuses() {
        return this.controller.getAbilities(MultiblockAbility.IMPORT_ITEMS);
    }

    @Override
    protected boolean calculateOverclock(long EUt, int duration) {
        super.calculateOverclock(EUt, duration);
        int numMaintenanceProblems = (this.metaTileEntity instanceof ParallelRecipeMapMultiblockController) ?
                ((ParallelRecipeMapMultiblockController) this.metaTileEntity).getNumProblems() : 0;

        double maintenanceDurationMultiplier = 1.0 + (0.2 * numMaintenanceProblems);
        int durationModified = (int) (this.overclockManager.getDuration() * maintenanceDurationMultiplier);
        this.overclockManager.setDuration(durationModified);
        return true;
    }

    @Override
    protected int getOverclockingTier(long voltage) {
        return GAUtility.getTierByVoltage(voltage);
    }

    @Override
    protected boolean completeRecipe(int i) {
        if (this.metaTileEntity instanceof ParallelRecipeMapMultiblockController) {
            ParallelRecipeMapMultiblockController gaController = (ParallelRecipeMapMultiblockController) this.metaTileEntity;
            //if (gaController.hasMufflerHatch()) {
            //    gaController.outputRecoveryItems();
            //}
            if (gaController.hasMaintenanceHatch()) {
                gaController.calculateMaintenance(this.maxProgressTime[i]);
            }
        }
        return super.completeRecipe(i);
    }

    @Override
    protected boolean trySearchNewRecipe(int i) {
        if (this.metaTileEntity instanceof ParallelRecipeMapMultiblockController) {
            ParallelRecipeMapMultiblockController controller = (ParallelRecipeMapMultiblockController) this.metaTileEntity;
            if (controller.getNumProblems() > 5)
                return false;

            if (controller.isDistinctBus())
                return this.trySearchNewRecipeDistinct(i);

        }
        return this.trySearchNewRecipeCombined(i);

    }

    // TODO May need to do more here
    protected boolean trySearchNewRecipeCombined(int i) {
        return super.trySearchNewRecipe(i);
    }

    protected boolean trySearchNewRecipeDistinct(int i) {
        long maxVoltage = this.maxVoltage.getAsLong();
        Recipe currentRecipe;
        List<IItemHandlerModifiable> importInventory = this.getInputBuses();
        IMultipleTankHandler importFluids = this.getInputTank();

        // Our caching implementation
        // This guarantees that if we get a recipe cache hit, our efficiency is no different from other machines
        Recipe foundRecipe = this.distinct ? this.previousRecipe.get(importInventory.get(this.lastRecipeIndex[i]), importFluids, i, this.occupiedRecipes) : this.previousRecipe.get(importInventory.get(this.lastRecipeIndex[i]), importFluids);
        HashSet<Integer> foundRecipeIndex = new HashSet<>();
        if (foundRecipe != null) {
            currentRecipe = foundRecipe;
            if (this.setupAndConsumeRecipeInputs(currentRecipe, this.lastRecipeIndex[i])) {
                this.occupiedRecipes.set(i, currentRecipe);
                this.setupRecipe(currentRecipe, i);
                return true;
            }
            foundRecipeIndex.add(this.lastRecipeIndex[i]);
        }

        for (int j = 0; j < importInventory.size(); j++) {
            if (j == this.lastRecipeIndex[i]) {
                continue;
            }
            foundRecipe = this.distinct ? this.previousRecipe.get(importInventory.get(this.lastRecipeIndex[i]), importFluids, i, this.occupiedRecipes) : this.previousRecipe.get(importInventory.get(this.lastRecipeIndex[i]), importFluids);
            if (foundRecipe != null) {
                currentRecipe = foundRecipe;
                if (this.setupAndConsumeRecipeInputs(currentRecipe, j)) {
                    this.occupiedRecipes.set(i, currentRecipe);
                    this.setupRecipe(currentRecipe, i);
                    return true;
                }
                foundRecipeIndex.add(j);
            }
        }

        // On a cache miss, our efficiency is much worse, as it will check
        // each bus individually instead of the combined inventory all at once.
        for (int j = 0; j < importInventory.size(); j++) {
            if (foundRecipeIndex.contains(j)) {
                continue;
            }

            IItemHandlerModifiable bus = importInventory.get(j);
            boolean dirty = this.checkRecipeInputsDirty(bus, importFluids, j);
            if (!dirty && !this.forceRecipeRecheck[i]) {
                continue;
            }
            this.forceRecipeRecheck[i] = false;
            currentRecipe = this.findRecipe(maxVoltage, bus, importFluids, this.useOptimizedRecipeLookUp);
            if (currentRecipe == null) {
                continue;
            }
            this.previousRecipe.put(currentRecipe);
            if (!this.setupAndConsumeRecipeInputs(currentRecipe, j)) {
                continue;
            }
            this.occupiedRecipes.set(i, currentRecipe);
            this.lastRecipeIndex[i] = j;
            this.setupRecipe(currentRecipe, i);
            return true;
        }
        return false;
    }

    // Replacing this for optimization reasons
    protected boolean checkRecipeInputsDirty(IItemHandler inputs, IMultipleTankHandler fluidInputs, int index) {
        boolean shouldRecheckRecipe = false;

        if (this.lastItemInputsMatrix == null || this.lastItemInputsMatrix.length != getInputBuses().size()) {
            this.lastItemInputsMatrix = new ItemStack[getInputBuses().size()][];
        }
        if (this.lastItemInputsMatrix[index] == null || this.lastItemInputsMatrix[index].length != inputs.getSlots()) {
            this.lastItemInputsMatrix[index] = new ItemStack[inputs.getSlots()];
            Arrays.fill(this.lastItemInputsMatrix[index], ItemStack.EMPTY);
        }
        if (this.lastFluidInputs == null || this.lastFluidInputs.length != fluidInputs.getTanks()) {
            this.lastFluidInputs = new FluidStack[fluidInputs.getTanks()];
        }
        for (int j = 0; j < this.lastItemInputsMatrix[index].length; j++) {
            ItemStack currentStack = inputs.getStackInSlot(j);
            ItemStack lastStack = this.lastItemInputsMatrix[index][j];
            if (!areItemStacksEqual(currentStack, lastStack)) {
                this.lastItemInputsMatrix[index][j] = currentStack.isEmpty() ? ItemStack.EMPTY : currentStack.copy();
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

    protected boolean setupAndConsumeRecipeInputs(Recipe recipe, int index) {
        ParallelRecipeMapMultiblockController controller = (ParallelRecipeMapMultiblockController) this.metaTileEntity;
        if (controller.checkRecipe(recipe, false)) {

            this.calculateOverclock(recipe.getEUt(), recipe.getDuration());
            long resultEU = this.overclockManager.getEUt();
            long totalEUt = resultEU * this.overclockManager.getDuration();
            IItemHandlerModifiable importInventory = this.getInputBuses().get(index);
            IItemHandlerModifiable exportInventory = this.getOutputInventory();
            IMultipleTankHandler importFluids = this.getInputTank();
            IMultipleTankHandler exportFluids = this.getOutputTank();
            boolean setup = (totalEUt >= 0 ? this.getEnergyStored() >= (totalEUt > this.getEnergyCapacity() / 2 ? resultEU : totalEUt) :
                    (this.getEnergyStored() - resultEU <= this.getEnergyCapacity())) &&
                    MetaTileEntity.addItemsToItemHandler(exportInventory, true, recipe.getAllItemOutputs(exportInventory.getSlots())) &&
                    MetaTileEntity.addFluidsToFluidHandler(exportFluids, true, recipe.getFluidOutputs()) &&
                    recipe.matches(true, importInventory, importFluids);

            if (setup) {
                controller.checkRecipe(recipe, true);
                return true;
            }
        }
        return false;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.lastRecipeIndex = new int[this.getSize()];
    }
}
