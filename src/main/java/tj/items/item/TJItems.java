package tj.items.item;

import appeng.api.definitions.IItemDefinition;
import appeng.core.Api;
import appeng.core.features.ItemDefinition;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import tj.TJ;
import tj.integration.appeng.items.TJFluidStorageCell;
import tj.integration.appeng.items.TJBlockContainerItemStorageCell;
import tj.integration.appeng.items.TJItemStorageCell;

public class TJItems {

    public static final Object2ObjectMap<ResourceLocation, Item> TJ_ITEM_REGISTRY = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectMap<ResourceLocation, IItemDefinition> TJ_ITEM_DEFINITION_REGISTRY = new Object2ObjectOpenHashMap<>();

    public static Item UNBREAKABLE_AXE;
    public static Item UNBREAKABLE_HOE;
    public static Item UNBREAKABLE_SHEARS;

    public static IItemDefinition MATERIAL_ITEM_CELL_65536K;
    public static IItemDefinition MATERIAL_ITEM_CELL_262144K;
    public static IItemDefinition MATERIAL_ITEM_CELL_1048M;
    public static IItemDefinition MATERIAL_ITEM_CELL_DIGITAL_SINGULARITY;
    public static IItemDefinition MATERIAL_FLUID_CELL_65536K;
    public static IItemDefinition MATERIAL_FLUID_CELL_262144K;
    public static IItemDefinition MATERIAL_FLUID_CELL_1048M;
    public static IItemDefinition MATERIAL_FLUID_CELL_DIGITAL_SINGULARITY;

    public static IItemDefinition ITEM_CELL_65536K;
    public static IItemDefinition ITEM_CELL_262144K;
    public static IItemDefinition ITEM_CELL_1048M;
    public static IItemDefinition ITEM_CELL_DIGITAL_SINGULARITY;
    public static IItemDefinition FLUID_CELL_65536K;
    public static IItemDefinition FLUID_CELL_262144K;
    public static IItemDefinition FLUID_CELL_1048M;
    public static IItemDefinition FLUID_CELL_DIGITAL_SINGULARITY;
    public static IItemDefinition ITEM_BLOCK_CONTAINER_65K;

    public static void init(IForgeRegistry<Item> registry) {
        UNBREAKABLE_AXE = registerItem(registry, new UnbreakableAxe(Item.ToolMaterial.DIAMOND));
        UNBREAKABLE_HOE = registerItem(registry, new UnbreakableHoe(Item.ToolMaterial.DIAMOND));
        UNBREAKABLE_SHEARS = registerItem(registry, new UnbreakableShears());
        MATERIAL_ITEM_CELL_65536K = registerItem(registry, new ItemDefinition("me.material.item_cell.65536k", new Item()));
        MATERIAL_ITEM_CELL_262144K = registerItem(registry, new ItemDefinition("me.material.item_cell.262144k", new Item()));
        MATERIAL_ITEM_CELL_1048M = registerItem(registry, new ItemDefinition("me.material.item_cell.1048m", new Item()));
        MATERIAL_ITEM_CELL_DIGITAL_SINGULARITY = registerItem(registry, new ItemDefinition("me.material.item_cell.digital_singularity", new Item()));
        MATERIAL_FLUID_CELL_65536K = registerItem(registry, new ItemDefinition("me.material.fluid_cell.65536k", new Item()));
        MATERIAL_FLUID_CELL_262144K = registerItem(registry, new ItemDefinition("me.material.fluid_cell.262144k", new Item()));
        MATERIAL_FLUID_CELL_1048M = registerItem(registry, new ItemDefinition("me.material.fluid_cell.1048m", new Item()));
        MATERIAL_FLUID_CELL_DIGITAL_SINGULARITY = registerItem(registry, new ItemDefinition("me.material.fluid_cell.digital_singularity", new Item()));

        ITEM_CELL_65536K = registerItem(registry, new ItemDefinition("me.item_cell.65536k", new TJItemStorageCell(MATERIAL_ITEM_CELL_65536K, 65536)));
        ITEM_CELL_262144K = registerItem(registry, new ItemDefinition("me.item_cell.262144k", new TJItemStorageCell(MATERIAL_ITEM_CELL_262144K, 262144)));
        ITEM_CELL_1048M = registerItem(registry, new ItemDefinition("me.item_cell.1048m", new TJItemStorageCell(MATERIAL_ITEM_CELL_1048M, 1048576)));
        ITEM_CELL_DIGITAL_SINGULARITY = registerItem(registry, new ItemDefinition("me.item_cell.digital_singularity", new TJItemStorageCell(MATERIAL_ITEM_CELL_DIGITAL_SINGULARITY, Integer.MAX_VALUE)));
        FLUID_CELL_65536K = registerItem(registry, new ItemDefinition("me.fluid_cell.65536k", new TJFluidStorageCell(MATERIAL_FLUID_CELL_65536K, 65536)));
        FLUID_CELL_262144K = registerItem(registry, new ItemDefinition("me.fluid_cell.262144k", new TJFluidStorageCell(MATERIAL_FLUID_CELL_262144K, 262144)));
        FLUID_CELL_1048M = registerItem(registry, new ItemDefinition("me.fluid_cell.1048m", new TJFluidStorageCell(MATERIAL_FLUID_CELL_1048M, 1048576)));
        FLUID_CELL_DIGITAL_SINGULARITY = registerItem(registry, new ItemDefinition("me.fluid_cell.digital_singularity", new TJFluidStorageCell(MATERIAL_FLUID_CELL_DIGITAL_SINGULARITY, Integer.MAX_VALUE)));
        ITEM_BLOCK_CONTAINER_65K = registerItem(registry, new ItemDefinition("me.block_container.item_cell.65536k", new TJBlockContainerItemStorageCell(Api.INSTANCE.definitions().materials().cell1kPart(), 64)));
    }

    private static Item registerItem(IForgeRegistry<Item> registry, Item item) {
        registry.register(item);
        TJ_ITEM_REGISTRY.put(item.getRegistryName(), item);
        return item;
    }

    private static IItemDefinition registerItem(IForgeRegistry<Item> registry, IItemDefinition itemDefinition) {
        final Item item = itemDefinition.maybeItem().orElse(null);
        final ResourceLocation resourceLocation = new ResourceLocation(TJ.MODID, itemDefinition.identifier());
        assert item != null;
        item.setRegistryName(resourceLocation);
        item.setTranslationKey(itemDefinition.identifier());
        registry.register(item);
        TJ_ITEM_DEFINITION_REGISTRY.put(resourceLocation, itemDefinition);
        return itemDefinition;
    }
}
