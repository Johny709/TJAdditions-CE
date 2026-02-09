package tj.textures;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.SimpleSidedCubeRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class TJSimpleSidedCubeRenderer implements ICubeRenderer, TextureUtils.IIconRegister {

    private final String modID;
    private final String basePath;

    @SideOnly(Side.CLIENT)
    private Map<SimpleSidedCubeRenderer.RenderSide, TextureAtlasSprite> sprites;

    public TJSimpleSidedCubeRenderer(String modID, String basePath) {
        this.modID = modID;
        this.basePath = basePath;
        TJTextures.iconRegisters.add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap textureMap) {
        this.sprites = new HashMap<>();
        for (SimpleSidedCubeRenderer.RenderSide overlayFace : SimpleSidedCubeRenderer.RenderSide.values()) {
            String faceName = overlayFace.name().toLowerCase();
            ResourceLocation resourceLocation = new ResourceLocation(this.modID, String.format("%s/%s", this.basePath, faceName));
            this.sprites.put(overlayFace, textureMap.registerSprite(resourceLocation));
        }
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide renderSide) {
        return this.sprites.get(renderSide);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getParticleSprite() {
        return getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.TOP);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, Cuboid6 bounds) {
        for (EnumFacing renderSide : EnumFacing.VALUES) {
            SimpleSidedCubeRenderer.RenderSide overlayFace = SimpleSidedCubeRenderer.RenderSide.bySide(renderSide);
            TextureAtlasSprite renderSprite = this.sprites.get(overlayFace);
            TJTextures.renderFace(renderState, translation, pipeline, renderSide, bounds, renderSprite);
        }
    }
}
