package tj.blocks.block;

import appeng.core.features.BlockDefinition;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import tj.TJ;
import tj.integration.appeng.blocks.BlockSuperInterface;
import tj.integration.appeng.tile.TileSuperInterface;

import java.util.function.Supplier;

public class TJBlocks {

    public static final Object2ObjectMap<ResourceLocation, BlockDefinition> TJ_BLOCK_DEFINITION_REGISTRY = new Object2ObjectOpenHashMap<>();

    public static BlockDefinition SUPER_INTERFACE;

    public static void init(IForgeRegistry<Block> registry) {
        SUPER_INTERFACE = registerBlock(registry, "me.super_interface", new BlockSuperInterface());
        GameRegistry.registerTileEntity(TileSuperInterface.class, SUPER_INTERFACE.maybeBlock().get().getRegistryName());
    }

    private static BlockDefinition registerBlock(IForgeRegistry<Block> registry, String resource, Block block) {
        return registerBlock(registry, resource, block, null);
    }

    private static BlockDefinition registerBlock(IForgeRegistry<Block> registry, String resource, Block block, Supplier<ItemBlock> itemBlockSupplier) {
        final ResourceLocation resourceLocation = new ResourceLocation(TJ.MODID, resource);
        final ItemBlock itemBlock;
        if (itemBlockSupplier != null) {
            itemBlock = itemBlockSupplier.get();
        } else itemBlock = new ItemBlock(block);
        itemBlock.setRegistryName(resourceLocation);
        itemBlock.setTranslationKey(resource);
        final BlockDefinition blockDefinition = new BlockDefinition(resource, block, itemBlock);
        block.setRegistryName(resourceLocation);
        block.setTranslationKey(resource);
        registry.register(block);
        TJ_BLOCK_DEFINITION_REGISTRY.put(resourceLocation, blockDefinition);
        return blockDefinition;
    }
}
