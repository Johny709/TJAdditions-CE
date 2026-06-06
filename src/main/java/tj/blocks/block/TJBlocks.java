package tj.blocks.block;

import appeng.block.AEBaseItemBlock;
import appeng.core.features.BlockDefinition;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import tj.TJ;
import tj.integration.ae2.blocks.*;
import tj.integration.ae2.tile.*;

import java.util.function.Function;

public class TJBlocks {

    public static final Object2ObjectMap<ResourceLocation, BlockDefinition> TJ_BLOCK_DEFINITION_REGISTRY = new Object2ObjectOpenHashMap<>();

    public static BlockDefinition SUPER_INTERFACE;
    public static BlockDefinition SUPER_FLUID_INTERFACE;
    public static BlockDefinition SUPER_DUAL_INTERFACE;
    public static BlockDefinition PATTERN_INTERFACE;
    public static BlockDefinition STOCKING_INTERFACE;

    public static BlockDefinition CRAFTING_STORAGE_65536K;
    public static BlockDefinition CRAFTING_STORAGE_262144K;
    public static BlockDefinition CRAFTING_STORAGE_1048M;
    public static BlockDefinition CRAFTING_STORAGE_SINGULARITY;

    public static void init(IForgeRegistry<Block> registry) {
        SUPER_INTERFACE = registerBlock(registry, "me.super_interface", new BlockSuperInterface(), AEBaseItemBlock::new);
        SUPER_FLUID_INTERFACE = registerBlock(registry, "me.super_fluid_interface", new BlockSuperFluidInterface(), AEBaseItemBlock::new);
        SUPER_DUAL_INTERFACE = registerBlock(registry, "me.super_dual_interface", new BlockSuperDualInterface(), AEBaseItemBlock::new);
        PATTERN_INTERFACE = registerBlock(registry, "me.pattern_interface", new BlockPatternInterface(), AEBaseItemBlock::new);
        STOCKING_INTERFACE = registerBlock(registry, "me.stocking_interface", new BlockStockingInterface(), AEBaseItemBlock::new);

        CRAFTING_STORAGE_65536K = registerBlock(registry, "me.crafting_storage.65536k", new BlockTJCraftingUnit(BlockTJCraftingUnit.TJCraftingUnitType.STORAGE_65536k));
        CRAFTING_STORAGE_262144K = registerBlock(registry, "me.crafting_storage.262144k", new BlockTJCraftingUnit(BlockTJCraftingUnit.TJCraftingUnitType.STORAGE_262144k));
        CRAFTING_STORAGE_1048M = registerBlock(registry, "me.crafting_storage.1048m", new BlockTJCraftingUnit(BlockTJCraftingUnit.TJCraftingUnitType.STORAGE_1048M));
        CRAFTING_STORAGE_SINGULARITY = registerBlock(registry, "me.crafting_storage.singularity", new BlockTJCraftingUnit(BlockTJCraftingUnit.TJCraftingUnitType.STORAGE_SINGULARITY));

        GameRegistry.registerTileEntity(TileSuperInterface.class, new ResourceLocation(TJ.MODID, "me.super_interface"));
        GameRegistry.registerTileEntity(TileSuperFluidInterface.class, new ResourceLocation(TJ.MODID, "me.super_fluid_interface"));
        GameRegistry.registerTileEntity(TileSuperDualInterface.class, new ResourceLocation(TJ.MODID, "me.super_dual_interface"));
        GameRegistry.registerTileEntity(TilePatternInterface.class, new ResourceLocation(TJ.MODID, "me.pattern_interface"));
        GameRegistry.registerTileEntity(TileStockingInterface.class, new ResourceLocation(TJ.MODID, "me.stocking_interface"));
        GameRegistry.registerTileEntity(TileTJCraftingStorageTile.class, new ResourceLocation(TJ.MODID, "me.crafting_storage"));
    }

    private static BlockDefinition registerBlock(IForgeRegistry<Block> registry, String resource, Block block) {
        return registerBlock(registry, resource, block, null);
    }

    private static BlockDefinition registerBlock(IForgeRegistry<Block> registry, String resource, Block block, Function<Block, ItemBlock> itemBlockFunction) {
        final ResourceLocation resourceLocation = new ResourceLocation(TJ.MODID, resource);
        final ItemBlock itemBlock;
        if (itemBlockFunction != null) {
            itemBlock = itemBlockFunction.apply(block);
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
