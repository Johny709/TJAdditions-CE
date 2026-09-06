package tj.integration.theoneprobe.providers;

import gregicadditions.GAValues;
import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import tj.TJValues;
import tj.capability.IMultipleWorkable;
import tj.capability.TJCapabilities;
import tj.integration.theoneprobe.impl.ElementProgressBar;
import tj.integration.theoneprobe.impl.ElementTJText;
import tj.util.Color;
import tj.util.TJUtility;


public class ParallelWorkableInfoProvider extends CapabilityInfoProvider<IMultipleWorkable> {

    @Override
    protected Capability<IMultipleWorkable> getCapability() {
        return TJCapabilities.CAPABILITY_MULTIPLE_WORKABLE;
    }

    @Override
    protected void addProbeInfo(IMultipleWorkable capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing enumFacing) {
        final int pageIndex = capability.getPageIndex();
        final int pageSize = capability.getPageSize();
        final int size = capability.getSize();

        final IProbeInfo pageInfo = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
        pageInfo.text(TextStyleClass.INFO + "§b(" + (pageIndex + 1) + "/" + size + ")");

        for (int i = pageIndex; i < pageIndex + pageSize; i++) {
            if (!(i < size)) break;
            final double maxProgress = (double) capability.getMaxProgress(i) / 20;
            final double progress = Math.min(maxProgress, (double) capability.getProgress(i) / 20);
            final long EUt = capability.getRecipeEUt(i);
            final int tier = TJUtility.getTierFromVoltage(EUt);
            final boolean isWorking = capability.isWorkingEnabled(i);
            final boolean isActive = capability.isInstanceActive(i);
            final boolean hasProblems = capability.hasProblems(i);

            final IProbeInfo nameInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
            nameInfo.text(TextStyleClass.INFO + "§b[" + (i + 1) + "]§r ");
            nameInfo.element(new ElementTJText(String.format("{*tj.multiblock.parallel.status[*%s*]*}", !isWorking ? "{*gregtech.multiblock.work_paused*}"
                    : hasProblems ? "{*machine.universal.has_problems*}"
                    : isActive ? "{*gregtech.multiblock.running*}"
                    : "{*gregtech.multiblock.idling*}")));

            final IProbeInfo progressInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
            progressInfo.element(new ElementProgressBar("{*gregtech.top.progress*}", String.valueOf(progress), String.valueOf(maxProgress),
                    "s", "s", Color.GREEN.toString(), ",##0.00"));
            final IProbeInfo EUtInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
            EUtInfo.element(new ElementTJText(String.format("{*tj.multiblock.eu[*%s;%s*]*}", TJValues.thousandFormat.format(EUt),
                    tier > 14 ? "§c§lM§e§lA§a§lX§b§l+§d§l" + (tier - 14) : TJValues.VCC[tier] + GAValues.VN[tier])));
        }
    }

    @Override
    public String getID() {
        return "tj:parallel_workable_provider";
    }
}
