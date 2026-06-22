package tj.mui.widgets.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.SizedTextureArea;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.api.util.function.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.mui.widgets.ButtonWidget;
import tj.util.consumers.QuadConsumer;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class TJToggleButtonWidget extends ButtonWidget<TJToggleButtonWidget> {

    private BiConsumer<Boolean, String> toggleButtonResponder;
    private BooleanSupplier isPressedCondition;
    private BooleanConsumer buttonBoolResponder;
    private TextureArea toggleTexture;
    private TextureArea activeTexture;
    private TextureArea baseTexture;
    private String activeTooltipHoverText;
    private String baseTooltipHoverText;
    private String activeDisplayText;
    private String baseDisplayText;
    private boolean useToggleTexture;
    private boolean invertTexture;
    private boolean isPressed;

    public TJToggleButtonWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public TJToggleButtonWidget(int x, int y, int width, int height, BooleanSupplier isPressedCondition, BooleanConsumer buttonBoolResponder) {
        this(x, y, width, height);
        this.isPressedCondition = isPressedCondition;
        this.buttonBoolResponder = buttonBoolResponder;
    }

    public TJToggleButtonWidget(int x, int y, int width, int height, BooleanSupplier isPressedCondition, Consumer<String> buttonResponder) {
        this(x, y, width, height);
        this.isPressedCondition = isPressedCondition;
        this.buttonResponder = buttonResponder;
    }

    public TJToggleButtonWidget(int x, int y, int width, int height, BooleanSupplier isPressedCondition, BiConsumer<Boolean, String> toggleButtonResponder) {
        this(x, y, width, height);
        this.isPressedCondition = isPressedCondition;
        this.toggleButtonResponder = toggleButtonResponder;
    }

    public TJToggleButtonWidget(int x, int y, int width, int height, BooleanSupplier isPressedCondition, QuadConsumer<String, Integer, Integer, Integer> textResponderWithMouse) {
        this(x, y, width, height);
        this.isPressedCondition = isPressedCondition;
        this.textResponderWithMouse = textResponderWithMouse;
    }

    /**
     * Supplier to get the state of button.
     * @param isPressedCondition is button pressed
     */
    @Nonnull
    public TJToggleButtonWidget setButtonSupplier(BooleanSupplier isPressedCondition) {
        this.isPressedCondition = isPressedCondition;
        return this;
    }

    /**
     * Set responder for when this button gets pressed. This respond with the state of button and buttonId.
     * @param toggleButtonResponder buttonId ->
     */
    public TJToggleButtonWidget setToggleButtonResponder(BiConsumer<Boolean, String> toggleButtonResponder) {
        this.toggleButtonResponder = toggleButtonResponder;
        return this;
    }

    /**
     * Toggle this mode to use button texture with On-Off state. Default: false.
     * @param useToggleTexture set to use toggle button texture
     */
    public TJToggleButtonWidget useToggleTexture(boolean useToggleTexture) {
        this.useToggleTexture = useToggleTexture;
        return this;
    }

    /**
     * Set texture for button with an On-Off state. {@link #useToggleTexture(boolean)} must be set to true.
     * @param toggleTexture toggle button texture
     */
    public TJToggleButtonWidget setToggleTexture(TextureArea toggleTexture) {
        this.toggleTexture = toggleTexture;
        return this;
    }

    /**
     * Set to invert toggle texture of button. Default: false.
     * @param invertTexture invert toggle texture
     */
    public TJToggleButtonWidget setInvertTexture(boolean invertTexture) {
        this.invertTexture = invertTexture;
        return this;
    }

    /**
     * {@link #useToggleTexture(boolean)} must be set to false.
     * @param baseTexture The texture shown when the button is pressed.
     * @param activeTexture The texture shown when the button is pressed.
     */
    public TJToggleButtonWidget setActiveTexture(TextureArea baseTexture, TextureArea activeTexture) {
        this.baseTexture = baseTexture;
        this.activeTexture = activeTexture;
        return this;
    }

    /**
     * This will attempt to translate the text if they're a lang string.
     * @param baseDisplayText The text shown on the button when it's not pressed.
     * @param activeDisplayText The text shown on the button when it's pressed.
     */
    public TJToggleButtonWidget setToggleDisplayText(String baseDisplayText, String activeDisplayText) {
        this.baseDisplayText = baseDisplayText;
        this.activeDisplayText = activeDisplayText;
        return this;
    }

    /**
     * Text that will be displayed upon hovering over this button.
     * This will attempt to translate the text if they're a lang string.
     * @param baseTooltipHoverText The text shown on the button when it's not pressed.
     * @param activeTooltipHoverText The text shown on the button when it's pressed.
     */
    public TJToggleButtonWidget setToggleTooltipHoverText(String baseTooltipHoverText, String activeTooltipHoverText) {
        this.baseTooltipHoverText = baseTooltipHoverText;
        this.activeTooltipHoverText = activeTooltipHoverText;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        super.drawInForeground(mouseX, mouseY);
        if (this.baseTooltipHoverText != null && this.activeTooltipHoverText != null && this.isMouseOverElement(mouseX, mouseY)) {
            final List<String> hover = Collections.singletonList(this.isPressed ? I18n.format(this.activeTooltipHoverText) : I18n.format(this.baseTooltipHoverText));
            this.drawHoveringText(ItemStack.EMPTY, hover, 300, mouseX, mouseY);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        final Position pos = this.getPosition();
        final Size size = this.getSize();
        if (!this.useToggleTexture) {
            if (this.invertTexture != this.isPressedCondition.getAsBoolean()) {
                this.activeTexture.draw(pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
            } else this.baseTexture.draw(pos.getX(), pos.getY(), size.getWidth(), size.getHeight());
        } else if (this.toggleTexture instanceof SizedTextureArea) {
            ((SizedTextureArea) this.toggleTexture).drawHorizontalCutSubArea(pos.x, pos.y, size.width, size.height, this.invertTexture != this.isPressed ? 0.5 : 0.0, 0.5);
        } else {
            this.toggleTexture.drawSubArea(pos.x, pos.y, size.width, size.height, 0.0, this.invertTexture != this.isPressed ? 0.5 : 0.0, 1.0, 0.5);
        }
        if (this.baseDisplayText != null && this.activeDisplayText != null) {
            final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            final String text = I18n.format(this.isPressed ? this.activeDisplayText : this.baseDisplayText);
            fontRenderer.drawString(text,
                    this.getPosition().getX() + this.getSize().getWidth() / 2 - fontRenderer.getStringWidth(text) / 2,
                    this.getPosition().getY() + this.getSize().getHeight() / 2 - fontRenderer.FONT_HEIGHT / 2, this.textColor);
            GlStateManager.color(1.0f, 1.0f, 1.0f);
        }
        super.drawInBackground(mouseX, mouseY, context);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isMouseOverElement(mouseX, mouseY)) {
            this.playButtonClickSound();
            this.isPressed = !this.isPressed;
            this.writeClientAction(1, buffer -> {
                buffer.writeString(this.buttonId != null ? this.buttonId : "");
                buffer.writeBoolean(this.isPressed);
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
            final String buttonId = buffer.readString(Short.MAX_VALUE);
            this.isPressed = buffer.readBoolean();
            final int mouseX = buffer.readInt();
            final int mouseY = buffer.readInt();
            final int button = buffer.readInt();
            if (this.buttonBoolResponder != null)
                this.buttonBoolResponder.apply(this.isPressed);
            if (this.buttonResponder != null)
                this.buttonResponder.accept(buttonId);
            if (this.toggleButtonResponder != null)
                this.toggleButtonResponder.accept(this.isPressed, buttonId);
            if (this.textResponderWithMouse != null)
                this.textResponderWithMouse.accept(buttonId, mouseX, mouseY, button);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.isPressedCondition != null) {
            boolean isPressed = this.isPressedCondition.getAsBoolean();
            if (this.isPressed != isPressed) {
                this.isPressed = isPressed;
                this.writeUpdateInfo(4, buffer -> buffer.writeBoolean(this.isPressed));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 4)
            this.isPressed = buffer.readBoolean();
    }
}
