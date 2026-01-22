package tj.mixin.gregicality;

import gregicadditions.machines.multi.override.MetaTileEntityVacuumFreezer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = MetaTileEntityVacuumFreezer.class, remap = false)
public abstract class MetaTileEntityVacuumFreezerMixin extends GARecipeMapMultiblockControllerMixin {
    public MetaTileEntityVacuumFreezerMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
}
