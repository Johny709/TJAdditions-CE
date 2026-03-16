package tj.mixin.gregicality;

import gregicadditions.machines.multi.mega.MegaMultiblockRecipeMapController;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = MegaMultiblockRecipeMapController.class, remap = false)
public abstract class MegaMultiblockRecipeMapControllerMixin extends LargeSimpleRecipeMapMultiblockControllerMixin {
    public MegaMultiblockRecipeMapControllerMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
}
