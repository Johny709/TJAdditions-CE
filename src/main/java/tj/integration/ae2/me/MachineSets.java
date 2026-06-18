package tj.integration.ae2.me;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class MachineSets extends HashSet<IGridNode> implements IMachineSet {

    public MachineSets() {}

    public MachineSets tryToAdd(IMachineSet machineSet) {
        if (machineSet instanceof Set)
            this.addAll((Set) machineSet);
        return this;
    }

    @Nonnull
    @Override
    public Class<? extends IGridHost> getMachineClass() {
        return null;
    }
}
