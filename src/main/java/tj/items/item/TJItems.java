package tj.items.item;

import appeng.api.definitions.IItemDefinition;
import appeng.api.parts.IPartItem;
import appeng.core.Api;
import appeng.core.features.ItemDefinition;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import tj.TJ;
import tj.integration.ae2.items.*;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TJItems {

    public static final Object2ObjectMap<ResourceLocation, Item> TJ_ITEM_REGISTRY = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectMap<ResourceLocation, IItemDefinition> TJ_ITEM_DEFINITION_REGISTRY = new Object2ObjectOpenHashMap<>();
    public static final Object2IntMap<Item> UPGRADES = new Object2IntOpenHashMap<>();

    public static Item UNBREAKABLE_AXE;
    public static Item UNBREAKABLE_HOE;
    public static Item UNBREAKABLE_SHEARS;
    public static Item MAX_CAPACITY_UPGRADE;

    public static IItemDefinition PART_SUPER_INTERFACE;
    public static IItemDefinition PART_SUPER_FLUID_INTERFACE;
    public static IItemDefinition PART_SUPER_DUAL_INTERFACE;
    public static IItemDefinition PART_PATTERN_INTERFACE;
    public static IItemDefinition PART_STOCKING_INTERFACE;
    public static IItemDefinition PART_STOCKING_FLUID_INTERFACE;

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
    public static IItemDefinition ITEM_BLOCK_CONTAINER_64K;
    public static IItemDefinition ITEM_BLOCK_CONTAINER_65536K;
    public static IItemDefinition ITEM_BLOCK_CONTAINER_SINGULARITY;

    public static void init(IForgeRegistry<Item> registry) {
        UNBREAKABLE_AXE = registerItem(registry, "unbreakable_axe", new ItemUnbreakableAxe(Item.ToolMaterial.DIAMOND));
        UNBREAKABLE_HOE = registerItem(registry, "unbreakable_hoe", new ItemUnbreakableHoe(Item.ToolMaterial.DIAMOND));
        UNBREAKABLE_SHEARS = registerItem(registry, "unbreakable_shears", new ItemUnbreakableShears());
        MAX_CAPACITY_UPGRADE = registerItem(registry, "me.max_capacity_upgrade", new ItemMaxCapacityUpgrade());

        PART_SUPER_INTERFACE = registerItem(registry, item -> new ItemDefinition("me.part.super_interface", new ItemPartSuperInterface()));
        PART_SUPER_FLUID_INTERFACE = registerItem(registry, item -> new ItemDefinition("me.part.super_fluid_interface", new ItemPartSuperFluidInterface()));
        PART_SUPER_DUAL_INTERFACE = registerItem(registry, item -> new ItemDefinition("me.part.super_dual_interface", new ItemPartSuperDualInterface()));
        PART_PATTERN_INTERFACE = registerItem(registry, item -> new ItemDefinition("me.part.pattern_interface", new ItemPartPatternInterface()));
        PART_STOCKING_INTERFACE = registerItem(registry, item -> new ItemDefinition("me.part.stocking_interface", new ItemPartStockingInterface()));
        PART_STOCKING_FLUID_INTERFACE = registerItem(registry, item -> new ItemDefinition("me.part.stocking_fluid_interface", new ItemPartStockingFluidInterface()));

        MATERIAL_ITEM_CELL_65536K = registerItem(registry, item -> new ItemDefinition("me.material.item_cell.65536k", item));
        MATERIAL_ITEM_CELL_262144K = registerItem(registry, item -> new ItemDefinition("me.material.item_cell.262144k", item));
        MATERIAL_ITEM_CELL_1048M = registerItem(registry, item -> new ItemDefinition("me.material.item_cell.1048m", item));
        MATERIAL_ITEM_CELL_DIGITAL_SINGULARITY = registerItem(registry, item -> new ItemDefinition("me.material.item_cell.digital_singularity", item));
        MATERIAL_FLUID_CELL_65536K = registerItem(registry, item -> new ItemDefinition("me.material.fluid_cell.65536k", item));
        MATERIAL_FLUID_CELL_262144K = registerItem(registry, item -> new ItemDefinition("me.material.fluid_cell.262144k", item));
        MATERIAL_FLUID_CELL_1048M = registerItem(registry, item -> new ItemDefinition("me.material.fluid_cell.1048m", item));
        MATERIAL_FLUID_CELL_DIGITAL_SINGULARITY = registerItem(registry, item -> new ItemDefinition("me.material.fluid_cell.digital_singularity", item));

        ITEM_CELL_65536K = registerItem(registry, item -> new ItemDefinition("me.item_cell.65536k", new TJItemStorageCell(MATERIAL_ITEM_CELL_65536K, 65536)));
        ITEM_CELL_262144K = registerItem(registry, item -> new ItemDefinition("me.item_cell.262144k", new TJItemStorageCell(MATERIAL_ITEM_CELL_262144K, 262144)));
        ITEM_CELL_1048M = registerItem(registry, item -> new ItemDefinition("me.item_cell.1048m", new TJItemStorageCell(MATERIAL_ITEM_CELL_1048M, 1048576)));
        ITEM_CELL_DIGITAL_SINGULARITY = registerItem(registry, item -> new ItemDefinition("me.item_cell.digital_singularity", new TJItemStorageCell(MATERIAL_ITEM_CELL_DIGITAL_SINGULARITY, Integer.MAX_VALUE)));
        FLUID_CELL_65536K = registerItem(registry, item -> new ItemDefinition("me.fluid_cell.65536k", new TJFluidStorageCell(MATERIAL_FLUID_CELL_65536K, 65536)));
        FLUID_CELL_262144K = registerItem(registry, item -> new ItemDefinition("me.fluid_cell.262144k", new TJFluidStorageCell(MATERIAL_FLUID_CELL_262144K, 262144)));
        FLUID_CELL_1048M = registerItem(registry, item -> new ItemDefinition("me.fluid_cell.1048m", new TJFluidStorageCell(MATERIAL_FLUID_CELL_1048M, 1048576)));
        FLUID_CELL_DIGITAL_SINGULARITY = registerItem(registry, item -> new ItemDefinition("me.fluid_cell.digital_singularity", new TJFluidStorageCell(MATERIAL_FLUID_CELL_DIGITAL_SINGULARITY, Integer.MAX_VALUE)));

        ITEM_BLOCK_CONTAINER_64K = registerItem(registry, item -> new ItemDefinition("me.block_container.item_cell.64k", new TJBlockContainerItemStorageCell(Api.INSTANCE.definitions().materials().cell1kPart(), 64)));
        ITEM_BLOCK_CONTAINER_65536K = registerItem(registry, item -> new ItemDefinition("me.block_container.item_cell.65536k", new TJBlockContainerItemStorageCell(MATERIAL_ITEM_CELL_65536K, 65536)));
        ITEM_BLOCK_CONTAINER_SINGULARITY = registerItem(registry, item -> new ItemDefinition("me.block_container.item_cell.singularity", new TJBlockContainerItemStorageCell(MATERIAL_ITEM_CELL_DIGITAL_SINGULARITY, Integer.MAX_VALUE)));

        Api.INSTANCE.getPartModels().registerModels(TJ_ITEM_DEFINITION_REGISTRY.values().stream()
                .map(definition -> definition.maybeItem().orElse(null))
                .filter(item -> item instanceof IPartItem<?> && item.getRegistryName() != null)
                .map(item -> "part/" + item.getRegistryName().getPath())
                .flatMap(path -> Stream.of(path + "_base", path + "_off", path + "_on", path + "_has_channel"))
                .map(path -> new ResourceLocation(TJ.MODID, path))
                .collect(Collectors.toList()));
    }

    private static Item registerItem(IForgeRegistry<Item> registry, String location, Item item) {
        item.setRegistryName(new ResourceLocation(TJ.MODID, location));
        item.setTranslationKey(location);
        registry.register(item);
        TJ_ITEM_REGISTRY.put(item.getRegistryName(), item);
        return item;
    }

    private static IItemDefinition registerItem(IForgeRegistry<Item> registry, Function<Item, IItemDefinition> itemDefinition) {
        final IItemDefinition definition = itemDefinition.apply(new Item());
        final Item item = definition.maybeItem().orElse(null);
        final ResourceLocation resourceLocation = new ResourceLocation(TJ.MODID, definition.identifier());
        assert item != null;
        item.setRegistryName(resourceLocation);
        item.setTranslationKey(definition.identifier());
        registry.register(item);
        TJ_ITEM_DEFINITION_REGISTRY.put(resourceLocation, definition);
        return definition;
    }
}
