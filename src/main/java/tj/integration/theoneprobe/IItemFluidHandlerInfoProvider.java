package tj.integration.theoneprobe;

import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import tj.capability.IItemFluidHandlerInfo;
import tj.capability.TJCapabilities;

import java.util.List;

public class IItemFluidHandlerInfoProvider extends CapabilityInfoProvider<IItemFluidHandlerInfo> {

    @Override
    protected Capability<IItemFluidHandlerInfo> getCapability() {
        return TJCapabilities.CAPABILITY_ITEM_FLUID_HANDLING;
    }

    @Override
    protected void addProbeInfo(IItemFluidHandlerInfo capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing enumFacing) {
        List<ItemStack> itemInputs = capability.getItemInputs();
        List<FluidStack> fluidInputs = capability.getFluidInputs();
        List<ItemStack> itemOutputs = capability.getItemOutputs();
        List<FluidStack> fluidOutputs = capability.getFluidOutputs();

        IProbeInfo inputInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
        inputInfo.text(TextStyleClass.INFO + "{*tj.top.inputs*} ");

        if (fluidInputs != null && !fluidInputs.isEmpty()) {
            for (FluidStack fluid : fluidInputs) {
                if (fluid == null)
                    continue;
                ItemStack fluidItem = FluidUtil.getFilledBucket(fluid);
                fluidItem.setCount(fluid.amount);
                inputInfo.item(fluidItem);
            }
        }

        if (itemInputs != null && !itemInputs.isEmpty()) {
            for (ItemStack item : itemInputs) {
                if (item.isEmpty())
                    continue;
                inputInfo.item(item);
            }
        }

        IProbeInfo outputInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
        outputInfo.text(TextStyleClass.INFO + "{*tj.top.outputs*} ");

        if (fluidOutputs != null && !fluidOutputs.isEmpty()) {
            for (FluidStack fluid : fluidOutputs) {
                if (fluid == null)
                    continue;
                ItemStack fluidItem = FluidUtil.getFilledBucket(fluid);
                fluidItem.setCount(fluid.amount);
                outputInfo.item(fluidItem);
            }
        }

        if (itemOutputs != null && !itemOutputs.isEmpty()) {
            for (ItemStack item : itemOutputs) {
                if (item.isEmpty())
                    continue;
                outputInfo.item(item);
            }
        }
    }

    @Override
    public String getID() {
        return "tj:item_handler_provider";
    }
}
