package tj.integration.theoneprobe;

import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbe;
import tj.integration.theoneprobe.impl.*;
import tj.integration.theoneprobe.providers.*;

public final class TheOneProbeCompatibility {

    public static int ELEMENT_FLUIDSTACK;
    public static int ELEMENT_TJ_TEXT;
    public static int ELEMENT_ITEMLIST;
    public static int ELEMENT_FLUIDLIST;
    public static int ELEMENT_PROGRESSBAR;

    public static void registerElements() {
        ELEMENT_FLUIDSTACK = TheOneProbe.theOneProbeImp.registerElementFactory(ElementFluidStack::new);
        ELEMENT_TJ_TEXT = TheOneProbe.theOneProbeImp.registerElementFactory(ElementTJText::new);
        ELEMENT_ITEMLIST = TheOneProbe.theOneProbeImp.registerElementFactory(ElementItemList::new);
        ELEMENT_FLUIDLIST = TheOneProbe.theOneProbeImp.registerElementFactory(ElementFluidList::new);
        ELEMENT_PROGRESSBAR = TheOneProbe.theOneProbeImp.registerElementFactory(ElementProgressBar::new);
    }

    public static void registerCompatibility() {
        final ITheOneProbe probe = TheOneProbe.theOneProbeImp;
        probe.registerProvider(new ParallelControllerInfoProvider());
        probe.registerProvider(new ParallelWorkableInfoProvider());
        probe.registerProvider(new LinkedPosInfoProvider());
        probe.registerProvider(new LinkEntityInfoProvider());
        probe.registerProvider(new HeatInfoProvider());
        probe.registerProvider(new GeneratorInfoProvider());
        probe.registerProvider(new RecipeInfoProvider());
        probe.registerProvider(new ParallelItemFluidHandlerInfoProvider());
        probe.registerProvider(new StructureInfoProvider());
        probe.registerProvider(new CoverWorkableInfoProvider());
    }
}
