package tj.rendering;

import tj.integration.ae2.blocks.TJCraftingUnitType;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import tj.TJ;
import tj.integration.ae2.render.TJCraftingCubeModel;

import javax.annotation.Nonnull;

public class BakedModelLoader implements ICustomModelLoader {

    private static final TJCraftingCubeModel CRAFTING_STORAGE_65536k_MODEL = new TJCraftingCubeModel(TJCraftingUnitType.STORAGE_65M);
    private static final TJCraftingCubeModel CRAFTING_STORAGE_262144K_MODEL = new TJCraftingCubeModel(TJCraftingUnitType.STORAGE_262M);
    private static final TJCraftingCubeModel CRAFTING_STORAGE_1048M_MODEL = new TJCraftingCubeModel(TJCraftingUnitType.STORAGE_1048M);
    private static final TJCraftingCubeModel CRAFTING_STORAGE_SINGULARITY_MODEL = new TJCraftingCubeModel(TJCraftingUnitType.STORAGE_SINGULARITY);

    private final Object2ObjectMap<ResourceLocation, IModel> models = new Object2ObjectOpenHashMap<>();

    public BakedModelLoader() {
        this.models.put(new ModelResourceLocation(new ResourceLocation(TJ.MODID, "me.crafting_storage.65536k"), "normal"), CRAFTING_STORAGE_65536k_MODEL);
        this.models.put(new ModelResourceLocation(new ResourceLocation(TJ.MODID, "me.crafting_storage.262144k"), "normal"), CRAFTING_STORAGE_262144K_MODEL);
        this.models.put(new ModelResourceLocation(new ResourceLocation(TJ.MODID, "me.crafting_storage.1048m"), "normal"), CRAFTING_STORAGE_1048M_MODEL);
        this.models.put(new ModelResourceLocation(new ResourceLocation(TJ.MODID, "me.crafting_storage.singularity"), "normal"), CRAFTING_STORAGE_SINGULARITY_MODEL);
    }

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {

    }

    @Override
    public boolean accepts(@Nonnull ResourceLocation modelLocation) {
        return this.models.containsKey(modelLocation);
    }

    @Override
    public IModel loadModel(@Nonnull ResourceLocation modelLocation) {
        return this.models.get(modelLocation);
    }
}
