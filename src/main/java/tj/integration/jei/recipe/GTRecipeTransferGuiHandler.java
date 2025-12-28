package tj.integration.jei.recipe;

import gregtech.api.gui.impl.ModularUIContainer;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import tj.gui.widgets.IGTRecipeTransferHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class GTRecipeTransferGuiHandler implements IRecipeTransferHandler<ModularUIContainer> {

    private final IRecipeTransferHandlerHelper transferHelper;

    public GTRecipeTransferGuiHandler(IRecipeTransferHandlerHelper transferHelper) {
        this.transferHelper = transferHelper;
    }

    @Nonnull
    @Override
    public Class<ModularUIContainer> getContainerClass() {
        return ModularUIContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(ModularUIContainer container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        Optional<IGTRecipeTransferHandler> transferHandler = container.getModularUI()
                .getFlatVisibleWidgetCollection().stream()
                .filter(widget -> widget instanceof IGTRecipeTransferHandler)
                .map(widget -> (IGTRecipeTransferHandler) widget)
                .findFirst();
        if (!transferHandler.isPresent()) {
            return transferHelper.createInternalError();
        }
        Map<Integer, IGuiIngredient<ItemStack>> itemGroup = new HashMap<>(recipeLayout.getItemStacks().getGuiIngredients());
        itemGroup.values().removeIf(it -> it.getAllIngredients().isEmpty());

        Map<Integer, IGuiIngredient<FluidStack>> fluidGroup = new HashMap<>(recipeLayout.getFluidStacks().getGuiIngredients());
        fluidGroup.values().removeIf(it -> it.getAllIngredients().isEmpty());

        String errorTooltip = transferHandler.get().transferRecipe(container, itemGroup, fluidGroup, player, maxTransfer, doTransfer);
        if (errorTooltip == null) {
            return null;
        }
        return transferHelper.createUserErrorWithTooltip(errorTooltip);
    }
}
