package tj.gui.widgets;

import com.google.common.base.Preconditions;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import tj.util.consumers.QuadConsumer;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ButtonWidget<R extends ButtonWidget<R>> extends Widget {

    protected QuadConsumer<String, Integer, Integer, Integer> textResponderWithMouse;
    protected Supplier<String[]> formatSupplier;
    protected Supplier<String> buttonIdSupplier;
    protected Consumer<String> buttonResponder;
    protected TextureArea[] backgroundTextures;
    protected String[] format;
    protected String buttonId;
    protected String displayText;
    protected String tooltipText;
    protected ItemStack displayItem;
    protected int textColor = 0xFFFFFF;
    protected long buttonIdAsLong;

    public ButtonWidget(int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
    }

    /**
     * Set responder for when this button gets pressed. This respond with the buttonId along with mouse click values.
     * @param textResponderWithMouse (buttonId, mouseX, mouseY, button) ->
     */
    public R setButtonResponderWithMouse(QuadConsumer<String, Integer, Integer, Integer> textResponderWithMouse) {
        this.textResponderWithMouse = textResponderWithMouse;
        return (R) this;
    }

    /**
     * Translates the passed in String for display when cursor is hovering over this widget.
     * @param tooltipText The String text to translate
     */
    public R setTooltipText(String tooltipText) {
        Preconditions.checkNotNull(tooltipText, "tooltipText");
        this.tooltipText = tooltipText;
        return (R) this;
    }

    /**
     * The format args used for translating series of text for TooltipText. Text is constantly updated by the supplier.
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
     * Set responder for when this button gets pressed. This respond with the buttonId.
     * @param buttonResponder buttonId ->
     */
    public R setButtonResponder(Consumer<String> buttonResponder) {
        this.buttonResponder = buttonResponder;
        return (R) this;
    }

    /**
     * Set buttonId that will be used to determine response type. Null will be treated as empty string.
     * This is redundant if {@link #setDynamicButtonId(Supplier)} is set.
     * @param buttonId button
     */
    public R setButtonId(String buttonId) {
        this.buttonId = buttonId;

        return (R) this;
    }

    /**
     * Appends extra string to the end of current buttonId.
     * @param buttonId button
     */
    public R appendButtonId(UnaryOperator<String> buttonId) {
        this.buttonId = buttonId.apply(this.buttonId);
        return (R) this;
    }

    /**
     * Add textures to render in background. Last texture passed in will be rendered on top of all the others.
     * @param backgroundTextures  textures
     */
    public R setBackgroundTextures(TextureArea... backgroundTextures) {
        this.backgroundTextures = backgroundTextures;
        return (R) this;
    }

    /**
     * Set item to display on button.
     * @param displayItem ItemStack
     */
    public R setItemDisplay(ItemStack displayItem) {
        this.displayItem = displayItem;
        return (R) this;
    }

    /**
     * Set text to display on button.
     * @param displayText text
     */
    public R setDisplayText(String displayText) {
        this.displayText = displayText;
        return (R) this;
    }

    /**
     * Color of display text.
     * @param textColor hex color
     */
    public R setDisplayTextColor(int textColor) {
        this.textColor = textColor;
        return (R) this;
    }

    public R setButtonIdAsLong(long buttonIdAsLong) {
        this.buttonIdAsLong = buttonIdAsLong;
        return (R) this;
    }

    /**
     * setting this will update the buttonId automatically which will replace buttonId defined in {@link #setButtonId(String)}
     */
    public R setDynamicButtonId(Supplier<String> buttonIdSupplier) {
        this.buttonIdSupplier = buttonIdSupplier;
        return (R) this;
    }

    public long getButtonIdAsLong() {
        return this.buttonIdAsLong;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (this.isMouseOverElement(mouseX, mouseY) && this.tooltipText != null) {
            String tooltipHoverString = this.tooltipText;
            Object[] format = this.format != null ? this.format : ArrayUtils.toArray("");
            List<String> hoverList = Arrays.asList(I18n.format(tooltipHoverString, format).split("/n"));
            this.drawHoveringText(ItemStack.EMPTY, hoverList, 300, mouseX, mouseY);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        if (this.backgroundTextures != null)
            for (TextureArea textureArea : this.backgroundTextures)
                textureArea.draw(this.getPosition().getX(), this.getPosition().getY(), this.getSize().getWidth(), this.getSize().getHeight());
        if (this.displayText != null) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            String text = I18n.format(this.displayText);
            fontRenderer.drawString(text,
                    this.getPosition().getX() + this.getSize().getWidth() / 2 - fontRenderer.getStringWidth(text) / 2,
                    this.getPosition().getY() + this.getSize().getHeight() / 2 - fontRenderer.FONT_HEIGHT / 2, this.textColor);
            GlStateManager.color(1.0f, 1.0f, 1.0f);
        }
        if (this.displayItem != null && !this.displayItem.isEmpty()) {
            drawItemStack(this.displayItem, this.getPosition().getX() + 1, this.getPosition().getY() + 1, null);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            this.playButtonClickSound();
            this.writeClientAction(1, buffer -> {
                buffer.writeString(this.buttonId != null ? this.buttonId : "");
                buffer.writeInt(mouseX);
                buffer.writeInt(mouseY);
                buffer.writeInt(button);
            });
            return true;
        }
        return false;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            String buttonId = buffer.readString(Short.MAX_VALUE);
            int mouseX = buffer.readInt();
            int mouseY = buffer.readInt();
            int button = buffer.readInt();
            if (this.buttonResponder != null)
                this.buttonResponder.accept(buttonId);
            if (this.textResponderWithMouse != null)
                this.textResponderWithMouse.accept(buttonId, mouseX, mouseY, button);
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (this.formatSupplier != null) {
            String[] formatArgs = this.formatSupplier.get();
            this.writeUpdateInfo(1, buffer -> {
                buffer.writeInt(formatArgs.length);
                for (String format : formatArgs) {
                    buffer.writeString(format);
                }
            });
        }
        if (this.buttonIdSupplier != null) {
            String buttonId = this.buttonIdSupplier.get();
            if (this.buttonId == null || !this.buttonId.equals(buttonId)) {
                this.buttonId = buttonId;
                this.writeUpdateInfo(2, buffer -> buffer.writeString(this.buttonId));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            int size = buffer.readInt();
            for (int i = 0; i < size; i++) {
               this.format[i] =  buffer.readString(Short.MAX_VALUE);
            }
        } else if (id == 2) {
            this.buttonId = buffer.readString(Short.MAX_VALUE);
        }
    }
}
