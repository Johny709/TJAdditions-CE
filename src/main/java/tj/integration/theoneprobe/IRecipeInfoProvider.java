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
import tj.integration.theoneprobe.impl.ElementFluidStack;
import tj.integration.theoneprobe.impl.ElementTJText;
import tj.util.TJUtility;

import java.util.List;

public class IRecipeInfoProvider extends CapabilityInfoProvider<IRecipeInfo> {

    @Override
    protected Capability<IRecipeInfo> getCapability() {
        return null;
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
            final IProbeInfo inputInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
            inputInfo.element(new ElementTJText("{*tj.top.inputs*}"));
            if (!fluidInputs.isEmpty()) {
                for (FluidStack fluid : fluidInputs) {
                    if (fluid == null) continue;
                    inputInfo.element(new ElementFluidStack(fluid));
                }
            }
            if (!itemInputs.isEmpty()) {
                for (ItemStack item : itemInputs) {
                    if (item.isEmpty()) continue;
                    inputInfo.item(item);
                }
            }
        }
        if (!fluidOutputs.isEmpty() || !itemOutputs.isEmpty()) {
            final IProbeInfo outputInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
            outputInfo.element(new ElementTJText("{*tj.top.outputs*}"));
            if (!fluidOutputs.isEmpty()) {
                for (FluidStack fluid : fluidOutputs) {
                    if (fluid == null) continue;
                    outputInfo.element(new ElementFluidStack(fluid));
                }
            }
            if (!itemOutputs.isEmpty()) {
                for (ItemStack item : itemOutputs) {
                    if (item.isEmpty()) continue;
                    outputInfo.item(item);
                }
            }
        }
    }

    @Override
    public String getID() {
        return "tj:recipeinfo";
    }
}
