package tj.builder.recipes;

import gregicadditions.recipes.impl.QubitConsumerRecipeBuilder;
import gregicadditions.recipes.impl.RecipeMapAssemblyLine;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import net.minecraftforge.items.IItemHandlerModifiable;
import tj.gui.widgets.impl.SlotScrollableWidgetGroup;

public class RecipeMapLargeAssemblyLine extends RecipeMapAssemblyLine<QubitConsumerRecipeBuilder> {

    public RecipeMapLargeAssemblyLine(String unlocalizedName, int minInputs, int maxInputs, int minOutputs, int maxOutputs, int minFluidInputs, int maxFluidInputs, int minFluidOutputs, int maxFluidOutputs) {
        super(unlocalizedName, minInputs, maxInputs, minOutputs, maxOutputs, minFluidInputs, maxFluidInputs, minFluidOutputs, maxFluidOutputs, new QubitConsumerRecipeBuilder());
    }

    @Override
    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids) {
        SlotScrollableWidgetGroup itemSlotGroup = new SlotScrollableWidgetGroup(32, 18, 72, 72, 4)
                .setScrollWidth(4);
        SlotScrollableWidgetGroup fluidSlotGroup = new SlotScrollableWidgetGroup(10, 18, 18, 72, 1)
                .setScrollWidth(4);
        for (int i = 0; i < 256; i++) {
            itemSlotGroup.addWidget(new SlotWidget(importItems, i, 18 * (i % 4), 18 * (i / 4))
                    .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.MOLD_OVERLAY));
        }
        for (int i = 0; i < 64; i++) {
            fluidSlotGroup.addWidget(new TankWidget(importFluids.getTankAt(i), 0, 18 * i, 18, 18)
                    .setBackgroundTexture(GuiTextures.FLUID_SLOT));
        }
        return ModularUI.builder(GuiTextures.BACKGROUND_EXTENDED, 176, 216)
                .widget(new ProgressWidget(() -> 0, 109, 22, 20, 20, GuiTextures.PROGRESS_BAR_ARROW, ProgressWidget.MoveType.HORIZONTAL))
                .widget(itemSlotGroup)
                .widget(fluidSlotGroup);
    }
}
