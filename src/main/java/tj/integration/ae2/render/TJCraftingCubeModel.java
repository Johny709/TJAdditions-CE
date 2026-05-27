package tj.integration.ae2.render;

import tj.integration.ae2.blocks.TJCraftingUnitType;
import appeng.core.AppEng;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import tj.TJ;

import java.util.Collection;
import java.util.function.Function;

public class TJCraftingCubeModel implements IModel {

    private static final ResourceLocation RING_CORNER = new ResourceLocation(AppEng.MOD_ID, "blocks/crafting/ring_corner");
    private static final ResourceLocation RING_SIDE_HOR = new ResourceLocation(AppEng.MOD_ID, "blocks/crafting/ring_side_hor");
    private static final ResourceLocation RING_SIDE_VER = new ResourceLocation(AppEng.MOD_ID, "blocks/crafting/ring_side_ver");
    private static final ResourceLocation LIGHT_BASE = new ResourceLocation(AppEng.MOD_ID, "blocks/crafting/light_base");
    private static final ResourceLocation CRAFTING_STORAGE_65536K_LIGHT = new ResourceLocation(TJ.MODID, "blocks/ae2/me.crafting_storage.65536k.light");
    private static final ResourceLocation CRAFTING_STORAGE_262144K_LIGHT = new ResourceLocation(TJ.MODID, "blocks/ae2/me.crafting_storage.262144k.light");
    private static final ResourceLocation CRAFTING_STORAGE_1048M_LIGHT = new ResourceLocation(TJ.MODID, "blocks/ae2/me.crafting_storage.1048m.light");
    private static final ResourceLocation CRAFTING_STORAGE_SINGULARITY_LIGHT = new ResourceLocation(TJ.MODID, "blocks/ae2/me.crafting_storage.singularity.light");

    private final TJCraftingUnitType type;

    public TJCraftingCubeModel(TJCraftingUnitType type) {
        this.type = type;
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return ImmutableList.of(RING_CORNER, RING_SIDE_HOR, RING_SIDE_VER, LIGHT_BASE, CRAFTING_STORAGE_65536K_LIGHT, CRAFTING_STORAGE_262144K_LIGHT, CRAFTING_STORAGE_1048M_LIGHT, CRAFTING_STORAGE_SINGULARITY_LIGHT);
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        // Retrieve our textures and pass them on to the baked model
        final TextureAtlasSprite ringCorner = bakedTextureGetter.apply(RING_CORNER);
        final TextureAtlasSprite ringSideHor = bakedTextureGetter.apply(RING_SIDE_HOR);
        final TextureAtlasSprite ringSideVer = bakedTextureGetter.apply(RING_SIDE_VER);

        switch (this.type) {
            case STORAGE_65M:
            case STORAGE_262M:
            case STORAGE_1048M:
            case STORAGE_SINGULARITY: return new TJLightBakedModel(format, ringCorner, ringSideHor, ringSideVer, bakedTextureGetter.apply(LIGHT_BASE), getLightTexture(bakedTextureGetter, this.type));
            default:
                throw new IllegalArgumentException("Crafting unit type " + this.type + " does not use a light texture.");
        }
    }

    private static TextureAtlasSprite getLightTexture(Function<ResourceLocation, TextureAtlasSprite> textureGetter, TJCraftingUnitType type) {
        switch (type) {
            case STORAGE_65M: return textureGetter.apply(CRAFTING_STORAGE_65536K_LIGHT);
            case STORAGE_262M: return textureGetter.apply(CRAFTING_STORAGE_262144K_LIGHT);
            case STORAGE_1048M: return textureGetter.apply(CRAFTING_STORAGE_1048M_LIGHT);
            case STORAGE_SINGULARITY: return textureGetter.apply(CRAFTING_STORAGE_SINGULARITY_LIGHT);
            default:
                throw new IllegalArgumentException("Crafting unit type " + type + " does not use a light texture.");
        }
    }
}
