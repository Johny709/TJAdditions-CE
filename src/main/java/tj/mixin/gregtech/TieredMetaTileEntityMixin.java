package tj.mixin.gregtech;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = TieredMetaTileEntity.class, remap = false)
public abstract class TieredMetaTileEntityMixin extends MetaTileEntity {

    @Shadow
    protected IEnergyContainer energyContainer;

    public TieredMetaTileEntityMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
}
