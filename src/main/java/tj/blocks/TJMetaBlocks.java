package tj.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.stream.Collectors;


public class TJMetaBlocks {

    public static final Set<EnergyPortCasings> ENERGY_PORT_CASINGS = new HashSet<>();
    public static final Set<AdvEnergyPortCasings> ADV_ENERGY_PORT_CASINGS = new HashSet<>();

    public static BlockSolidCasings SOLID_CASING;
    public static EnergyPortCasings ENERGY_PORT_CASING;
    public static AdvEnergyPortCasings ADV_ENERGY_PORT_CASING;
    public static BlockPipeCasings PIPE_CASING;
    public static BlockFusionCasings FUSION_CASING;
    public static BlockFusionGlass FUSION_GLASS;
    public static AbilityBlocks ABILITY_BLOCKS;

    public static void init() {
        SOLID_CASING = new BlockSolidCasings();

        ENERGY_PORT_CASINGS.add(ENERGY_PORT_CASING = new EnergyPortCasings(2));
        ENERGY_PORT_CASINGS.add(new EnergyPortCasings(4));
        ENERGY_PORT_CASINGS.add(new EnergyPortCasings(16));
        ENERGY_PORT_CASINGS.add(new EnergyPortCasings(64));
        ENERGY_PORT_CASINGS.add(new EnergyPortCasings(128));
        ENERGY_PORT_CASINGS.add(new EnergyPortCasings(256));

        ADV_ENERGY_PORT_CASINGS.add(ADV_ENERGY_PORT_CASING = new AdvEnergyPortCasings(2));
        ADV_ENERGY_PORT_CASINGS.add(new AdvEnergyPortCasings(4));
        ADV_ENERGY_PORT_CASINGS.add(new AdvEnergyPortCasings(16));
        ADV_ENERGY_PORT_CASINGS.add(new AdvEnergyPortCasings(64));
        ADV_ENERGY_PORT_CASINGS.add(new AdvEnergyPortCasings(128));
        ADV_ENERGY_PORT_CASINGS.add(new AdvEnergyPortCasings(256));

        PIPE_CASING = new BlockPipeCasings();
        FUSION_CASING = new BlockFusionCasings();
        FUSION_GLASS = new BlockFusionGlass();
        ABILITY_BLOCKS = new AbilityBlocks();

    }

    @SideOnly(Side.CLIENT)
    public static void registerItemModels() {
        registerItemModel(SOLID_CASING);
        registerItemModel(PIPE_CASING);
        registerItemModel(FUSION_CASING);
        registerItemModel(FUSION_GLASS);
        registerItemModel(ABILITY_BLOCKS);
        ENERGY_PORT_CASINGS.forEach(TJMetaBlocks::registerItemModel);
        ADV_ENERGY_PORT_CASINGS.forEach(TJMetaBlocks::registerItemModel);
    }

    @SideOnly(Side.CLIENT)
    private static void registerItemModel(Block block) {
        for (IBlockState state : block.getBlockState().getValidStates()) {
            //noinspection ConstantConditions
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block),
                    block.getMetaFromState(state),
                    new ModelResourceLocation(block.getRegistryName(),
                            statePropertiesToString(state.getProperties())));
        }
    }

    private static String statePropertiesToString(Map<IProperty<?>, Comparable<?>> properties) {
        StringBuilder stringbuilder = new StringBuilder();

        List<Map.Entry<IProperty<?>, Comparable<?>>> entries = properties.entrySet().stream()
                .sorted(Comparator.comparing(c -> c.getKey().getName()))
                .collect(Collectors.toList());

        for (Map.Entry<IProperty<?>, Comparable<?>> entry : entries) {
            if (stringbuilder.length() != 0) {
                stringbuilder.append(",");
            }

            IProperty<?> property = entry.getKey();
            stringbuilder.append(property.getName());
            stringbuilder.append("=");
            stringbuilder.append(getPropertyName(property, entry.getValue()));
        }

        if (stringbuilder.length() == 0) {
            stringbuilder.append("normal");
        }

        return stringbuilder.toString();
    }
    private static <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> value) {
        return property.getName((T) value);
    }

}
