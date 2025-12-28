package tj.integration.theoneprobe;

import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbe;

public class TheOneProbeCompatibility {

    public static void registerCompatibility() {
        ITheOneProbe probe = TheOneProbe.theOneProbeImp;
        probe.registerProvider(new ParallelControllerInfoProvider());
        probe.registerProvider(new ParallelWorkableInfoProvider());
        probe.registerProvider(new LinkedPosInfoProvider());
        probe.registerProvider(new LinkEntityInfoProvider());
        probe.registerProvider(new IHeatInfoProvider());
        probe.registerProvider(new IGeneratorInfoProvider());
        probe.registerProvider(new IItemFluidHandlerInfoProvider());
        probe.registerProvider(new StructureInfoProvider());
    }
}
