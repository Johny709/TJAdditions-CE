package tj.integration.theoneprobe.providers;

import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import tj.capability.IHeatInfo;
import tj.capability.TJCapabilities;
import tj.integration.theoneprobe.impl.ElementProgressBar;
import tj.util.Color;


public class IHeatInfoProvider extends CapabilityInfoProvider<IHeatInfo> {

    @Override
    protected Capability<IHeatInfo> getCapability() {
        return TJCapabilities.CAPABILITY_HEAT;
    }

    @Override
    protected void addProbeInfo(IHeatInfo capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing enumFacing) {
        final String heat = String.valueOf(capability.heat());
        final String maxHeat = String.valueOf(capability.maxHeat());

        final IProbeInfo pageInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
        pageInfo.element(new ElementProgressBar("{*tj.top.progress.heat*}", heat, maxHeat, "°C", "°C", Color.RED.toString(), ",###"));
    }

    @Override
    public String getID() {
        return "tj:heat_provider";
    }
}
