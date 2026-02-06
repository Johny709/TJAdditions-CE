package appeng.render.crafting;

import appeng.client.render.cablebus.CubeBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;

public class TJUnitBakedModel extends TJCraftingCubeBakedModel {

    private final TextureAtlasSprite unitTexture;

    public TJUnitBakedModel(VertexFormat format, TextureAtlasSprite ringCorner, TextureAtlasSprite ringHor, TextureAtlasSprite ringVer, TextureAtlasSprite unitTexture) {
        super(format, ringCorner, ringHor, ringVer);
        this.unitTexture = unitTexture;
    }

    @Override
    protected void addInnerCube(EnumFacing facing, IBlockState state, CubeBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2) {
        builder.setTexture(this.unitTexture);
        builder.addCube(x1, y1, z1, x2, y2, z2);
    }
}
