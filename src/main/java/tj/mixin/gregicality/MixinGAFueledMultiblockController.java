package tj.mixin.gregicality;

import gregicadditions.machines.multi.GAFueledMultiblockController;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import tj.mixin.gregtech.MixinFueledMultiblockController;

@Mixin(value = GAFueledMultiblockController.class, remap = false)
public abstract class MixinGAFueledMultiblockController extends MixinFueledMultiblockController {
    public MixinGAFueledMultiblockController(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
}
