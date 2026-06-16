package tj.integration.theoneprobe;

import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import tj.capability.IParallelItemFluidHandlerInfo;
import tj.capability.TJCapabilities;

import java.util.List;

public class IParallelItemFluidHandlerInfoProvider extends CapabilityInfoProvider<IParallelItemFluidHandlerInfo> {

    @Override
    protected Capability<IParallelItemFluidHandlerInfo> getCapability() {
        return TJCapabilities.CAPABILITY_PARALLEL_ITEM_FLUID_HANDLING;
    }

    @Override
    protected void addProbeInfo(IParallelItemFluidHandlerInfo capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing enumFacing) {
        final Int2ObjectMap<List<ItemStack>> itemInputs = capability.getAllItemInputs();
        final Int2ObjectMap<List<FluidStack>> fluidInputs = capability.getAllFluidInputs();
        final Int2ObjectMap<List<ItemStack>> itemOutputs = capability.getAllItemOutputs();
        final Int2ObjectMap<List<FluidStack>> fluidOutputs = capability.getAllFluidOutputs();
        if (fluidInputs != null && !fluidInputs.isEmpty() || itemInputs != null && !itemInputs.isEmpty()) {
            final IProbeInfo inputInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
            inputInfo.text(TextStyleClass.INFO + "{*tj.top.inputs*} ");
            if (fluidInputs != null && !fluidInputs.isEmpty()) {
                for (Int2ObjectMap.Entry<List<FluidStack>> entry : fluidInputs.int2ObjectEntrySet()) {
                    if (entry.getValue() == null) continue;
                    for (FluidStack fluid : entry.getValue()) {
                        if (fluid == null) continue;
                        final ItemStack fluidItem = FluidUtil.getFilledBucket(fluid);
                        fluidItem.setCount(fluid.amount);
                        inputInfo.item(fluidItem);
                    }
                }
            }
            if (itemInputs != null && !itemInputs.isEmpty()) {
                for (Int2ObjectMap.Entry<List<ItemStack>> entry : itemInputs.int2ObjectEntrySet()) {
                    if (entry.getValue() == null) continue;
                    for (ItemStack item : entry.getValue()) {
                        if (item.isEmpty()) continue;
                        inputInfo.item(item);
                    }
                }
            }
        }
        if (fluidOutputs != null && !fluidOutputs.isEmpty() || itemOutputs != null && !itemOutputs.isEmpty()) {
            final IProbeInfo outputInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
            outputInfo.text(TextStyleClass.INFO + "{*tj.top.outputs*} ");
            if (fluidOutputs != null && !fluidOutputs.isEmpty()) {
                for (Int2ObjectMap.Entry<List<FluidStack>> entry : fluidOutputs.int2ObjectEntrySet()) {
                    if (entry.getValue() == null) continue;
                    for (FluidStack fluid : entry.getValue()) {
                        if (fluid == null) continue;
                        final ItemStack fluidItem = FluidUtil.getFilledBucket(fluid);
                        fluidItem.setCount(fluid.amount);
                        outputInfo.item(fluidItem);
                    }
                }
            }
            if (itemOutputs != null && !itemOutputs.isEmpty()) {
                for (Int2ObjectMap.Entry<List<ItemStack>> entry : itemOutputs.int2ObjectEntrySet()) {
                    if (entry.getValue() == null) continue;
                    for (ItemStack item : entry.getValue()) {
                        if (item.isEmpty()) continue;
                        outputInfo.item(item);
                    }
                }
            }
        }
    }

    @Override
    public String getID() {
        return "tj:item_handler_provider";
    }
}
