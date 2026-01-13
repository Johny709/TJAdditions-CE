package tj.mixin.gregtech;

import gregtech.api.metatileentity.WorkableTieredMetaTileEntity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = WorkableTieredMetaTileEntity.class, remap = false)
public abstract class WorkableTieredMetaTileEntityMixin extends TieredMetaTileEntityMixin {
    public WorkableTieredMetaTileEntityMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
}
