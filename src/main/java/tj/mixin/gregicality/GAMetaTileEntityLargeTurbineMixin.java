package tj.mixin.gregicality;

import gregicadditions.machines.multi.override.MetaTileEntityLargeTurbine;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import tj.mixin.gregtech.MetaTileEntityLargeTurbineMixin;


@Mixin(value = MetaTileEntityLargeTurbine.class, remap = false)
public abstract class GAMetaTileEntityLargeTurbineMixin extends MetaTileEntityLargeTurbineMixin {

    public GAMetaTileEntityLargeTurbineMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
}
