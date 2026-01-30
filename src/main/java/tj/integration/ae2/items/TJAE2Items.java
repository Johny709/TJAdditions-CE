package tj.integration.ae2.items;

import appeng.api.definitions.IItemDefinition;
import appeng.bootstrap.FeatureFactory;
import appeng.core.Api;
import appeng.core.features.AEFeature;
import tj.integration.ae2.items.cells.TJFluidStorageCell;
import tj.integration.ae2.items.cells.TJItemStorageCell;
import tj.integration.ae2.items.materials.TJAE2MaterialType;

public final class TJAE2Items {

    public static final TJAE2Items INSTANCE = new TJAE2Items();
    private final IItemDefinition cell65m;
    private final IItemDefinition cell262m;
    private final IItemDefinition cell1048m;
    private final IItemDefinition cellDigitalSingularity;
    private final IItemDefinition fluidCell65m;
    private final IItemDefinition fluidCell262m;
    private final IItemDefinition fluidCell1048m;
    private final IItemDefinition fluidCellDigitalSingularity;

    private TJAE2Items() {
        FeatureFactory storageCells = Api.INSTANCE.definitions().getRegistry().features(AEFeature.STORAGE_CELLS);
        this.cell65m = storageCells.item("storage_cell_65m", () -> new TJItemStorageCell(TJAE2MaterialType.CELL_65M_PART, 65_536)).build();
        this.cell262m = storageCells.item("storage_cell_262m", () -> new TJItemStorageCell(TJAE2MaterialType.CELL_262M_PART, 262_144)).build();
        this.cell1048m = storageCells.item("storage_cell_1048m", () -> new TJItemStorageCell(TJAE2MaterialType.CELL_1048M_PART, 1_048_576)).build();
        this.cellDigitalSingularity = storageCells.item("storage_item_digital_singularity", () -> new TJItemStorageCell(TJAE2MaterialType.ITEM_CELL_DIGITAL_SINGULARITY, Integer.MAX_VALUE)).build();
        this.fluidCell65m = storageCells.item("storage_fluid_cell_65m", () -> new TJFluidStorageCell(TJAE2MaterialType.FLUID_CELL_65M_PART, 65_536)).build();
        this.fluidCell262m = storageCells.item("storage_fluid_cell_262m", () -> new TJFluidStorageCell(TJAE2MaterialType.FLUID_CELL_262M_PART, 262_144)).build();
        this.fluidCell1048m = storageCells.item("storage_fluid_cell_1048m", () -> new TJFluidStorageCell(TJAE2MaterialType.FLUID_CELL_1048M_PART, 1_048_576)).build();
        this.fluidCellDigitalSingularity = storageCells.item("storage_fluid_digital_singularity", () -> new TJFluidStorageCell(TJAE2MaterialType.FLUID_CELL_DIGITAL_SINGULARITY, Integer.MAX_VALUE)).build();
    }

    public IItemDefinition getCell65m() {
        return this.cell65m;
    }

    public IItemDefinition getCell262m() {
        return this.cell262m;
    }

    public IItemDefinition getCell1048m() {
        return this.cell1048m;
    }

    public IItemDefinition getCellDigitalSingularity() {
        return this.cellDigitalSingularity;
    }

    public IItemDefinition getFluidCell65m() {
        return this.fluidCell65m;
    }

    public IItemDefinition getFluidCell262m() {
        return this.fluidCell262m;
    }

    public IItemDefinition getFluidCell1048m() {
        return this.fluidCell1048m;
    }

    public IItemDefinition getFluidCellDigitalSingularity() {
        return this.fluidCellDigitalSingularity;
    }
}
