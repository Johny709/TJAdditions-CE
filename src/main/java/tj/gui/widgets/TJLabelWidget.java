package tj.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.RenderUtil;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.gui.TJGuiUtils;

public class TJLabelWidget extends Widget {

    private final TextureArea labelTexture;
    private int color = 0x404040;
    private int offsetX;
    private int tickCounter;
    private boolean slideAtEnd;
    private String locale;
    private ItemStack itemLabel;
    private FluidStack fluidLabel;

    public TJLabelWidget(int x, int y, int width, int height, TextureArea labelTexture) {
        super(new Position(x, y), new Size(width, height));
        this.labelTexture = labelTexture;
    }

    public TJLabelWidget setItemLabel(ItemStack itemLabel) {
        this.itemLabel = itemLabel;
        return this;
    }

    public TJLabelWidget setFluidLabel(FluidStack fluidLabel) {
        this.fluidLabel = fluidLabel;
        return this;
    }

    public TJLabelWidget setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    public TJLabelWidget setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateScreen() {
        this.tickCounter++;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        int widthApplied = 5;
        Size size = this.getSize();
        Position pos = this.getPosition();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        this.labelTexture.draw(pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
        if (this.itemLabel != null) {
            Widget.drawItemStack(this.itemLabel, pos.getX() + 3, pos.getY() + 2, null);
            widthApplied += 16;
        }
        if (this.fluidLabel != null) {
            TJGuiUtils.drawFluidForGui(this.fluidLabel, this.fluidLabel.amount, this.fluidLabel.amount, pos.getX(), pos.getY(), 18, 18);
            widthApplied += 18;
        }
        if (this.locale != null) {
            int finalX = pos.getX() + widthApplied - this.offsetX;
            String locale = I18n.format(this.locale);
            int length = fontRenderer.getStringWidth(locale);
            length -= this.offsetX;
            if (length < 0)
                this.slideAtEnd = true;
            RenderUtil.useScissor(pos.getX() + widthApplied, pos.getY(), size.getWidth(), size.getHeight(), () -> fontRenderer.drawString(locale, finalX, pos.getY() + 6, this.color));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            if (this.slideAtEnd) {
                this.slideAtEnd = false;
                this.offsetX = -160;
            } else if (this.tickCounter % 2 == 0 || this.tickCounter % 3 == 0)
                this.offsetX++;
        } else this.offsetX = 0;
    }
}
