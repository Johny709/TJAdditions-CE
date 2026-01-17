package tj.mixin.gregtech;

import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.metatileentity.WorkableTieredMetaTileEntity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = WorkableTieredMetaTileEntity.class, remap = false)
public abstract class WorkableTieredMetaTileEntityMixin extends TieredMetaTileEntityMixin {

    @Shadow
    @Final
    protected RecipeLogicEnergy workable;

    public WorkableTieredMetaTileEntityMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
}
