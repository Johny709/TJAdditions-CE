package tj.integration.ae2.render.crafting;

import appeng.core.AppEng;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import tj.integration.ae2.block.crafting.TJCraftingUnitType;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

public class TJCraftingCubeModel implements IModel {

    private static final ResourceLocation RING_CORNER = texture("ring_corner");
    private static final ResourceLocation RING_SIDE_HOR = texture("ring_side_hor");
    private static final ResourceLocation RING_SIDE_VER = texture("ring_side_ver");
    private static final ResourceLocation UNIT_BASE = texture("unit_base");
    private static final ResourceLocation LIGHT_BASE = texture("light_base");
    private static final ResourceLocation ACCELERATOR_LIGHT = texture("accelerator_light");
    private static final ResourceLocation STORAGE_65M_LIGHT = texture("storage_65m_light");
    private static final ResourceLocation STORAGE_262M_LIGHT = texture("storage_262m_light");
    private static final ResourceLocation STORAGE_1048M_LIGHT = texture("storage_1048m_light");
    private static final ResourceLocation STORAGE_DIGITAL_SINGULARITY = texture("storage_digital_singularity_light");

    private final TJCraftingUnitType type;

    public TJCraftingCubeModel(TJCraftingUnitType type) {
        this.type = type;
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return Arrays.asList(RING_CORNER, RING_SIDE_HOR, RING_SIDE_VER, UNIT_BASE, LIGHT_BASE, ACCELERATOR_LIGHT, STORAGE_65M_LIGHT, STORAGE_262M_LIGHT, STORAGE_1048M_LIGHT, STORAGE_DIGITAL_SINGULARITY);
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        // Retrieve our textures and pass them on to the baked model
        TextureAtlasSprite ringCorner = bakedTextureGetter.apply(RING_CORNER);
        TextureAtlasSprite ringSideHor = bakedTextureGetter.apply(RING_SIDE_HOR);
        TextureAtlasSprite ringSideVer = bakedTextureGetter.apply(RING_SIDE_VER);

        switch (this.type) {
            case UNIT:
                return new TJUnitBakedModel(format, ringCorner, ringSideHor, ringSideVer, bakedTextureGetter.apply(UNIT_BASE));
//            case ACCELERATOR:
            case STORAGE_65M:
            case STORAGE_262M:
            case STORAGE_1048M:
            case STORAGE_SINGULARITY:
                return new TJLightBakedModel(format, ringCorner, ringSideHor, ringSideVer, bakedTextureGetter
                        .apply(LIGHT_BASE), getLightTexture(bakedTextureGetter, this.type));
//            case MONITOR:
//                return new MonitorBakedModel(format, ringCorner, ringSideHor, ringSideVer, bakedTextureGetter.apply(UNIT_BASE), bakedTextureGetter
//                        .apply(MONITOR_BASE), bakedTextureGetter.apply(
//                        MONITOR_LIGHT_DARK), bakedTextureGetter.apply(MONITOR_LIGHT_MEDIUM), bakedTextureGetter.apply(MONITOR_LIGHT_BRIGHT));
            default: throw new IllegalArgumentException("Unsupported crafting unit type: " + this.type);
        }
    }

    private static TextureAtlasSprite getLightTexture(Function<ResourceLocation, TextureAtlasSprite> textureGetter, TJCraftingUnitType type) {
        switch (type) {
//            case ACCELERATOR:
//                return textureGetter.apply(ACCELERATOR_LIGHT);
            case STORAGE_65M:
                return textureGetter.apply(STORAGE_65M_LIGHT);
            case STORAGE_262M:
                return textureGetter.apply(STORAGE_262M_LIGHT);
            case STORAGE_1048M:
                return textureGetter.apply(STORAGE_1048M_LIGHT);
            case STORAGE_SINGULARITY:
                return textureGetter.apply(STORAGE_DIGITAL_SINGULARITY);
            default:
                throw new IllegalArgumentException("Crafting unit type " + type + " does not use a light texture.");
        }
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(AppEng.MOD_ID, "blocks/crafting/" + name);
    }
}
