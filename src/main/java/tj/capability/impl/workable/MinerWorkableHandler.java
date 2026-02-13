package tj.capability.impl.workable;

import gregtech.api.metatileentity.MetaTileEntity;
import tj.capability.AbstractWorkableHandler;
import tj.capability.IMachineHandler;

public class MinerWorkableHandler extends AbstractWorkableHandler<IMachineHandler> {
    public MinerWorkableHandler(MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    protected boolean startRecipe() {
        this.metaTileEntity.getWorld().getChunk(this.metaTileEntity.getPos());
        return super.startRecipe();
    }
}
