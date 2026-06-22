package tj.integration.theoneprobe;

import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbe;
import tj.integration.theoneprobe.impl.ElementFluidStack;

public class TheOneProbeCompatibility {

    public static int ELEMENT_FLUIDSTACK;

    public static void registerElements() {
        ELEMENT_FLUIDSTACK = TheOneProbe.theOneProbeImp.registerElementFactory(ElementFluidStack::new);
    }

    public static void registerCompatibility() {
        ITheOneProbe probe = TheOneProbe.theOneProbeImp;
        probe.registerProvider(new ParallelControllerInfoProvider());
        probe.registerProvider(new ParallelWorkableInfoProvider());
        probe.registerProvider(new LinkedPosInfoProvider());
        probe.registerProvider(new LinkEntityInfoProvider());
        probe.registerProvider(new IHeatInfoProvider());
        probe.registerProvider(new IGeneratorInfoProvider());
        probe.registerProvider(new IItemFluidHandlerInfoProvider());
        probe.registerProvider(new IParallelItemFluidHandlerInfoProvider());
        probe.registerProvider(new StructureInfoProvider());
    }
}
