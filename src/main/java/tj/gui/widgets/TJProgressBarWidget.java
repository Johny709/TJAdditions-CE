package tj.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.DoubleSupplier;

public class TJProgressBarWidget extends Widget {

    private double progress;
    private double maxProgress;
    private TextureArea backgroundTexture;
    private TextureArea barTexture;
    private TextureArea startTexture;
    private TextureArea endTexture;

    private final DoubleSupplier progressSupplier;
    private final DoubleSupplier maxProgressSupplier;

    public TJProgressBarWidget(int x, int y, int width, int height, DoubleSupplier progressSupplier, DoubleSupplier maxProgressSupplier) {
        super(new Position(x, y), new Size(width, height));
        this.progressSupplier = progressSupplier;
        this.maxProgressSupplier = maxProgressSupplier;
    }

    public TJProgressBarWidget setTexture(TextureArea backgroundTextures) {
        this.backgroundTexture = backgroundTextures;
        return this;
    }

    public TJProgressBarWidget setBarTexture(TextureArea barTexture) {
        this.barTexture = barTexture;
        return this;
    }

    public TJProgressBarWidget setStartTexture(TextureArea startTexture) {
        this.startTexture = startTexture;
        return this;
    }

    public TJProgressBarWidget setEndTexture(TextureArea endTexture) {
        this.endTexture = endTexture;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        Size size = this.getSize();
        Position pos = this.getPosition();
        int width = (int) (size.getWidth() * (this.progress / this.maxProgress));
        this.backgroundTexture.draw(pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
        this.barTexture.draw(pos.getX() + 1, pos.getY() + 1, width, 8);
        this.startTexture.draw(pos.getX(), pos.getY(), (int) this.startTexture.imageWidth, size.getHeight());
        this.endTexture.draw(pos.getX() + size.getWidth() - 1, pos.getY(), (int) this.endTexture.imageWidth, size.getHeight());
    }

    @Override
    public void detectAndSendChanges() {
        this.progress = this.progressSupplier.getAsDouble();
        this.maxProgress = this.maxProgressSupplier.getAsDouble();
        this.writeUpdateInfo(1, buffer -> {
            buffer.writeDouble(this.progress);
            buffer.writeDouble(this.maxProgress);
        });
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            this.progress = buffer.readDouble();
            this.maxProgress = buffer.readDouble();
        }
    }
}
