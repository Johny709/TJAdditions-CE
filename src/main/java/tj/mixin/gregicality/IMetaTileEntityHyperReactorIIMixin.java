package tj.mixin.gregicality;

import gregicadditions.machines.multi.advance.hyper.MetaTileEntityHyperReactorII;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MetaTileEntityHyperReactorII.class, remap = false)
public interface IMetaTileEntityHyperReactorIIMixin {

    @Accessor("booster")
    FluidStack getBoosterFluid();
}
