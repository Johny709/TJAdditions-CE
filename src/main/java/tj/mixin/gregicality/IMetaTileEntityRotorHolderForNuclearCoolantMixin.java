package tj.mixin.gregicality;

import gregicadditions.machines.multi.impl.MetaTileEntityRotorHolderForNuclearCoolant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MetaTileEntityRotorHolderForNuclearCoolant.class, remap = false)
public interface IMetaTileEntityRotorHolderForNuclearCoolantMixin {

    @Accessor("currentRotorSpeed")
    void setCurrentRotorSpeed(int currentRotorSpeed);
}
