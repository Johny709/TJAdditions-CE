package tj.integration.theoneprobe;

import gregtech.api.util.TextFormattingUtil;
import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.TJCapabilities;
import tj.integration.theoneprobe.impl.ElementFluidStack;

import java.util.List;

public class IItemFluidHandlerInfoProvider extends CapabilityInfoProvider<IItemFluidHandlerInfo> {

    @Override
    protected Capability<IItemFluidHandlerInfo> getCapability() {
        return TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING;
    }

    @Override
    protected void addProbeInfo(IItemFluidHandlerInfo capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing enumFacing) {
        final List<ItemStack> itemInputs = capability.getItemInputs();
        final List<FluidStack> fluidInputs = capability.getFluidInputs();
        final List<ItemStack> itemOutputs = capability.getItemOutputs();
        final List<FluidStack> fluidOutputs = capability.getFluidOutputs();
        if (fluidInputs != null && !fluidInputs.isEmpty() || itemInputs != null && !itemInputs.isEmpty()) {
            final IProbeInfo inputInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
            inputInfo.text(TextStyleClass.INFO + "{*tj.top.inputs*} ");
            if (fluidInputs != null && !fluidInputs.isEmpty()) {
                for (FluidStack fluid : fluidInputs) {
                    if (fluid == null) continue;
                    inputInfo.element(new ElementFluidStack(fluid));
                }
            }
            if (itemInputs != null && !itemInputs.isEmpty()) {
                for (ItemStack item : itemInputs) {
                    if (item.isEmpty()) continue;
                    inputInfo.item(item);
                }
            }
        }
        if (fluidOutputs != null && !fluidOutputs.isEmpty() || itemOutputs != null && !itemOutputs.isEmpty()) {
            final IProbeInfo outputInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
            outputInfo.text(TextStyleClass.INFO + "{*tj.top.outputs*} ");
            if (fluidOutputs != null && !fluidOutputs.isEmpty()) {
                for (FluidStack fluid : fluidOutputs) {
                    if (fluid == null) continue;
                    outputInfo.element(new ElementFluidStack(fluid));
                }
            }
            if (itemOutputs != null && !itemOutputs.isEmpty()) {
                for (ItemStack item : itemOutputs) {
                    if (item.isEmpty()) continue;
                    outputInfo.item(item);
                }
            }
        }
    }

    @Override
    public String getID() {
        return "tj:item_handler_provider";
    }
}
