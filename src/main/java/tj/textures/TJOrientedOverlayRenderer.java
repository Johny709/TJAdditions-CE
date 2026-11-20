package tj.textures;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.render.BloomRenderUtils;
import gregtech.api.render.MetaTileEntityRenderer;
import gregtech.api.render.ResourceHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class TJOrientedOverlayRenderer implements TextureUtils.IIconRegister {

    private static final String EMISSIVE = "_emissive";

    private final String basePath;
    private final OverlayFace[] faces;
    private final String modID;
    private final String overlay;

    @SideOnly(Side.CLIENT)
    private Map<OverlayFace, ActivePredicate> sprites;

    @SideOnly(Side.CLIENT)
    private static class ActivePredicate {

        private final TextureAtlasSprite normalSprite;
        private final TextureAtlasSprite activeSprite;
        private final TextureAtlasSprite pausedSprite;
        private final TextureAtlasSprite problemSprite;

        private final TextureAtlasSprite normalEmissive;
        private final TextureAtlasSprite activeEmissive;
        private final TextureAtlasSprite pausedEmissive;
        private final TextureAtlasSprite problemEmissive;

        public ActivePredicate(TextureAtlasSprite normalSprite, TextureAtlasSprite activeSprite,
                               TextureAtlasSprite pausedSprite, TextureAtlasSprite problemSprite,
                               TextureAtlasSprite normalEmissive, TextureAtlasSprite activeEmissive,
                               TextureAtlasSprite pausedEmissive, TextureAtlasSprite problemEmissive) {
            this.normalSprite = normalSprite;
            this.activeSprite = activeSprite;
            this.pausedSprite = pausedSprite;
            this.problemSprite = problemSprite;
            this.normalEmissive = normalEmissive;
            this.activeEmissive = activeEmissive;
            this.pausedEmissive = pausedEmissive;
            this.problemEmissive = problemEmissive;
        }

        public TextureAtlasSprite getSprite(boolean active, boolean hasProblem, boolean isWorking) {
            TextureAtlasSprite sprite = active ? this.activeSprite : this.normalSprite;
            if (hasProblem)
                sprite = this.problemSprite;
            if (!isWorking)
                sprite = this.pausedSprite;
            return sprite;
        }

        public TextureAtlasSprite getEmissiveSprite(boolean active, boolean hasProblem, boolean isWorking) {
            TextureAtlasSprite sprite = active ? this.activeEmissive : this.normalEmissive;
            if (hasProblem)
                sprite = this.problemEmissive;
            if (!isWorking)
                sprite = this.pausedEmissive;
            return sprite;
        }
    }

    public TJOrientedOverlayRenderer(String modID, String basePath, String overlay, OverlayFace... faces) {
        this.modID = modID;
        this.basePath = basePath;
        this.faces = faces;
        this.overlay = overlay;
        TJTextures.iconRegisters.add(this);
    }

    public TJOrientedOverlayRenderer(String modID, String basePath, OverlayFace... faces) {
        this(modID, basePath, null, faces);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        this.sprites = new HashMap<>();
        boolean bloom = MetaTileEntityRenderer.useBloom();

        for (OverlayFace overlayFace : this.faces) {
            String faceName = this.overlay != null ? this.overlay : overlayFace.name().toLowerCase();

            String normalPath = String.format("blocks/%s/overlay_%s", this.basePath, faceName);
            String activePath = String.format("blocks/%s/overlay_%s_active", this.basePath, faceName);
            String pausedPath = String.format("blocks/%s/overlay_%s_paused", this.basePath, faceName);
            String problemPath = String.format("blocks/%s/overlay_%s_problem", this.basePath, faceName);

            TextureAtlasSprite normalSprite = textureMap.registerSprite(new ResourceLocation(this.modID, normalPath));
            TextureAtlasSprite activeSprite = textureMap.registerSprite(new ResourceLocation(this.modID, activePath));
            TextureAtlasSprite pausedSprite = textureMap.registerSprite(new ResourceLocation(this.modID, pausedPath));
            TextureAtlasSprite problemSprite = textureMap.registerSprite(new ResourceLocation(this.modID, problemPath));

            TextureAtlasSprite normalEmissive = null;
            TextureAtlasSprite activeEmissive = null;
            TextureAtlasSprite pausedEmissive = null;
            TextureAtlasSprite problemEmissive = null;

            if (bloom) {
                normalEmissive = this.registerEmissive(textureMap, normalPath);
                activeEmissive = this.registerEmissive(textureMap, activePath);
                pausedEmissive = this.registerEmissive(textureMap, pausedPath);
                problemEmissive = this.registerEmissive(textureMap, problemPath);
            }

            this.sprites.put(overlayFace, new ActivePredicate(normalSprite, activeSprite, pausedSprite, problemSprite, normalEmissive, activeEmissive, pausedEmissive, problemEmissive));
        }
    }

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite registerEmissive(TextureMap textureMap, String path) {
        String emissivePath = path + EMISSIVE;
        if (ResourceHelper.doResourcepacksHaveTexture(this.modID, emissivePath, true)) {
            return textureMap.registerSprite(new ResourceLocation(this.modID, emissivePath));
        }

        return null;
    }

    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] ops, Cuboid6 bounds, EnumFacing frontFacing, boolean isActive, boolean hasProblem, boolean isWorking) {
        boolean bloom = MetaTileEntityRenderer.useBloom();

        for (EnumFacing renderSide : EnumFacing.VALUES) {
            OverlayFace overlayFace = OverlayFace.bySide(renderSide, frontFacing);
            ActivePredicate spritePredicate = this.sprites.get(overlayFace);
            if (spritePredicate == null) {
                continue;
            }

            TextureAtlasSprite renderSprite = spritePredicate.getSprite(isActive, hasProblem, isWorking);
            TJTextures.renderFace(renderState, translation, ops, renderSide, bounds, renderSprite);

            if (!bloom) {
                continue;
            }

            TextureAtlasSprite emissiveSprite = spritePredicate.getEmissiveSprite(isActive, hasProblem, isWorking);
            if (emissiveSprite != null) {
                BloomRenderUtils.draw(ops, renderState, translation, renderSide, emissiveSprite);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, EnumFacing frontFacing, boolean isActive) {
        this.render(renderState, translation, pipeline, Cuboid6.full, frontFacing, isActive, false, true);
    }

    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, EnumFacing frontFacing, boolean isActive, boolean hasProblem, boolean isWorking) {
        this.render(renderState, translation, pipeline, Cuboid6.full, frontFacing, isActive, hasProblem, isWorking);
    }
}
