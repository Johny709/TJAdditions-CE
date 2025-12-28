package tj.event;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class MTELinkEvent extends Event {

    private final MetaTileEntity transmitter;
    private final MetaTileEntity receiver;

    public MTELinkEvent(MetaTileEntity transmitter, MetaTileEntity receiver) {
        this.transmitter = transmitter;
        this.receiver = receiver;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    /**
     * The MetaTileEntity being linked from
     * @return MetaTileEntity
     */
    public MetaTileEntity getTransmitter() {
        return transmitter;
    }

    /**
     * The MetaTileEntity being linked to
     * @return MetaTileEntity
     */
    public MetaTileEntity getReceiver() {
        return receiver;
    }
}
