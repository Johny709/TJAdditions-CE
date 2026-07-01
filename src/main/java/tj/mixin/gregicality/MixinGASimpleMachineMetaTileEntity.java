package tj.mixin.gregicality;

import gregicadditions.Gregicality;
import gregicadditions.machines.overrides.GASimpleMachineMetaTileEntity;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.DischargerSlotWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.common.gui.widget.GhostCircuitWidget;
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
import tj.mui.TJGuiTextures;
import tj.mui.widgets.impl.*;
import tj.mixin.gregtech.IMixinAbstractRecipeLogic;
import tj.util.TJItemUtils;

@Mixin(value = GASimpleMachineMetaTileEntity.class, remap = false)
public abstract class MixinGASimpleMachineMetaTileEntity extends MixinGAWorkableTieredMetaTileEntity {

    @Shadow
    private ItemStackHandler chargerInventory;

    public MixinGASimpleMachineMetaTileEntity(ResourceLocation metaTileEntityId) {
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
                .setFluidOutputSupplier(((IMixinAbstractRecipeLogic) this.workable)::getFluidOutputs)
                .setItemOutputSupplier(((IMixinAbstractRecipeLogic) this.workable)::getItemOutputs)
                .setItemOutputInventorySupplier(this::getExportItems)
                .setFluidOutputTankSupplier(this::getExportFluids);
        ModularUI.Builder newBuilder = ((IRecipeMap) this.workable.recipeMap).createUITemplateAdvanced(this.workable::getProgressPercent, this.importItems, this.exportItems, this.importFluids, this.exportFluids, displayWidget)
                .image(-28, 0, 26, 104, GuiTextures.BORDERED_BACKGROUND)
                .image(-28, 138, 26, 26, GuiTextures.BORDERED_BACKGROUND)
                .image(-28, 108, 26, 26, GuiTextures.BORDERED_BACKGROUND)
                .widget(new TJLabelWidget(7, -18, 162, 18, TJGuiTextures.MACHINE_LABEL_2, () -> Gregicality.MODID + ":" + this.workable.recipeMap.getUnlocalizedName())
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(new TJProgressBarWidget(-24, 4, 18, 78, this.energyContainer::getEnergyStored, this.energyContainer::getEnergyCapacity, ProgressWidget.MoveType.VERTICAL)
                        .setLocale("tj.multiblock.bars.energy", null)
                        .setBarTexture(TJGuiTextures.BAR_YELLOW)
                        .setTexture(TJGuiTextures.FLUID_BAR)
                        .setInverted(true))
                .widget(new DischargerSlotWidget(this.chargerInventory, 0, -24, 82)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY))
                .widget(new ImageWidget(79, 42, 18, 18, GuiTextures.INDICATOR_NO_ENERGY)
                        .setPredicate(this.workable::isHasNotEnoughEnergy))
                .widget(new TJToggleButtonWidget(-24, 142, 18, 18, TJGuiTextures.TOGGLE_POWER_BUTTON, this.workable::isWorkingEnabled, this.workable::setWorkingEnabled)
                        .setToggleTitleTooltipHoverText("machine.universal.toggle.run.mode.disabled", "machine.universal.toggle.run.mode.enabled"))
                .widget(new TJToggleButtonWidget(rightButtonStartX, 60, 20, 20,
                        GuiTextures.BUTTON_OVERCLOCK, this.workable::isAllowOverclocking, this.workable::setAllowOverclocking)
                        .setToggleTitleTooltipHoverText("gregtech.gui.overclock.disabled", "gregtech.gui.overclock.enabled"))
                .widget(new GhostCircuitWidget(this.ghostCircuitInventory, 133, 62))
                .bindPlayerInventory(player.inventory)
                .widget(displayWidget)
                .widget(new ButtonPopUpWidget<>()
                        .addPopup(widgetGroup -> true)
                        .addPopup(new TJToggleButtonWidget(-24, 112, 18, 18)
                                .setItemDisplay(TJItemUtils.getItemStackFromName("enderio:item_material", 1, 11))
                                .setToggleTexture(GuiTextures.TOGGLE_BUTTON_BACK)
                                .useToggleTexture(true), widgetGroup -> {
                            final GASimpleMachineMetaTileEntity simpleMachineMetaTile = (GASimpleMachineMetaTileEntity) (Object) this;
                            widgetGroup.addWidget(new WorldSceneRenderWidget(4, 4, 168, 76, simpleMachineMetaTile)
                                    .setBackgroundTexture(TJGuiTextures.MULTIBLOCK_DISPLAY_BASE));
                            return false;
                        }));

        leftButtonStartX = 7;
        if (this.workable.recipeMap instanceof SimpleMachineMetaTileEntity.RecipeMapWithConfigButton) {
            leftButtonStartX += ((SimpleMachineMetaTileEntity.RecipeMapWithConfigButton) workable.recipeMap).getLeftButtonOffset();
        }
        if (this.exportItems.getSlots() > 0) {
            newBuilder.widget(new TJToggleButtonWidget(leftButtonStartX, 62, 18, 18,
                    GuiTextures.BUTTON_ITEM_OUTPUT, this::isAutoOutputItems, this::setAutoOutputItems)
                    .setToggleTitleTooltipHoverText("gregtech.gui.item_auto_output.tooltip.disabled", "gregtech.gui.item_auto_output.tooltip.enabled"));
            leftButtonStartX += 18;
        }
        if (this.exportFluids.getTanks() > 0) {
            newBuilder.widget(new TJToggleButtonWidget(leftButtonStartX, 62, 18, 18,
                    GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setAutoOutputFluids)
                    .setToggleTitleTooltipHoverText("gregtech.gui.fluid_auto_output.tooltip.disabled", "gregtech.gui.fluid_auto_output.tooltip.emabled"));
        }
        cir.setReturnValue(newBuilder);
    }
}
