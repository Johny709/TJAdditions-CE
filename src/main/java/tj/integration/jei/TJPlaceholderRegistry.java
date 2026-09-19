package tj.integration.jei;


import gregtech.api.GTValues;

import gregtech.integration.jei.multiblock.channel.PlaceholderBlockRegistry;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
import tj.machines.TJMetaTileEntities;


public class TJPlaceholderRegistry {

    public static void init() {
        PlaceholderBlockRegistry.register(PlaceholderType.CRAFTER_HATCH, (context) -> PlaceholderBlockRegistry.MTEHolderBuilder(TJMetaTileEntities.CRAFTER_HATCHES[Math.max(0,context.voltageTier-1)],context.facing));
    }
}
