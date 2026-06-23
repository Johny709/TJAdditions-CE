package tj.mixin.gregicality;

import gregicadditions.machines.multi.mega.MegaMultiblockRecipeMapController;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = MegaMultiblockRecipeMapController.class, remap = false)
public abstract class MixinMegaMultiblockRecipeMapController extends MixinLargeSimpleRecipeMapMultiblockController {
    public MixinMegaMultiblockRecipeMapController(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
}
