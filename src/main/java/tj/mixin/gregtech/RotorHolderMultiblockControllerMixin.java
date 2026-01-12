package tj.mixin.gregtech;

import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityRotorHolder;
import gregtech.common.metatileentities.multi.electric.generator.RotorHolderMultiblockController;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = RotorHolderMultiblockController.class, remap = false)
public abstract class RotorHolderMultiblockControllerMixin extends FueledMultiblockControllerMixin {

    @Shadow
    public abstract MetaTileEntityRotorHolder getRotorHolder();

    @Shadow
    public abstract boolean isRotorFaceFree();

    public RotorHolderMultiblockControllerMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
}
