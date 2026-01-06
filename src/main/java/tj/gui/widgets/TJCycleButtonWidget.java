package tj.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.SizedTextureArea;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class TJCycleButtonWidget extends CycleButtonWidget {

    private final TextureArea[] buttonBackground;
    private Supplier<String[]> formatSupplier;
    private String[] format;
    private boolean toggle;

    public <T extends Enum<T> & IStringSerializable> TJCycleButtonWidget(int xPosition, int yPosition, int width, int height, Class<T> enumClass, Supplier<T> supplier, Consumer<T> updater, TextureArea... buttonBackground) {
        super(xPosition, yPosition, width, height, enumClass, supplier, updater);
        this.buttonBackground = buttonBackground;
    }

    /**
     * The format args used for translating series of text for TooltipText. Text is constantly updated by the supplier.
     *
     * @apiNote
     * <p>Very similar to I18n.format() where Object... param is the series of text being translated. See setTooltipText for String translateKey param.
     * <pre>{@code
     *     I18n.format(String translateKey, Object... parameters)
     * }</pre>
     *
     * @param formatSupplier translate series of text
     */
    public TJCycleButtonWidget setTooltipFormat(Supplier<String[]> formatSupplier) {
        this.formatSupplier = formatSupplier;
        this.format = new String[formatSupplier.get().length];
        return this;
    }

    /**
     * Set for this button widget to have an On or Off state.
     * @param toggle toggle on/off
     */
    public TJCycleButtonWidget setToggle(boolean toggle) {
        this.toggle = toggle;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (buttonTexture instanceof SizedTextureArea) {
            ((SizedTextureArea) buttonTexture).drawHorizontalCutSubArea(pos.x, pos.y, size.width, size.height, 0.0, 1.0);
        } else {
            double drawV = toggle && currentOption == 0 ? 0.0 : 0.5;
            double drawHeight = toggle ? 0.5 : 1.0;
            buttonTexture.drawSubArea(pos.x, pos.y, size.width, size.height, 0.0, drawV, 1.0, drawHeight);
        }
        if (currentOption < buttonBackground.length)
            buttonBackground[currentOption].draw(pos.getX() + 1, pos.getY() + 1, size.getWidth() - 2, size.getHeight() - 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        boolean isHovered = isMouseOverElement(mouseX, mouseY);
        boolean wasHovered = this.isMouseHovered;
        if (isHovered && !wasHovered) {
            this.isMouseHovered = true;
            this.hoverStartTime = System.currentTimeMillis();
        } else if (!isHovered && wasHovered) {
            this.isMouseHovered = false;
            this.hoverStartTime = 0L;
        } else if (isHovered) {
            if (this.tooltipHoverString != null) {
                Object[] format = this.format != null ? this.format : ArrayUtils.toArray("");
                List<String> hoverList = Arrays.asList(I18n.format(this.tooltipHoverString, format).split("/n"));
                drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.formatSupplier != null)
            IntStream.range(0, this.formatSupplier.get().length)
                    .forEach(i -> writeUpdateInfo(i + 2, buffer -> buffer.writeString(this.formatSupplier.get()[i])));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (this.formatSupplier != null)
            IntStream.range(0, this.formatSupplier.get().length).forEach(i -> {
                if (i + 2 == id)
                    this.format[i] = buffer.readString(Short.MAX_VALUE);
            });
    }
}
