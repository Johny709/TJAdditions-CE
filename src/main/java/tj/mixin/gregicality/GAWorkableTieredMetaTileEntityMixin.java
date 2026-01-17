package tj.mixin.gregicality;

import gregicadditions.machines.overrides.GAWorkableTieredMetaTileEntity;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GAWorkableTieredMetaTileEntity.class, remap = false)
public abstract class GAWorkableTieredMetaTileEntityMixin extends GATieredMetaTileEntityMixin {

    @Shadow
    @Final
    protected RecipeLogicEnergy workable;

    public GAWorkableTieredMetaTileEntityMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
}
