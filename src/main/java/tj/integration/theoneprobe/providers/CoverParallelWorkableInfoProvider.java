package tj.integration.theoneprobe.providers;

import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import tj.capability.IMultipleWorkable;
import tj.capability.TJCapabilities;
import tj.integration.theoneprobe.impl.ElementProgressBar;
import tj.integration.theoneprobe.impl.ElementTJText;
import tj.util.Color;

public class CoverParallelWorkableInfoProvider extends CoverCapabilityInfo<IMultipleWorkable> {

    @Override
    protected Capability<IMultipleWorkable> getCapability() {
        return TJCapabilities.CAPABILITY_MULTIPLE_WORKABLE;
    }

    @Override
    protected void addProbeInfo(IMultipleWorkable multipleWorkable, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing enumFacing) {
        final int pageIndex = multipleWorkable.getPageIndex();
        final int pageSize = multipleWorkable.getPageSize();
        final int size = multipleWorkable.getSize();

        final IProbeInfo pageInfo = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
        pageInfo.text(TextStyleClass.INFO + "§b(" + (pageIndex + 1) + "/" + size + ")");

        for (int i = pageIndex; i < pageIndex + pageSize; i++) {
            if (!(i < size)) break;
            final double maxProgress = (double) multipleWorkable.getMaxProgress(i) / 20;
            final double progress = Math.min(maxProgress, (double) multipleWorkable.getProgress(i) / 20);
            final boolean isWorking = multipleWorkable.isWorkingEnabled(i);
            final boolean isActive = multipleWorkable.isInstanceActive(i);
            final boolean hasProblems = multipleWorkable.hasProblems(i);

            final IProbeInfo nameInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
            nameInfo.text(TextStyleClass.INFO + "§b[" + (i + 1) + "]§r ");
            nameInfo.element(new ElementTJText(String.format("{*tj.multiblock.parallel.status[*%s*]*}", !isWorking ? "{*gregtech.multiblock.work_paused*}"
                    : hasProblems ? "{*machine.universal.has_problems*}"
                    : isActive ? "{*gregtech.multiblock.running*}"
                    : "{*gregtech.multiblock.idling*}")));

            final IProbeInfo progressInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
            progressInfo.element(new ElementProgressBar("{*gregtech.top.progress*}", String.valueOf(progress), String.valueOf(maxProgress),
                    "s", "s", Color.GREEN.toString(), ",##0.00"));
        }
    }

    @Override
    public String getID() {
        return "tj:cover_multiple_workable_info";
    }
}
