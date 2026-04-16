package tj.gui.widgets;

import com.mojang.realmsclient.gui.ChatFormatting;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.igredient.IIngredientSlot;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import tj.builder.multicontrollers.GUIDisplayBuilder;
import tj.gui.TJGuiTextures;
import tj.gui.TJGuiUtils;
import tj.util.TextUtils;
import tj.util.consumers.QuadConsumer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * More advanced form of {@link gregtech.api.gui.widgets.AdvancedTextWidget} that can also display items {@link net.minecraft.item.ItemStack} and fluids {@link net.minecraftforge.fluids.FluidStack}
 */
public class AdvancedDisplayWidget extends Widget implements IIngredientSlot {

    protected int maxWidthLimit;

    @SideOnly(Side.CLIENT)
    private WrapScreen wrapScreen;

    private final List<QuadConsumer<String, String, ClickData, EntityPlayer>> clickHandlers = new ArrayList<>();
    private final Consumer<GUIDisplayBuilder> textSupplier;
    private final int color;

    private List<TextComponentWrapper<?>> displayText = new ArrayList<>();
    private List<TextComponentWrapper<?>> hoverDisplayText;
    private int lastHoverX;
    private int lastHoverY;
    private int tick;
    private BiConsumer<String, ClickData> clickHandler;
    private String textId;

    public AdvancedDisplayWidget(int x, int y, Consumer<GUIDisplayBuilder> textSupplier, int color) {
        super(new Position(x, y), Size.ZERO);
        this.textSupplier = textSupplier;
        this.color = color;
    }

    public AdvancedDisplayWidget setMaxWidthLimit(int maxWidthLimit) {
        this.maxWidthLimit = maxWidthLimit;
        if (isClientSide()) {
            final Size size = this.updateComponentTextSize(this.displayText);
            if (this.getSize().getWidth() != size.getWidth() || this.getSize().getHeight() != size.getHeight())
                this.setSize(size);
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
        final GUIDisplayBuilder displayBuilder = new GUIDisplayBuilder(false);
        this.textSupplier.accept(displayBuilder);
        final List<TextComponentWrapper<?>> displayText = displayBuilder.getTextComponentWrappers();
        if (this.displayText != null && this.displayText.size() == displayText.size()) {
            for (int i = 0; i < displayText.size(); i++) {
                final TextComponentWrapper<?> textComponentWrapper = displayText.get(i);
                if (!textComponentWrapper.equals(this.displayText.get(i).getValue())) {
                    areEqual = false;
                    break;
                }
                final List<TextComponentWrapper<?>> hoverDisplayText = this.displayText.get(i).getAdvancedHoverComponent();
                final List<TextComponentWrapper<?>> innerDisplayText = textComponentWrapper.getAdvancedHoverComponent();
                if (hoverDisplayText != null && hoverDisplayText.size() == innerDisplayText.size()) {
                    for (int j = 0; j < innerDisplayText.size(); j++) {
                        final TextComponentWrapper<?> innerTextComponentWrapper = innerDisplayText.get(j);
                        if (!innerTextComponentWrapper.equals(hoverDisplayText.get(j).getValue())) {
                            areEqual = false;
                            break;
                        }
                    }
                }
            }
        } else areEqual = false;
        if (areEqual) return;
        this.displayText = displayText;
        this.writeUpdateInfo(1, buffer -> this.writeToBuffer(this.displayText, buffer));
    }

    private void writeToBuffer(List<TextComponentWrapper<?>> displayText, PacketBuffer buffer) {
        buffer.writeInt(displayText.size());
        for (TextComponentWrapper<?> textComponentWrapper : displayText) {
            if (textComponentWrapper.getValue() instanceof ItemStack) {
                buffer.writeByte(1);
                buffer.writeItemStack((ItemStack) textComponentWrapper.getValue());
            } else if (textComponentWrapper.getValue() instanceof FluidStack) {
                buffer.writeByte(2);
                buffer.writeCompoundTag(((FluidStack) textComponentWrapper.getValue()).writeToNBT(new NBTTagCompound()));
            } else if (textComponentWrapper.getValue() instanceof CountableIngredient) {
                final CountableIngredient ingredient = (CountableIngredient) textComponentWrapper.getValue();
                buffer.writeByte(3);
                buffer.writeInt(ingredient.getCount());
                buffer.writeInt(ingredient.getIngredient().getMatchingStacks().length);
                for (ItemStack stack : ingredient.getIngredient().getMatchingStacks())
                    buffer.writeItemStack(stack);
            } else if (textComponentWrapper.getValue() instanceof ITextComponent) {
                buffer.writeByte(4);
                buffer.writeString(ITextComponent.Serializer.componentToJson((ITextComponent) textComponentWrapper.getValue()));
            }
            buffer.writeInt(textComponentWrapper.getPriority());
            buffer.writeBoolean(textComponentWrapper.getAdvancedHoverComponent() != null);
            if (textComponentWrapper.getAdvancedHoverComponent() != null) {
                this.writeToBuffer(textComponentWrapper.getAdvancedHoverComponent(), buffer);
            }
        }
    }

    private void readFromBuffer(List<TextComponentWrapper<?>> displayText, PacketBuffer buffer) {
        final int count = buffer.readInt();
        for (int i = 0; i < count; i++) {
            TextComponentWrapper<?> componentWrapper = null;
            switch (buffer.readByte()) {
                case 1:
                    try {
                        displayText.add(componentWrapper = new TextComponentWrapper<>(buffer.readItemStack()).setPriority(buffer.readInt()));
                    } catch (IOException e) {
                        GTLog.logger.info(e.getMessage());
                    }
                    break;
                case 2:
                    try {
                        displayText.add(componentWrapper = new TextComponentWrapper<>(FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag())).setPriority(buffer.readInt()));
                    } catch (IOException e) {
                        GTLog.logger.info(e.getMessage());
                    }
                    break;
                case 3:
                    final int count1 = buffer.readInt();
                    final int size = buffer.readInt();
                    final List<ItemStack> stacks = new ArrayList<>();
                    try {
                        for (int j = 0; j < size; j++) {
                            final ItemStack stack = buffer.readItemStack();
                            stack.setCount(Math.max(1, count1));
                            stacks.add(stack);
                        }
                    } catch (IOException e) {
                        GTLog.logger.info(e.getMessage());
                    }
                    displayText.add(componentWrapper = new TextComponentWrapper<>(stacks).setPriority(buffer.readInt()));
                    break;
                case 4:
                    displayText.add(componentWrapper = new TextComponentWrapper<>(ITextComponent.Serializer.jsonToComponent(buffer.readString(Short.MAX_VALUE))).setPriority(buffer.readInt()));
                    break;
            }
            if (buffer.readBoolean()) {
                List<TextComponentWrapper<?>> componentWrappers = new ArrayList<>();
                this.readFromBuffer(componentWrappers, buffer);
                componentWrappers = this.formatDisplayText(componentWrappers);
                if (componentWrapper != null)
                    componentWrapper.setAdvancedHoverComponent(componentWrappers);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateScreen() {
        this.tick++;
        if (!this.isShiftDown() && this.hoverDisplayText != null) {
            this.hoverDisplayText = null;
            this.lastHoverX = 0;
            this.lastHoverY = 0;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 1) {
            this.displayText.clear();
            this.readFromBuffer(this.displayText, buffer);
            this.displayText = this.formatDisplayText(this.displayText);
            final Size size = this.updateComponentTextSize(this.displayText);
            if (this.getSize().getWidth() != size.getWidth() || this.getSize().getHeight() != size.getHeight())
                this.setSize(size);
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            final EntityPlayer player = this.gui.entityPlayer;
            final ClickData clickData = ClickData.readFromBuf(buffer);
            final String textId = buffer.readString(Short.MAX_VALUE);
            final String componentData = buffer.readString(128);
            if (this.clickHandler != null)
                this.clickHandler.accept(componentData, clickData);
            for (QuadConsumer<String, String, ClickData, EntityPlayer> clickHandler : this.clickHandlers) {
                clickHandler.accept(componentData, textId, clickData, player);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private Size updateComponentTextSize(List<TextComponentWrapper<?>> displayText) {
        int slot = 0;
        int totalHeight = 0;
        int maxStringWidth = 0;
        boolean stackApplied = false;
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        for (TextComponentWrapper<?> component : displayText) {
            if (component.getValue() instanceof ITextComponent) {
                if (stackApplied) {
                    stackApplied = false;
                    totalHeight += 20;
                }
                maxStringWidth = Math.max(maxStringWidth, fontRenderer.getStringWidth(((ITextComponent) component.getValue()).getUnformattedText()));
                totalHeight += 11;
            } else {
                if (slot++ > 8 || !stackApplied) {
                    slot = 0;
                    if (stackApplied)
                        totalHeight += 18;
                } else maxStringWidth = MathHelper.clamp(maxStringWidth + 3, 0, Integer.MAX_VALUE);
                stackApplied = true;
            }
        }
        if (stackApplied)
            totalHeight += 20;
        return new Size(maxStringWidth, totalHeight);
    }

    @Override
    protected void onSizeUpdate() {
        if (this.uiAccess != null) {
            this.uiAccess.notifySizeChange();
        }
    }

    @SideOnly(Side.CLIENT)
    private List<TextComponentWrapper<?>> formatDisplayText(List<TextComponentWrapper<?>> displayText) {
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        final int maxTextWidthResult = this.maxWidthLimit == 0 ? Integer.MAX_VALUE : this.maxWidthLimit;
        return displayText.stream()
                .flatMap(component -> component.getValue() instanceof ITextComponent ? TextUtils.splitText((ITextComponent) component.getValue(), maxTextWidthResult, fontRenderer).stream()
                        .map(component2 -> new TextComponentWrapper<>(component2).setPriority(component.getPriority()).setAdvancedHoverComponent(component.getAdvancedHoverComponent()))
                        : Stream.of(component))
                .sorted(Comparator.comparingInt(TextComponentWrapper::getPriority))
                .collect(Collectors.toList());
    }

    @SideOnly(Side.CLIENT)
    private boolean handleCustomComponentClick(ITextComponent textComponent) {
        final Style style = textComponent.getStyle();
        if (style.getClickEvent() != null) {
            final ClickEvent clickEvent = style.getClickEvent();
            final String componentText = clickEvent.getValue();
            if (clickEvent.getAction() == ClickEvent.Action.OPEN_URL && componentText.startsWith("@!")) {
                final String rawText = componentText.substring(2);
                final ClickData clickData = new ClickData(Mouse.getEventButton(), isShiftDown(), isCtrlDown());
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
        final TextComponentWrapper<?> textComponent = this.getTextUnderMouse(mouseX, mouseY, this.hoverDisplayText != null ? this.hoverDisplayText : this.displayText, this.hoverDisplayText != null);
        if (textComponent != null && textComponent.getValue() instanceof ITextComponent) {
            if (this.handleCustomComponentClick((ITextComponent) textComponent.getValue()) || this.getWrapScreen().handleComponentClick((ITextComponent) textComponent.getValue())) {
                this.playButtonClickSound();
                return true;
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        this.drawDisplayText(this.getPosition().getX(), this.getPosition().getY(), this.displayText);
    }

    @SideOnly(Side.CLIENT)
    private void drawDisplayText(int x, int y, List<TextComponentWrapper<?>> displayText) {
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
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
                fontRenderer.drawString(((ITextComponent) component.getValue()).getUnformattedText(), x + widthApplied, y + heightApplied, color);
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
                    GuiTextures.SLOT.draw(x + widthApplied, y + heightApplied, 18, 18);
                    Widget.drawItemStack((ItemStack) component.getValue(), x + widthApplied + 1, y + heightApplied + 1, null);
                } else if (component.getValue() instanceof List<?>) {
                    GuiTextures.SLOT.draw(x + widthApplied, y + heightApplied, 18, 18);
                    Widget.drawItemStack(this.getItemStackOreDict((List<ItemStack>) component.getValue()), x + widthApplied + 1, y + heightApplied + 1, null);
                } else {
                    final FluidStack fluidStack = (FluidStack) component.getValue();
                    GuiTextures.FLUID_SLOT.draw(x + widthApplied, y + heightApplied, 18, 18);
                    GlStateManager.disableBlend();
                    TJGuiUtils.drawFluidForGui(fluidStack, Math.max(1, fluidStack.amount), Math.max(1, fluidStack.amount), x + widthApplied + 1, y + heightApplied + 1, 17, 17);
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(0.5, 0.5, 1);
                    final String s = TextFormattingUtil.formatLongToCompactString(fluidStack.amount, 4) + "L";
                    fontRenderer.drawStringWithShadow(s, (x + widthApplied + 6) * 2 - fontRenderer.getStringWidth(s) + 21, (y + heightApplied + 14) * 2, 0xFFFFFF);
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
        final boolean hovering = this.hoverDisplayText != null;
        final TextComponentWrapper<?> component = this.getTextUnderMouse(mouseX, mouseY, hovering ? this.hoverDisplayText : this.displayText, hovering);
        if (this.hoverDisplayText == null) {
            this.lastHoverX = mouseX;
            this.lastHoverY = mouseY;
            if (this.isShiftDown() && component != null)
                this.hoverDisplayText = component.getAdvancedHoverComponent();
        } else this.hoverDisplayText = this.getTextUnderMouse(this.lastHoverX, this.lastHoverY, this.displayText, false).getAdvancedHoverComponent();
        final List<TextComponentWrapper<?>> displayText = hovering ? this.hoverDisplayText
                : component != null && component.getAdvancedHoverComponent() != null && !component.getAdvancedHoverComponent().isEmpty() ? component.getAdvancedHoverComponent()
                : null;
        if (displayText != null) {
            final Size size = this.updateComponentTextSize(displayText);
            TJGuiTextures.TOOLTIP_BOX.draw(this.lastHoverX, this.lastHoverY, size.getWidth() + 7, size.getHeight() + 5);
            this.drawDisplayText(this.lastHoverX + 4, this.lastHoverY + 4, displayText);
        }
        if (component == null) return;
        if (component.getValue() instanceof ITextComponent) {
            this.getWrapScreen().handleComponentHover((ITextComponent) component.getValue(), mouseX, mouseY);
        } else if (component.getValue() instanceof TextComponentWrapper<?>) {
            TextComponentWrapper<?> subComponent = (TextComponentWrapper<?>) component.getValue();
            this.getWrapScreen().handleComponentHover((ITextComponent) subComponent.getValue(), mouseX, mouseY);
        }
    }

    @SideOnly(Side.CLIENT)
    protected TextComponentWrapper<?> getTextUnderMouse(int mouseX, int mouseY, List<TextComponentWrapper<?>> displayText, boolean hover) {
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int slot = 0;
        int lastHeight = 0, lastWidth = 0;
        int heightApplied = 0, widthApplied = 0;
        boolean stackApplied = false;
        int x = this.getPosition().getX();
        int y = this.getPosition().getY();
        if (hover) {
            x += this.lastHoverX - x;
            y += this.lastHoverY - y;
        }
        for (TextComponentWrapper<?> component : displayText) {
            if (component.getValue() instanceof ITextComponent) {
                slot = 0;
                heightApplied += 11;
                if (stackApplied) {
                    stackApplied = false;
                    heightApplied += 2;
                }
                if (mouseY >= y + lastHeight && mouseY <= y + heightApplied) {
                    int currentOffset = 0;
                    final int mouseOffset = mouseX - x;
                    for (ITextComponent lineComponent : (ITextComponent) component.getValue()) {
                        currentOffset += fontRenderer.getStringWidth(lineComponent.getUnformattedComponentText());
                        if (currentOffset >= mouseOffset) {
                            return new TextComponentWrapper<>(lineComponent).setAdvancedHoverComponent(component.getAdvancedHoverComponent());
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
                if (mouseX >= x + lastWidth && mouseX <= x + widthApplied && mouseY >= y + lastHeight && mouseY <= y + heightApplied) {
                    if (component.getValue() instanceof ItemStack || component.getValue() instanceof List<?>) {
                        final ItemStack itemStack = component.getValue() instanceof ItemStack ? (ItemStack) component.getValue() : this.getItemStackOreDict((List<ItemStack>) component.getValue());
                        final List<String> tooltip = getItemToolTip(itemStack);
                        String name = itemStack.getDisplayName();
                        final ITextComponent hoverComponent = new TextComponentString("");
                        if (tooltip != null && !tooltip.isEmpty()) {
                            name = tooltip.get(0);
                            for (int j = 1; j < tooltip.size(); j++)
                                hoverComponent.appendText("\n" + tooltip.get(j));
                        }
                        return new TextComponentWrapper<>(new TextComponentWrapper<>(new TextComponentString("")
                                .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(name)
                                        .appendSibling(hoverComponent)
                                        .appendText("\n" + I18n.format("tj.machine.universal.item_amount", itemStack.getCount()))))))
                                .setAdvancedHoverComponent(Collections.singletonList(new TextComponentWrapper<>(itemStack))))
                                .setAdvancedHoverComponent(component.getAdvancedHoverComponent());
                    } else {
                        final FluidStack fluidStack = (FluidStack) component.getValue();
                        // Add chemical formula tooltip
                        String formula = FluidTooltipUtil.getFluidTooltip(fluidStack);
                        formula = formula == null || formula.isEmpty() ? "" : "\n" + formula;
                        return new TextComponentWrapper<>(new TextComponentWrapper<>(new TextComponentString("")
                                .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(fluidStack.getLocalizedName())
                                        .appendText(ChatFormatting.GRAY + formula)
                                        .appendText("\n" + I18n.format("tj.machine.universal.fluid_amount", fluidStack.amount))))))
                                .setAdvancedHoverComponent(Collections.singletonList(new TextComponentWrapper<>(fluidStack))))
                                .setAdvancedHoverComponent(component.getAdvancedHoverComponent());
                    }
                }
            }
            lastWidth = widthApplied;
            lastHeight = heightApplied;
        }
        return null;
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (!this.isMouseOverElement(mouseX, mouseY))
            return null;
        final TextComponentWrapper<?> component = this.getTextUnderMouse(mouseX, mouseY, this.hoverDisplayText != null ? this.hoverDisplayText : this.displayText, this.hoverDisplayText != null);
        if (!(component instanceof TextComponentWrapper<?>) || !(component.getValue() instanceof TextComponentWrapper<?>))
            return null;
        final TextComponentWrapper<?> subComponent = (TextComponentWrapper<?>) component.getValue();
        if (subComponent.getAdvancedHoverComponent().isEmpty())
            return null;
        return subComponent.getAdvancedHoverComponent().get(0).getValue();
    }

    private ItemStack getItemStackOreDict(List<ItemStack> stacks) {
        final int index = Math.min(stacks.size() - 1, this.tick % (stacks.size() * 20) / 20);
        return stacks.get(index);
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

    public static final class TextComponentWrapper<T> {

        private final T value;
        private int priority;
        private List<TextComponentWrapper<?>> advancedHoverComponent;

        public TextComponentWrapper(T value) {
            this.value = value;
        }

        public TextComponentWrapper<?> setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public TextComponentWrapper<?> setAdvancedHoverComponent(List<TextComponentWrapper<?>> advancedHoverComponent) {
            this.advancedHoverComponent = advancedHoverComponent;
            return this;
        }

        public T getValue() {
            return this.value;
        }

        public int getPriority() {
            return this.priority;
        }

        public List<TextComponentWrapper<?>> getAdvancedHoverComponent() {
            return this.advancedHoverComponent;
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
