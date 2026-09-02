package tj.integration.theoneprobe.providers;

import gregicadditions.GAValues;
import gregtech.api.recipes.RecipeMap;
import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import tj.TJValues;
import tj.capability.IParallelController;
import tj.capability.TJCapabilities;
import tj.integration.theoneprobe.impl.ElementTJText;
import tj.util.TJUtility;

public class ParallelControllerInfoProvider extends CapabilityInfoProvider<IParallelController> {

    @Override
    protected Capability<IParallelController> getCapability() {
        return TJCapabilities.CAPABILITY_PARALLEL_CONTROLLER;
    }

    @Override
    protected void addProbeInfo(IParallelController capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing enumFacing) {
        final long energyStored = capability.getEnergyStored();
        final long energyCapacity = capability.getEnergyCapacity();
        final long maxEUt = capability.getMaxEUt();
        final long totalEnergy = capability.getTotalEnergyConsumption();
        final long voltageTier = capability.getVoltageTier();
        final int energyBonus = capability.getEUBonus();
        final int tier = TJUtility.getTierFromVoltage(voltageTier);
        final int euTier = TJUtility.getTierFromVoltage(maxEUt);
        final RecipeMap<?> multiblockRecipe = capability.getMultiblockRecipe();

        final IProbeInfo controllerInfo = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
        controllerInfo.element(new ElementTJText(String.format("{*tj.multiblock.max_voltage[*%s;%s*]*}", TJValues.thousandFormat.format(maxEUt), TJValues.VCC[euTier] + GAValues.VN[euTier])));
        if (energyBonus > 0)
            controllerInfo.element(new ElementTJText(String.format("{*tj.multiblock.parallel_controller.energy_bonus[*%s*]*}", 100 - energyBonus)));
        controllerInfo.element(new ElementTJText(String.format("{*machine.universal.tooltip.voltage_tier[*%s*]*}", TJValues.VCC[tier] + GAValues.VN[tier])));
        controllerInfo.element(new ElementTJText(String.format("{*tj.multiblock.parallel.sum[*%s*]*}", TJValues.thousandFormat.format(totalEnergy))));
        if (multiblockRecipe != null)
            controllerInfo.element(new ElementTJText(String.format("{*tj.multiblock.universal.tooltip.1[*%s*]*}", "{*recipemap." + multiblockRecipe.getUnlocalizedName() + ".name*}")));
        if (energyCapacity > 0) {
            final int energyPercent = (int) Math.floor(energyStored / (energyCapacity * 1.0) * 100);
            final String displayEnergy = String.format("%s/%s EU ", TJValues.thousandFormat.format(energyStored), TJValues.thousandFormat.format(energyCapacity));
            final IProbeInfo energyStoredInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
            energyStoredInfo.element(new ElementTJText("{*tj.top.parallel_controller.energy_stored*}"));
            energyStoredInfo.progress(energyPercent, 100, probeInfo.defaultProgressStyle()
                    .prefix(displayEnergy)
                    .suffix("%")
                    .alternateFilledColor(0xFFF8EB34)
                    .filledColor(0xFFF8EB34)
                    .width((int) (displayEnergy.length() * 6.2)));
        }
    }

    @Override
    public String getID() {
        return "tj:parallel_controller_provider";
    }

}
