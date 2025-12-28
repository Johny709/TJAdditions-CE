package tj.mixin.gregic_probe;

import gregtech.api.capability.IWorkable;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tj.TJConfig;
import tj.TJValues;
import vfyjxf.gregicprobe.config.GregicProbeConfig;
import vfyjxf.gregicprobe.integration.gregtech.WorkableInforProvider;


@Mixin(value = WorkableInforProvider.class, remap = false)
public abstract class WorkableInfoProviderMixin {

    @Inject(method = "addProbeInfo(Lgregtech/api/capability/IWorkable;Lmcjty/theoneprobe/api/IProbeInfo;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;)V",
            at = @At("HEAD"), cancellable = true)
    private void injectAddProbeInfo(IWorkable capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing sideHit, CallbackInfo ci) {
        if (!TJConfig.machines.theOneProbeInfoProviderOverrides) return;
        double currentProgress = capability.getProgress();
        double maxProgress = capability.getMaxProgress();
        if (maxProgress > 0) {
            int progressPercent = (int) Math.floor(currentProgress / (maxProgress) * 100);
            String displayProgress = String.format("%ss / %ss | ", TJValues.thousandTwoPlaceFormat.format(currentProgress / 20), TJValues.thousandTwoPlaceFormat.format(maxProgress / 20));
            IProbeInfo progressInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
            progressInfo.text(TextStyleClass.INFO + "{*gregtech.top.progress*} ");
            progressInfo.progress(progressPercent, 100, probeInfo.defaultProgressStyle()
                    .width((int) (displayProgress.length() * 6.2))
                    .prefix(displayProgress)
                    .suffix("%")
                    .borderColor(GregicProbeConfig.borderColorProgress)
                    .backgroundColor(GregicProbeConfig.backgroundColorProgress)
                    .filledColor(GregicProbeConfig.filledColorProgress)
                    .alternateFilledColor(GregicProbeConfig.alternateFilledColorProgress));
        }
        ci.cancel();
    }
}


