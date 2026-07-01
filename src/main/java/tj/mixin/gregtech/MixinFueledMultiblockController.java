package tj.mixin.gregtech;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FuelRecipeLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.recipes.machines.FuelRecipeMap;
import gregtech.common.metatileentities.multi.electric.generator.FueledMultiblockController;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.capability.impl.workable.TJFuelRecipeLogic;
import tj.mui.TJGuiTextures;
import tj.mui.widgets.impl.TJToggleButtonWidget;

import java.util.List;

import static tj.mui.TJGuiTextures.TOGGLE_POWER_BUTTON;

@Mixin(value = FueledMultiblockController.class, remap = false)
public abstract class MixinFueledMultiblockController extends MixinMultiblockWithDisplayBase implements IMixinFueledMultiblockController {

    @Shadow
    protected FuelRecipeLogic workableHandler;

    @Shadow
    protected IMultipleTankHandler importFluidHandler;

    @Shadow
    protected IEnergyContainer energyContainer;

    @Shadow
    @Final
    protected FuelRecipeMap recipeMap;

    @Shadow
    protected abstract void addDisplayText(List<ITextComponent> textList);

    public MixinFueledMultiblockController(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void addMainDisplayTab(List<Widget> widgetGroup) {
        super.addMainDisplayTab(widgetGroup);
        FuelRecipeLogic recipeLogic = this.getFuelRecipeLogic();
        widgetGroup.add(new TJToggleButtonWidget(175, 169, 18, 18, TOGGLE_POWER_BUTTON, recipeLogic::isWorkingEnabled, recipeLogic::setWorkingEnabled)
                .setToggleTitleTooltipHoverText("machine.universal.toggle.run.mode.disabled", "machine.universal.toggle.run.mode.enabled"));
        if (recipeLogic instanceof TJFuelRecipeLogic) {
            widgetGroup.add(new TJToggleButtonWidget(175, 151, 18, 18, ((TJFuelRecipeLogic) recipeLogic)::isVoidEnergy, ((TJFuelRecipeLogic) recipeLogic)::setVoidEnergy)
                    .setDynamicTooltipText(() -> ((TJFuelRecipeLogic) recipeLogic).isVoidEnergy() ? "machine.universal.toggle.energy_voiding.enabled" : "machine.universal.toggle.energy_voiding.disabled")
                    .setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                    .setBackgroundTextures(TJGuiTextures.ENERGY_VOID)
                    .useToggleTexture(true));
        }
    }

    @Override
    public String getJEIRecipeUid() {
        return GTValues.MODID + ":" + this.recipeMap.getUnlocalizedName();
    }
}
