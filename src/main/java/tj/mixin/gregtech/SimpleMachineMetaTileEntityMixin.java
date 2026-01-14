package tj.mixin.gregtech;

import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
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

@Mixin(value = SimpleMachineMetaTileEntity.class, remap = false)
public abstract class SimpleMachineMetaTileEntityMixin extends WorkableTieredMetaTileEntityMixin {

    public SimpleMachineMetaTileEntityMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Inject(method = "createGuiTemplate", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void injectCreateUITemplate(EntityPlayer player, CallbackInfoReturnable<ModularUI.Builder> ci,
                                        ModularUI.Builder builder, int leftButtonStartX, int rightButtonStartX) {
        if (!TJConfig.machines.multiblockUIOverrides) return;
        builder.image(7, 5, 162, 18, TJGuiTextures.UI_COVER)
                .widget(new TJLabelWidget(7, -18, 166, 18, TJGuiTextures.MACHINE_LABEL)
                        .setItemLabel(this.getStackForm()).setLocale(this.getMetaFullName()))
                .widget(new TJProgressBarWidget(7, 5, 18, 56, () -> this.energyContainer.getEnergyStored(), () -> this.energyContainer.getEnergyCapacity(), ProgressWidget.MoveType.VERTICAL)
                        .setTexture(TJGuiTextures.FLUID_BAR)
                        .setLocale("tj.multiblock.bars.energy", null)
                        .setBarTexture(TJGuiTextures.BAR_YELLOW));
    }
}
