package tj.mixin.gregtech;

import gregtech.api.capability.IFuelInfo;
import gregtech.api.capability.IFuelable;
import gregtech.api.capability.impl.ItemFuelInfo;
import gregtech.integration.theoneprobe.provider.FuelableInfoProvider;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tj.TJConfig;
import tj.TJValues;
import tj.integration.theoneprobe.impl.ElementProgressBar;
import tj.util.Color;

import java.util.Collection;
import java.util.Iterator;


@Mixin(value = FuelableInfoProvider.class, remap = false)
public abstract class MixinFuelableInfoProvider {

    @Inject(method = "addProbeInfo(Lgregtech/api/capability/IFuelable;Lmcjty/theoneprobe/api/IProbeInfo;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;)V",
            at = @At(value = "INVOKE_ASSIGN", target = "Lmcjty/theoneprobe/api/IProbeInfo;horizontal(Lmcjty/theoneprobe/api/ILayoutStyle;)Lmcjty/theoneprobe/api/IProbeInfo;", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void injectAddProbeInfo(IFuelable capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing sideHit, CallbackInfo ci,
                                    Collection<IFuelInfo> fuels, Iterator<IFuelInfo> var6, IFuelInfo fuelInfo, final String fuelName, final int fuelRemaining, final int fuelCapacity, final int fuelMinConsumed, final long burnTime, IProbeInfo horizontalPane) {
        if (!TJConfig.machines.theOneProbeInfoProviderOverrides) return;
        final double burnTimePrecise = fuelInfo.getFuelBurnTimeLong() / 20.0;
        if (fuelInfo instanceof ItemFuelInfo) {
            horizontalPane.text(TextStyleClass.INFO + "{*gregtech.top.fuel_name*} §b").itemLabel(((ItemFuelInfo) fuelInfo).getItemStack());
        } else horizontalPane.text(TextStyleClass.INFO + "{*gregtech.top.fuel_name*} §7(§b{*" + fuelName + "*}§7)");
        probeInfo.element(new ElementProgressBar("", String.valueOf(fuelRemaining), String.valueOf(fuelCapacity), "", "", Color.GOLD.toString(), ",###"));
        if (fuelRemaining < fuelMinConsumed)
            horizontalPane.text(" §r{*gregtech.top.fuel_min_consume*}§b " + TJValues.thousandTwoPlaceFormat.format(fuelMinConsumed));
        else horizontalPane.text(" §r{*gregtech.top.fuel_burn*}§b " + TJValues.thousandTwoPlaceFormat.format(burnTimePrecise) + " §r{*gregtech.top.fuel_time*}");
        ci.cancel();
    }
}
