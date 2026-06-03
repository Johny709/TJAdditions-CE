package tj.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import tj.mui.TJGuiUtils;

import javax.annotation.Nonnull;

public class GuiToggleButton extends GuiButton {

    private final ResourceLocation textureLocation;
    public boolean on;

    public GuiToggleButton(int buttonId, int x, int y, int width, int height, ResourceLocation textureLocation) {
        super(buttonId, x, y, "");
        this.width = width;
        this.height = height;
        this.textureLocation = textureLocation;
    }

    @Override
    public boolean mousePressed(@Nonnull Minecraft mc, int mouseX, int mouseY) {
        final boolean pressed = super.mousePressed(mc, mouseX, mouseY);
        if (pressed) {
            this.on = !this.on;
        }
        return pressed;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;
        TJGuiUtils.drawSubArea(this.x, this.y, this.width, this.height, 0.0, this.on ? 1.0 : 0.5, 1.0, 0.5, this.textureLocation);
    }
}
