package tj.integration.theoneprobe;

import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbe;
import tj.integration.theoneprobe.impl.ElementFluidList;
import tj.integration.theoneprobe.impl.ElementFluidStack;
import tj.integration.theoneprobe.impl.ElementItemList;
import tj.integration.theoneprobe.impl.ElementTJText;

public final class TheOneProbeCompatibility {

    public static int ELEMENT_FLUIDSTACK;
    public static int ELEMENT_TJ_TEXT;
    public static int ELEMENT_ITEMLIST;
    public static int ELEMENT_FLUIDLIST;

    public static void registerElements() {
        ELEMENT_FLUIDSTACK = TheOneProbe.theOneProbeImp.registerElementFactory(ElementFluidStack::new);
        ELEMENT_TJ_TEXT = TheOneProbe.theOneProbeImp.registerElementFactory(ElementTJText::new);
        ELEMENT_ITEMLIST = TheOneProbe.theOneProbeImp.registerElementFactory(ElementItemList::new);
        ELEMENT_FLUIDLIST = TheOneProbe.theOneProbeImp.registerElementFactory(ElementFluidList::new);
    }

    public static void registerCompatibility() {
        ITheOneProbe probe = TheOneProbe.theOneProbeImp;
        probe.registerProvider(new ParallelControllerInfoProvider());
        probe.registerProvider(new ParallelWorkableInfoProvider());
        probe.registerProvider(new LinkedPosInfoProvider());
        probe.registerProvider(new LinkEntityInfoProvider());
        probe.registerProvider(new IHeatInfoProvider());
        probe.registerProvider(new IGeneratorInfoProvider());
        probe.registerProvider(new IRecipeInfoProvider());
        probe.registerProvider(new IParallelItemFluidHandlerInfoProvider());
        probe.registerProvider(new StructureInfoProvider());
    }
}
