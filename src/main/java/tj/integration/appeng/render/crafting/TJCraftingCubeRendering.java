package tj.integration.appeng.render.crafting;

import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.core.AppEng;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.integration.appeng.block.crafting.TJBlockCraftingUnit;
import tj.integration.appeng.block.crafting.TJCraftingUnitType;

import java.util.HashMap;
import java.util.Map;

/**
 * Rendering customization for the crafting cube.
 */
public class TJCraftingCubeRendering extends BlockRenderingCustomizer {

    private final String registryName;

    private final TJCraftingUnitType type;

    public TJCraftingCubeRendering(String registryName, TJCraftingUnitType type) {
        this.registryName = registryName;
        this.type = type;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
        ResourceLocation baseName = new ResourceLocation(AppEng.MOD_ID, this.registryName);

        // Disable auto-rotation
//        if (this.type != BlockCraftingUnit.CraftingUnitType.MONITOR) {
//            rendering.modelCustomizer((loc, model) -> model);
//        }
        rendering.modelCustomizer((loc, model) -> model);
        // This is the standard blockstate model
        ModelResourceLocation defaultModel = new ModelResourceLocation(baseName, "normal");

        // This is the built-in model
        String builtInName = "models/block/crafting/" + this.registryName + "/builtin";
        ModelResourceLocation builtInModelName = new ModelResourceLocation(new ResourceLocation(AppEng.MOD_ID, builtInName), "normal");

        rendering.builtInModel(builtInName, new TJCraftingCubeModel(this.type));

        rendering.stateMapper(block -> this.mapState(block, defaultModel, builtInModelName));

//        if (this.type == BlockCraftingUnit.CraftingUnitType.MONITOR) {
//            rendering.tesr(new CraftingMonitorTESR());
//        }
    }

    private Map<IBlockState, ModelResourceLocation> mapState(Block block, ModelResourceLocation defaultModel, ModelResourceLocation formedModel) {
        Map<IBlockState, ModelResourceLocation> result = new HashMap<>();
        for (IBlockState state : block.getBlockState().getValidStates()) {
            if (state.getValue(TJBlockCraftingUnit.FORMED)) {
                // Always use the builtin model if the multiblock is formed
                result.put(state, formedModel);
            } else {
                // Use the default model
                result.put(state, defaultModel);
            }
        }
        return result;
    }
}
