package tj.integration.ae2;

import appeng.api.config.CondenserOutput;
import appeng.api.config.LockCraftingMode;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;

public interface ISuperInterface extends ICustomNameObject, IPriorityHost, IInterfaceHost {

    void setBlockingMode(boolean blockingMode);

    void setLockCrafting(LockCraftingMode lockCraftingMode);

    void setInterfaceTerminal(boolean interfaceTerminal);

    void setFluidPacket(boolean fluidPacket);

    void setSplittingItemsFluids(boolean splittingItemsFluids);

    void setBlockModeEx(CondenserOutput blockModeEx);

    void setIntelligentBlocking(boolean intelligentBlocking);

    void setStackSize(String size, String id);

    String getStackSize(int index);

    void setPriority(String priority, String id);

    void setAutoPull(boolean autoPull);

    default int getTickTime() {
        return 1;
    }

    default void setTickTime(String tickTime, String id) {

    }
}
