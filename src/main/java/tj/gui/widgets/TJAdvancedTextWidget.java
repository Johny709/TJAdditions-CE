package tj.gui.widgets;

import gregtech.api.gui.widgets.AdvancedTextWidget;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import tj.util.consumers.QuadConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TJAdvancedTextWidget extends AdvancedTextWidget {

    @SideOnly(Side.CLIENT)
    private WrapScreen wrapScreen;
    private String textId;
    protected final List<QuadConsumer<String, String, ClickData, EntityPlayer>> clickHandlers = new ArrayList<>();

    public TJAdvancedTextWidget(int xPosition, int yPosition, Consumer<List<ITextComponent>> text, int color) {
        super(xPosition, yPosition, text, color);
    }

    public TJAdvancedTextWidget addClickHandler(QuadConsumer<String, String, ClickData, EntityPlayer> clickHandler) {
        this.clickHandlers.add(clickHandler);
        return this;
    }

    public TJAdvancedTextWidget setTextId(String textId) {
        this.textId = textId;
        return this;
    }

    public static ITextComponent withButton(ITextComponent textComponent, String componentData) {
        Style style = textComponent.getStyle();
        style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "@!" + componentData));
        return textComponent;
    }

    @SideOnly(Side.CLIENT)
    private boolean handleCustomComponentClick(ITextComponent textComponent) {
        Style style = textComponent.getStyle();
        if (style.getClickEvent() != null) {
            ClickEvent clickEvent = style.getClickEvent();
            String componentText = clickEvent.getValue();
            if (clickEvent.getAction() == ClickEvent.Action.OPEN_URL && componentText.startsWith("@!")) {
                String rawText = componentText.substring(2);
                ClickData clickData = new ClickData(Mouse.getEventButton(), isShiftDown(), isCtrlDown());
                writeClientAction(1, buf -> {
                    clickData.writeToBuf(buf);
                    buf.writeString(this.textId != null ? this.textId : "");
                    buf.writeString(rawText);
                });
                return true;
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        ITextComponent textComponent = getTextUnderMouse(mouseX, mouseY);
        if (textComponent != null) {
            if (handleCustomComponentClick(textComponent) ||
                    getWrapScreen().handleComponentClick(textComponent)) {
                playButtonClickSound();
                return true;
            }
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    private WrapScreen getWrapScreen() {
        if (wrapScreen == null)
            wrapScreen = new WrapScreen();
        return wrapScreen;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            EntityPlayer player = this.gui.entityPlayer;
            ClickData clickData = ClickData.readFromBuf(buffer);
            String textId = buffer.readString(Short.MAX_VALUE);
            String componentData = buffer.readString(128);
            for (QuadConsumer<String, String, ClickData, EntityPlayer> clickHandler : this.clickHandlers) {
                clickHandler.accept(componentData, textId, clickData, player);
            }
        }
    }

    /**
     * Used to call mc-related chat component handling code,
     * for example component hover rendering and default click handling
     */
    @SideOnly(Side.CLIENT)
    private static class WrapScreen extends GuiScreen {
        @Override
        public void handleComponentHover(ITextComponent component, int x, int y) {
            super.handleComponentHover(component, x, y);
        }

        @Override
        public boolean handleComponentClick(ITextComponent component) {
            return super.handleComponentClick(component);
        }

        @Override
        protected void drawHoveringText(List<String> textLines, int x, int y, FontRenderer font) {
            GuiUtils.drawHoveringText(textLines, x, y, width, height, 256, font);
        }
    }
}
