package tj.mixin.gregicality;

import gregicadditions.machines.multi.GAFueledMultiblockController;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import tj.mixin.gregtech.FueledMultiblockControllerMixin;

@Mixin(value = GAFueledMultiblockController.class, remap = false)
public abstract class GAFueledMultiblockControllerMixin extends FueledMultiblockControllerMixin {
    public GAFueledMultiblockControllerMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }
}
