package tj.mixin.gregicality;

import gregicadditions.machines.overrides.GATieredMetaTileEntity;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GATieredMetaTileEntity.class, remap = false)
public abstract class GATieredMetaTileEntityMixin extends MetaTileEntity {

    @Shadow
    protected IEnergyContainer energyContainer;

    public GATieredMetaTileEntityMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
}
