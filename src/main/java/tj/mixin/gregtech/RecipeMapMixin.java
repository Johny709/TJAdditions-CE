package tj.mixin.gregtech;

import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.recipes.RecipeMap;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.capability.IRecipeMap;
import tj.gui.widgets.impl.RecipeOutputDisplayWidget;
import tj.gui.widgets.impl.RecipeOutputSlotWidget;

import java.util.function.DoubleSupplier;

@Mixin(value = RecipeMap.class, remap = false)
public abstract class RecipeMapMixin implements IRecipeMap {

    @Shadow
    protected TextureArea progressBarTexture;

    @Shadow
    protected ProgressWidget.MoveType moveType;

    @Shadow
    protected static int[] determineSlotsGrid(int itemInputsCount) {
        return null;
    }

    @Shadow
    protected abstract void addSlot(ModularUI.Builder builder, int x, int y, int slotIndex, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, boolean isFluid, boolean isOutputs);

    @Override
    public ModularUI.Builder createUITemplateAdvanced(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, RecipeOutputDisplayWidget displayWidget) {
        ModularUI.Builder builder = ModularUI.defaultBuilder();
        builder.widget(new ProgressWidget(progressSupplier, 77, 22, 21, 20, this.progressBarTexture, this.moveType));
        this.addInventorySlotGroupAdvanced(builder, importItems, importFluids, false, displayWidget);
        this.addInventorySlotGroupAdvanced(builder, exportItems, exportFluids, true, displayWidget);
        return builder;
    }

    @Override
    public void addInventorySlotGroupAdvanced(ModularUI.Builder builder, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, boolean isOutputs, RecipeOutputDisplayWidget displayWidget) {
        int itemInputsCount = itemHandler.getSlots();
        int fluidInputsCount = fluidHandler.getTanks();
        boolean invertFluids = false;
        if (itemInputsCount == 0) {
            int tmp = itemInputsCount;
            itemInputsCount = fluidInputsCount;
            fluidInputsCount = tmp;
            invertFluids = true;
        }
        int[] inputSlotGrid = determineSlotsGrid(itemInputsCount);
        int itemSlotsToLeft = inputSlotGrid[0];
        int itemSlotsToDown = inputSlotGrid[1];
        int startInputsX = isOutputs ? 106 : 69 - itemSlotsToLeft * 18;
        int startInputsY = 32 - (int) (itemSlotsToDown / 2.0 * 18);
        for (int i = 0; i < itemSlotsToDown; i++) {
            for (int j = 0; j < itemSlotsToLeft; j++) {
                int slotIndex = i * itemSlotsToLeft + j;
                int x = startInputsX + 18 * j;
                int y = startInputsY + 18 * i;
                addSlot(builder, x, y, slotIndex, itemHandler, fluidHandler, invertFluids, isOutputs);
                if (isOutputs) {
                    builder.widget(new RecipeOutputSlotWidget(slotIndex, x, y, 18, 18, displayWidget::getItemAt, null));
                }
            }
        }
        if (fluidInputsCount > 0 || invertFluids) {
            if (itemSlotsToDown >= fluidInputsCount && itemSlotsToLeft < 3) {
                int startSpecX = isOutputs ? startInputsX + itemSlotsToLeft * 18 : startInputsX - 18;
                for (int i = 0; i < fluidInputsCount; i++) {
                    int y = startInputsY + 18 * i;
                    addSlot(builder, startSpecX, y, i, itemHandler, fluidHandler, !invertFluids, isOutputs);
                    if (isOutputs) {
                        builder.widget(new RecipeOutputSlotWidget(i, startSpecX, y, 18, 18, null, displayWidget::getFluidAt));
                    }
                }
            } else {
                int startSpecY = startInputsY + itemSlotsToDown * 18;
                for (int i = 0; i < fluidInputsCount; i++) {
                    int x = isOutputs ? startInputsX + 18 * (i % 3) : startInputsX + itemSlotsToLeft * 18 - 18 - 18 * (i % 3);
                    int y = startSpecY + (i / 3) * 18;
                    addSlot(builder, x, y, i, itemHandler, fluidHandler, !invertFluids, isOutputs);
                    if (isOutputs) {
                        builder.widget(new RecipeOutputSlotWidget(i, x, y, 18, 18, null, displayWidget::getFluidAt));
                    }
                }
            }
        }
    }
}
