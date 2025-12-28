package tj;


import codechicken.lib.texture.TextureUtils;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import tj.blocks.TJMetaBlocks;
import tj.items.TJMetaItems;
import tj.items.item.TJItems;
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
        TJMetaBlocks.registerItemModels();
        TJMetaItems.registerModels();
        TJItems.TJ_ITEM_REGISTRY.forEach((item, location) -> ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory")));
    }
}
