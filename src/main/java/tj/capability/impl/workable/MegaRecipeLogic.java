package tj.capability.impl.workable;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.capability.IGTRecipe;
import tj.capability.impl.handler.IRecipeHandler;
import tj.util.ItemStackHelper;
import tj.util.TJFluidUtils;
import tj.util.TJUtility;

public class MegaRecipeLogic<R extends IRecipeHandler> extends BasicRecipeLogic<R> {
    public MegaRecipeLogic(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected int checkItemInputsAmount(int parallels, Recipe recipe, IItemHandlerModifiable itemInputs) {
        for (CountableIngredient ingredient : ((IGTRecipe) recipe).getMergedItemInputs()) {
            if (ingredient.getCount() > 0) {
                parallels = (int) Math.min(parallels, ItemStackHelper.extractFromItemHandlerByIngredientLong(itemInputs, ingredient.getIngredient(), (long) ingredient.getCount() * parallels, true) / ingredient.getCount());
                if (parallels < 1) return 0;
            } else if (!ItemStackHelper.checkItemHandlerForIngredient(itemInputs, ingredient.getIngredient()))
                return 0;
        }
        return parallels;
    }

    @Override
    protected int checkFluidInputsAmount(int parallels, Recipe recipe) {
        for (FluidStack stack : ((IGTRecipe) recipe).getMergedFluidInputs()) {
            if (stack.amount > 0) {
                parallels = (int) Math.min(parallels, TJFluidUtils.drainFromTanksLong(this.handler.getImportFluidTank(), stack, (long) stack.amount * parallels, false) / stack.amount);
                if (parallels < 1) return 0;
            } else if (!TJFluidUtils.findFluidFromTanks(this.handler.getImportFluidTank(), stack))
                return 0;
        }
        return parallels;
    }

    @Override
    protected void consumeItemInputs(int parallels, Recipe recipe, IItemHandlerModifiable itemInputs) {
        for (CountableIngredient ingredient : ((IGTRecipe) recipe).getMergedItemInputs()) {
            ItemStackHelper.extractFromItemHandlerByIngredientToListLong(itemInputs, ingredient.getIngredient(), (long) ingredient.getCount() * parallels, false, this.itemInputs);
        }
    }

    @Override
    protected void consumeFluidInputs(int parallels, Recipe recipe) {
        for (FluidStack stack : ((IGTRecipe) recipe).getMergedFluidInputs()) {
            long amount = (long) stack.amount * parallels;
            TJFluidUtils.drainFromTanksLong(this.handler.getImportFluidTank(), stack, amount, true);
            for (; amount > 0; amount -= Integer.MAX_VALUE) {
                stack = stack.copy();
                stack.amount = (int) Math.min(Integer.MAX_VALUE, amount);
                this.fluidInputs.add(stack);
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
        int tier = this.handler.getTier() - TJUtility.getTierFromVoltage(this.overclockManager.getEUt());
        for (Recipe.ChanceEntry entry : recipe.getChancedOutputs()) {
            int chance = entry.getChance() + (entry.getBoostPerTier() * tier) / this.overclockManager.getChanceMultiplier() * 100;
            if (Math.random() * 10000 < chance) {
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
