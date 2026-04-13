package tj.capability.impl.workable;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.capability.IGTRecipe;
import tj.capability.impl.handler.IRecipeHandler;
import tj.util.TJItemUtils;
import tj.util.TJFluidUtils;
import tj.util.TJUtility;
import tj.util.wrappers.GTFluidStackWrapper;
import tj.util.wrappers.GTIngredientWrapper;

public class MegaRecipeLogic<R extends IRecipeHandler> extends BasicRecipeLogic<R> {

    public MegaRecipeLogic(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected int checkItemInputsAmount(int parallels, Recipe recipe, IItemHandlerModifiable itemInputs) {
        for (GTIngredientWrapper ingredient : ((IGTRecipe) recipe).getMergedItemInputs()) {
            if (ingredient.getCountLong() > 0) {
                parallels = (int) Math.min(parallels, TJItemUtils.extractFromItemHandlerByIngredientLong(itemInputs, ingredient.getIngredient(), ingredient.getCountLong() * parallels, true) / ingredient.getCountLong());
                if (parallels < 1) return 0;
            } else if (!TJItemUtils.checkItemHandlerForIngredient(itemInputs, ingredient.getIngredient()))
                return 0;
        }
        return parallels;
    }

    @Override
    protected int checkFluidInputsAmount(int parallels, Recipe recipe) {
        for (GTFluidStackWrapper stack : ((IGTRecipe) recipe).getMergedFluidInputs()) {
            if (stack.getCountLong() > 0) {
                parallels = (int) Math.min(parallels, TJFluidUtils.drainFromTanksLong(this.handler.getImportFluidTank(), stack.getFluidStack(), stack.getCountLong() * parallels, false) / stack.getCountLong());
                if (parallels < 1) return 0;
            } else if (!TJFluidUtils.findFluidFromTanks(this.handler.getImportFluidTank(), stack.getFluidStack()))
                return 0;
        }
        return parallels;
    }

    @Override
    protected void consumeItemInputs(int parallels, Recipe recipe, IItemHandlerModifiable itemInputs) {
        for (GTIngredientWrapper ingredient : ((IGTRecipe) recipe).getMergedItemInputs()) {
            TJItemUtils.extractFromItemHandlerByIngredientToListLong(itemInputs, ingredient.getIngredient(), ingredient.getCountLong() * parallels, false, this.itemInputs);
        }
    }

    @Override
    protected void consumeFluidInputs(int parallels, Recipe recipe) {
        for (GTFluidStackWrapper stack : ((IGTRecipe) recipe).getMergedFluidInputs()) {
            FluidStack fluidStack = stack.getFluidStack();
            long amount = stack.getCountLong() * parallels;
            TJFluidUtils.drainFromTanksLong(this.handler.getImportFluidTank(), fluidStack, amount, true);
            for (; amount > 0; amount -= Integer.MAX_VALUE) {
                fluidStack = fluidStack.copy();
                fluidStack.amount = (int) Math.min(Integer.MAX_VALUE, amount);
                this.fluidInputs.add(fluidStack);
            }
        }
    }

    @Override
    protected void addItemOutputs(int parallels, Recipe recipe) {
        for (ItemStack stack : recipe.getOutputs()) {
            long amount = (long) stack.getCount() * parallels;
            for (; amount > 0; amount -= Integer.MAX_VALUE) {
                stack = stack.copy();
                stack.setCount((int) Math.min(Integer.MAX_VALUE, amount));
                this.itemOutputs.add(stack);
            }
        }
    }

    @Override
    protected void addChancedOutputs(int parallels, Recipe recipe) {
        final int tier = this.handler.getTier() - TJUtility.getTierFromVoltage(this.overclockManager.getEUt());
        for (Recipe.ChanceEntry entry : recipe.getChancedOutputs()) {
            final int chance = entry.getChance() + (entry.getBoostPerTier() * tier) / this.overclockManager.getChanceMultiplier() * 100;
            if (this.metaTileEntity.getWorld().rand.nextInt(10000) < chance) {
                ItemStack stack = entry.getItemStack();
                long amount = (long) stack.getCount() * parallels;
                for (; amount > 0; amount -= Integer.MAX_VALUE) {
                    stack = stack.copy();
                    stack.setCount((int) Math.min(Integer.MAX_VALUE, amount));
                    this.itemOutputs.add(stack);
                }
            }
        }
    }

    @Override
    protected void addFluidOutputs(int parallels, Recipe recipe) {
        for (FluidStack stack : recipe.getFluidOutputs()) {
            long amount = (long) stack.amount * parallels;
            for (; amount > 0; amount -= Integer.MAX_VALUE) {
                stack = stack.copy();
                stack.amount = (int) Math.min(Integer.MAX_VALUE, amount);
                this.fluidOutputs.add(stack);
            }
        }
    }
}
