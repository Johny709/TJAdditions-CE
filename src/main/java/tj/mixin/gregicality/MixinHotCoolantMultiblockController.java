package tj.mixin.gregicality;

import gregicadditions.machines.multi.impl.HotCoolantMultiblockController;
import gregicadditions.machines.multi.impl.HotCoolantRecipeLogic;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.mixin.gregtech.MixinMultiblockWithDisplayBase;


@Mixin(value = HotCoolantMultiblockController.class, remap = false)
public abstract class MixinHotCoolantMultiblockController extends MixinMultiblockWithDisplayBase {

    @Shadow
    protected HotCoolantRecipeLogic workableHandler;

    @Shadow
    protected IEnergyContainer energyContainer;

    @Shadow
    protected IMultipleTankHandler importFluidHandler;

    public MixinHotCoolantMultiblockController(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
}
