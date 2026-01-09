package tj.gui.widgets;

import com.mojang.realmsclient.gui.ChatFormatting;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import tj.builder.multicontrollers.UIDisplayBuilder;
import tj.gui.TJGuiUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * More advanced form of {@link gregtech.api.gui.widgets.AdvancedTextWidget} that can also display items {@link net.minecraft.item.ItemStack} and fluids {@link net.minecraftforge.fluids.FluidStack}
 */
public class AdvancedDisplayWidget extends Widget {

    protected int maxWidthLimit;

    @SideOnly(Side.CLIENT)
    private WrapScreen wrapScreen;

    protected final Consumer<UIDisplayBuilder> textSupplier;
    private List<TextComponentWrapper<?>> displayText = new ArrayList<>();

    protected BiConsumer<String, ClickData> clickHandler;
    private final int color;

    public AdvancedDisplayWidget(int x, int y, Consumer<UIDisplayBuilder> textSupplier, int color) {
        super(new Position(x, y), Size.ZERO);
        this.textSupplier = textSupplier;
        this.color = color;
    }

    public AdvancedDisplayWidget setMaxWidthLimit(int maxWidthLimit) {
        this.maxWidthLimit = maxWidthLimit;
        if (isClientSide()) {
            updateComponentTextSize();
        }
        return this;
    }

    public AdvancedDisplayWidget setClickHandler(BiConsumer<String, ClickData> clickHandler) {
        this.clickHandler = clickHandler;
        return this;
    }

    @SideOnly(Side.CLIENT)
    private WrapScreen getWrapScreen() {
        if (wrapScreen == null)
            wrapScreen = new WrapScreen();
        return wrapScreen;
    }

    @SideOnly(Side.CLIENT)
    private void resizeWrapScreen() {
        if (sizes != null) {
            getWrapScreen().setWorldAndResolution(Minecraft.getMinecraft(), sizes.getScreenWidth(), sizes.getScreenHeight());
        }
    }

    @Override
    public void initWidget() {
        super.initWidget();
        if (isClientSide()) {
            resizeWrapScreen();
        }
    }

    @Override
    protected void onPositionUpdate() {
        super.onPositionUpdate();
        if (isClientSide()) {
            resizeWrapScreen();
        }
    }

    @Override
    public void detectAndSendChanges() {
        boolean areEqual = true;
        UIDisplayBuilder displayBuilder = new UIDisplayBuilder();
        this.textSupplier.accept(displayBuilder);
        List<TextComponentWrapper<?>> displayText = displayBuilder.getTextComponentWrappers();
        if (this.displayText != null && this.displayText.size() == displayText.size()) {
            for (int i = 0; i < displayText.size(); i++) {
                TextComponentWrapper<?> textComponentWrapper = displayText.get(i);
                if (!textComponentWrapper.equals(this.displayText.get(i).getValue())) {
                    areEqual = false;
                    break;
                }
            }
        } else areEqual = false;
        if (areEqual) return;
        this.displayText = displayText;
        this.writeUpdateInfo(1, buffer -> {
            buffer.writeInt(this.displayText.size());
            for (TextComponentWrapper<?> textComponentWrapper : this.displayText) {
                if (textComponentWrapper.getValue() instanceof ItemStack) {
                    buffer.writeInt(1);
                    buffer.writeItemStack((ItemStack) textComponentWrapper.getValue());
                } else if (textComponentWrapper.getValue() instanceof FluidStack) {
                    buffer.writeInt(2);
                    buffer.writeCompoundTag(((FluidStack) textComponentWrapper.getValue()).writeToNBT(new NBTTagCompound()));
                } else if (textComponentWrapper.getValue() instanceof ITextComponent) {
                    buffer.writeInt(3);
                    buffer.writeString(ITextComponent.Serializer.componentToJson((ITextComponent) textComponentWrapper.getValue()));
                }
            }
        });
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            this.displayText.clear();
            int count = buffer.readInt();
            for (int i = 0; i < count; i++) {
                switch (buffer.readInt()) {
                    case 1:
                        try {
                            this.displayText.add(new TextComponentWrapper<>(buffer.readItemStack()));
                        } catch (IOException e) {
                            GTLog.logger.info(e.getMessage());
                        }
                        break;
                    case 2:
                        try {
                            this.displayText.add(new TextComponentWrapper<>(FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag())));
                        } catch (IOException e) {
                            GTLog.logger.info(e.getMessage());
                        }
                        break;
                    case 3:
                        this.displayText.add(new TextComponentWrapper<>(ITextComponent.Serializer.jsonToComponent(buffer.readString(Short.MAX_VALUE))));
                }
            }
            formatDisplayText();
            updateComponentTextSize();
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            ClickData clickData = ClickData.readFromBuf(buffer);
            String componentData = buffer.readString(128);
            if (clickHandler != null) {
                clickHandler.accept(componentData, clickData);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void updateComponentTextSize() {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int maxStringWidth = 0;
        int totalHeight = 0;
        for (TextComponentWrapper<?> textComponent : this.displayText) {
            if (textComponent.getValue() instanceof ItemStack || textComponent.getValue() instanceof FluidStack) {
                totalHeight += 20;
            } else if (textComponent.getValue() instanceof ITextComponent) {
                maxStringWidth = Math.max(maxStringWidth, fontRenderer.getStringWidth(((ITextComponent) textComponent.getValue()).getFormattedText()));
                totalHeight += fontRenderer.FONT_HEIGHT + 2;
            }
        }
        totalHeight -= 2;
        setSize(new Size(maxStringWidth, totalHeight));
        if (this.uiAccess != null) {
            this.uiAccess.notifySizeChange();
        }
    }

    @SideOnly(Side.CLIENT)
    private void formatDisplayText() {
//        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
//        int maxTextWidthResult = maxWidthLimit == 0 ? Integer.MAX_VALUE : maxWidthLimit;
//        this.displayText = displayText.stream()
//                .flatMap(c -> GuiUtilRenderComponents.splitText(c, maxTextWidthResult, fontRenderer, true, true).stream())
//                .collect(Collectors.toList());
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
        ITextComponent textComponent = this.getTextUnderMouse(mouseX, mouseY);
        if (textComponent != null) {
            if (this.handleCustomComponentClick(textComponent) || this.getWrapScreen().handleComponentClick(textComponent)) {
                this.playButtonClickSound();
                return true;
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        Position position = this.getPosition();
        int widthApplied = 0, heightApplied = 0;
        boolean stackApplied = false;
        for (int i = 0; i < displayText.size(); i++) {
            TextComponentWrapper<?> component = this.displayText.get(i);
            if (component.getValue() instanceof ItemStack) {
                if (i > 0 && this.displayText.get(i - 1).getValue() instanceof ItemStack) {
                    widthApplied += 18;
                } else {
                    widthApplied = 0;
                    if (stackApplied)
                        heightApplied += 20;
                }
                stackApplied = true;
                GuiTextures.SLOT.draw(position.getX() + widthApplied, position.getY() + heightApplied, 18, 18);
                Widget.drawItemStack((ItemStack) component.getValue(), position.getX() + widthApplied + 1, position.getY() + heightApplied + 1, null);
            } else if (component.getValue() instanceof FluidStack) {
                if (i > 0 && this.displayText.get(i - 1).getValue() instanceof FluidStack) {
                    widthApplied += 18;
                } else {
                    widthApplied = 0;
                    if (stackApplied)
                        heightApplied += 20;
                }
                stackApplied = true;
                FluidStack fluidStack = (FluidStack) component.getValue();
                GuiTextures.FLUID_SLOT.draw(position.getX() + widthApplied, position.getY() + heightApplied, 18, 18);
                GlStateManager.disableBlend();
                TJGuiUtils.drawFluidForGui(fluidStack, fluidStack.amount, fluidStack.amount, position.getX() + widthApplied + 1, position.getY() + heightApplied + 1, 17, 17);
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.5, 0.5, 1);
                String s = TextFormattingUtil.formatLongToCompactString(fluidStack.amount, 4) + "L";
                fontRenderer.drawStringWithShadow(s, (position.getX() + widthApplied + 6) * 2 - fontRenderer.getStringWidth(s) + 21, (position.getY() + heightApplied + 14) * 2, 0xFFFFFF);
                GlStateManager.popMatrix();
                GlStateManager.enableBlend();
                GlStateManager.color(1.0f, 1.0f, 1.0f);
            } else if (component.getValue() instanceof ITextComponent) {
                if (stackApplied) {
                    widthApplied = 0;
                    heightApplied += 20;
                    stackApplied = false;
                }
                fontRenderer.drawString(((ITextComponent) this.displayText.get(i).getValue()).getFormattedText(), position.getX() + widthApplied, position.getY() + heightApplied, color);
                heightApplied += 11;
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        ITextComponent component = this.getTextUnderMouse(mouseX, mouseY);
        if (component != null) {
            this.getWrapScreen().handleComponentHover(component, mouseX, mouseY);
        }
    }

    @SideOnly(Side.CLIENT)
    protected ITextComponent getTextUnderMouse(int mouseX, int mouseY) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        Position position = this.getPosition();
        int lastHeight = 0, lastWidth = 0;
        int heightApplied = 0, widthApplied = 0;
        for (int i = 0; i < this.displayText.size(); i++) {
            TextComponentWrapper<?> component = this.displayText.get(i);
            if (component.getValue() instanceof ItemStack) {
                if (!(i > 0 && this.displayText.get(i - 1).getValue() instanceof ItemStack)) {
                    lastWidth = 0;
                    widthApplied = 0;
                    heightApplied += 20;
                } else lastHeight -= 20;
                widthApplied += 18;
                if (mouseX >= position.getX() + lastWidth && mouseX <= position.getX() + widthApplied && mouseY >= position.getY() + lastHeight && mouseY <= position.getY() + heightApplied) {
                    ItemStack itemStack = (ItemStack) component.getValue();
                    List<String> tooltip = getItemToolTip(itemStack);
                    String name = itemStack.getDisplayName();
                    ITextComponent hoverComponent = new TextComponentString("");
                    if (tooltip != null && !tooltip.isEmpty()) {
                        name = tooltip.get(0);
                        for (int j = 1; j < tooltip.size(); j++)
                            hoverComponent.appendText("\n" + tooltip.get(j));
                    }
                    return new TextComponentString("")
                            .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(name)
                                    .appendSibling(hoverComponent))));
                }
            } else if (component.getValue() instanceof FluidStack) {
                if (!(i > 0 && this.displayText.get(i - 1).getValue() instanceof FluidStack)) {
                    lastWidth = 0;
                    widthApplied = 0;
                    heightApplied += 20;
                } else lastHeight -= 20;
                widthApplied += 18;
                if (mouseX >= position.getX() + lastWidth && mouseX <= position.getX() + widthApplied && mouseY >= position.getY() + lastHeight && mouseY <= position.getY() + heightApplied) {
                    FluidStack fluidStack = (FluidStack) component.getValue();
                    // Add chemical formula tooltip
                    String formula = FluidTooltipUtil.getFluidTooltip(fluidStack);
                    formula = formula == null || formula.isEmpty() ? "" : "\n" + formula;
                    return new TextComponentString("")
                            .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(fluidStack.getLocalizedName())
                                    .appendText(ChatFormatting.GRAY + formula))));
                }
            } else if (component.getValue() instanceof ITextComponent) {
                heightApplied += 11;
                if (mouseY >= position.getY() + lastHeight && mouseY <= position.getY() + heightApplied) {
                    int currentOffset = 0;
                    int mouseOffset = mouseX - position.getX();
                    for (ITextComponent lineComponent : (ITextComponent) component.getValue()) {
                        currentOffset += fontRenderer.getStringWidth(lineComponent.getUnformattedComponentText());
                        if (currentOffset >= mouseOffset) {
                            return lineComponent;
                        }
                    }
                }
            }
            lastWidth = widthApplied;
            lastHeight = heightApplied;
        }
        return null;
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

    public static class TextComponentWrapper<T> {

        T value;

        public TextComponentWrapper(T value) {
            this.value = value;
        }

        public T getValue() {
            return this.value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this.getValue() instanceof ItemStack && obj instanceof ItemStack)
                return ItemStack.areItemStacksEqual((ItemStack) this.getValue(), (ItemStack) obj);
            if (this.getValue() instanceof TextComponentBase && obj instanceof TextComponentBase)
                return this.getValue().equals(obj);
            if (this.getValue() instanceof FluidStack && obj instanceof FluidStack)
                return this.getValue().equals(obj);
            return super.equals(obj);
        }
    }
}
