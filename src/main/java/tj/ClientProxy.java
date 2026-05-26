package tj;


import codechicken.lib.texture.TextureUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import tj.blocks.TJMetaBlocks;
import tj.blocks.block.TJBlocks;
import tj.items.TJMetaItems;
import tj.items.item.TJItems;
import tj.rendering.BakedModelLoader;
import tj.rendering.BasicStateMapper;
import tj.textures.TJTextures;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public void onPreLoad() {
        super.onPreLoad();
        TextureUtils.addIconRegister(TJTextures::register);
    }

    @Override
    public void onLoad() {

    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new BakedModelLoader());
        TJMetaBlocks.registerItemModels();
        TJMetaItems.registerModels();
        TJBlocks.TJ_BLOCK_DEFINITION_REGISTRY.forEach((location, blockDefinition) -> ModelLoader.setCustomStateMapper(blockDefinition.maybeBlock().get(), new BasicStateMapper(new ModelResourceLocation(location.toString()))));
        TJItems.TJ_ITEM_REGISTRY.forEach((location, item) -> ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(location, "inventory")));
        TJItems.TJ_ITEM_DEFINITION_REGISTRY.forEach((location, itemDefinition) -> ModelLoader.setCustomModelResourceLocation(itemDefinition.maybeItem().get(), 0, new ModelResourceLocation(location, "inventory")));
    }

    @Override
    public void onPostLoad() {
        TJBlocks.TJ_BLOCK_DEFINITION_REGISTRY.forEach((location, blockDefinition) -> Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(blockDefinition.maybeItem().get(), 0, new ModelResourceLocation(location, "inventory")));
    }
}
