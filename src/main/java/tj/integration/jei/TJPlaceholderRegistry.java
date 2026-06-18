package tj.integration.jei;

import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.GATransparentCasing;
import gregtech.api.util.BlockInfo;
import gregtech.integration.jei.multiblock.channel.PlaceholderBlockRegistry;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
import tj.machines.TJMetaTileEntities;

public class TJPlaceholderRegistry {

    public static void init() {
        PlaceholderBlockRegistry.register(PlaceholderType.CRAFTER_HATCH, (context) -> PlaceholderBlockRegistry.MTEHolderBuilder(TJMetaTileEntities.CRAFTER_HATCHES[context.voltageTier],context.facing));
    }
}
