package tj.mixin.gregic_probe;

import gregtech.api.capability.IWorkable;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tj.TJConfig;
import tj.integration.theoneprobe.impl.ElementProgressBar;
import tj.util.Color;
import vfyjxf.gregicprobe.integration.gregtech.WorkableInforProvider;


@Mixin(value = WorkableInforProvider.class, remap = false)
public abstract class MixinWorkableInfoProvider {

    @Inject(method = "addProbeInfo(Lgregtech/api/capability/IWorkable;Lmcjty/theoneprobe/api/IProbeInfo;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;)V",
            at = @At("HEAD"), cancellable = true)
    private void injectAddProbeInfo(IWorkable capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing sideHit, CallbackInfo ci) {
        if (!TJConfig.machines.theOneProbeInfoProviderOverrides) return;
        final double maxProgress = (double) capability.getMaxProgress() / 20;
        final double progress = Math.min(maxProgress, (double) capability.getProgress() / 20);
        if (maxProgress > 0) {
            IProbeInfo progressInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
            progressInfo.element(new ElementProgressBar("{*gregtech.top.progress*}", String.valueOf(progress), String.valueOf(maxProgress),
                    "s", "s", Color.GREEN.toString(), ",##0.00"));
        }
        ci.cancel();
    }
}


