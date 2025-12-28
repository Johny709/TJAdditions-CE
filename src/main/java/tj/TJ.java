package tj;

import gregtech.api.GTValues;
import gregtech.api.util.GTLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import tj.blocks.TJMetaBlocks;
import tj.capability.TJSimpleCapabilityManager;
import tj.gui.uifactory.PlayerUIFactory;
import tj.integration.theoneprobe.TheOneProbeCompatibility;
import tj.items.TJCoverBehaviours;
import tj.machines.TJMetaTileEntities;


@Mod(modid = TJ.MODID, name = TJ.NAME, version = TJ.VERSION)
public class TJ {

    public static final String MODID = "tj";
    public static final String NAME = "TJ";
    public static final String VERSION = "1.0";

    public static final String MODPACK_VERSION = "1.4";

    @SidedProxy(modId = MODID, clientSide = "tj.ClientProxy", serverSide = "tj.CommonProxy")
    public static CommonProxy proxy;

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.onPreLoad();
        PlayerUIFactory.INSTANCE.init();
        TJMetaBlocks.init();
        TJMetaTileEntities.init();
        TJSimpleCapabilityManager.init();
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.onLoad();
        if (GTValues.isModLoaded(GTValues.MODID_TOP)) {
            GTLog.logger.info("TheOneProbe found. Enabling integration...");
            TheOneProbeCompatibility.registerCompatibility();
        }
        TJCoverBehaviours.init();
    }
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.onPostLoad();
    }
}
