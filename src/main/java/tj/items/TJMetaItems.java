package tj.items;

import gregtech.api.items.metaitem.MetaItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class TJMetaItems {

    public static List<MetaItem<?>> ITEMS = MetaItem.getMetaItems();

    public static MetaItem<?>.MetaValueItem TOOLBOX;
    public static MetaItem<?>.MetaValueItem CREATIVE_FLUID_COVER;
    public static MetaItem<?>.MetaValueItem CREATIVE_ITEM_COVER;
    public static MetaItem<?>.MetaValueItem CREATIVE_ENERGY_COVER;
    public static MetaItem<?>.MetaValueItem LINKING_DEVICE;
    public static MetaItem<?>.MetaValueItem VOID_PLUNGER;
    public static MetaItem<?>.MetaValueItem NBT_READER;
    public static MetaItem<?>.MetaValueItem FLUID_REGULATOR_UHV;
    public static MetaItem<?>.MetaValueItem FLUID_REGULATOR_UMV;
    public static MetaItem<?>.MetaValueItem FLUID_REGULATOR_MAX;
    public static MetaItem<?>.MetaValueItem REMOTE_MULTIBLOCK_CONTROLLER;

    public static final MetaItem<?>.MetaValueItem[] UNIVERSAL_CIRCUITS = new MetaItem.MetaValueItem[15];
    public static final MetaItem<?>.MetaValueItem[] ENDER_FLUID_COVERS = new MetaItem.MetaValueItem[12];
    public static final MetaItem<?>.MetaValueItem[] ENDER_ITEM_COVERS = new MetaItem.MetaValueItem[12];
    public static final MetaItem<?>.MetaValueItem[] ENDER_ENERGY_COVERS = new MetaItem.MetaValueItem[12];
    public static final MetaItem<?>.MetaValueItem[] TURBINE_UPGRADES = new MetaItem.MetaValueItem[6];

    public static void init() {
        TJMetaItem1 item = new TJMetaItem1();
        item.setRegistryName("meta_item");
    }

    public static void registerOreDict() {
        for (MetaItem<?> item : ITEMS) {
            if (item instanceof TJMetaItem1) {
                ((TJMetaItem1) item).registerOreDict();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        MinecraftForge.EVENT_BUS.register(TJMetaItems.class);
        for (MetaItem<?> item : ITEMS) {
            item.registerModels();
        }
    }
}
