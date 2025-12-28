package tj.mixin.gregicality;

import gregicadditions.machines.multi.TileEntityFusionReactor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileEntityFusionReactor.class, remap = false)
public interface ITileEntityFusionReactorMixin {

    @Accessor("tier")
    int getTier();
}
