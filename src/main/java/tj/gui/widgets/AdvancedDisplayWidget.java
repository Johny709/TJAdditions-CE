package tj.gui.widgets;

import com.mojang.realmsclient.gui.ChatFormatting;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
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
import tj.util.consumers.QuadConsumer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * More advanced form of {@link gregtech.api.gui.widgets.AdvancedTextWidget} that can also display items {@link net.minecraft.item.ItemStack} and fluids {@link net.minecraftforge.fluids.FluidStack}
 */
public class AdvancedDisplayWidget extends Widget {

    protected int maxWidthLimit;

    @SideOnly(Side.CLIENT)
    private WrapScreen wrapScreen;

    private final List<QuadConsumer<String, String, ClickData, EntityPlayer>> clickHandlers = new ArrayList<>();
    private final Consumer<UIDisplayBuilder> textSupplier;
    private final int color;

    private List<TextComponentWrapper<?>> displayText = new ArrayList<>();
    private BiConsumer<String, ClickData> clickHandler;
    private String textId;

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

    public AdvancedDisplayWidget addClickHandler(QuadConsumer<String, String, ClickData, EntityPlayer> clickHandler) {
        this.clickHandlers.add(clickHandler);
        return this;
    }

    public AdvancedDisplayWidget setClickHandler(BiConsumer<String, ClickData> clickHandler) {
        this.clickHandler = clickHandler;
        return this;
    }

    public AdvancedDisplayWidget setTextId(String textId) {
        this.textId = textId;
        return this;
    }

    public String getTextId() {
        return this.textId;
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
                buffer.writeInt(textComponentWrapper.getPriority());
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
                            this.displayText.add(new TextComponentWrapper<>(buffer.readItemStack())
                                    .setPriority(buffer.readInt()));
                        } catch (IOException e) {
                            GTLog.logger.info(e.getMessage());
                        }
                        break;
                    case 2:
                        try {
                            this.displayText.add(new TextComponentWrapper<>(FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag()))
                                    .setPriority(buffer.readInt()));
                        } catch (IOException e) {
                            GTLog.logger.info(e.getMessage());
                        }
                        break;
                    case 3:
                        this.displayText.add(new TextComponentWrapper<>(ITextComponent.Serializer.jsonToComponent(buffer.readString(Short.MAX_VALUE)))
                                .setPriority(buffer.readInt()));
                }
            }
            this.formatDisplayText();
            this.updateComponentTextSize();
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            EntityPlayer player = this.gui.entityPlayer;
            ClickData clickData = ClickData.readFromBuf(buffer);
            String textId = buffer.readString(Short.MAX_VALUE);
            String componentData = buffer.readString(128);
            if (this.clickHandler != null)
                this.clickHandler.accept(componentData, clickData);
            for (QuadConsumer<String, String, ClickData, EntityPlayer> clickHandler : this.clickHandlers) {
                clickHandler.accept(componentData, textId, clickData, player);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void updateComponentTextSize() {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int slot = 0;
        int totalHeight = 0;
        int maxStringWidth = 0;
        boolean stackApplied = false;
        for (TextComponentWrapper<?> component : this.displayText) {
            if (component.getValue() instanceof ITextComponent) {
                if (stackApplied) {
                    stackApplied = false;
                    totalHeight += 20;
                }
                maxStringWidth = Math.max(maxStringWidth, fontRenderer.getStringWidth(((ITextComponent) component.getValue()).getFormattedText()));
                totalHeight += 11;
            } else {
                if (slot++ > 8 || !stackApplied) {
                    slot = 0;
                    if (stackApplied)
                        totalHeight += 18;
                } else maxStringWidth = MathHelper.clamp(maxStringWidth + 3, 0, 30);
                stackApplied = true;
            }
        }
        if (stackApplied)
            totalHeight += 20;
        if (this.getSize().getWidth() != maxStringWidth || this.getSize().getHeight() != totalHeight)
            this.setSize(new Size(maxStringWidth, totalHeight));
    }

    @Override
    protected void onSizeUpdate() {
        if (this.uiAccess != null) {
            this.uiAccess.notifySizeChange();
        }
    }

    @SideOnly(Side.CLIENT)
    private void formatDisplayText() {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int maxTextWidthResult = this.maxWidthLimit == 0 ? Integer.MAX_VALUE : this.maxWidthLimit;
        this.displayText = this.displayText.stream()
                .flatMap(component -> component.getValue() instanceof ITextComponent ? GuiUtilRenderComponents.splitText((ITextComponent) component.getValue(), maxTextWidthResult, fontRenderer, true, true).stream()
                        .map(component2 -> new TextComponentWrapper<>(component2).setPriority(component.getPriority()))
                        : Stream.of(component))
                .sorted(Comparator.comparingInt(TextComponentWrapper::getPriority))
                .collect(Collectors.toList());
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
        int slot = 0;
        int widthApplied = 0, heightApplied = 0;
        boolean stackApplied = false;
        for (TextComponentWrapper<?> component : displayText) {
            if (component.getValue() instanceof ITextComponent) {
                if (stackApplied) {
                    stackApplied = false;
                    widthApplied = 0;
                    heightApplied += 20;
                }
                fontRenderer.drawString(((ITextComponent) component.getValue()).getFormattedText(), position.getX() + widthApplied, position.getY() + heightApplied, color);
                heightApplied += 11;
            } else {
                if (slot++ > 8 || !stackApplied) {
                    slot = 0;
                    widthApplied = 0;
                    if (stackApplied)
                        heightApplied += 18;
                } else widthApplied += 18;
                stackApplied = true;
                if (component.getValue() instanceof ItemStack) {
                    GuiTextures.SLOT.draw(position.getX() + widthApplied, position.getY() + heightApplied, 18, 18);
                    Widget.drawItemStack((ItemStack) component.getValue(), position.getX() + widthApplied + 1, position.getY() + heightApplied + 1, null);
                } else {
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
                }
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
        int slot = 0;
        int lastHeight = 0, lastWidth = 0;
        int heightApplied = 0, widthApplied = 0;
        boolean stackApplied = false;
        for (TextComponentWrapper<?> component : this.displayText) {
            if (component.getValue() instanceof ITextComponent) {
                slot = 0;
                heightApplied += 11;
                if (stackApplied) {
                    stackApplied = false;
                    heightApplied += 2;
                }
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
            } else {
                if (slot++ > 8 || !stackApplied) {
                    slot = 0;
                    lastWidth = 0;
                    widthApplied = 0;
                    heightApplied += 18;
                } else lastHeight -= 18;
                stackApplied = true;
                widthApplied += 18;
                if (mouseX >= position.getX() + lastWidth && mouseX <= position.getX() + widthApplied && mouseY >= position.getY() + lastHeight && mouseY <= position.getY() + heightApplied) {
                    if (component.getValue() instanceof ItemStack) {
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
                    } else {
                        FluidStack fluidStack = (FluidStack) component.getValue();
                        // Add chemical formula tooltip
                        String formula = FluidTooltipUtil.getFluidTooltip(fluidStack);
                        formula = formula == null || formula.isEmpty() ? "" : "\n" + formula;
                        return new TextComponentString("")
                                .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(fluidStack.getLocalizedName())
                                        .appendText(ChatFormatting.GRAY + formula))));
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

        private final T value;
        private int priority;

        public TextComponentWrapper(T value) {
            this.value = value;
        }

        public TextComponentWrapper<?> setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public T getValue() {
            return this.value;
        }

        public int getPriority() {
            return this.priority;
        }

        @Override
        public boolean equals(Object obj) {
            if (this.getValue() instanceof ItemStack && obj instanceof ItemStack)
                return ItemStack.areItemStacksEqual((ItemStack) this.getValue(), (ItemStack) obj);
            else if (this.getValue() instanceof FluidStack && obj instanceof FluidStack)
                return ((FluidStack) this.getValue()).isFluidStackIdentical((FluidStack) obj);
            else if (this.getValue() instanceof TextComponentBase && obj instanceof TextComponentBase)
                return this.getValue().equals(obj);
            return super.equals(obj);
        }
    }
}
