package tj.mixin.gregicality;

import gregicadditions.machines.multi.advance.hyper.MetaTileEntityHyperReactorI;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MetaTileEntityHyperReactorI.class, remap = false)
public interface IMetaTileEntityHyperReactorIMixin {

    @Accessor("booster")
    FluidStack getBoosterFluid();
}
