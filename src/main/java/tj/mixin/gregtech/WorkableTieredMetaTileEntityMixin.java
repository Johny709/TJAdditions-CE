package tj.mixin.gregtech;

import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.metatileentity.WorkableTieredMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.render.OrientedOverlayRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.capability.IProcessorProvider;

@Mixin(value = WorkableTieredMetaTileEntity.class, remap = false)
public abstract class WorkableTieredMetaTileEntityMixin extends TieredMetaTileEntityMixin implements IProcessorProvider {

    @Shadow
    @Final
    protected RecipeLogicEnergy workable;

    @Shadow
    @Final
    protected ItemStackHandler ghostCircuitInventory;

    @Shadow
    @Final
    protected OrientedOverlayRenderer renderer;

    public WorkableTieredMetaTileEntityMixin(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return this.workable.recipeMap;
    }

    @Override
    public OrientedOverlayRenderer getRendererOverlay() {
        return this.renderer;
    }

    @Override
    public int getMachineTier() {
        return this.getTier();
    }
}
