package tj.rendering;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;

public class BasicStateMapper extends StateMapperBase {

    private final ModelResourceLocation modelResourceLocation;

    public BasicStateMapper(ModelResourceLocation modelResourceLocation) {
        this.modelResourceLocation = modelResourceLocation;
    }

    @Override
    protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
        return this.modelResourceLocation;
    }
}
