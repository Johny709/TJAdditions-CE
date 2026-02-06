package tj.mixin.ae2;

import appeng.api.definitions.IItemDefinition;
import appeng.bootstrap.FeatureFactory;
import appeng.core.api.definitions.ApiItems;
import appeng.core.features.AEFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tj.integration.appeng.IApiItems;
import appeng.items.cells.TJFluidStorageCell;
import appeng.items.cells.TJItemStorageCell;
import appeng.items.materials.TJAE2MaterialType;

@Mixin(value = ApiItems.class, remap = false)
public abstract class ApiItemsMixin implements IApiItems {

    @Unique
    private IItemDefinition cell65m;

    @Unique
    private IItemDefinition cell262m;

    @Unique
    private IItemDefinition cell1048m;

    @Unique
    private IItemDefinition cellDigitalSingularity;

    @Unique
    private IItemDefinition fluidCell65m;

    @Unique
    private IItemDefinition fluidCell262m;

    @Unique
    private IItemDefinition fluidCell1048m;

    @Unique
    private IItemDefinition fluidCellDigitalSingularity;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectApiItems_Init(FeatureFactory registry, CallbackInfo ci) {
        FeatureFactory storageCells = registry.features(AEFeature.STORAGE_CELLS);
        this.cell65m = storageCells.item("storage_cell_65m", () -> new TJItemStorageCell(TJAE2MaterialType.CELL_65M_PART, 65_536)).build();
        this.cell262m = storageCells.item("storage_cell_262m", () -> new TJItemStorageCell(TJAE2MaterialType.CELL_262M_PART, 262_144)).build();
        this.cell1048m = storageCells.item("storage_cell_1048m", () -> new TJItemStorageCell(TJAE2MaterialType.CELL_1048M_PART, 1_048_576)).build();
        this.cellDigitalSingularity = storageCells.item("storage_item_digital_singularity", () -> new TJItemStorageCell(TJAE2MaterialType.ITEM_CELL_DIGITAL_SINGULARITY, Integer.MAX_VALUE)).build();
        this.fluidCell65m = storageCells.item("storage_fluid_cell_65m", () -> new TJFluidStorageCell(TJAE2MaterialType.FLUID_CELL_65M_PART, 65_536)).build();
        this.fluidCell262m = storageCells.item("storage_fluid_cell_262m", () -> new TJFluidStorageCell(TJAE2MaterialType.FLUID_CELL_262M_PART, 262_144)).build();
        this.fluidCell1048m = storageCells.item("storage_fluid_cell_1048m", () -> new TJFluidStorageCell(TJAE2MaterialType.FLUID_CELL_1048M_PART, 1_048_576)).build();
        this.fluidCellDigitalSingularity = storageCells.item("storage_fluid_digital_singularity", () -> new TJFluidStorageCell(TJAE2MaterialType.FLUID_CELL_DIGITAL_SINGULARITY, Integer.MAX_VALUE)).build();
    }

    @Override
    public IItemDefinition getCell65m() {
        return this.cell65m;
    }

    @Override
    public IItemDefinition getCell262m() {
        return this.cell262m;
    }

    @Override
    public IItemDefinition getCell1048m() {
        return this.cell1048m;
    }

    @Override
    public IItemDefinition getCellDigitalSingularity() {
        return this.cellDigitalSingularity;
    }

    @Override
    public IItemDefinition getFluidCell65m() {
        return this.fluidCell65m;
    }

    @Override
    public IItemDefinition getFluidCell262m() {
        return this.fluidCell262m;
    }

    @Override
    public IItemDefinition getFluidCell1048m() {
        return this.fluidCell1048m;
    }

    @Override
    public IItemDefinition getFluidCellDigitalSingularity() {
        return this.fluidCellDigitalSingularity;
    }
}
