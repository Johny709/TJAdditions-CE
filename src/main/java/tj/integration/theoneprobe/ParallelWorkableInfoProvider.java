package tj.integration.theoneprobe;

import gregtech.integration.theoneprobe.provider.CapabilityInfoProvider;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.capabilities.Capability;
import tj.TJValues;
import tj.capability.IMultipleWorkable;
import tj.capability.TJCapabilities;


public class ParallelWorkableInfoProvider extends CapabilityInfoProvider<IMultipleWorkable> {

    @Override
    protected Capability<IMultipleWorkable> getCapability() {
        return TJCapabilities.CAPABILITY_MULTIPLE_WORKABLE;
    }

    @Override
    protected void addProbeInfo(IMultipleWorkable capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing enumFacing) {
        int pageIndex = capability.getPageIndex();
        int pageSize = capability.getPageSize();
        int size = capability.getSize();

        IProbeInfo pageInfo = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
        pageInfo.text(TextStyleClass.INFO + "§b(" + (pageIndex + 1) + "/" + size + ")");

        for (int i = pageIndex; i < pageIndex + pageSize; i++) {
            if (i < size) {
                float currentProgress = capability.getProgress(i);
                float maxProgress = capability.getMaxProgress(i);
                long EUt = capability.getRecipeEUt(i);
                int progressScaled = maxProgress == 0 ? 0 : (int) Math.floor(currentProgress / (maxProgress * 1.0) * 100);
                boolean isWorking = capability.isWorkingEnabled(i);
                boolean isActive = capability.isInstanceActive(i);
                boolean hasProblems = capability.hasProblems(i);

                IProbeInfo nameInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
                nameInfo.text(TextStyleClass.INFO + "§b[" + (i + 1) + "]§r ");
                nameInfo.text(TextStyleClass.INFO + I18n.translateToLocalFormatted("tj.multiblock.parallel.status", (!isWorking ? "§e{*gregtech.multiblock.work_paused*}§r"
                        : hasProblems ? "§6{*machine.universal.has_problems*}§r"
                        : isActive ? "§a{*gregtech.multiblock.running*}§r"
                        : "§7{*gregtech.multiblock.idling*}")));

                String displayProgress = String.format("%ss / %ss | ", TJValues.thousandTwoPlaceFormat.format(currentProgress / 20), TJValues.thousandTwoPlaceFormat.format(maxProgress / 20));
                IProbeInfo progressInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
                progressInfo.text(TextStyleClass.INFO + "{*gregtech.top.progress*} ");
                progressInfo.progress(progressScaled, 100, probeInfo.defaultProgressStyle()
                        .width((int) (displayProgress.length() * 6.2))
                        .prefix(displayProgress)
                        .suffix("%")
                        .borderColor(0x00000000)
                        .backgroundColor(0x00000000)
                        .filledColor(0xFF000099)
                        .alternateFilledColor(0xFF000077));
                IProbeInfo EUtInfo = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_TOPLEFT));
                EUtInfo.text(TextStyleClass.INFO + I18n.translateToLocalFormatted("tj.multiblock.eu", EUt));
            }
        }
    }

    @Override
    public String getID() {
        return "tj:parallel_workable_provider";
    }
}
