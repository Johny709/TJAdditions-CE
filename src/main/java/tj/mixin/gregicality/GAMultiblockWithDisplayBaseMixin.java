package tj.mixin.gregicality;

import gregicadditions.machines.multi.GAMultiblockWithDisplayBase;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import tj.mixin.gregtech.MultiblockWithDisplayBaseMixin;

@Mixin(value = GAMultiblockWithDisplayBase.class, remap = false)
public abstract class GAMultiblockWithDisplayBaseMixin extends MultiblockWithDisplayBaseMixin {
    public GAMultiblockWithDisplayBaseMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
}
