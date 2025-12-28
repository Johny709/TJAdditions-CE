package tj.mixin.gregic_probe;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IEnergyContainer;
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
import vfyjxf.gregicprobe.integration.gregtech.EnergyInfoProvider;


@Mixin(value = EnergyInfoProvider.class, remap = false)
public abstract class ElectricContainerInfoProviderMixin {

    @Inject(method = "addProbeInfo(Lgregtech/api/capability/IEnergyContainer;Lmcjty/theoneprobe/api/IProbeInfo;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;)V",
            at = @At("HEAD"), cancellable = true)
    private void injectAddProbeInfo(IEnergyContainer capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing sideHit, CallbackInfo ci) {
        if (!TJConfig.machines.theOneProbeInfoProviderOverrides) return;
        long energyStored = capability.getEnergyStored();
        long maxStorage = capability.getEnergyCapacity();
        if (maxStorage > 0) {
            int energyPercent = (int) Math.floor(energyStored / (maxStorage * 1.0) * 100);
            String displayEnergy = String.format("%s/%s EU | ", TJValues.thousandFormat.format(energyStored), TJValues.thousandFormat.format(maxStorage));
            IProbeInfo horizontalPane = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
            String additionalSpacing = tileEntity.hasCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, sideHit) ? "   " : "";
            horizontalPane.text(TextStyleClass.INFO + "{*gregtech.top.energy_stored*} " + additionalSpacing);
            horizontalPane.progress(energyPercent, 100, probeInfo.defaultProgressStyle()
                    .width((int) (displayEnergy.length() * 6.2))
                    .prefix(displayEnergy)
                    .suffix("%")
                    .borderColor(GregicProbeConfig.borderColorEnergy)
                    .backgroundColor(GregicProbeConfig.backgroundColorEnergy)
                    .alternateFilledColor(GregicProbeConfig.alternateFilledColorEnergy)
                    .filledColor(GregicProbeConfig.filledColorEnergy));
        }
        ci.cancel();
    }
}
