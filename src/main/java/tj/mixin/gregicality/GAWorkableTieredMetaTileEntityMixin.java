package tj.mixin.gregicality;

import gregicadditions.machines.overrides.GAWorkableTieredMetaTileEntity;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.render.OrientedOverlayRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tj.capability.IProcessorProvider;

@Mixin(value = GAWorkableTieredMetaTileEntity.class, remap = false)
public abstract class GAWorkableTieredMetaTileEntityMixin extends GATieredMetaTileEntityMixin implements IProcessorProvider {

    @Shadow
    @Final
    protected RecipeLogicEnergy workable;

    @Shadow
    @Final
    protected OrientedOverlayRenderer renderer;

    @Shadow
    @Final
    protected ItemStackHandler ghostCircuitInventory;

    public GAWorkableTieredMetaTileEntityMixin(ResourceLocation metaTileEntityId) {
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
