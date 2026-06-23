package tj.mixin.gregicality;

import gregicadditions.machines.multi.override.MetaTileEntityLargeTurbine;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import tj.mixin.gregtech.MixinMetaTileEntityLargeTurbine;


@Mixin(value = MetaTileEntityLargeTurbine.class, remap = false)
public abstract class MixinGAMetaTileEntityLargeTurbine extends MixinMetaTileEntityLargeTurbine {

    public MixinGAMetaTileEntityLargeTurbine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
}
