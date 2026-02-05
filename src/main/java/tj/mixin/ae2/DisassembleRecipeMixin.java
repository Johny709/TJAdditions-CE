package tj.mixin.ae2;

import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import appeng.core.Api;
import appeng.recipes.game.DisassembleRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tj.integration.appeng.IApiItems;
import tj.integration.appeng.IApiMaterials;

import java.util.Map;

@Mixin(value = DisassembleRecipe.class, remap = false)
public abstract class DisassembleRecipeMixin {

    @Shadow
    @Final
    private Map<IItemDefinition, IItemDefinition> cellMappings;

    @Shadow
    @Final
    private Map<IItemDefinition, IItemDefinition> nonCellMappings;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectDisassembleRecipe_Init(CallbackInfo ci) {
        final IDefinitions definitions = Api.INSTANCE.definitions();
        final IApiItems items = (IApiItems) definitions.items();
        final IApiMaterials materials = (IApiMaterials) definitions.materials();

        this.cellMappings.put(items.getCell65m(), materials.getCell65mPart());
        this.cellMappings.put(items.getCell262m(), materials.getCell262mPart());
        this.cellMappings.put(items.getCell1048m(), materials.getCell1048mPart());
        this.cellMappings.put(items.getCellDigitalSingularity(), materials.getCellDigitalSingularityPart());
        this.cellMappings.put(items.getFluidCell65m(), materials.getFluidCell65mPart());
        this.cellMappings.put(items.getFluidCell262m(), materials.getFluidCell262mPart());
        this.cellMappings.put(items.getFluidCell1048m(), materials.getFluidCell1048mPart());
        this.cellMappings.put(items.getFluidCellDigitalSingularity(), materials.getFluidCellDigitalSingularityPart());

        // AE2 UEL forgot to add disassemble recipes for their fluid cells
        this.cellMappings.put(items.fluidCell1k(), materials.fluidCell1kPart());
        this.cellMappings.put(items.fluidCell4k(), materials.fluidCell4kPart());
        this.cellMappings.put(items.fluidCell16k(), materials.fluidCell16kPart());
        this.cellMappings.put(items.fluidCell64k(), materials.fluidCell64kPart());
    }
}
