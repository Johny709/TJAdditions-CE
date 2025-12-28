package tj.mixin.gregicality;

import gregicadditions.machines.multi.advance.MetaTileEntityAdvFusionReactor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MetaTileEntityAdvFusionReactor.class, remap = false)
public interface IMetaTileEntityAdvFusionReactorMixin {

    @Accessor("tier")
    int getTier();
}
