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
import tj.items.item.TJItems;

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

        //TODO remove in v2.5.0
        this.cellMappings.put(items.getCell65m(), materials.getCell65mPart());
        this.cellMappings.put(items.getCell262m(), materials.getCell262mPart());
        this.cellMappings.put(items.getCell1048m(), materials.getCell1048mPart());
        this.cellMappings.put(items.getCellDigitalSingularity(), materials.getCellDigitalSingularityPart());
        this.cellMappings.put(items.getFluidCell65m(), materials.getFluidCell65mPart());
        this.cellMappings.put(items.getFluidCell262m(), materials.getFluidCell262mPart());
        this.cellMappings.put(items.getFluidCell1048m(), materials.getFluidCell1048mPart());
        this.cellMappings.put(items.getFluidCellDigitalSingularity(), materials.getFluidCellDigitalSingularityPart());

        this.cellMappings.put(TJItems.ITEM_CELL_65536K, TJItems.MATERIAL_ITEM_CELL_65536K);
        this.cellMappings.put(TJItems.ITEM_CELL_262144K, TJItems.MATERIAL_ITEM_CELL_262144K);
        this.cellMappings.put(TJItems.ITEM_CELL_1048M, TJItems.MATERIAL_ITEM_CELL_1048M);
        this.cellMappings.put(TJItems.ITEM_CELL_DIGITAL_SINGULARITY, TJItems.ITEM_CELL_DIGITAL_SINGULARITY);
        this.cellMappings.put(TJItems.FLUID_CELL_65536K, TJItems.MATERIAL_FLUID_CELL_65536K);
        this.cellMappings.put(TJItems.FLUID_CELL_262144K, TJItems.MATERIAL_FLUID_CELL_262144K);
        this.cellMappings.put(TJItems.FLUID_CELL_1048M, TJItems.MATERIAL_FLUID_CELL_1048M);
        this.cellMappings.put(TJItems.FLUID_CELL_DIGITAL_SINGULARITY, TJItems.MATERIAL_FLUID_CELL_DIGITAL_SINGULARITY);
        this.cellMappings.put(TJItems.ITEM_BLOCK_CONTAINER_64K, definitions.materials().cell1kPart());
        this.cellMappings.put(TJItems.ITEM_BLOCK_CONTAINER_65536K, TJItems.MATERIAL_ITEM_CELL_65536K);
        this.cellMappings.put(TJItems.ITEM_BLOCK_CONTAINER_SINGULARITY, TJItems.MATERIAL_ITEM_CELL_DIGITAL_SINGULARITY);

        // AE2 UEL forgot to add disassemble recipes for their fluid cells
        this.cellMappings.put(items.fluidCell1k(), materials.fluidCell1kPart());
        this.cellMappings.put(items.fluidCell4k(), materials.fluidCell4kPart());
        this.cellMappings.put(items.fluidCell16k(), materials.fluidCell16kPart());
        this.cellMappings.put(items.fluidCell64k(), materials.fluidCell64kPart());
    }
}
