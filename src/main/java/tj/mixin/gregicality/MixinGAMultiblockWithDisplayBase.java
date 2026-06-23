package tj.mixin.gregicality;

import gregicadditions.machines.multi.GAMultiblockWithDisplayBase;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import tj.mixin.gregtech.MixinMultiblockWithDisplayBase;

@Mixin(value = GAMultiblockWithDisplayBase.class, remap = false)
public abstract class MixinGAMultiblockWithDisplayBase extends MixinMultiblockWithDisplayBase {
    public MixinGAMultiblockWithDisplayBase(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
}
