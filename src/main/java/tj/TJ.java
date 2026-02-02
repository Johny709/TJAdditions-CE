package tj;

import appeng.api.config.Upgrades;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IParts;
import appeng.core.Api;
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
import tj.integration.appeng.IApiBlocks;
import tj.integration.appeng.IApiItems;
import tj.integration.appeng.IApiParts;
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
        final IApiItems items = ((IApiItems) (IItems) Api.INSTANCE.definitions().items());
        final IApiParts parts = ((IApiParts) (IParts) Api.INSTANCE.definitions().parts());
        final IApiBlocks blocks = ((IApiBlocks) (IBlocks) Api.INSTANCE.definitions().blocks());
        // Item Storage Cells
        Upgrades.FUZZY.registerItem(items.getCell65m(), 1);
        Upgrades.INVERTER.registerItem(items.getCell65m(), 1);
        Upgrades.STICKY.registerItem(items.getCell65m(), 1);

        Upgrades.FUZZY.registerItem(items.getCell262m(), 1);
        Upgrades.INVERTER.registerItem(items.getCell262m(), 1);
        Upgrades.STICKY.registerItem(items.getCell262m(), 1);

        Upgrades.FUZZY.registerItem(items.getCell1048m(), 1);
        Upgrades.INVERTER.registerItem(items.getCell1048m(), 1);
        Upgrades.STICKY.registerItem(items.getCell1048m(), 1);

        Upgrades.FUZZY.registerItem(items.getCellDigitalSingularity(), 1);
        Upgrades.INVERTER.registerItem(items.getCellDigitalSingularity(), 1);
        Upgrades.STICKY.registerItem(items.getCellDigitalSingularity(), 1);

        // Fluid Storage Cells
        Upgrades.INVERTER.registerItem(items.getFluidCell65m(), 1);
        Upgrades.STICKY.registerItem(items.getFluidCell65m(), 1);

        Upgrades.INVERTER.registerItem(items.getFluidCell262m(), 1);
        Upgrades.STICKY.registerItem(items.getFluidCell262m(), 1);

        Upgrades.INVERTER.registerItem(items.getFluidCell1048m(), 1);
        Upgrades.STICKY.registerItem(items.getFluidCell1048m(), 1);

        Upgrades.INVERTER.registerItem(items.getFluidCellDigitalSingularity(), 1);
        Upgrades.STICKY.registerItem(items.getFluidCellDigitalSingularity(), 1);

        // Super Interface
        Upgrades.CRAFTING.registerItem(parts.getSuperInterface(), 1);
        Upgrades.CRAFTING.registerItem(blocks.getSuperInterface(), 1);
        Upgrades.PATTERN_EXPANSION.registerItem(parts.getSuperInterface(), 3);
        Upgrades.PATTERN_EXPANSION.registerItem(blocks.getSuperInterface(), 3);

        // Super Fluid Interface
        Upgrades.CAPACITY.registerItem(parts.getSuperFluidInterface(), 4);
        Upgrades.CAPACITY.registerItem(blocks.getSuperFluidInterface(), 4);
    }
}
