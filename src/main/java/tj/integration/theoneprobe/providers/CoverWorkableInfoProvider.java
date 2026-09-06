package tj.integration.theoneprobe.providers;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import tj.integration.theoneprobe.impl.ElementProgressBar;
import tj.integration.theoneprobe.impl.ElementTJText;
import tj.util.Color;

public class CoverWorkableInfoProvider extends CoverCapabilityInfo<IWorkable> {

    @Override
    protected Capability<IWorkable> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_WORKABLE;
    }

    @Override
    protected void addProbeInfo(IWorkable workable, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing enumFacing) {
        final double maxProgress = (double) workable.getMaxProgress() / 20;
        final double progress = Math.min(maxProgress, (double) workable.getProgress() / 20);

        final IProbeInfo progressInfo = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
        progressInfo.element(new ElementProgressBar("{*gregtech.top.progress*}", String.valueOf(progress), String.valueOf(maxProgress),
                "s", "s", Color.GREEN.toString(), ",##0.00"));
        if (!workable.isWorkingEnabled())
            progressInfo.element(new ElementTJText("{*gregtech.multiblock.work_paused*}"));
        else if (workable.isActive()) {
            progressInfo.element(new ElementTJText("{*gregtech.multiblock.running*}"));
        }
    }

    @Override
    public String getID() {
        return "tj:cover_progress_provider";
    }
}
