package tj.mixin.gregtech;

import gregicadditions.Gregicality;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.RecipeMap;
import gregtech.common.ConfigHolder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.builder.multicontrollers.UIDisplayBuilder;

import java.util.List;

import static tj.gui.TJGuiTextures.POWER_BUTTON;

@Mixin(value = RecipeMapMultiblockController.class, remap = false)
public abstract class RecipeMapMultiblockControllerMixin extends MultiblockWithDisplayBaseMixin {

    @Shadow
    protected IEnergyContainer energyContainer;

    @Shadow
    protected MultiblockRecipeLogic recipeMapWorkable;

    @Shadow
    public abstract IEnergyContainer getEnergyContainer();

    @Shadow
    protected IItemHandlerModifiable inputInventory;

    @Shadow
    protected IMultipleTankHandler inputFluidInventory;

    @Shadow
    protected IItemHandlerModifiable outputInventory;

    @Shadow
    protected IMultipleTankHandler outputFluidInventory;

    @Shadow
    @Final
    public RecipeMap<?> recipeMap;

    @Shadow
    public abstract IMultipleTankHandler getInputFluidInventory();

    public RecipeMapMultiblockControllerMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void addMainDisplayTab(List<Widget> widgetGroup) {
        super.addMainDisplayTab(widgetGroup);
        widgetGroup.add(new ToggleButtonWidget(175, 169, 18, 18, POWER_BUTTON, this.recipeMapWorkable::isWorkingEnabled, this.recipeMapWorkable::setWorkingEnabled)
                .setTooltipText("machine.universal.toggle.run.mode"));
    }

    @Override
    protected void configureDisplayText(UIDisplayBuilder builder) {
        super.configureDisplayText(builder);
        if (!this.isStructureFormed()) return;
        builder.voltageInLine(this.energyContainer)
                .energyInputLine(this.getEnergyContainer(), this.recipeMapWorkable.getRecipeEUt())
                .customLine(text -> {
                    if (ConfigHolder.debug_options_for_caching) {
                        text.addTextComponent(new TextComponentString(String.format("Cache size (%s) hit (%s) miss (%s)", this.recipeMapWorkable.previousRecipe.getCachedRecipeCount(), this.recipeMapWorkable.previousRecipe.getCacheHit(), this.recipeMapWorkable.previousRecipe.getCacheMiss()))
                                .setStyle(new Style().setColor(TextFormatting.WHITE)));
                    }
                }).isWorkingLine(this.recipeMapWorkable.isWorkingEnabled(), this.recipeMapWorkable.isActive(), this.recipeMapWorkable.getProgress(), this.recipeMapWorkable.getMaxProgress(), 999)
                .addRecipeOutputLine(this.recipeMapWorkable, 1000);
    }

    @Override
    public String getJEIRecipeUid() {
        return Gregicality.MODID + ":" + this.recipeMap.getUnlocalizedName();
    }
}
