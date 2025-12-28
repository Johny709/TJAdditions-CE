package tj.mixin.gregicality;

import gregicadditions.machines.multi.advance.hyper.MetaTileEntityHyperReactorIII;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MetaTileEntityHyperReactorIII.class, remap = false)
public interface IMetaTileEntityHyperReactorIIIMixin {

    @Accessor("booster")
    FluidStack getBoosterFluid();
}
