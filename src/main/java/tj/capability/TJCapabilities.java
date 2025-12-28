package tj.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class TJCapabilities {

    @CapabilityInject(IMultipleWorkable.class)
    public static Capability<IMultipleWorkable> CAPABILITY_MULTIPLE_WORKABLE = null;

    @CapabilityInject(IMultiControllable.class)
    public static Capability<IMultiControllable> CAPABILITY_MULTI_CONTROLLABLE = null;

    @CapabilityInject(IParallelController.class)
    public static Capability<IParallelController> CAPABILITY_PARALLEL_CONTROLLER = null;

    @CapabilityInject(LinkPos.class)
    public static Capability<LinkPos> CAPABILITY_LINK_POS = null;

    @CapabilityInject(LinkEntity.class)
    public static Capability<LinkEntity> CAPABILITY_LINK_ENTITY = null;

    @CapabilityInject(IHeatInfo.class)
    public static Capability<IHeatInfo> CAPABILITY_HEAT = null;

    @CapabilityInject(IItemFluidHandlerInfo.class)
    public static Capability<IItemFluidHandlerInfo> CAPABILITY_ITEM_FLUID_HANDLING = null;

    @CapabilityInject(IGeneratorInfo.class)
    public static Capability<IGeneratorInfo> CAPABILITY_GENERATOR = null;
}
