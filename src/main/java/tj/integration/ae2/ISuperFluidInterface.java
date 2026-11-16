package tj.integration.ae2;

import appeng.fluids.helper.IConfigurableFluidInventory;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IPriorityHost;

public interface ISuperFluidInterface extends ICustomNameObject, IPriorityHost, IFluidInterfaceHost, IConfigurableFluidInventory, ITickSetter, IPrioritySetter {

    default void setFluidAutoPull(boolean autoPull) {}

    default void setFluidAutoPush(boolean autoPush) {}
}
