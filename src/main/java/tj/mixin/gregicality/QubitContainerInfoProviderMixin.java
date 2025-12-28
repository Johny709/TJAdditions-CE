package tj.mixin.gregicality;

import gregicadditions.capabilities.IQubitContainer;
import gregicadditions.theoneprobe.QubitContainerInfoProvider;
import gregtech.api.capability.GregtechTileCapabilities;
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

@Mixin(value = QubitContainerInfoProvider.class, remap = false)
public abstract class QubitContainerInfoProviderMixin {

    @Inject(method = "addProbeInfo(Lgregicadditions/capabilities/IQubitContainer;Lmcjty/theoneprobe/api/IProbeInfo;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;)V",
            at = @At("HEAD"), cancellable = true)
    private void injectAddProbeInfo(IQubitContainer capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing sideHit, CallbackInfo ci) {
        if (!TJConfig.machines.theOneProbeInfoProviderOverrides) return;
        long qubitStored = capability.getQubitStored();
        long qubitCapacity = capability.getQubitStored();
        if (qubitCapacity > 0) {
            int qubitPercent = (int) Math.floor(qubitStored / (qubitCapacity * 1.0) * 100);
            String displayQubit = String.format("%s/%s Qubit | ", TJValues.thousandFormat.format(qubitStored), TJValues.thousandFormat.format(qubitCapacity));
            IProbeInfo horizontalPane = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
            String additionalSpacing = tileEntity.hasCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, sideHit) ? "   " : "";
            horizontalPane.text(TextStyleClass.INFO + "{*gtaddition.top.qubit_stored*} " + additionalSpacing);
            horizontalPane.progress(qubitPercent, 100, probeInfo.defaultProgressStyle()
                    .width((int) (displayQubit.length() * 6.2))
                    .prefix(displayQubit)
                    .suffix("%")
                    .borderColor(0x00000000)
                    .backgroundColor(0x00000000)
                    .alternateFilledColor(0xFFFFE000)
                    .filledColor(0xFFEED000));
        }
        ci.cancel();
    }
}
