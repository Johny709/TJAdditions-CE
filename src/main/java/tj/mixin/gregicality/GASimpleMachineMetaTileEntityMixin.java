package tj.mixin.gregicality;

import gregicadditions.Gregicality;
import gregicadditions.machines.overrides.GASimpleMachineMetaTileEntity;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.DischargerSlotWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tj.TJConfig;
import tj.capability.IRecipeMap;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.TJProgressBarWidget;
import tj.gui.widgets.impl.RecipeOutputDisplayWidget;
import tj.mixin.gregtech.IAbstractRecipeLogicMixin;

@Mixin(value = GASimpleMachineMetaTileEntity.class, remap = false)
public abstract class GASimpleMachineMetaTileEntityMixin extends GAWorkableTieredMetaTileEntityMixin {

    @Shadow
    private ItemStackHandler chargerInventory;

    public GASimpleMachineMetaTileEntityMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Shadow
    public abstract boolean isAutoOutputItems();

    @Shadow
    public abstract void setAutoOutputItems(boolean autoOutputItems);

    @Shadow
    public abstract boolean isAutoOutputFluids();

    @Shadow
    public abstract void setAutoOutputFluids(boolean autoOutputFluids);

    @Inject(method = "createGuiTemplate", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void injectCreateGUITemplate(EntityPlayer player, CallbackInfoReturnable<ModularUI.Builder> cir,
                                         ModularUI.Builder builder, int leftButtonStartX, int rightButtonStartX) {
        if (!TJConfig.machines.multiblockUIOverrides) return;
        RecipeOutputDisplayWidget displayWidget = new RecipeOutputDisplayWidget(77, 22, 21, 20)
                .setFluidOutputSupplier(((IAbstractRecipeLogicMixin) this.workable)::getFluidOutputs)
                .setItemOutputSupplier(((IAbstractRecipeLogicMixin) this.workable)::getItemOutputs)
                .setItemOutputInventorySupplier(this::getExportItems)
                .setFluidOutputTankSupplier(this::getExportFluids);
        ModularUI.Builder newBuilder = ((IRecipeMap) this.workable.recipeMap).createUITemplateAdvanced(this.workable::getProgressPercent, this.importItems, this.exportItems, this.importFluids, this.exportFluids, displayWidget)
                .image(-28, 0, 26, 86, GuiTextures.BORDERED_BACKGROUND)
                .image(-28, 138, 26, 26, GuiTextures.BORDERED_BACKGROUND)
                .widget(new TJLabelWidget(7, -18, 166, 18, TJGuiTextures.MACHINE_LABEL, () -> Gregicality.MODID + ":" + this.workable.recipeMap.getUnlocalizedName())
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(new TJProgressBarWidget(-24, 4, 18, 78, () -> this.energyContainer.getEnergyStored(), () -> this.energyContainer.getEnergyCapacity(), ProgressWidget.MoveType.VERTICAL)
                        .setLocale("tj.multiblock.bars.energy", null)
                        .setBarTexture(TJGuiTextures.BAR_YELLOW)
                        .setTexture(TJGuiTextures.FLUID_BAR)
                        .setInverted(true))
                .widget(new DischargerSlotWidget(this.chargerInventory, 0, 79, 62)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY))
                .widget(new ImageWidget(79, 42, 18, 18, GuiTextures.INDICATOR_NO_ENERGY)
                        .setPredicate(this.workable::isHasNotEnoughEnergy))
                .widget(new ToggleButtonWidget(-24, 142, 18, 18, TJGuiTextures.POWER_BUTTON, this.workable::isWorkingEnabled, this.workable::setWorkingEnabled)
                        .setTooltipText("machine.universal.toggle.run.mode"))
                .widget(new ToggleButtonWidget(rightButtonStartX, 60, 20, 20,
                        GuiTextures.BUTTON_OVERCLOCK, this.workable::isAllowOverclocking, this.workable::setAllowOverclocking)
                        .setTooltipText("gregtech.gui.overclock"))
                .bindPlayerInventory(player.inventory)
                .widget(displayWidget);

        leftButtonStartX = 7;
        if (this.workable.recipeMap instanceof SimpleMachineMetaTileEntity.RecipeMapWithConfigButton) {
            leftButtonStartX += ((SimpleMachineMetaTileEntity.RecipeMapWithConfigButton) workable.recipeMap).getLeftButtonOffset();
        }
        if (this.exportItems.getSlots() > 0) {
            newBuilder.widget(new ToggleButtonWidget(leftButtonStartX, 62, 18, 18,
                    GuiTextures.BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setAutoOutputItems)
                    .setTooltipText("gregtech.gui.item_auto_output.tooltip"));
            leftButtonStartX += 18;
        }
        if (this.exportFluids.getTanks() > 0) {
            newBuilder.widget(new ToggleButtonWidget(leftButtonStartX, 62, 18, 18,
                    GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setAutoOutputFluids)
                    .setTooltipText("gregtech.gui.fluid_auto_output.tooltip"));
        }
        cir.setReturnValue(newBuilder);
    }
}
