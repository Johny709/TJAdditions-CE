package tj.mixin.ae2;

import appeng.api.definitions.IItemDefinition;
import appeng.bootstrap.FeatureFactory;
import appeng.core.api.definitions.ApiParts;
import appeng.core.features.DamagedItemDefinition;
import appeng.core.features.registries.PartModels;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tj.integration.ae2.IApiParts;
import tj.integration.ae2.items.parts.TJItemPart;
import tj.integration.ae2.items.parts.TJItemPartRendering;
import tj.integration.ae2.items.parts.TJPartType;

@Mixin(value = ApiParts.class, remap = false)
public abstract class ApiPartsMixin implements IApiParts {

    @Unique
    private IItemDefinition superInterface;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectApiParts_Init(FeatureFactory registry, PartModels partModels, CallbackInfo ci) {
        final TJItemPart tjItemPart = new TJItemPart();
        registry.item("tj_part", () -> tjItemPart).rendering(new TJItemPartRendering(partModels, tjItemPart)).build();

        // Register all part models
        for (TJPartType partType : TJPartType.values()) {
            partModels.registerModels(partType.getModels());
        }
        this.superInterface = new DamagedItemDefinition("part.super_interface", tjItemPart.createPart(TJPartType.SUPER_INTERFACE));
    }

    @Override
    public IItemDefinition getSuperInterface() {
        return this.superInterface;
    }
}
