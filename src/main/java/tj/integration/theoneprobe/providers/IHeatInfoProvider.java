package tj.integration.theoneprobe.providers;

import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import tj.TJValues;
import tj.capability.IHeatInfo;
import tj.capability.TJCapabilities;
import tj.integration.theoneprobe.impl.ElementTJText;


public class IHeatInfoProvider extends CapabilityInfoProvider<IHeatInfo> {

    @Override
    protected Capability<IHeatInfo> getCapability() {
        return TJCapabilities.CAPABILITY_HEAT;
    }

    @Override
    protected void addProbeInfo(IHeatInfo capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing enumFacing) {
        long heat = capability.heat();
        long maxHeat = capability.maxHeat();
        int progressScaled = maxHeat == 0 ? 0 : (int) Math.floor(heat / (maxHeat * 1.0) * 100);
        String displayHeat = String.format("%s/%s °C | ", TJValues.thousandFormat.format(heat), TJValues.thousandFormat.format(maxHeat));

        IProbeInfo pageInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
        pageInfo.element(new ElementTJText("{*tj.top.progress.heat*}"));
        pageInfo.progress(progressScaled, 100, probeInfo.defaultProgressStyle()
                .width((int) (displayHeat.length() * 6.2))
                .prefix(displayHeat)
                .suffix("%")
                .alternateFilledColor(0xFFF10000)
                .filledColor(0xFFF10000));
    }

    @Override
    public String getID() {
        return "tj:heat_provider";
    }
}
