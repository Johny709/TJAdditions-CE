package tj.integration.ae2;

import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.ITileDefinition;
import appeng.bootstrap.FeatureFactory;
import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import appeng.core.Api;
import appeng.core.features.AEFeature;
import appeng.core.features.DamagedItemDefinition;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.integration.ae2.items.cells.TJFluidStorageCell;
import tj.integration.ae2.items.cells.TJItemStorageCell;
import tj.integration.ae2.items.materials.TJAE2MaterialType;
import tj.integration.ae2.items.materials.TJItemMaterial;

import java.util.Arrays;
import java.util.stream.Collectors;


public final class TJAE2API {

    public static final TJAE2API INSTANCE = new TJAE2API();

    private final IItemDefinition cell65m;
    private final IItemDefinition cell262m;
    private final IItemDefinition cell1048m;
    private final IItemDefinition cellDigitalSingularity;
    private final IItemDefinition fluidCell65m;
    private final IItemDefinition fluidCell262m;
    private final IItemDefinition fluidCell1048m;
    private final IItemDefinition fluidCellDigitalSingularity;

    private final IItemDefinition cell65mPart;
    private final IItemDefinition cell262mPart;
    private final IItemDefinition cell1048mPart;
    private final IItemDefinition cellDigitalSingularityPart;
    private final IItemDefinition fluidCell65mPart;
    private final IItemDefinition fluidCell262mPart;
    private final IItemDefinition fluidCell1048mPart;
    private final IItemDefinition fluidCellDigitalSingularityPart;

    private TJAE2API() {
        FeatureFactory storageCells = Api.INSTANCE.definitions().getRegistry().features(AEFeature.STORAGE_CELLS);
        this.cell65m = storageCells.item("storage_cell_65m", () -> new TJItemStorageCell(TJAE2MaterialType.CELL_65M_PART, 65_536)).build();
        this.cell262m = storageCells.item("storage_cell_262m", () -> new TJItemStorageCell(TJAE2MaterialType.CELL_262M_PART, 262_144)).build();
        this.cell1048m = storageCells.item("storage_cell_1048m", () -> new TJItemStorageCell(TJAE2MaterialType.CELL_1048M_PART, 1_048_576)).build();
        this.cellDigitalSingularity = storageCells.item("storage_item_digital_singularity", () -> new TJItemStorageCell(TJAE2MaterialType.ITEM_CELL_DIGITAL_SINGULARITY, Integer.MAX_VALUE)).build();
        this.fluidCell65m = storageCells.item("storage_fluid_cell_65m", () -> new TJFluidStorageCell(TJAE2MaterialType.FLUID_CELL_65M_PART, 65_536)).build();
        this.fluidCell262m = storageCells.item("storage_fluid_cell_262m", () -> new TJFluidStorageCell(TJAE2MaterialType.FLUID_CELL_262M_PART, 262_144)).build();
        this.fluidCell1048m = storageCells.item("storage_fluid_cell_1048m", () -> new TJFluidStorageCell(TJAE2MaterialType.FLUID_CELL_1048M_PART, 1_048_576)).build();
        this.fluidCellDigitalSingularity = storageCells.item("storage_fluid_digital_singularity", () -> new TJFluidStorageCell(TJAE2MaterialType.FLUID_CELL_DIGITAL_SINGULARITY, Integer.MAX_VALUE)).build();

        final TJItemMaterial materials = new TJItemMaterial();
        Api.INSTANCE.definitions().getRegistry().item("tj_material",() -> materials)
                .rendering(new ItemRenderingCustomizer() {
                    @Override
                    @SideOnly(Side.CLIENT)
                    public void customize(IItemRendering rendering) {
                        rendering.meshDefinition(is -> materials.getTypeByStack(is).getModel());
                        // Register a resource location for every material type
                        rendering.variants(Arrays.stream(TJAE2MaterialType.values())
                                .map(TJAE2MaterialType::getModel)
                                .collect(Collectors.toList()));
                    }
                }).build();

        this.cell65mPart = new DamagedItemDefinition("material.cell.storage.65m", materials.createMaterial(TJAE2MaterialType.CELL_65M_PART));
        this.cell262mPart = new DamagedItemDefinition("material.cell.storage.262m", materials.createMaterial(TJAE2MaterialType.CELL_262M_PART));
        this.cell1048mPart = new DamagedItemDefinition("material.cell.storage.1048m", materials.createMaterial(TJAE2MaterialType.CELL_1048M_PART));
        this.cellDigitalSingularityPart = new DamagedItemDefinition("material.cell.digital.singularity", materials.createMaterial(TJAE2MaterialType.ITEM_CELL_DIGITAL_SINGULARITY));
        this.fluidCell65mPart = new DamagedItemDefinition("material.cell.storage.65m", materials.createMaterial(TJAE2MaterialType.FLUID_CELL_65M_PART));
        this.fluidCell262mPart = new DamagedItemDefinition("material.cell.storage.262m", materials.createMaterial(TJAE2MaterialType.FLUID_CELL_262M_PART));
        this.fluidCell1048mPart = new DamagedItemDefinition("material.cell.storage.1048m", materials.createMaterial(TJAE2MaterialType.FLUID_CELL_1048M_PART));
        this.fluidCellDigitalSingularityPart = new DamagedItemDefinition("material.cell.digital.singularity", materials.createMaterial(TJAE2MaterialType.FLUID_CELL_DIGITAL_SINGULARITY));
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

    public IItemDefinition getCell65mPart() {
        return this.cell65mPart;
    }

    public IItemDefinition getCell262mPart() {
        return this.cell262mPart;
    }

    public IItemDefinition getCell1048mPart() {
        return this.cell1048mPart;
    }

    public IItemDefinition getCellDigitalSingularityPart() {
        return this.cellDigitalSingularityPart;
    }

    public IItemDefinition getFluidCell65mPart() {
        return this.fluidCell65mPart;
    }

    public IItemDefinition getFluidCell262mPart() {
        return this.fluidCell262mPart;
    }

    public IItemDefinition getFluidCell1048mPart() {
        return this.fluidCell1048mPart;
    }

    public IItemDefinition getFluidCellDigitalSingularityPart() {
        return this.fluidCellDigitalSingularityPart;
    }
}
