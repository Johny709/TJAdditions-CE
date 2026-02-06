package tj.capability;

import static gregtech.api.capability.SimpleCapabilityManager.registerCapabilityWithNoDefault;

public final class TJSimpleCapabilityManager {

    public static void init() {
        registerCapabilityWithNoDefault(IMultiControllable.class);
        registerCapabilityWithNoDefault(IMultipleWorkable.class);
        registerCapabilityWithNoDefault(IParallelController.class);
        registerCapabilityWithNoDefault(LinkPos.class);
        registerCapabilityWithNoDefault(LinkEntity.class);
        registerCapabilityWithNoDefault(IHeatInfo.class);
        registerCapabilityWithNoDefault(IGeneratorInfo.class);
        registerCapabilityWithNoDefault(IItemFluidHandlerInfo.class);
    }
}
