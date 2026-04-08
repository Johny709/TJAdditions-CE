package tj.gui.widgets.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AnimatedImageWidget extends Widget {

    private final TextureArea backgroundTexture;
    private final int ticksPerFrame;
    private final int frames;
    private int timer;
    private int tick;

    public AnimatedImageWidget(int xPosition, int yPosition, int width, int height, int frames, TextureArea backgroundTexture) {
        this(xPosition, yPosition, width, height, frames, 1, backgroundTexture);
    }

    public AnimatedImageWidget(int xPosition, int yPosition, int width, int height, int frames, int ticksPerFrame, TextureArea backgroundTexture) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.backgroundTexture = backgroundTexture;
        this.ticksPerFrame = ticksPerFrame;
        this.frames = frames;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateScreen() {
        if (this.timer++ % this.ticksPerFrame == 0) this.tick++;
        if (this.tick >= this.frames)
            this.tick = 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        if (this.backgroundTexture == null) return;
        final Size size = this.getSize();
        final Position pos = this.getPosition();
        final double offsetY = 1.0 / this.frames;
        this.backgroundTexture.drawSubArea(pos.getX(), pos.getY(), size.getWidth(), size.getHeight(),  0.0, offsetY * this.tick, 1.0, offsetY);
    }
}
