package tj.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.util.Position;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.BooleanSupplier;

@Deprecated
public class PopUpWidgetGroup extends AbstractWidgetGroup {

    private final int width;
    private final int height;
    private final TextureArea textureArea;
    private BooleanSupplier predicate;
    private boolean isEnabled;
    private boolean inverted;

    public PopUpWidgetGroup(int x, int y, int width, int height, TextureArea textureArea) {
        super(new Position(x, y));
        this.width = width;
        this.height = height;
        this.textureArea = textureArea;
    }

    public PopUpWidgetGroup(int x, int y, int width, int height) {
        this(x, y, width, height, null);
    }

    public PopUpWidgetGroup setPredicate(BooleanSupplier predicate) {
        this.predicate = predicate;
        return this;
    }

    public PopUpWidgetGroup setInverted() {
        this.inverted = !this.inverted;
        return this;
    }

    public PopUpWidgetGroup setEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
        this.writeUpdateInfo(2, buffer -> buffer.writeBoolean(isEnabled));
        return this;
    }

    public PopUpWidgetGroup addWidgets(Widget widget) {
        super.addWidget(widget);
        return this;
    }

    @Override
    public void addWidget(Widget widget) {
        super.addWidget(widget);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible && this.isEnabled);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.predicate != null) {
            this.isEnabled = this.predicate.getAsBoolean() == this.inverted;
            this.writeUpdateInfo(2, buffer -> buffer.writeBoolean(this.isEnabled));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        if (this.isEnabled) {
            if (this.textureArea != null)
                this.textureArea.draw(getPosition().getX(), getPosition().getY(), width, height);
            super.drawInBackground(mouseX, mouseY, context);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 2) {
            this.isEnabled = buffer.readBoolean();
            this.setVisible(this.isEnabled);
        }
    }
}
