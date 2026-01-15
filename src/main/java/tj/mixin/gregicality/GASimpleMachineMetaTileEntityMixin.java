package tj.mixin.gregicality;

import gregicadditions.Gregicality;
import gregicadditions.machines.overrides.GASimpleMachineMetaTileEntity;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tj.TJConfig;
import tj.gui.TJGuiTextures;
import tj.gui.widgets.TJLabelWidget;
import tj.gui.widgets.TJProgressBarWidget;

@Mixin(value = GASimpleMachineMetaTileEntity.class, remap = false)
public abstract class GASimpleMachineMetaTileEntityMixin extends GAWorkableTieredMetaTileEntityMixin {
    public GASimpleMachineMetaTileEntityMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Inject(method = "createGuiTemplate", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void injectCreateGUITemplate(EntityPlayer player, CallbackInfoReturnable<ModularUI.Builder> cir,
                                         ModularUI.Builder builder, int leftButtonStartX, int rightButtonStartX) {
        if (!TJConfig.machines.multiblockUIOverrides) return;
        builder.image(3, 5, 170, 18, TJGuiTextures.UI_COVER)
                .widget(new TJLabelWidget(7, -18, 166, 20, TJGuiTextures.MACHINE_LABEL, () -> Gregicality.MODID + ":" + this.workable.recipeMap.getUnlocalizedName())
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(new TJProgressBarWidget(7, 5, 18, 56, () -> this.energyContainer.getEnergyStored(), () -> this.energyContainer.getEnergyCapacity(), ProgressWidget.MoveType.VERTICAL)
                        .setLocale("tj.multiblock.bars.energy", null)
                        .setBarTexture(TJGuiTextures.BAR_YELLOW)
                        .setTexture(TJGuiTextures.FLUID_BAR));
    }
}
