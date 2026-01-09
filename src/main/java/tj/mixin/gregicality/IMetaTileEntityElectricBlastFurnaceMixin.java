package tj.mixin.gregicality;

import gregicadditions.machines.multi.override.MetaTileEntityElectricBlastFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MetaTileEntityElectricBlastFurnace.class, remap = false)
public interface IMetaTileEntityElectricBlastFurnaceMixin {

    @Accessor("blastFurnaceTemperature")
    int getBlastFurnaceTemperature();

    @Accessor("bonusTemperature")
    int getBonusTemperature();
}
