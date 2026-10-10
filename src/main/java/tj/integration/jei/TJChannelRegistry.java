package tj.integration.jei;




import gregtech.integration.jei.multiblock.channel.Channel;
import gregtech.integration.jei.multiblock.channel.PlaceholderType;
import tj.machines.TJMetaTileEntities;


public class TJChannelRegistry {
    public static final PlaceholderType CRAFTER_HATCH = PlaceholderType.create("crafter hatch");

    public static void init() {

        CRAFTER_HATCH.registerResolver(context -> PlaceholderType.mteHolder(
                TJMetaTileEntities.CRAFTER_HATCHES[PlaceholderType.clampIndex(
                        context.getTier(Channel.VOLTAGE), 1,  TJMetaTileEntities.CRAFTER_HATCHES.length)],
                context.facing), Channel.VOLTAGE
        );

    }
}
