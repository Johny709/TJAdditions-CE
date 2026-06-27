package tj.integration.ae2.me;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class MachineSets extends HashSet<IGridNode> implements IMachineSet {

    public MachineSets() {}

    public MachineSets addAll(IMachineSet machineSet) {
        for (IGridNode node : machineSet)
            this.add(node);
        return this;
    }

    @Nonnull
    @Override
    public Class<? extends IGridHost> getMachineClass() {
        return null; // Not being used at all.
    }
}
