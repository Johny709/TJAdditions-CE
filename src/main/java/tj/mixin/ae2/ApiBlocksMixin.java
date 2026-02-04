package tj.mixin.ae2;

import appeng.api.definitions.ITileDefinition;
import appeng.block.crafting.ItemCraftingStorage;
import appeng.bootstrap.FeatureFactory;
import appeng.bootstrap.definitions.TileEntityDefinition;
import appeng.core.api.definitions.ApiBlocks;
import appeng.core.features.AEFeature;
import appeng.core.features.registries.PartModels;
import appeng.fluids.block.BlockFluidInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tj.integration.appeng.IApiBlocks;
import appeng.block.crafting.TJBlockCraftingUnit;
import appeng.block.crafting.TJCraftingUnitType;
import appeng.block.misc.BlockSuperInterface;
import appeng.render.crafting.TJCraftingCubeRendering;
import appeng.tile.crafting.TJTileCraftingStorageTile;
import appeng.tile.misc.TJTileFluidInterface;
import appeng.tile.misc.TileSuperInterface;

@Mixin(value = ApiBlocks.class, remap = false)
public abstract class ApiBlocksMixin implements IApiBlocks {

    @Unique
    private ITileDefinition craftingStorage65m;

    @Unique
    private ITileDefinition craftingStorage262m;

    @Unique
    private ITileDefinition craftingStorage1048m;

    @Unique
    private ITileDefinition craftingStorageSingularity;

    @Unique
    private ITileDefinition superInterface;

    @Unique
    private ITileDefinition superFluidInterface;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectApiBlocks_Init(FeatureFactory registry, PartModels partModels, CallbackInfo ci) {
        FeatureFactory craftingCPU = registry.features(AEFeature.CRAFTING_CPU);
        this.craftingStorage65m = craftingCPU.block("crafting_storage_65m", () -> new TJBlockCraftingUnit(TJCraftingUnitType.STORAGE_65M))
                .rendering(new TJCraftingCubeRendering("crafting_storage_65m", TJCraftingUnitType.STORAGE_65M))
                .tileEntity(new TileEntityDefinition(TJTileCraftingStorageTile.class, "crafting_storage"))
                .useCustomItemModel()
                .build();
        this.craftingStorage262m = craftingCPU.block("crafting_storage_262m", () -> new TJBlockCraftingUnit(TJCraftingUnitType.STORAGE_262M))
                .rendering(new TJCraftingCubeRendering("crafting_storage_262m", TJCraftingUnitType.STORAGE_262M))
                .tileEntity(new TileEntityDefinition(TJTileCraftingStorageTile.class, "crafting_storage"))
                .item(ItemCraftingStorage::new)
                .useCustomItemModel()
                .build();
        this.craftingStorage1048m = craftingCPU.block("crafting_storage_1048m", () -> new TJBlockCraftingUnit(TJCraftingUnitType.STORAGE_1048M))
                .rendering(new TJCraftingCubeRendering("crafting_storage_1048m", TJCraftingUnitType.STORAGE_1048M))
                .tileEntity(new TileEntityDefinition(TJTileCraftingStorageTile.class, "crafting_storage"))
                .item(ItemCraftingStorage::new)
                .useCustomItemModel()
                .build();
        this.craftingStorageSingularity = craftingCPU.block("crafting_storage_digital_singularity", () -> new TJBlockCraftingUnit(TJCraftingUnitType.STORAGE_SINGULARITY))
                .rendering(new TJCraftingCubeRendering("crafting_storage_digital_singularity", TJCraftingUnitType.STORAGE_SINGULARITY))
                .tileEntity(new TileEntityDefinition(TJTileCraftingStorageTile.class, "crafting_storage"))
                .item(ItemCraftingStorage::new)
                .useCustomItemModel()
                .build();
        this.superInterface = registry.block("super_interface", BlockSuperInterface::new)
                .features(AEFeature.INTERFACE)
                .tileEntity(new TileEntityDefinition(TileSuperInterface.class))
                .build();
        this.superFluidInterface = registry.block("super_fluid_interface", BlockFluidInterface::new)
                .features(AEFeature.FLUID_INTERFACE)
                .tileEntity(new TileEntityDefinition(TJTileFluidInterface.class))
                .build();
    }

    @Override
    public ITileDefinition getCraftingStorage65m() {
        return this.craftingStorage65m;
    }

    @Override
    public ITileDefinition getCraftingStorage262m() {
        return this.craftingStorage262m;
    }

    @Override
    public ITileDefinition getCraftingStorage1048m() {
        return this.craftingStorage1048m;
    }

    @Override
    public ITileDefinition getCraftingStorageSingularity() {
        return this.craftingStorageSingularity;
    }

    @Override
    public ITileDefinition getSuperInterface() {
        return this.superInterface;
    }

    @Override
    public ITileDefinition getSuperFluidInterface() {
        return this.superFluidInterface;
    }
}
