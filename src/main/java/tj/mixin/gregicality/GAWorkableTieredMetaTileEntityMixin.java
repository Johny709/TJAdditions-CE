package tj.mixin.gregicality;

import gregicadditions.machines.overrides.GAWorkableTieredMetaTileEntity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = GAWorkableTieredMetaTileEntity.class, remap = false)
public abstract class GAWorkableTieredMetaTileEntityMixin extends GATieredMetaTileEntityMixin {
    public GAWorkableTieredMetaTileEntityMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
}
