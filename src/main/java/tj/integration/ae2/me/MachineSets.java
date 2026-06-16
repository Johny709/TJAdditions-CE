package tj.integration.ae2.me;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.me.MachineSet;

import javax.annotation.Nonnull;
import java.util.HashSet;

public class MachineSets extends HashSet<IGridNode> implements IMachineSet {

    public MachineSets() {}

    public MachineSets(MachineSet... machineSet) {
        for (MachineSet set : machineSet) {
            this.addAll(set);
        }
    }

    public MachineSets tryToAdd(IMachineSet machineSet) {
        if (machineSet instanceof MachineSet)
            this.addAll(((MachineSet) machineSet));
        return this;
    }

    @Nonnull
    @Override
    public Class<? extends IGridHost> getMachineClass() {
        return null;
    }
}
