package tj.integration.theoneprobe;

import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import tj.capability.IGeneratorInfo;
import tj.capability.TJCapabilities;

public class IGeneratorInfoProvider extends CapabilityInfoProvider<IGeneratorInfo> {

    @Override
    protected Capability<IGeneratorInfo> getCapability() {
        return TJCapabilities.CAPABILITY_GENERATOR;
    }

    @Override
    protected void addProbeInfo(IGeneratorInfo capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing enumFacing) {
        long consumption = capability.getConsumption();
        long generation = capability.getProduction();
        String[] productionInfo = capability.productionInfo();
        String[] consumptionInfo = capability.consumptionInfo();
        IProbeInfo pageInfo = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));

        this.pageInfo(consumptionInfo, consumption, pageInfo);
        this.pageInfo(productionInfo, generation, pageInfo);
    }

    private void pageInfo(String[] info, long amount, IProbeInfo probeInfo) {
        if (amount < 1 || info == null)
            return;
        StringBuilder prefixBuilder = new StringBuilder(), suffixBuilder = new StringBuilder();
        boolean suffix = false;
        for (String text : info) {
            if (text == null)
                continue;
            if (text.equals("suffix")) {
                suffix = true;
                continue;
            }

            String textInfo = text.startsWith("§") ? text
                    : text.startsWith(" ") ? " "
                    : "{*" + text + "*}";

            if (!suffix)
                prefixBuilder.append(textInfo);
            else
                suffixBuilder.append(textInfo);
        }
        probeInfo.text(TextStyleClass.INFO + prefixBuilder.toString() + String.format("%,d", amount) + suffixBuilder);
    }

    @Override
    public String getID() {
        return "tj:generator_provider";
    }
}
