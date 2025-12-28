package tj.mixin.gregtech;

import gregtech.api.capability.IEnergyContainer;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityEnergyHatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MetaTileEntityEnergyHatch.class, remap = false)
public interface IMetaTileEntityEnergyHatchMixin {

    @Accessor("isExportHatch")
    boolean isExport();

    @Accessor("energyContainer")
    IEnergyContainer getEnergyContainer();

}
