package tj.gui.widgets;

import com.google.common.base.Preconditions;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.widgets.TextFieldWidget;
import gregtech.api.util.Position;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@Deprecated
public class TJTextFieldWidget extends TextFieldWidget {

    private String tooltipText;
    private String backgroundText;
    private int backgroundTextColor;
    private Supplier<String[]> formatSupplier;
    private String[] format;

    public TJTextFieldWidget(int xPosition, int yPosition, int width, int height, boolean enableBackground, Supplier<String> textSupplier, Consumer<String> textResponder) {
        super(xPosition, yPosition, width, height, enableBackground, textSupplier, textResponder);
    }

    /**
     * Translates the passed in String for display when cursor is hovering over this widget.
     * @param tooltipText The String text to translate
     */

    public TJTextFieldWidget setTooltipText(String tooltipText) {
        Preconditions.checkNotNull(tooltipText, "tooltipText");
        this.tooltipText = tooltipText;
        return this;
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

    public TJTextFieldWidget setTooltipFormat(Supplier<String[]> formatSupplier) {
        this.formatSupplier = formatSupplier;
        this.format = new String[formatSupplier.get().length];
        return this;
    }

    /**
     * Sets the background text when this TextFieldWidget has empty text or being typed in.
     * @param backgroundText String text
     * @param color Hex color
     */
    public TJTextFieldWidget setBackgroundText(String backgroundText, int color) {
        this.backgroundText = backgroundText;
        this.backgroundTextColor = color;
        return this;
    }

    /**
     * Sets the background text when this TextFieldWidget has empty text or being typed in.
     * @param backgroundText String text
     */
    public TJTextFieldWidget setBackgroundText(String backgroundText) {
        return this.setBackgroundText(backgroundText, 0xAAAAAA);
    }

    /**
     * Sets the maximum length for the text in this textbox. default is set to 32.
     * @param length max length
     */
    public TJTextFieldWidget setTextLength(int length) {
        this.maxStringLength = length;
        if (isClientSide())
            this.textField.setMaxStringLength(length);
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY) && this.tooltipText != null) {
            String tooltipHoverString = this.tooltipText;
            Object[] format = this.format != null ? this.format : ArrayUtils.toArray("");
            List<String> hoverList = Arrays.asList(I18n.format(tooltipHoverString, format).split("/n"));
            this.drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, context);
        if (this.backgroundText != null && this.textField.getText().isEmpty() && !this.textField.isFocused()) {
            Position position = getPosition();
            String locale = net.minecraft.util.text.translation.I18n.translateToLocal(this.backgroundText);
            this.drawStringSized(locale, position.getX(), position.getY(), this.backgroundTextColor, true, 1, false);
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
