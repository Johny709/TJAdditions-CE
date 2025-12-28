package tj.mixin.gregicality;

import gregicadditions.machines.multi.multiblockpart.GAMetaTileEntityEnergyHatch;
import gregtech.api.capability.IEnergyContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GAMetaTileEntityEnergyHatch.class, remap = false)
public interface IGAMetaTileEntityEnergyHatchMixin {

    @Accessor("isExportHatch")
    boolean isExport();

    @Accessor("energyContainer")
    IEnergyContainer getEnergyContainer();
}
