package tj.gui.widgets;

import com.google.common.base.Preconditions;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.MCGuiUtil;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class NewTextFieldWidget<R extends NewTextFieldWidget<R>> extends Widget {

    @SideOnly(Side.CLIENT)
    protected GuiTextField textField;

    protected int maxStringLength = 32;
    protected int backgroundTextColor;
    protected boolean updateOnType;
    protected boolean enableBackground;
    protected Predicate<String> textValidator;
    protected BiConsumer<String, String> textResponder;
    protected Supplier<String> textSupplier;
    protected Supplier<String[]> formatSupplier;
    protected String currentString;
    protected String backgroundText;
    protected String tooltipText;
    protected String textId;
    protected String[] format;

    public NewTextFieldWidget(int x, int y, int width, int height) {
        this(x, y, width, height, false);
    }

    public NewTextFieldWidget(int x, int y, int width, int height, Supplier<String> textSupplier, BiConsumer<String, String> textResponder) {
        this(x, y, width, height, false);
        this.textSupplier = textSupplier;
        this.textResponder = textResponder;
    }

    public NewTextFieldWidget(int x, int y, int width, int height, boolean enableBackground) {
        super(new Position(x, y), new Size(width, height));
        if (isClientSide()) {
            final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            this.textField = new GuiTextField(0, fontRenderer, x, y, width, height);
            this.textField.setCanLoseFocus(true);
            this.textField.setEnableBackgroundDrawing(enableBackground);
            this.textField.setMaxStringLength(this.maxStringLength);
            this.textField.setGuiResponder(MCGuiUtil.createTextFieldResponder(this::onTextChanged));
        }
    }

    /**
     * Renders the texture {@link gregtech.api.gui.GuiTextures#DISPLAY Display} in the background of this text field.
     * @param enableBackground set to enable background.
     */
    public R enableBackground(boolean enableBackground) {
        this.enableBackground = enableBackground;
        return (R) this;
    }

    /**
     * Set responder that sends text whenever the textbox gets typed in. {@link NewTextFieldWidget#setUpdateOnTyping(boolean)} must be set to true. Default: false.
     * @param textResponder responder
     */
    public R setTextResponder(BiConsumer<String, String> textResponder) {
        this.textResponder = textResponder;
        return (R) this;
    }

    /**
     * Set to register a response every time a key has been typed in the textbox. Pairs very well with {@link #setTextSupplier(Supplier)}
     * @param updateOnType toggle update
     */
    public R setUpdateOnTyping(boolean updateOnType) {
        this.updateOnType = updateOnType;
        return (R) this;
    }

    /**
     * Set the supplier that updates the textbox. If the string inside the supplier is null, then its treated as an empty string. Pairs very well with {@link #setUpdateOnTyping(boolean)}
     * @param textSupplier supplier
     */
    public R setTextSupplier(Supplier<String> textSupplier) {
        this.textSupplier = textSupplier;
        return (R) this;
    }

    /**
     * The hover text displayed when mouse is hovering over textbox. The passed in text is translated.
     * @param tooltipText The String text to translate
     */
    public R setTooltipText(String tooltipText) {
        Preconditions.checkNotNull(tooltipText, "tooltipText");
        this.tooltipText = tooltipText;
        return (R) this;
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
    public R setTooltipFormat(Supplier<String[]> formatSupplier) {
        this.formatSupplier = formatSupplier;
        this.format = new String[formatSupplier.get().length];
        return (R) this;
    }

    /**
     * Sets the background text when this TextFieldWidget has empty text or being typed in.
     * @param backgroundText String text
     * @param color Hex color
     */
    public R setBackgroundText(String backgroundText, int color) {
        this.backgroundText = backgroundText;
        this.backgroundTextColor = color;
        return (R) this;
    }

    /**
     * Sets the background text when this TextFieldWidget has empty text or being typed in.
     * @param backgroundText String text
     */
    public R setBackgroundText(String backgroundText) {
        return this.setBackgroundText(backgroundText, 0xAAAAAA);
    }

    /**
     * Set color of the text
     * @param textColor color in hex format
     */
    public R setTextColor(int textColor) {
        if (isClientSide())
            this.textField.setTextColor(textColor);
        return (R) this;
    }

    /**
     * Sets the maximum length for the text in this textbox. default is set to 32.
     * @param maxStringLength max length
     */
    public R setMaxStringLength(int maxStringLength) {
        this.maxStringLength = maxStringLength;
        if (isClientSide())
            this.textField.setMaxStringLength(maxStringLength);
        return (R) this;
    }

    /**
     * Set the regex validator. Any key that don't match the validator won't be typed in the textbox.
     * @param validator string predicate
     */
    public R setValidator(Predicate<String> validator) {
        this.textValidator = validator;
        if (isClientSide()) {
            Objects.requireNonNull(validator);
            this.textField.setValidator(validator::test);
        }
        return (R) this;
    }

    public R setTextId(String textId) {
        this.textId = textId;
        if (!isClientSide())
            this.writeUpdateInfo(2, buffer -> buffer.writeString(this.textId));
        return (R) this;
    }

    public String getTextId() {
        return this.textId != null ? this.textId : "";
    }

    /**
     * Usually called by other widgets to initiate a manual response. e.g. a button press
     */
    public void triggerResponse() {
        this.textResponder.accept(this.currentString, this.textId);
    }

    /**
     * Usually called by other widgets to initiate a manual response. e.g. a button press
     */
    public <T> void triggerResponse(T t) {
        this.textResponder.accept(this.currentString, this.textId);
    }

    /**
     * Usually called by other widgets to initiate a manual response. e.g. a button press
     */
    public <T, U> void triggerResponse(T t, U u) {
        this.textResponder.accept(this.currentString, this.textId);
    }

    /**
     * Usually called by other widgets to initiate a manual response. e.g. a button press
     */
    public <T, U, V> void triggerResponse(T t, U u, V v) {
        this.textResponder.accept(this.currentString, this.textId);
    }

    /**
     * Usually called by other widgets to initiate a manual response. e.g. a button press
     */
    public <T, U, V, O> void triggerResponse(T t, U u, V v, O o) {
        this.textResponder.accept(this.currentString, this.textId);
    }

    @SideOnly(Side.CLIENT)
    public String getText() {
        return this.textField.getText();
    }

    @SideOnly(Side.CLIENT)
    public void setEnableTextBox(boolean enable) {
        this.textField.setEnabled(enable);
        this.textField.setVisible(enable);
    }

    @Override
    protected void onPositionUpdate() {
        if (isClientSide() && this.textField != null) {
            final Position position = this.getPosition();
            final GuiTextField textField = this.textField;
            textField.x = position.x;
            textField.y = position.y;
        }
    }

    @Override
    protected void onSizeUpdate() {
        if (isClientSide() && this.textField != null) {
            final Size size = this.getSize();
            final GuiTextField textField = this.textField;
            textField.width = size.width;
            textField.height = size.height;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY) && this.tooltipText != null) {
            final String tooltipHoverString = this.tooltipText;
            final Object[] format = this.format != null ? this.format : ArrayUtils.toArray("");
            final List<String> hoverList = Arrays.asList(I18n.format(tooltipHoverString, format).split("/n"));
            this.drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        final Size size = this.getSize();
        final Position position = this.getPosition();
        if (this.enableBackground)
            GuiTextures.DISPLAY.draw(position.getX() - 3, position.getY() - 5, size.getWidth(), size.getHeight());
        this.textField.drawTextBox();
        if (this.backgroundText != null && this.textField.getText().isEmpty() && !this.textField.isFocused()) {;
            final String locale = I18n.format(this.backgroundText);
            this.drawStringSized(locale, position.getX(), position.getY(), this.backgroundTextColor, true, 1, false);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return this.textField.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean keyTyped(char charTyped, int keyCode) {
        return this.textField.textboxKeyTyped(charTyped, keyCode);
    }

    @SideOnly(Side.CLIENT)
    private void onTextChanged(String text) {
        if (this.textValidator.test(text))
            this.writeClientAction(1, buffer -> buffer.writeString(text));
    }

    @Override
    public void detectAndSendChanges() {
        if (this.textSupplier != null) {
            final String text = this.textSupplier.get();
            if (text != null && !text.equals(this.currentString)) {
                this.currentString = text;
                this.writeUpdateInfo(1, buffer -> buffer.writeString(this.currentString));
            }
        }
        if (this.formatSupplier != null) {
            final String[] formatArgs = this.formatSupplier.get();
            this.writeUpdateInfo(3, buffer -> {
                buffer.writeInt(formatArgs.length);
                for (String format : formatArgs) {
                    buffer.writeString(format);
                }
            });
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            this.currentString = buffer.readString(Short.MAX_VALUE);
            this.textField.setText(this.currentString);
        } else if (id == 2) {
            this.textId = buffer.readString(Short.MAX_VALUE);
        } else if (id == 3) {
            final int size = buffer.readInt();
            for (int i = 0; i < size; i++) {
                this.format[i] =  buffer.readString(Short.MAX_VALUE);
            }
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            this.currentString = buffer.readString(Short.MAX_VALUE);
            if (this.updateOnType && this.textResponder != null)
                this.textResponder.accept(this.currentString, this.textId);
        }
    }
}
