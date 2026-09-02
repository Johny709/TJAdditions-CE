package tj.integration.theoneprobe;

import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import tj.capability.IParallelItemFluidHandlerInfo;
import tj.capability.TJCapabilities;
import tj.integration.theoneprobe.impl.ElementFluidList;
import tj.integration.theoneprobe.impl.ElementItemList;
import tj.integration.theoneprobe.impl.ElementTJText;

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
            boolean areInputsEmpty = true;
            if (fluidInputs != null && !fluidInputs.isEmpty()) {
                for (Int2ObjectMap.Entry<List<FluidStack>> entry : fluidInputs.int2ObjectEntrySet()) {
                    if (entry.getValue() == null) continue;
                    if (!entry.getValue().isEmpty()) {
                        areInputsEmpty = false;
                        break;
                    }
                }
            }
            if (itemInputs != null && !itemInputs.isEmpty()) {
                for (Int2ObjectMap.Entry<List<ItemStack>> entry : itemInputs.int2ObjectEntrySet()) {
                    if (entry.getValue() == null) continue;
                    if (!entry.getValue().isEmpty()) {
                        areInputsEmpty = false;
                        break;
                    }
                }
            }
            if (!areInputsEmpty) {
                probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT))
                        .element(new ElementTJText("{*tj.top.inputs*}"));
                final IProbeInfo inputInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
                if (itemInputs != null && !itemInputs.isEmpty()) {
                    for (Int2ObjectMap.Entry<List<ItemStack>> entry : itemInputs.int2ObjectEntrySet()) {
                        if (entry.getValue() == null) continue;
                        inputInfo.element(new ElementItemList(entry.getValue()));
                    }
                }
                if (fluidInputs != null && !fluidInputs.isEmpty()) {
                    for (Int2ObjectMap.Entry<List<FluidStack>> entry : fluidInputs.int2ObjectEntrySet()) {
                        if (entry.getValue() == null) continue;
                        inputInfo.element(new ElementFluidList(entry.getValue()));
                    }
                }
            }
        }
        if (fluidOutputs != null && !fluidOutputs.isEmpty() || itemOutputs != null && !itemOutputs.isEmpty()) {
            boolean areOutputsEmpty = true;
            if (fluidOutputs != null && !fluidOutputs.isEmpty()) {
                for (Int2ObjectMap.Entry<List<FluidStack>> entry : fluidOutputs.int2ObjectEntrySet()) {
                    if (entry.getValue() == null) continue;
                    if (!entry.getValue().isEmpty()) {
                        areOutputsEmpty = false;
                        break;
                    }
                }
            }
            if (itemOutputs != null && !itemOutputs.isEmpty()) {
                for (Int2ObjectMap.Entry<List<ItemStack>> entry : itemOutputs.int2ObjectEntrySet()) {
                    if (entry.getValue() == null) continue;
                    if (!entry.getValue().isEmpty()) {
                        areOutputsEmpty = false;
                        break;
                    }
                }
            }
            if (!areOutputsEmpty) {
                probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT))
                        .element(new ElementTJText("{*tj.top.outputs*}"));
                final IProbeInfo outputInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
                if (itemOutputs != null && !itemOutputs.isEmpty()) {
                    for (Int2ObjectMap.Entry<List<ItemStack>> entry : itemOutputs.int2ObjectEntrySet()) {
                        if (entry.getValue() == null) continue;
                        outputInfo.element(new ElementItemList(entry.getValue()));
                    }
                }
                if (fluidOutputs != null && !fluidOutputs.isEmpty()) {
                    for (Int2ObjectMap.Entry<List<FluidStack>> entry : fluidOutputs.int2ObjectEntrySet()) {
                        if (entry.getValue() == null) continue;
                        outputInfo.element(new ElementFluidList(entry.getValue()));
                    }
                }
            }
        }
    }

    @Override
    public String getID() {
        return "tj:parallel_item_handler_provider";
    }
}
