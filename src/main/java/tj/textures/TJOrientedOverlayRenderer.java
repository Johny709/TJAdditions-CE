package tj.textures;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class TJOrientedOverlayRenderer implements TextureUtils.IIconRegister {

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

        public ActivePredicate(TextureAtlasSprite normalSprite, TextureAtlasSprite activeSprite, TextureAtlasSprite pausedSprite, TextureAtlasSprite problemSprite) {
            this.normalSprite = normalSprite;
            this.activeSprite = activeSprite;
            this.pausedSprite = pausedSprite;
            this.problemSprite = problemSprite;
        }

        public TextureAtlasSprite getSprite(boolean active) {
            return active ? this.activeSprite : this.normalSprite;
        }

        public TextureAtlasSprite getPausedSprite() {
            return this.pausedSprite;
        }

        public TextureAtlasSprite getProblemSprite() {
            return this.problemSprite;
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
        for (OverlayFace overlayFace : this.faces) {
            String faceName = this.overlay != null ? this.overlay : overlayFace.name().toLowerCase();
            ResourceLocation normalLocation = new ResourceLocation(this.modID, String.format("blocks/%s/overlay_%s", this.basePath, faceName));
            ResourceLocation activeLocation = new ResourceLocation(this.modID, String.format("blocks/%s/overlay_%s_active", this.basePath, faceName));
            ResourceLocation pausedLocation = new ResourceLocation(this.modID, String.format("blocks/%s/overlay_%s_paused", this.basePath, faceName));
            ResourceLocation problemLocation = new ResourceLocation(this.modID, String.format("blocks/%s/overlay_%s_problem", this.basePath, faceName));
            TextureAtlasSprite normalSprite = textureMap.registerSprite(normalLocation);
            TextureAtlasSprite activeSprite = textureMap.registerSprite(activeLocation);
            TextureAtlasSprite pausedSprite = textureMap.registerSprite(pausedLocation);
            TextureAtlasSprite problemSprite = textureMap.registerSprite(problemLocation);
            this.sprites.put(overlayFace, new ActivePredicate(normalSprite, activeSprite, pausedSprite, problemSprite));
        }
    }

    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] ops, Cuboid6 bounds, EnumFacing frontFacing, boolean isActive, boolean hasProblem, boolean isWorking) {
        for (EnumFacing renderSide : EnumFacing.VALUES) {
            OverlayFace overlayFace = OverlayFace.bySide(renderSide, frontFacing);
            if (this.sprites.containsKey(overlayFace)) {
                ActivePredicate spritePredicate = this.sprites.get(overlayFace);
                TextureAtlasSprite renderSprite = spritePredicate.getSprite(isActive);
                if (hasProblem)
                    renderSprite = spritePredicate.getProblemSprite();
                if (!isWorking)
                    renderSprite = spritePredicate.getPausedSprite();
                TJTextures.renderFace(renderState, translation, ops, renderSide, bounds, renderSprite);
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
