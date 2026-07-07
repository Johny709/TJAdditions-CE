package tj.mui.widgets;

import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.BooleanSupplier;

public abstract class TJWidget<R extends TJWidget<R>> extends Widget {

    protected BooleanSupplier activeSupplier;
    protected boolean isActive = true;

    public TJWidget(Position selfPosition, Size size) {
        super(selfPosition, size);
    }

    /**
     * Initially set the active state of this widget without sending packets.
     */
    public R setActiveInit(boolean isActive) {
        this.isActive = isActive;
        return (R) this;
    }

    public R setActiveSupplier(BooleanSupplier activeSupplier) {
        this.activeSupplier = activeSupplier;
        return (R) this;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 10)
            this.isActive = buffer.readBoolean();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 10) {
            this.isActive = buffer.readBoolean();
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (this.activeSupplier == null) return;
        final boolean isActive = this.activeSupplier.getAsBoolean();
        if (isActive != this.isActive) {
            this.isActive = isActive;
            this.writeUpdateInfo(10, buffer -> buffer.writeBoolean(this.isActive));
        }
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
        if (isClientSide()) {
            this.writeClientAction(10, buffer -> buffer.writeBoolean(this.isActive));
        } else this.writeUpdateInfo(10, buffer -> buffer.writeBoolean(this.isActive));
    }

    public boolean isActive() {
        return this.isActive;
    }
}
