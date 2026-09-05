package tj.mixin.gregic_probe;

import gregtech.api.capability.IEnergyContainer;
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
import vfyjxf.gregicprobe.integration.gregtech.EnergyInfoProvider;


@Mixin(value = EnergyInfoProvider.class, remap = false)
public abstract class MixinElectricContainerInfoProvider {

    @Inject(method = "addProbeInfo(Lgregtech/api/capability/IEnergyContainer;Lmcjty/theoneprobe/api/IProbeInfo;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;)V",
            at = @At("HEAD"), cancellable = true)
    private void injectAddProbeInfo(IEnergyContainer capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing sideHit, CallbackInfo ci) {
        if (!TJConfig.machines.theOneProbeInfoProviderOverrides) return;
        final long energyStored = capability.getEnergyStored();
        final long energyCapacity = capability.getEnergyCapacity();
        if (energyCapacity > 0) {
            final IProbeInfo horizontalPane = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
            horizontalPane.element(new ElementProgressBar("{*gregtech.top.energy_stored*}", String.valueOf(energyStored),
                    String.valueOf(energyCapacity), " EU", " EU", Color.YELLOW.toString(), ",###"));
        }
        ci.cancel();
    }
}
