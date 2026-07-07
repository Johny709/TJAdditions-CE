package tj.mixin.ae2;

import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.core.Api;
import appeng.recipes.game.DisassembleRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tj.blocks.block.TJBlocks;
import tj.items.item.TJItems;

import java.util.Map;

@Mixin(value = DisassembleRecipe.class, remap = false)
public abstract class MixinDisassembleRecipe {

    @Shadow
    @Final
    private Map<IItemDefinition, IItemDefinition> cellMappings;

    @Shadow
    @Final
    private Map<IItemDefinition, IItemDefinition> nonCellMappings;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectDisassembleRecipe_Init(CallbackInfo ci) {
        final IDefinitions definitions = Api.INSTANCE.definitions();
        final IItems items = definitions.items();
        final IMaterials materials = definitions.materials();

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

        this.nonCellMappings.put(TJBlocks.CRAFTING_STORAGE_65536K, TJItems.MATERIAL_ITEM_CELL_65536K);
        this.nonCellMappings.put(TJBlocks.CRAFTING_STORAGE_262144K, TJItems.MATERIAL_ITEM_CELL_262144K);
        this.nonCellMappings.put(TJBlocks.CRAFTING_STORAGE_1048M, TJItems.MATERIAL_ITEM_CELL_1048M);
        this.nonCellMappings.put(TJBlocks.CRAFTING_STORAGE_SINGULARITY, TJItems.MATERIAL_ITEM_CELL_DIGITAL_SINGULARITY);
    }
}
