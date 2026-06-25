package tj.mui.widgets.impl;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.SizedTextureArea;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tj.mui.widgets.ButtonWidget;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TJCycleButtonWidget<T extends Enum<T>> extends ButtonWidget<TJCycleButtonWidget<T>> {

    private final Supplier<? extends Enum<?>> cycleSupplier;
    private final EnumSet<? extends Enum<?>> cycles;
    private final Consumer<T> onCycle;
    private String[] cycleTitleHoverTooltipText;
    private String[] cycleHoverTooltipText;
    private String[] cycleDisplayText;
    private TextureArea cycleTexture;
    private int index;

    public TJCycleButtonWidget(int x, int y, int width, int height, Class<T> cycles, Supplier<? extends Enum<?>> cycleSupplier, Consumer<T> onCycle) {
        this(x, y, width, height, EnumSet.allOf(cycles), cycleSupplier, onCycle);
    }

    public TJCycleButtonWidget(int x, int y, int width, int height, EnumSet<? extends Enum<?>> cycles, Supplier<? extends Enum<?>> cycleSupplier, Consumer<T> onCycle) {
        super(x, y, width, height);
        this.cycleSupplier = cycleSupplier;
        this.onCycle = onCycle;
        this.cycles = cycles;
        this.setCycleDisplayText(cycles.stream()
                .filter(e -> e instanceof IStringSerializable)
                .map(e -> ((IStringSerializable) e).getName())
                .toArray(String[]::new));
    }

    public TJCycleButtonWidget<T> setCycleTexture(TextureArea cycleTexture) {
        this.cycleTexture = cycleTexture;
        return this;
    }

    /**
     * Set cycle text to display on cycle button.
     * @param cycleDisplayText text
     */
    public TJCycleButtonWidget<T> setCycleDisplayText(String... cycleDisplayText) {
        this.cycleDisplayText = cycleDisplayText;
        return this;
    }

    /**
     * Button title hover tooltip.
     * Text that will be displayed upon hovering over this button.
     * This will attempt to translate the texts if they're a lang string.
     * @param cycleTitleHoverTooltipText array of texts to display.
     */
    public TJCycleButtonWidget<T> setCycleTitleHoverTooltipText(String... cycleTitleHoverTooltipText) {
        this.cycleTitleHoverTooltipText = cycleTitleHoverTooltipText;
        return this;
    }

    /**
     * Button description hover tooltip.
     * Text that will be displayed upon hovering over this button.
     * This will attempt to translate the texts if they're a lang string.
     * @param cycleHoverTooltipText array of texts to display.
     */
    public TJCycleButtonWidget<T> setCycleHoverTooltipText(String... cycleHoverTooltipText) {
        this.cycleHoverTooltipText = cycleHoverTooltipText;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInForeground(int mouseX, int mouseY) {
        if (!this.isActive || !this.isMouseOverElement(mouseX, mouseY)) return;
        final List<String> hover = new ArrayList<>();
        if (this.cycleTitleHoverTooltipText != null)
            hover.add(I18n.format(this.cycleTitleHoverTooltipText[this.index]));
        if (this.cycleHoverTooltipText != null)
            hover.add("§7" + I18n.format(this.cycleHoverTooltipText[this.index]));
        this.drawHoveringText(ItemStack.EMPTY, hover, 300, mouseX, mouseY);
        super.drawInForeground(mouseX, mouseY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        if (!this.isActive) return;
        final Size size = this.getSize();
        final Position pos = this.getPosition();
        if (this.cycleTexture != null) {
            final double offsetY = 1.0 / this.cycles.size();
            if (this.cycleTexture instanceof SizedTextureArea) {
                ((SizedTextureArea) this.cycleTexture).drawHorizontalCutSubArea(pos.getX(), pos.getY(), size.getWidth(), size.getHeight(), this.isMouseOverElement(mouseX, mouseY) ? 0.5 : 0.0, 0.5);
            } else this.cycleTexture.drawSubArea(pos.getX(), pos.getY(), size.getWidth(), size.getHeight(), 0.0, offsetY * this.index, 1.0, offsetY);
        }
        if (this.cycleDisplayText != null && this.cycleDisplayText.length > 0) {
            final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            final String text = I18n.format(this.cycleDisplayText[this.index]);
            fontRenderer.drawString(text,
                    pos.getX() + size.getWidth() / 2 - fontRenderer.getStringWidth(text) / 2,
                    pos.getY() + size.getHeight() / 2 - fontRenderer.FONT_HEIGHT / 2, this.textColor);
            GlStateManager.color(1.0f, 1.0f, 1.0f);
        }
        super.drawInBackground(mouseX, mouseY, context);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (this.isActive && this.isMouseOverElement(mouseX, mouseY)) {
            final int lastIndex = this.index;
            if (button == 0) {  // Left-Click
                this.index++;
            } else if (button == 1) { // Right-Click
                this.index--;
            }
            if (this.index >= this.cycles.size())
                this.index = 0;
            if (this.index < 0)
                this.index = this.cycles.size() - 1;
            if (lastIndex != this.index) {
                this.writeClientAction(2, buffer -> buffer.writeInt(this.index));
                return true;
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 4) {
            this.index = buffer.readInt();
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 2) {
            this.index = buffer.readInt();
            if (this.onCycle != null) {
                this.onCycle.accept((T) this.cycles.stream()
                        .filter(c -> c.ordinal() == this.index)
                        .findFirst()
                        .orElse(null));
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.cycleSupplier != null) {
            final int index = this.cycleSupplier.get().ordinal();
            if (index != this.index) {
                this.index = index;
                this.writeUpdateInfo(4, buffer -> buffer.writeInt(this.index));
            }
        }
    }
}
