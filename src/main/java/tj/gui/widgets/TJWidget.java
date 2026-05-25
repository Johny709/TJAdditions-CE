package tj.gui.widgets;

import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TJWidget extends Widget {

    protected boolean isActive;

    public TJWidget(Position selfPosition, Size size) {
        super(selfPosition, size);
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 10)
            this.isActive = buffer.readBoolean();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 10)
            this.isActive = buffer.readBoolean();
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
