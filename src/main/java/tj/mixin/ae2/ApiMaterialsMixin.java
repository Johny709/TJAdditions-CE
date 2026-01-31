package tj.mixin.ae2;

import appeng.api.definitions.IItemDefinition;
import appeng.bootstrap.FeatureFactory;
import appeng.bootstrap.IItemRendering;
import appeng.bootstrap.ItemRenderingCustomizer;
import appeng.core.api.definitions.ApiMaterials;
import appeng.core.features.DamagedItemDefinition;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tj.integration.ae2.IApiMaterials;
import tj.integration.ae2.items.materials.TJAE2MaterialType;
import tj.integration.ae2.items.materials.TJItemMaterial;

import java.util.Arrays;
import java.util.stream.Collectors;

@Mixin(value = ApiMaterials.class, remap = false)
public abstract class ApiMaterialsMixin implements IApiMaterials {

    @Unique
    private IItemDefinition cell65mPart;

    @Unique
    private IItemDefinition cell262mPart;

    @Unique
    private IItemDefinition cell1048mPart;

    @Unique
    private IItemDefinition cellDigitalSingularityPart;

    @Unique
    private IItemDefinition fluidCell65mPart;

    @Unique
    private IItemDefinition fluidCell262mPart;

    @Unique
    private IItemDefinition fluidCell1048mPart;

    @Unique
    private IItemDefinition fluidCellDigitalSingularityPart;


    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectApiMaterials_Init(FeatureFactory registry, CallbackInfo ci) {
        final TJItemMaterial materials = new TJItemMaterial();
        registry.item("tj_material",() -> materials)
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

    @Override
    public IItemDefinition getCell65mPart() {
        return this.cell65mPart;
    }

    @Override
    public IItemDefinition getCell262mPart() {
        return this.cell262mPart;
    }

    @Override
    public IItemDefinition getCell1048mPart() {
        return this.cell1048mPart;
    }

    @Override
    public IItemDefinition getCellDigitalSingularityPart() {
        return this.cellDigitalSingularityPart;
    }

    @Override
    public IItemDefinition getFluidCell65mPart() {
        return this.fluidCell65mPart;
    }

    @Override
    public IItemDefinition getFluidCell262mPart() {
        return this.fluidCell262mPart;
    }

    @Override
    public IItemDefinition getFluidCell1048mPart() {
        return this.fluidCell1048mPart;
    }

    @Override
    public IItemDefinition getFluidCellDigitalSingularityPart() {
        return this.fluidCellDigitalSingularityPart;
    }
}
