package tj.mixin.gregtech;

import gregtech.api.capability.IControllable;
import gregtech.api.capability.IWorkable;
import gregtech.integration.theoneprobe.provider.ControllableInfoProvider;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tj.TJConfig;
import tj.capability.IRecipeInfo;

@Mixin(value = ControllableInfoProvider.class, remap = false)
public abstract class ControllableInfoProviderMixin {

    @Inject(method = "addProbeInfo(Lgregtech/api/capability/IControllable;Lmcjty/theoneprobe/api/IProbeInfo;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;)V", at = @At("HEAD"), cancellable = true)
    private void tj_injectAddProbeInfo(IControllable capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing sideHit, CallbackInfo ci) {
        if (!TJConfig.machines.theOneProbeInfoProviderOverrides) return;
        if (capability.isWorkingEnabled()) {
            probeInfo.text(TextStyleClass.INFOIMP + "{*machine.universal.work_paused*}");
        } else if (capability instanceof IRecipeInfo && ((IRecipeInfo) capability).hasProblem()) {
            probeInfo.text(TextStyleClass.INFOIMP + "{*machine.universal.has_problems*}");
        } else if (capability instanceof IWorkable && ((IWorkable) capability).isActive()) {
            probeInfo.text(TextStyleClass.INFOIMP + "{*machine.universal.running*}");
        }
        ci.cancel();
    }
}
