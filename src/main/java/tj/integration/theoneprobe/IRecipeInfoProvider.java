package tj.integration.theoneprobe;

import gregicadditions.GAValues;
import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import tj.TJValues;
import tj.capability.IRecipeInfo;
import tj.capability.TJCapabilities;
import tj.integration.theoneprobe.impl.ElementFluidList;
import tj.integration.theoneprobe.impl.ElementItemList;
import tj.integration.theoneprobe.impl.ElementTJText;
import tj.util.TJUtility;

import java.util.List;

public class IRecipeInfoProvider extends CapabilityInfoProvider<IRecipeInfo> {

    @Override
    protected Capability<IRecipeInfo> getCapability() {
        return TJCapabilities.CAPABILITY_RECIPE_INFO;
    }

    @Override
    protected void addProbeInfo(IRecipeInfo capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing enumFacing) {
        final List<ItemStack> itemInputs = capability.getItemInputs();
        final List<FluidStack> fluidInputs = capability.getFluidInputs();
        final List<ItemStack> itemOutputs = capability.getItemOutputs();
        final List<FluidStack> fluidOutputs = capability.getFluidOutputs();
        if (capability.getEnergyPerTick() > 0) {
            final IProbeInfo horizontalPane = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
            final int tier = TJUtility.getTierFromVoltage(capability.getEnergyPerTick());
            horizontalPane.element(new ElementTJText(String.format("{*tj.multiblock.eu[*%s;%s*]*}",
                    TJValues.thousandFormat.format(capability.getEnergyPerTick()),
                    tier > 14 ? "§c§lM§e§lA§a§lX§b§l+§d§l" + (tier - 14) : TJValues.VCC[tier] + GAValues.VN[tier])));
        }
        if (capability.isHasProblems()) {
            probeInfo.element(new ElementTJText("{*machine.universal.has_problems*}"));
        } else if (capability.isActive()) {
            probeInfo.element(new ElementTJText("{*machine.universal.running*}"));
        }
        if (!fluidInputs.isEmpty() || !itemInputs.isEmpty()) {
            probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT))
                    .element(new ElementTJText("{*tj.top.inputs*}"));
            final IProbeInfo inputInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
            if (!itemInputs.isEmpty())
                inputInfo.element(new ElementItemList(itemInputs));
            if (!fluidInputs.isEmpty())
                inputInfo.element(new ElementFluidList(fluidInputs));
        }
        if (!fluidOutputs.isEmpty() || !itemOutputs.isEmpty()) {
            probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT))
                    .element(new ElementTJText("{*tj.top.outputs*}"));
            final IProbeInfo outputInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
            if (!itemOutputs.isEmpty())
                outputInfo.element(new ElementItemList(itemOutputs));
            if (!fluidOutputs.isEmpty())
                outputInfo.element(new ElementFluidList(fluidOutputs));
        }
    }

    @Override
    public String getID() {
        return "tj:recipeinfo";
    }
}
